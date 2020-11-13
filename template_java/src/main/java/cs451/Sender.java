package cs451;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// Sender class is used to send a message to a single destination process
public class Sender extends Thread {

//	private Process process;
//	private Message toSend;
//	private int destinationPort;
//	private InetAddress destinationIp;
//	
//	public Sender(Process p, Message msg, int destPort, InetAddress destIp) {
//		this.process = p;
//		this.toSend = msg;
//		this.destinationPort = destPort;
//		this.destinationIp = destIp;
//	}

//	public void run() {
//		try {
//			sendMessage(this.toSend, this.process.getSocket());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	private Process process;
	private int resendBound = 12;
	private ArrayList<InetSocketAddress> allProcesses;
	private DatagramSocket processSocket;

	public Sender(Process p) {
		this.process = p;
		this.allProcesses = p.getAllProcesses();
		this.processSocket = this.process.getSocket();
	}
	
	public void run() {
		int resendAttempts = 0;
		// Run the sender thread while the process is alive
		while (this.process.isAlive()) {
			ConcurrentHashMap<InetSocketAddress, PriorityQueue<Message>> toSend = this.process.getMessagesToSend();
			if (toSend.size() > 0) {
				// For every process, try to send the most urgent message (e.g. message with lowest msg ID) we know we have to send
				for (int i = 0; i < toSend.size(); i ++) {
					InetSocketAddress dest = this.allProcesses.get(i);
					PriorityQueue<Message> msgs = toSend.get(dest);
					if (msgs.size() > 0) {
						Message nextToSend  = toSend.get(dest).peek();
						resendAttempts = 0;
				
						byte[] msgBytes = null;
						try {
							msgBytes = getBytesArrayFromMessageObject(nextToSend);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				
						if (msgBytes != null) {
							DatagramPacket packetToSend = new DatagramPacket(msgBytes, msgBytes.length, dest.getAddress(), dest.getPort()); 
							// Retransmit the message until it has been acknowledged or an attempts limit has been reached
							while(!this.process.hasBeenAcknowledged(nextToSend, dest) && resendAttempts < this.resendBound) {
								try {
									this.processSocket.send(packetToSend);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try {
									// exponential backoff for retransmission
									TimeUnit.MILLISECONDS.sleep(2^resendAttempts);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								resendAttempts++;
							}
							// if the message was acknowledged, remove it from the to-be-sent datastructure
							if (this.process.hasBeenAcknowledged(nextToSend, dest)) {
								this.process.removeMessageFromSend(dest, nextToSend);
							}
						}
					}
				}
			}
		}
	}
	
	public void sendAck(Message ack, InetSocketAddress dest) {
		byte[] msgBytes = null;
		try {
			msgBytes = getBytesArrayFromMessageObject(ack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		DatagramPacket packetToSend = new DatagramPacket(msgBytes, msgBytes.length, dest.getAddress(), dest.getPort()); 
		try {
			this.processSocket.send(packetToSend);
			if (ack.isAck()) {
//				System.out.println("SENDING ACK " + ack.getMsgId() + " to " + dest.getPort());
			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
	private byte[] getBytesArrayFromMessageObject(Message msg) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		byte[] msgBytes = null;
		
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(msg);
			out.flush();
			msgBytes = bos.toByteArray();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
			}
		}
		
		return msgBytes;
	}
	
}