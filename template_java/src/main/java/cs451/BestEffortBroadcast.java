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
		System.out.println("b " + msgId);
		this.process.addToOutput("b " + msgId);
		for (InetSocketAddress addr : allProcesses) {
			Message m = new Message(content, msgId, this.process.getProcessId(), addr.getPort(), addr.getAddress(), this.process.getProcessPort(), this.process.getProcessAddress(), false);
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	public void bebBroadcast(Message m) {
		for (InetSocketAddress addr : this.process.getAllProcesses()) {
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	public boolean bebDeliver(Message msg) {
		// only deliver message if it has not been yet delivered - underlying perfect links abstraction
		// already takes care of that
		return this.process.addDelieveredMessage(msg);
	}
	
	public Process getProcess() {
		return this.process;
	}
}
