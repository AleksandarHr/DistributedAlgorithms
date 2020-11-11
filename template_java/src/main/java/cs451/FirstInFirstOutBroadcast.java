package cs451;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

public class FirstInFirstOutBroadcast {
	private Process process;
	private UniformReliableBroadcast urb;
	
	// contains the number of messages received by respective pid (e.g. highest message id received)
	private int[] vectorClock;
	ReentrantLock vcLock = new ReentrantLock();
	private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>> pending;
	
	long startTime, endTime, elapsed;
	
	public FirstInFirstOutBroadcast(UniformReliableBroadcast urb) {
		this.urb = urb;		
		this.process = urb.getProcess();
	}
	
	public void fifoBroadcast(int msgId) {
		this.startTime = System.nanoTime();
		if (this.vectorClock == null) {
			int processCount = this.process.getAllProcesses().size();
			this.vectorClock = new int[processCount];
		}
		this.process.addToOutput("b " + msgId);
		this.urb.urbBroadcast(msgId);
	}
	
	public int[] getVc() {
		this.vcLock.lock();
		int[] copied = new int[this.vectorClock.length];
		System.arraycopy(this.vectorClock, 0, copied, 0, this.vectorClock.length);
		this.vcLock.unlock();
		
		return copied;
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
//		System.out.println("TRID TO URB DELIVEr");
		if (urbDelivered) {
			int pid = msg.getOriginalPid();
			ConcurrentSkipListSet<Message> relevantPending = this.pending.get(pid);
			// check vector clock - should we try to deliver this message?
			this.vcLock.lock();
			try {
				if (msg.getMsgId() == (this.vectorClock[pid-1] + 1)) {
					this.vectorClock[pid-1]++;
					this.process.addToOutput("d " + msg.getOriginalPid() + " " + msg.getMsgId());
					System.out.println("d " + msg.getOriginalPid() + " " + msg.getMsgId());
					// if this is a message we are expecting, go over pending and try to urbDeliver
					// messages from the same source
					ConcurrentSkipListSet<Message> tempPending = new ConcurrentSkipListSet<Message>(relevantPending);
					for (Message m : relevantPending) {
						if (m.getMsgId() == (this.vectorClock[pid-1]+1)) {
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
				this.vcLock.unlock();
			}
		}			
	}
	
	public boolean allDone() {
		int[] vc = this.getVc();
		int msgs = this.process.getMessageCount();
		boolean done = true;
		for (int i = 0; i < vc.length; i++) {
			if (vc[i] != msgs) {
				return false;
			}
		}
		this.endTime = System.nanoTime();
		this.elapsed = (this.endTime - this.startTime) / 1000000;
		
		return true;
	}
	
	public long getElapsedTime() {
		return this.elapsed;
	}
}
