package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener extends Thread {
	
	private final int UDPPACKETSIZELIMIT = 65535;
	
	private Process process;
	private byte[] buffer = new byte[UDPPACKETSIZELIMIT];
	private DatagramPacket receivedPacket = null;
	private DatagramSocket socket;

//	private final int threadPoolSize = 1;
//	private ExecutorService threadPool;
	
	public Listener(Process process) {
		this.process = process;
		this.socket = this.process.getSocket();
//		this.threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
	}
	
	public void run() {
		// Listen for incoming packets while the process is alive
		while (this.process.isAlive()) {
			this.receivedPacket = new DatagramPacket(buffer, buffer.length);;
			try {
				this.socket.receive(receivedPacket);
				InetAddress senderIp = receivedPacket.getAddress();
				int senderPort = receivedPacket.getPort();
				InetSocketAddress senderAddr = new InetSocketAddress(senderIp, senderPort);
				Message msg = getMessageObjectFromByteArray(receivedPacket.getData());
				
				if (msg != null) {
					if (msg.isAck()) {
//						System.out.println("ACK for message with ID = " + msg.getMsgId() + " from process = " + senderPid);
						this.handleAckMessage(msg, senderAddr);
					} else {
//						System.out.println("MESSAGE " + msg.getMsgId() + " form = " + senderPort);
						this.handleRegularMessage(msg, senderIp, senderPort);
					}
				}
				
			} catch (IOException e) {
				System.out.println("Cannot read received datagram.");
			} catch (ClassNotFoundException e) {
				System.out.println("Class Message not found.");
			}
		}
	}
	
	// stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
	private Message getMessageObjectFromByteArray(byte[] messageBytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(messageBytes);
		ObjectInput in = null;
		Message msg = null;
		try {
			in = new ObjectInputStream(bis);
			msg = (Message) in.readObject();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				
			}
		}
		
		return msg;
	}
	
	/*
	 * 
	 */
	private void handleAckMessage(Message msg, InetSocketAddress sender) {
		// Add ack for the message from the given sender and try to FIFO deliver
		this.process.addAcknowledgement(msg, sender);
//		this.process.getUrb().urbDeliver(msg, sender);
//		this.process.getFifo().fifoDeliver(msg, sender);
		this.process.getLcb().lcbDeliver(msg, sender);
	}
	
	/*
	 * 
	 */
	private void handleRegularMessage(Message msg, InetAddress ip, int port) {
		// Send an ack for the message to the sender & try to fifo deliver
		Message ack = new Message(msg);
		this.process.sendAck(ack, new InetSocketAddress(ip, port));
//		this.process.getUrb().urbDeliver(msg, new InetSocketAddress(ip, port));
//		this.process.getFifo().fifoDeliver(msg, new InetSocketAddress(ip, port));
		this.process.getLcb().lcbDeliver(msg, new InetSocketAddress(ip, port));
	}
}
