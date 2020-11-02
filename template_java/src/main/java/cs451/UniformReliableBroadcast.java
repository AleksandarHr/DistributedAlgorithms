package cs451;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UniformReliableBroadcast {
	
	private Process process;
	private BestEffortBroadcast beb;
	private ConcurrentHashMap<Message, List<InetSocketAddress>> acknowledgmentsAddresses;
	private int processesCount;
	
	public UniformReliableBroadcast(BestEffortBroadcast beb) {
		this.process = beb.getProcess();
		this.beb = beb;
		this.acknowledgmentsAddresses = new ConcurrentHashMap<Message, List<InetSocketAddress>>();
		this.processesCount = this.process.getAllProcesses().size();
	}
	
	public void urbBroadcast(Message m) {
		this.beb.bebBroadcast(m);
	}
	
	public void urbDeliver(Message m, InetSocketAddress source) {
		if (!this.acknowledgmentsAddresses.containsKey(m)) {
			// if this is the first acknowledgement for the given message
			List<InetSocketAddress> addresses = new LinkedList<InetSocketAddress>();
			addresses.add(source);
			this.acknowledgmentsAddresses.put(m, addresses);
		} else {
			// if we have received acknowledgements for the given message before
			List<InetSocketAddress> currentAddresses = this.acknowledgmentsAddresses.get(m);
			if (!currentAddresses.contains(source)) {
				// if we have not received an acknowledgment for this message from this source
				if (currentAddresses.size()+1 > (this.processesCount / 2)) {
					// we have majority acks -- bebDeliver
					this.beb.bebDeliver(m);
				} else {
					// we don't have majority acks yet -- update hashmap
					currentAddresses.add(source);
					this.acknowledgmentsAddresses.put(m, currentAddresses);
				}
			}
		}
	}
}
