package cs451;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

// Sender class is used to send a message to a single destination process
public class Sender extends Thread {

	private Process process;
	private Message toSend;
	private int destinationPort;
	private InetAddress destinationIp;
	
	public Sender(Process p, Message msg, int destPort, InetAddress destIp) {
		this.process = p;
		this.toSend = msg;
		this.destinationPort = destPort;
		this.destinationIp = destIp;
	}
	
	public void run() {
		try {
			if (this.process.getSocket() == null) {
				System.out.println("WHY IS NULLL??");
			}
			sendMessage(this.toSend, this.process.getSocket());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Sends a message to a single destination 
	private void sendMessage(Message toSend, DatagramSocket processSocket) throws IOException, InterruptedException {
		byte[] msgBytes = getBytesArrayFromMessageObject(toSend);
		DatagramPacket packetToSend = new DatagramPacket(msgBytes, msgBytes.length, this.destinationIp, this.destinationPort);
		try {
			if (toSend.isAck()) {
				// send an acknowledgment message
				processSocket.send(packetToSend);
			} else {
				// keep sending the message until we get an acknowledgment from the destination process
				while(!this.process.hasBeenAcknowledged(toSend)) {
					processSocket.send(packetToSend);
					TimeUnit.SECONDS.sleep(1);
				}
			}
		} catch (IOException e) {
			System.out.println("Packet sending failed.\n");
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