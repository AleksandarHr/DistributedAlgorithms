package cs451;

public class UniformReliableBroadcast {
	
	private Process process;
	private BestEffortBroadcast beb;
	
	public UniformReliableBroadcast(BestEffortBroadcast beb) {
		this.process = beb.getProcess();
		this.beb = beb;
	}
	
	public void urbBroadcast(Message m) {
		this.beb.bebBroadcast(m);
	}
	
	public void urbDeliver(Message m) {
		
	}
}
