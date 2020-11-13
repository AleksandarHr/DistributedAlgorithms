package cs451;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class BestEffortBroadcast {

	private Process process;
	
	public BestEffortBroadcast(Process p) {
		this.process = p;
	}
	
	// create a message with given ID and broadcast it to all processes
	public void bebBroadcast(int msgId) {
		ArrayList<InetSocketAddress> allProcesses = this.process.getAllProcesses();
		System.out.println("b " + msgId);
		Message m = new Message(msgId, this.process.getProcessId());
		for (InetSocketAddress addr : allProcesses) {
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	// broadcast a message to all processes
	public void bebBroadcast(Message m) {
		for (InetSocketAddress addr : this.process.getAllProcesses()) {
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	// Only deliver if message has not been delivered yet - no duplication
	public boolean bebDeliver(Message msg) {
		return this.process.plDeliverMessage(msg);
	}
	
	public Process getProcess() {
		return this.process;
	}
}
