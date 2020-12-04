package cs451;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

public class LocalizedCausalBroadcast {
	private Process process;
	private UniformReliableBroadcast urb;
	
	private int[] vectorClock;
	ReentrantLock vcLock = new ReentrantLock();
	private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>> pending;

	public LocalizedCausalBroadcast(UniformReliableBroadcast urb) {
		this.urb = urb;
		this.process = this.urb.getProcess();
	}
	
	public void lcbBroadcast(int msgId) {
		if (this.vectorClock == null) {
			int processCount = this.process.getAllProcesses().size();
			this.vectorClock = new int[processCount];
		}
		this.process.addToOutput("b " + msgId);
		System.out.println("creating msg to LCB broadcast");
		// Create a message to broadcast with provided msgId and current process VC
		Message msg = new Message(msgId, this.process.getProcessId(), this.prepareMessageVc());
		this.urb.urbBroadcast(msg);
	}
	
	public int[] prepareMessageVc() {
		int[] msgVc = this.getVc();
		Set<Integer> processDependencies = this.process.getDependencies();
		for (int i = 0; i < msgVc.length; i++) {
			if (!processDependencies.contains(i+1)) {
				msgVc[i] = 0;
			}
		}
		printArray(msgVc);
		return msgVc;
	}
	
	private void printArray(int[] vc) {
		for (int i = 0; i < vc.length; i++) {
			System.out.print(vc[i] + " ");
		}
		System.out.println();
	}
	
	// returns a copy of the current VC states
	public int[] getVc() {
		this.vcLock.lock();
		int[] copied = new int[this.vectorClock.length];
		System.arraycopy(this.vectorClock, 0, copied, 0, this.vectorClock.length);
		this.vcLock.unlock();
		return copied;
	}
	
	private boolean hasDeliveredAllDependencies(Message msg) {
		int[] messageVc = msg.getMessageVc();
		// TODO: double check things with the lock etc
		int[] processVc = this.getVc();
		// Compare process VC with message VC - if a message dependency is > what process has
		// delivered by this dependency, then we cannot deliver the message yet
		int pid = msg.getOriginalPid();
		if (msg.getMsgId() != (this.vectorClock[pid-1] + 1)) {
			return false;
		}
		if (messageVc == null) {
			System.out.println("MESSAGE VC IS NULL");
			if (msg.isAck() == true) {
				System.out.println("and it's an ACK");
			}
		}
		for (int i = 0; i < processVc.length; i++) {
			if (messageVc[i] > processVc[i]) {
				return false;
			}
		}

		return true;
	}
	
	public void lcbDeliver(Message msg, InetSocketAddress source) {
		int processCount = this.process.getAllProcesses().size();
		// initialize the pending messages data structure
		if (this.pending == null) {
			this.pending = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>>();
			// initialize pending
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
		
		// try to URB deliver a message from a given source
		boolean urbDelivered = this.urb.urbDeliver(msg , source);
		if (urbDelivered) {
			int pid = msg.getOriginalPid();
			ConcurrentSkipListSet<Message> relevantPending = this.pending.get(pid);
			// check vector clock - should we try to deliver this message?
			this.vcLock.lock();
			try {
				if (this.hasDeliveredAllDependencies(msg)) {
					this.vectorClock[pid-1]++;
					this.process.addToOutput("d " + msg.getOriginalPid() + " " + msg.getMsgId());
					System.out.println("d " + msg.getOriginalPid() + " " + msg.getMsgId());
					// if this is a message we are expecting, go over pending and try to urbDeliver
					// messages from the same source - maybe we can deliver some of them now
					ConcurrentSkipListSet<Message> tempPending = new ConcurrentSkipListSet<Message>(relevantPending);
					for (Message m : relevantPending) {
						if (this.hasDeliveredAllDependencies(m)) {
							// if we successfully delivered message, update vector clock
							this.vectorClock[pid - 1] = this.vectorClock[pid-1] + 1;
							// and remove message from pending
							tempPending.remove(m);
							this.process.addToOutput("d " + m.getOriginalPid() + " " + m.getMsgId());
							System.out.println("d " + m.getOriginalPid() + " " + m.getMsgId());							
						} else {
							break;
						}
					}
					this.pending.put(pid, tempPending);
				} else {
					relevantPending.add(msg);
					this.pending.put(pid, relevantPending);
				}
			} finally {
//				boolean done = this.allDone();
//				if (done) {
//					this.process.setElapsed(this.elapsed);
//				}
				this.vcLock.unlock();
			}
		}			
	}
}
