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
import java.util.concurrent.atomic.AtomicBoolean;

public class Process {

	// Process communication info - socket, ip, port
	private DatagramSocket socket;
	private InetAddress ip;
	private Integer port;
	private AtomicBoolean isAlive;

	// Info from membership file - process id, list of all processes, broadcast
	// count
	private Integer pid;
	private ArrayList<InetSocketAddress> allProcesses;

	// Listener object of the process
	private Listener listener;

	// hashmap of this process' messages which have been acknowledged
	private volatile ConcurrentHashMap<Message, Set<InetSocketAddress>> acks;

	private BestEffortBroadcast beb;
	private UniformReliableBroadcast urb;
	private FirstInFirstOutBroadcast fifo;

	private StringBuilder output;
	private int messageCount;

	public Process(InetAddress ip, int port, int pid, int messageCount) {
		this.ip = ip;
		this.port = port;
		this.pid = pid;
		this.isAlive = new AtomicBoolean(true);
		this.messageCount = messageCount;
		this.output = new StringBuilder();

		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("Unable to open socket: Port = " + this.port + " :: IP = " + this.ip);
		}

		this.acks = new ConcurrentHashMap<Message, Set<InetSocketAddress>>();

		this.listener = new Listener(this);
		System.out.println("Opening listener thread");
		this.listener.start();
		this.beb = new BestEffortBroadcast(this);
		this.urb = new UniformReliableBroadcast(this.beb);
		this.fifo = new FirstInFirstOutBroadcast(this.urb);
	}

	public void beginFifo() {
		for (int i = 1; i <= this.messageCount; i++) {
			this.fifo.fifoBroadcast(i);
		}
	}

	public void sendP2PMessage(Message m, InetAddress ip, int port) {
		new Sender(this, m, port, ip).start();
	}

	public boolean hasBeenAcknowledged(Message msg, InetSocketAddress acker) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
		return currentAcks.contains(acker);
	}

	public int ackerCount(Message msg) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
		return currentAcks.size();
	}

	public void addAcknowledgement(Message ack, InetSocketAddress acker) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(ack, new HashSet<InetSocketAddress>());
		currentAcks.add(acker);
		this.acks.put(ack, currentAcks);
	}

	public void setAllProcesses(ArrayList<InetSocketAddress> processes) {
		this.allProcesses = processes;
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
		return this.isAlive.get();
	}

	public void setIsAlive(boolean isAlive) {
		this.isAlive.set(isAlive);
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

	public void addToOutput(String toAdd) {
		this.output.append(toAdd);
		this.output.append(System.getProperty("line.separator"));
	}

	public String getOutput() {
		return this.output.toString();
	}

	public void killProcess() {
		this.isAlive.set(false);
		this.listener.interrupt();
	}
}
