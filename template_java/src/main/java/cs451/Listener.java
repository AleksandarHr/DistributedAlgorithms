package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Listener extends Thread {
	
	private final int UDPPACKETSIZELIMIT = 65535;
	
	private Process process;
	private InetAddress ip;
	private Integer port;
	private boolean running;
	private byte[] buffer = new byte[UDPPACKETSIZELIMIT];
	DatagramPacket receivedPacket = null;
	
	public Listener(Process process) {
		this.process = process;
	}
	
	public void run() {
		running = true;
		DatagramSocket socket = this.process.getSocket();
		while (running) {
			receivedPacket = new DatagramPacket(buffer, buffer.length);;
			try {
				socket.receive(receivedPacket);
				InetAddress senderIp = receivedPacket.getAddress();
				int senderPort = receivedPacket.getPort();
				Message msg = getMessageObjectFromByteArray(receivedPacket.getData());
			
				if (msg.isSimpleBroadcast()) {
					this.handleBroadcastMessage(msg);
				} else if (msg.isAck()) {
					this.handleAckMessage(msg);
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
	private void handleBroadcastMessage(Message msg) {
		// If message is from another process, broadcast the message
		if (msg.getSenderId() != this.process.getProcessId()) {
			
		}
		
		// Deliver the message
		
		
		// Send an ack for the message
	}
	
	/*
	 * 
	 */
	private void handleAckMessage(Message msg) {
		
	}
}
