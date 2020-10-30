package cs451;

public class Broadcaster extends Thread{

	private Sender sender;
	private Process process;
	private boolean running = false;
	
	public Broadcaster (Process p) {
		this.process = p;
	}
	
	public void run() {
		this.running = true;
	}
}
