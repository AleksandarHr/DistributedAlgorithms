package cs451;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class BestEffortBroadcast {

	private Process process;
	
	public BestEffortBroadcast(Process p) {
		this.process = p;
	}
	
	public void bebBroadcast(int msgId) {
		ArrayList<InetSocketAddress> allProcesses = this.process.getAllProcesses();
		System.out.println("b " + msgId);
		for (InetSocketAddress addr : allProcesses) {
			Message m = new Message(msgId, this.process.getProcessId(), addr.getPort(), addr.getAddress(), this.process.getProcessPort(), this.process.getProcessAddress());
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	public void bebBroadcast(Message m) {
		for (InetSocketAddress addr : this.process.getAllProcesses()) {
			this.process.sendP2PMessage(m, addr.getAddress(), addr.getPort());
		}
	}
	
	public boolean bebDeliver(Message msg) {
		return true;
	}
	
	public Process getProcess() {
		return this.process;
	}
}
