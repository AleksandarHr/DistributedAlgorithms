package cs451;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;import java.util.Map;
import java.util.Set;
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

	public void urbBroadcast(int msgId) {
		this.beb.bebBroadcast(msgId);
	}
	
	public boolean urbDeliver(Message msg, InetSocketAddress source) {
		this.beb.bebDeliver(msg);
		// add the source of the message to the set of processes which have acked this message

		if (!this.forward.containsKey(msg)) {
			this.forward.put(msg, new AtomicInteger(1));
			if (msg.getOriginalPid() != this.process.getProcessId()) {
				Message rebroadcastMsg = new Message(msg);
				this.beb.bebBroadcast(rebroadcastMsg);
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
	
	private boolean shouldDeliver(Message msg) {
		int ackCount = this.process.ackerCount(msg);
		int processesCount = this.process.getAllProcesses().size();
		return ackCount > (processesCount/2);
	}
	
	public Process getProcess() {
		return this.process;
	}
}
