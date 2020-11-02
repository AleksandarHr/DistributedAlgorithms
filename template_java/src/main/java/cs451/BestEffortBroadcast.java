package cs451;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class BestEffortBroadcast {

	private Process process;
	
	public BestEffortBroadcast(Process p) {
		this.process = p;
	}
	
	public void bebBroadcast(String content, int msgId) {
		ArrayList<InetSocketAddress> allProcesses = this.process.getAllProcesses();
		for (InetSocketAddress addr : allProcesses) {
			Message m = new Message(content, msgId, addr.getPort(), addr.getAddress(), this.process.getProcessPort(), this.process.getProcessAddress(), false);
			System.out.println("BEB to " + addr.getPort());
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	public void bebBroadcast(Message m) {
		for (InetSocketAddress addr : this.process.getAllProcesses()) {
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	public void bebDeliver(Message msg) {
		// only deliver message if it has not been yet delivered - underlying perfect links abstraction
		// already takes care of that
		this.process.addDelieveredMessage(msg);
	}
	
	public Process getProcess() {
		return this.process;
	}
}
