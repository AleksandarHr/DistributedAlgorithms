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
//	private ConcurrentHashMap<Message, Set<InetSocketAddress>> acks;
	
	private ConcurrentHashMap<Message, AtomicInteger> delivered;
	private ConcurrentHashMap<Message, AtomicInteger> forward;
	
	public UniformReliableBroadcast(BestEffortBroadcast beb) {
		this.process = beb.getProcess();
		this.beb = beb;
		this.delivered = new ConcurrentHashMap<Message, AtomicInteger>();
		this.forward = new ConcurrentHashMap<Message, AtomicInteger>();
	}

	public void urbBroadcast(String content, int msgId) {
		this.beb.bebBroadcast(content, msgId);
	}
	
	public boolean urbDeliver(Message msg, InetSocketAddress source) {
		this.beb.bebDeliver(msg);
		
//		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
//		// add ourselves to the set of processes which have acked this message
////		currentAcks.add(new InetSocketAddress(this.process.getProcessAddress(), this.process.getProcessPort()));
//		// add the source of the message to the set of processes which have acked this message
//		currentAcks.add(source);
//		this.acks.put(msg, currentAcks);
		
		if (!this.forward.containsKey(msg)) {
			this.forward.put(msg, new AtomicInteger(1));
			if (msg.getOriginalPid() != this.process.getProcessId()) {
				Message rebroadcastMsg = new Message(msg, true);
//				System.out.println("REBROADCAST");
				this.beb.bebBroadcast(rebroadcastMsg);
			}
		}

		if (this.forward.containsKey(msg)) {
//			System.out.println("will check if SHOULD urb deliver msg " + msg.getMsgId() + " :: from process " + msg.getOriginalPid());
			if (!this.delivered.containsKey(msg) && this.shouldDeliver(msg)) {
				System.out.println("DELIVER msg " + msg.getMsgId() + " from " + msg.getOriginalPid() + " having MAJORITY of " + this.process.ackerCount(msg));
				this.delivered.put(msg, new AtomicInteger(1));
				return true;
			}
		}
		return false;
	}
	
	private boolean shouldDeliver(Message msg) {
		int ackCount = this.process.ackerCount(msg);
		int processesCount = this.process.getAllProcesses().size();
//		System.out.println("PROCESS count = " + processesCount + " :: ACK count = " + ackCount);
		return ackCount > (processesCount/2);
	}
}
