package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Listener extends Thread {
	
	private final int UDPPACKETSIZELIMIT = 65535;
	
	private Process process;
	private InetAddress ip;
	private Integer port;
	private byte[] buffer = new byte[UDPPACKETSIZELIMIT];
	DatagramPacket receivedPacket = null;
	
	public Listener(Process process) {
		this.process = process;
	}
	
	public void run() {
		DatagramSocket socket = this.process.getSocket();
		while (this.process.isAlive()) {
			receivedPacket = new DatagramPacket(buffer, buffer.length);;
			try {
				socket.receive(receivedPacket);
				InetAddress senderIp = receivedPacket.getAddress();
				int senderPort = receivedPacket.getPort();
				InetSocketAddress senderAddr = new InetSocketAddress(senderIp, senderPort);
				int senderPid = this.process.getAddressesAndPids().get(senderAddr);
				Message msg = getMessageObjectFromByteArray(receivedPacket.getData());
				
				if (msg != null) {
					if (msg.isAck()) {
						System.out.println("Receiving an ACK for message with ID = " + msg.getMsgId() + " from process = " + senderPid);
						this.handleAckMessage(msg);
					} else if (msg.isBroadcastMessage()){
						System.out.println("Receiving a BROADCAST from " + senderPort);
						this.handleBroadcastMessage(msg, senderIp, senderPort, this.process.getBeb());
					} else {
						System.out.println("Receiving a MESSAGE with ID = " + msg.getMsgId() + " sent by process = " + msg.getOriginalPid());
						this.handleRegularMessage(msg, senderIp, senderPort, this.process.getBeb());
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
	private void handleBroadcastMessage(Message msg, InetAddress ip, int port, BestEffortBroadcast beb) {
		// Deliver the message, if not previously delivered
		beb.bebDeliver(msg);
	
		// Send an ack for the message to the sender
		Message ack = new Message(msg);
		this.process.sendP2PMessage(ack, ip, port);
	}
	
	/*
	 * 
	 */
	private void handleAckMessage(Message msg) {
		this.process.addAcknowledgement(msg);
	}
	
	/*
	 * 
	 */
	private void handleRegularMessage(Message msg, InetAddress ip, int port, BestEffortBroadcast beb) {
		// Deliver the message, if not previously delivered
		beb.bebDeliver(msg);
		
		// Send an ack for the message to the sender
		System.out.println("Sending an ACK for message with ID = " + msg.getMsgId() + " to process with ID = " + msg.getOriginalPid());
		Message ack = new Message(msg);
		this.process.sendP2PMessage(ack, ip, port);
	}
}
