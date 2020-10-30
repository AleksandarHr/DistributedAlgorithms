package cs451;

public class BestEffortBroadcast {

	private Process process;
	
	public BestEffortBroadcast(Process p) {
		this.process = p;
	}
	
	public void bebBroadcast(Message m) {
		// this.process.SendMessage(m)
	}
	
	public boolean bebDeliver(Message m) {
		// always deliver
		return true;
	}
}
