package cs451;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class FirstInFirstOutBroadcast {
	private Process process;
	private UniformReliableBroadcast urb;
	
	// contains the number of messages received by respective pid (e.g. highest message id received)
	private int[] vectorClock;
	private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>> pending;
	
	public FirstInFirstOutBroadcast(UniformReliableBroadcast urb) {
		this.urb = urb;		
		this.process = urb.getProcess();
	}
	
	public void fifoBroadcast(String content, int msgId) {
		if (this.vectorClock == null) {
			int processCount = this.process.getAllProcesses().size();
			this.vectorClock = new int[processCount];
		}
		
		this.urb.urbBroadcast(content, msgId);
	}
	
	public void fifoDeliver(Message msg, InetSocketAddress source) {
		int processCount = this.process.getAllProcesses().size();
		if (this.pending == null) {
			this.pending = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>>();
			// initialize pending
			this.process = urb.getProcess();

			for (int i = 1; i <= processCount; i++) {
				ConcurrentSkipListSet<Message> skipListSet = new ConcurrentSkipListSet<Message>(
						(m1, m2) -> {
							if (m1.getMsgId() < m2.getMsgId()) {
								return -1;
							} else if (m1.getMsgId() > m2.getMsgId()) {
								return 1;
							}
							return 0;
				});
				this.pending.put(i, skipListSet);
			}
		}
		if (this.vectorClock == null) {
			this.vectorClock = new int[processCount];
		}
		
		boolean urbDelivered = this.urb.urbDeliver(msg , source);
		if (urbDelivered) {
			int pid = msg.getOriginalPid();
			ConcurrentSkipListSet<Message> relevantPending = this.pending.get(pid);
			// check vector clock - should we try to deliver this message?
			if (msg.getMsgId() == (this.vectorClock[pid-1] + 1)) {
				this.vectorClock[pid-1]++;
				System.out.println("d " + msg.getOriginalPid() + " " + msg.getMsgId());
				this.process.addToOutput("d " + msg.getOriginalPid() + " " + msg.getMsgId());
				// if this is a message we are expecting, go over pending and try to urbDeliver
				// messages from the same source
				ConcurrentSkipListSet<Message> tempPending = new ConcurrentSkipListSet<Message>(relevantPending);
				for (Message m : relevantPending) {
//					boolean delivered = this.urb.urbDeliver(m , source);
					if (m.getMsgId() == (this.vectorClock[pid-1]+1)) {
						// if we successfully delivered message, update vector clock
						this.vectorClock[pid - 1] = this.vectorClock[pid-1] + 1;
						// and remove message from pending
						tempPending.remove(m);
						System.out.println("d " + m.getOriginalPid() + " " + m.getMsgId());
						this.process.addToOutput("d " + m.getOriginalPid() + " " + m.getMsgId());
					} else {
						break;
					}
				}
				this.pending.put(pid, tempPending);
			} else {
				relevantPending.add(msg);
				this.pending.put(pid, relevantPending);
			}
		}

		
		
		
//		// put msg in pending
//		int pid = msg.getOriginalPid();
//		ConcurrentSkipListSet<Message> relevantPending = this.pending.get(pid);
//
//		relevantPending.add(msg);
//		this.pending.put(pid, relevantPending);
//		boolean del = this.urb.urbDeliver(msg , source);
//
//		// check vector clock - should we try to deliver this message?
//		if (msg.getMsgId() == (this.vectorClock[pid-1] + 1)) {
//			System.out.println("Passed VC test :: VC[" + (pid - 1) + "] = " + this.vectorClock[pid-1] + " :: msg ID = " + msg.getMsgId());
//			// if this is a message we are expecting, go over pending and try to urbDeliver
//			// messages from the same source
//			ConcurrentSkipListSet<Message> tempPending = new ConcurrentSkipListSet<Message>(relevantPending);
//			for (Message m : relevantPending) {
//				boolean delivered = this.urb.urbDeliver(m , source);
//
//				if (delivered) {
//					// if we successfully delivered message, update vector clock
//					this.vectorClock[pid - 1] = this.vectorClock[pid-1] + 1;
//					// and remove message from pending
//					tempPending.remove(m);
//				} else {
//					// if cannot urb deliver, do not iterate through pending anymore
//					break;
//				}
//			}
//			this.pending.put(pid, tempPending);
//		}
		
			
	}
}
