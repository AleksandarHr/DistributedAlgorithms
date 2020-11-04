package cs451;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UniformReliableBroadcast {

	private Process process;
	private BestEffortBroadcast beb;
	private ConcurrentHashMap<Message, Set<InetSocketAddress>> acks;
	private int processesCount;
	
	private ConcurrentHashMap<Message, AtomicInteger> delivered;
	private ConcurrentHashMap<Message, AtomicInteger> forward;
	
	public UniformReliableBroadcast(BestEffortBroadcast beb) {
		this.process = beb.getProcess();
		this.beb = beb;
		this.acks = new ConcurrentHashMap<Message, Set<InetSocketAddress>>();
		this.delivered = new ConcurrentHashMap<Message, AtomicInteger>();
		this.forward = new ConcurrentHashMap<Message, AtomicInteger>();
		
		this.processesCount = this.process.getAllProcesses().size();
	}

	public void urbBroadcast(String content, int msgId) {
		this.beb.bebBroadcast(content, msgId);
	}
	
	public boolean urbDeliver(Message msg, InetSocketAddress source) {
		// if we still haven't delivered this message
		if (this.beb.bebDeliver(msg)) {
			Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
			currentAcks.add(source);
			this.acks.put(msg, currentAcks);
			
			if (!this.forward.contains(msg)) {
				this.forward.put(msg, new AtomicInteger(1));
				this.beb.bebBroadcast(msg);
			}
		}
		if (this.forward.contains(msg)) {
			if (!this.delivered.contains(msg) && this.shouldDeliver(msg)) {
				this.delivered.put(msg, new AtomicInteger(1));
				return true;
			}
		}
		return false;
	}
	
	
	private boolean shouldDeliver(Message msg) {
		int ackCount = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>()).size();
		return ackCount > (this.processesCount/2);
	}
	
//	public void urbBroadcast(Message m) {
//		this.beb.bebBroadcast(m);
//	}
//
//	public void urbDeliver(Message m, InetSocketAddress source) {
//		if (!this.acknowledgmentsAddresses.containsKey(m)) {
//			// if this is the first acknowledgement for the given message
//			List<InetSocketAddress> addresses = new LinkedList<InetSocketAddress>();
//			addresses.add(source);
//			this.acknowledgmentsAddresses.put(m, addresses);
//		} else {
//			// if we have received acknowledgements for the given message before
//			List<InetSocketAddress> currentAddresses = this.acknowledgmentsAddresses.get(m);
//			if (!currentAddresses.contains(source)) {
//				// if we have not received an acknowledgment for this message from this source
//				if (currentAddresses.size() + 1 > (this.processesCount / 2)) {
//					// we have majority acks -- bebDeliver
//					this.beb.bebDeliver(m);
//				} else {
//					// we don't have majority acks yet -- update hashmap
//					currentAddresses.add(source);
//					this.acknowledgmentsAddresses.put(m, currentAddresses);
//				}
//			}
//		}
//	}
}
