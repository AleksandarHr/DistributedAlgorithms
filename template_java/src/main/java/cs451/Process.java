package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	
	// Message ID counter for messages of this processes
	static Integer msgId;
	
	// hashmap of messages which the process has delivered
	private volatile ConcurrentHashMap<Message, Boolean> delivered;
	
	// hashmap of this process' messages which have been acknowledged
//	private volatile ConcurrentHashMap<Message, Boolean> acknowledged;
	private volatile ConcurrentHashMap<Message, Set<InetSocketAddress>> acks;

	DatagramPacket packet = null;

	private BestEffortBroadcast beb;
	private UniformReliableBroadcast urb;
	private FirstInFirstOutBroadcast fifo;
	
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
		this.acks = new ConcurrentHashMap<Message, Set<InetSocketAddress>>();

		this.listener = new Listener(this);
		System.out.println("Opening listener thread");
		this.listener.start();
		this.beb = new BestEffortBroadcast(this);
		this.urb = new UniformReliableBroadcast(this.beb);
		this.fifo = new FirstInFirstOutBroadcast(this.urb);
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
	
	public boolean hasBeenAcknowledged(Message msg, InetSocketAddress acker) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
		return currentAcks.contains(acker);
	}
	
	public int ackerCount(Message msg) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
		System.out.println("ENTRIES " + this.acks.size());
		for (Map.Entry<Message, Set<InetSocketAddress>> entry : this.acks.entrySet()) {
//			System.out.println("ENTRY msg " + entry.getKey().getMsgId() + " vs. MSG " + msg.getMsgId() + " :: " + "ENTRY pid " + entry.getKey().getOriginalPid() + " vs. pid " + msg.getOriginalPid());
			if (msg.getMsgId() == entry.getKey().getMsgId() && msg.getOriginalPid() == entry.getKey().getOriginalPid()) {
				System.out.println(" WE HAVE A MATCH --> " + entry.getKey().equals(msg));
			}
		}
		return currentAcks.size();
	}
	
	public void addAcknowledgement(Message ack, InetSocketAddress acker) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(ack, new HashSet<InetSocketAddress>());
		currentAcks.add(acker);
		System.out.println("msg " + ack.getMsgId() + " has " + currentAcks.size() + " acks");
		this.acks.put(ack, currentAcks);
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
	
	public FirstInFirstOutBroadcast getFifo() {
		return this.fifo;
	}
	
	public int getPidFromAddres(InetSocketAddress addr) {
		return this.addressesToPids.get(addr);
	}
}
