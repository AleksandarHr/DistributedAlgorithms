package cs451;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UniformReliableBroadcast {

	private Process process;
	private BestEffortBroadcast beb;
	
	private ConcurrentHashMap<Message, AtomicInteger> delivered;
	private ConcurrentHashMap<Message, AtomicInteger> forward;
	
	public UniformReliableBroadcast(BestEffortBroadcast beb) {
		this.process = beb.getProcess();
		this.beb = beb;
		this.delivered = new ConcurrentHashMap<Message, AtomicInteger>();
		this.forward = new ConcurrentHashMap<Message, AtomicInteger>();
	}

	// URB broadcast - simply call BEB broadcast
	public void urbBroadcast(int msgId) {
		this.beb.bebBroadcast(msgId);
	}

	// Perform URB deliver algorithm
	public boolean urbDeliver(Message msg, InetSocketAddress source) {
		this.beb.bebDeliver(msg);

		if (!this.forward.containsKey(msg)) {
			this.forward.put(msg, new AtomicInteger(1));
			if (msg.getOriginalPid() != this.process.getProcessId()) {
				this.beb.bebBroadcast(msg);
			}
		}

		if (this.forward.containsKey(msg)) {
			boolean shouldDeliver = this.shouldDeliver(msg);
			if (!this.delivered.containsKey(msg) && shouldDeliver) {
//				System.out.println("URB deliver msg " + msg.getMsgId() + " from " + msg.getOriginalPid() + " having MAJORITY of " + this.process.ackerCount(msg));
				this.delivered.put(msg, new AtomicInteger(1));
				return true;
			}
		}
		return false;
	}
	
	// should URB deliver only if we have majority acks
	private boolean shouldDeliver(Message msg) {
		int ackCount = this.process.ackerCount(msg);
		int processesCount = this.process.getAllProcesses().size();
		return ackCount > (processesCount/2);
	}
	
	public Process getProcess() {
		return this.process;
	}
	
	// Used to make sure the correct number of messages have been URB delivered at the end
	public int countDelivered() {
		return this.delivered.size();
	}
}
