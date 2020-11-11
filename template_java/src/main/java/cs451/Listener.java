package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Listener extends Thread {
	
	private final int UDPPACKETSIZELIMIT = 65535;
	
	private Process process;
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
				Message msg = getMessageObjectFromByteArray(receivedPacket.getData());
				
				if (msg != null) {
					if (msg.isAck()) {
//						System.out.println("ACK for message with ID = " + msg.getMsgId() + " from process = " + senderPid);
						this.handleAckMessage(msg, senderAddr, this.process.getUrb(), this.process.getFifo());
					} else {
//						System.out.println("MESSAGE " + msg.getMsgId() + " form = " + senderPort);
						this.handleRegularMessage(msg, senderIp, senderPort, this.process.getUrb(), this.process.getFifo());
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
	private void handleAckMessage(Message msg, InetSocketAddress sender, UniformReliableBroadcast urb, FirstInFirstOutBroadcast fifo) {
//		System.out.println("RECEIVING AN ACK for " + msg.getMsgId() + " from " + sender.getPort());
		this.process.addAcknowledgement(msg, sender);
//		urb.urbDeliver(msg, sender);
		fifo.fifoDeliver(msg, sender);
	}
	
	/*
	 * 
	 */
	private void handleRegularMessage(Message msg, InetAddress ip, int port, UniformReliableBroadcast urb , FirstInFirstOutBroadcast fifo) {
		Message ack = new Message(msg);
		this.process.sendAck(ack, new InetSocketAddress(ip, port));
//		this.process.sendP2PMessage(ack, ip, port);
//		urb.urbDeliver(msg, new InetSocketAddress(ip, port));
		fifo.fifoDeliver(msg, new InetSocketAddress(ip, port));
	}
}
