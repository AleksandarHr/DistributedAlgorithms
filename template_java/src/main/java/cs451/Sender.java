package cs451;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// Sender class is used to send a message to a single destination process
public class Sender {

	private Process process;
	private Message toSend;
	private int destinationPort;
	private InetAddress destinationIp;
	
	public Sender(Process p, Message msg) {
		this.process = p;
		this.toSend = msg;
		this.destinationPort = msg.getDestPort();
		this.destinationIp = msg.getDestAddr();
	}
	
	// Sends a message to a single destination 
	public void sendMessage() throws IOException {
		byte[] msgBytes = getBytesArrayFromMessageObject(this.toSend);
		DatagramPacket packetToSend = new DatagramPacket(msgBytes, msgBytes.length, this.destinationIp, this.destinationPort);
		DatagramSocket processSocket = this.process.getSocket();
		try {
			processSocket.send(packetToSend);
		} catch (IOException e) {
			System.out.println("Packet sending failed.");
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