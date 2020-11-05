package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Process {
	
	// Process communication info - socket, ip, port
	private DatagramSocket socket;
	private InetAddress ip;
	private Integer port;
	private boolean isAlive;
	
	// Info from membership file - process id, list of all processes, broadcast count
	private Integer pid;
	private ArrayList<InetSocketAddress> allProcesses;
    private HashMap<InetSocketAddress, Integer> addressesToPids;
    private HashMap<Integer, InetSocketAddress> pidsToAddresses;
	
	// Listener object of the process
	private Listener listener;
	// Sender object of the process
	private Sender sender;
	
	// Message ID counter for messages of this processes
	static Integer msgId;
	
	// hashmap of messages which the process has delivered
	private volatile ConcurrentHashMap<Message, Boolean> delivered;
	
	// hashmap of this process' messages which have been acknowledged
	private volatile ConcurrentHashMap<Message, Boolean> acknowledged;
	
	DatagramPacket packet = null;

	private BestEffortBroadcast beb;
	private UniformReliableBroadcast urb;
	
	public Process(InetAddress ip, int port, int pid) {
		this.ip = ip;
		this.port = port;
		this.pid = pid;		
		this.isAlive = true;
		
		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("Unable to open socket: Port = " + this.port + " :: IP = " + this.ip);
		}
		
		this.delivered = new ConcurrentHashMap<Message, Boolean>();
		this.acknowledged = new ConcurrentHashMap<Message, Boolean>();

		this.listener = new Listener(this);
		System.out.println("Opening listener thread");
		this.listener.start();
		this.beb = new BestEffortBroadcast(this);
		this.urb = new UniformReliableBroadcast(this.beb);
	}
	
	
	public void sendP2PMessage(Message m, InetAddress ip, int port) {
		new Sender(this, m, port, ip).start();
	}
	
	public boolean addDelieveredMessage(Message msg) {
		if (!this.hasBeenDelievered(msg)) {
			this.delivered.put(msg, true);
			return true;
//			System.out.println("DELIVERING NOW");
		}
		return false;
	}
	
	public boolean hasBeenDelievered(Message msg) {
		return this.delivered.containsKey(msg);
	}
	
	public boolean hasBeenAcknowledged(Message msg) {
		return this.acknowledged.containsKey(msg);
	}
	
	public void addAcknowledgement(Message ack) {
		this.acknowledged.put(ack, true);
	}
	
	public void setAllProcesses(ArrayList<InetSocketAddress> processes) {
		this.allProcesses = processes;
	}
	
	public void setAddressesToPids(HashMap<InetSocketAddress, Integer> procs) {
		this.addressesToPids = procs;
	}
	
	public HashMap<InetSocketAddress, Integer> getAddressesToPids() {
		return this.addressesToPids;
	}

	public void setPidsToAddresses(HashMap<Integer, InetSocketAddress> procs) {
		this.pidsToAddresses = procs;
	}
	
	public HashMap<Integer, InetSocketAddress> getPidsToAddresses() {
		return this.pidsToAddresses;
	}
	
	public ArrayList<InetSocketAddress> getAllProcesses() {
		return this.allProcesses;
	}
	
	public DatagramSocket getSocket() {
		return this.socket;
	}
	
	public Integer getProcessId() {
		return this.pid;
	}
	
	public Integer getProcessPort() {
		return this.port;
	}
	
	public InetAddress getProcessAddress() {
		return this.ip;
	}
	
	public boolean isAlive() {
		return this.isAlive;
	}
	
	public void setIsAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
	
	public BestEffortBroadcast getBeb() {
		return this.beb;
	}
	
	public UniformReliableBroadcast getUrb() {
		return this.urb;
	}
}
