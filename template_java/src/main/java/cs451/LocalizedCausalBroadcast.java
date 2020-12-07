package cs451;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class LocalizedCausalBroadcast {
	private Process process;
	private UniformReliableBroadcast urb;
	
	private int[] vectorClock;
	ReentrantLock vcLock = new ReentrantLock();
	private PendingHandler pendingThread;

	private CopyOnWriteArrayList<Message> pendingArray;
	
	public LocalizedCausalBroadcast(UniformReliableBroadcast urb) {
		this.urb = urb;
		this.process = this.urb.getProcess();
		this.pendingArray = new CopyOnWriteArrayList<Message>();
		this.pendingThread = new PendingHandler();
		this.pendingThread.start();
	}
	
	public void killPendingThread() {
		this.pendingThread.interrupt();
	}
	
	public void lcbBroadcast(int msgId) {
		if (this.vectorClock == null) {
			int processCount = this.process.getAllProcesses().size();
			this.vectorClock = new int[processCount];
		}
		this.process.addToOutput("b " + msgId);
		System.out.println("creating msg to LCB broadcast");
		// Create a message to broadcast with provided msgId and current process VC
		int[] msgVc = this.prepareMessageVc();
		Message msg = new Message(msgId, this.process.getProcessId(), msgVc);
		System.out.println("PASSED VC:");
		printArray(msg.getMessageVc());
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
		for (int i = 0; i < processVc.length; i++) {
//			System.out.println("MSG VC = " + messageVc[i] + " :: PROCESS VC = " + processVc[i]);
			if (messageVc[i] > processVc[i]) {
				return false;
			}
		}

		return true;
	}
	
	public void lcbDeliver(Message msg, InetSocketAddress source) {
		int processCount = this.process.getAllProcesses().size();
		// initialize the pending messages data structure

		if (this.vectorClock == null) {
			this.vectorClock = new int[processCount];
		}
		
		// try to URB deliver a message from a given source
		boolean urbDelivered = this.urb.urbDeliver(msg , source);
		if (urbDelivered) {
			int pid = msg.getOriginalPid();
			// check vector clock - should we try to deliver this message?
			this.vcLock.lock();
			try {
				if (this.hasDeliveredAllDependencies(msg)) {
					this.vectorClock[pid-1]++;
					this.process.addToOutput("d " + msg.getOriginalPid() + " " + msg.getMsgId());
					System.out.println("d " + msg.getOriginalPid() + " " + msg.getMsgId());
					// if this is a message we are expecting, go over pending and try to urbDeliver
					// messages from the same source - maybe we can deliver some of them now

				} else {
					this.pendingArray.add(msg);
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
	
	private class PendingHandler extends Thread {
		
		public void run() {
			while (true) {
				try {
					for (Message msg : pendingArray) {
						if (hasDeliveredAllDependencies(msg)) {
//							 if we successfully delivered message, update vector clock
							int pid = msg.getOriginalPid();
							vcLock.lock();
							vectorClock[pid - 1] = vectorClock[pid-1] + 1;
							vcLock.unlock();
							// and remove message from pending
							pendingArray.remove(msg);
							process.addToOutput("d " + msg.getOriginalPid() + " " + msg.getMsgId());
							System.out.println("d " + msg.getOriginalPid() + " " + msg.getMsgId());						
						}
					}
				} finally {
					
				}
			}
		}
	}
}
