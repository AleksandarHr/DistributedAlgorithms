package cs451;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class LocalizedCausalBroadcast {
	private Process process;
	private FirstInFirstOutBroadcast fifo;
	
	private int[] vectorClock;
	ReentrantLock vcLock = new ReentrantLock();

	public LocalizedCausalBroadcast(FirstInFirstOutBroadcast fifo) {
		this.fifo = fifo;
		this.process = fifo.getProcess();
	}
	
	public void lcbBroadcast(int msgId) {
		if (this.vectorClock == null) {
			int processCount = this.process.getAllProcesses().size();
			this.vectorClock = new int[processCount];
		}
		this.process.addToOutput("b " + msgId);
		// Create a message to broadcast with provided msgId and current process VC
		Message msg = new Message(msgId, this.process.getProcessId(), this.prepareMessageVc());
		this.fifo.fifoBroadcast(msg);
	}
	
	public int[] prepareMessageVc() {
		int[] msgVc = this.getVc();
		Set<Integer> processDependencies = this.process.getDependencies();
		for (int i = 0; i < msgVc.length; i++) {
			if (!processDependencies.contains(i+1)) {
				msgVc[i] = 0;
			}
		}
		return msgVc;
	}
	
	// returns a copy of the current VC states
	public int[] getVc() {
		this.vcLock.lock();
		int[] copied = new int[this.vectorClock.length];
		System.arraycopy(this.vectorClock, 0, copied, 0, this.vectorClock.length);
		this.vcLock.unlock();
		return copied;
	}
	
	public void lcbDeliver(Message msg, InetSocketAddress source) {
		
	}
}
