package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Process {
	
	// Process communication info - socket, ip, port
	private DatagramSocket socket;
	private InetAddress ip;
	private Integer port;
	
	// Info from membership file - process id, list of all processes, broadcast count
	private Integer pid;
	private ArrayList<InetSocketAddress> allProcesses;
	private Integer broadcastCount;
	
	// Listener object of the process
	private Listener listener;
	// Sender object of the process
	private Sender sender;
	// Broadcaster object of the process
	private Broadcaster broadcaster;
	
	// Message ID counter for messages of this processes
	static Integer msgId;
	
	// hashmap of messages which the process has delivered
	private volatile ConcurrentHashMap<Message, Boolean> delivered;
	
	// hashmap of messages which the process has acknowledged
	private volatile ConcurrentHashMap<Message, Boolean> acknowledged;
	
	private boolean running;
	private byte[] buffer = new byte[256];
	DatagramPacket packet = null;
	
	public Process(InetAddress ip, int port, int pid) {
		try {
			this.socket = new DatagramSocket(this. port, this.ip);
		} catch (SocketException e) {
			System.out.println("Unable to open socket.");
		}

		this.ip = ip;
		this.port = port;
		this.pid = pid;		
		
		this.delivered = new ConcurrentHashMap<Message, Boolean>();
		this.acknowledged = new ConcurrentHashMap<Message, Boolean>();

		this.listener = new Listener(this);
		this.broadcaster = new Broadcaster(this);
		this.listener.run();
		this.broadcaster.run();
	}
	
	
	
	
	
	
	
	public DatagramSocket getSocket() {
		return this.socket;
	}
	
	public Integer getProcessId() {
		return this.pid;
	}
}
