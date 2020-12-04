package cs451;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

	private Set<Integer> dependenciesSet;
	
	// Listener object of the process
	private Listener listener;
	private Sender sender;
	
	// hashmap of this process' messages which have been acknowledged
	private volatile ConcurrentHashMap<Message, Set<InetSocketAddress>> acks;
	// hashmap to store a priority queue (based on message id in ascending order) of messages
	// to be sent to other processes
	private volatile ConcurrentHashMap<InetSocketAddress, PriorityQueue<Message>> toSend;
	// contains all messages delivered on the Perfect Link (and BEB) level
	private volatile ConcurrentHashMap<Message, AtomicInteger> delivered;
	
	private BestEffortBroadcast beb;
	private UniformReliableBroadcast urb;
	private FirstInFirstOutBroadcast fifo;

	private StringBuilder output;
	private int messageCount;
	private long elapsedTime;
	
	public Process(InetAddress ip, int port, int pid, int messageCount) {
		this.ip = ip;
		this.port = port;
		this.pid = pid;
		this.isAlive = new AtomicBoolean(true);
		this.messageCount = messageCount;
		this.output = new StringBuilder();
		this.dependenciesSet = new HashSet<Integer>();
		
		// try to open the process' socket
		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("Unable to open socket: Port = " + this.port + " :: IP = " + this.ip);
		}

		this.acks = new ConcurrentHashMap<Message, Set<InetSocketAddress>>();
		this.toSend = new ConcurrentHashMap<InetSocketAddress, PriorityQueue<Message>>();
		this.delivered = new ConcurrentHashMap<Message, AtomicInteger>();
		
		// start the listener thread
		this.listener = new Listener(this);
		this.listener.start();
		
		this.beb = new BestEffortBroadcast(this);
		this.urb = new UniformReliableBroadcast(this.beb);
		this.fifo = new FirstInFirstOutBroadcast(this.urb);
	}

	// FIFO broadcast 'messageCount' number of messages with IDs from 1 to messageCount
	public void beginFifo() {
		for (int i = 1; i <= this.messageCount; i++) {
			this.fifo.fifoBroadcast(i);
		}
	}

	public void sendP2PMessage(Message msg, InetAddress ip, int port) {
		// if msg is not an ack, add to hashmap of messages to be sent
		if (!msg.isAck()) {
			InetSocketAddress dest = new InetSocketAddress(ip, port);
			this.addMessageToSend(dest, msg);
		}
	}

	// send an ack - send only once, no retransmissions
	public void sendAck(Message msg, InetSocketAddress dest) {
		this.sender.sendAck(msg, dest);
	}
	
	// check if a message has been acknowledged by the provided process' address
	public boolean hasBeenAcknowledged(Message msg, InetSocketAddress acker) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
		return currentAcks.contains(acker);
	}

	// count number of processes that have acknowledged the message
	public int ackerCount(Message msg) {
		Set<InetSocketAddress> currentAcks = this.acks.getOrDefault(msg, new HashSet<InetSocketAddress>());
		return currentAcks.size();
	}

	// add process' address as an acknowledger of given message
	public void addAcknowledgement(Message ack, InetSocketAddress acker) {
		Set<InetSocketAddress> currentAcks = this.acks.computeIfAbsent(ack, k -> new HashSet<InetSocketAddress>());
		synchronized(currentAcks) {
			currentAcks.add(acker);			
		}
	}

	// PL deliver a message - only if it has not been delivered yet (no duplication)
	public boolean plDeliverMessage(Message msg) {
		if (!this.hasBeenDelivered(msg)) {
			this.delivered.put(msg, new AtomicInteger(1));
			return true;
		}
		return false;
	}
	
	public boolean hasBeenDelivered(Message msg) {
		return this.delivered.containsKey(msg);
	}
	
	// Initializes some stuff
	public void setAllProcesses(ArrayList<InetSocketAddress> processes) {
		this.allProcesses = processes;		
		for (InetSocketAddress addr : this.allProcesses) {
			this.toSend.putIfAbsent(addr, new PriorityQueue<Message>(new MessageComparator()));
		}
		// Only start sender thread after necessary setup is done
		this.sender = new Sender(this);
		this.sender.start();
	}

	public void setDependencies(String[] dependencies) {
		for (int i = 0; i < dependencies.length; i++) {
			int dependencyId = Integer.parseInt(dependencies[i]);
			this.dependenciesSet.add(dependencyId);
		}
	}
	
	public Set<Integer> getDependencies() {
		return this.dependenciesSet;
	}
	
	// Checks if the process is still running
	public boolean isAlive() {
		return this.isAlive.get();
	}

	// Updates the running status of the process
	public void setIsAlive(boolean isAlive) {
		this.isAlive.set(isAlive);
	}

	// appends to the output string
	public void addToOutput(String toAdd) {
		this.output.append(toAdd);
		this.output.append(System.getProperty("line.separator"));
	}

	// sets process' alive to false, closes the sender and listener threads
	public void killProcess() {
		this.isAlive.set(false);
		this.listener.interrupt();
		this.sender.interrupt();
	}
	
	// Add message to be sent to a particualr destination process
	public void addMessageToSend(InetSocketAddress dest, Message msg) {
		PriorityQueue<Message> curr = this.toSend.computeIfAbsent(dest, k -> new PriorityQueue<Message>());
		synchronized(curr) {
			curr.add(msg);
		}
	}
	
	// Remove a message from the to-be-sent to a specific process
	public void removeMessageFromSend(InetSocketAddress dest, Message msg) {
		PriorityQueue<Message> curr = this.toSend.computeIfAbsent(dest, k -> new PriorityQueue<Message>());
		synchronized(curr) {
			curr.remove(msg);
		}
	}
	
	// Message comparator used for the message priority queue
	class MessageComparator implements Comparator<Message> {

		@Override
		public int compare(Message m1, Message m2) {
			return (m1.getMsgId() <= m2.getMsgId()) ? -1 : 1;
		}
	}
	
	
	// SOME GETTERS

	public BestEffortBroadcast getBeb() {
		return this.beb;
	}

	public UniformReliableBroadcast getUrb() {
		return this.urb;
	}

	public FirstInFirstOutBroadcast getFifo() {
		return this.fifo;
	}

	public ArrayList<InetSocketAddress> getAllProcesses() {
		return this.allProcesses;
	}
	
	public ConcurrentHashMap<InetSocketAddress, PriorityQueue<Message>> getMessagesToSend() {
		return this.toSend;
	}
	
	public PriorityQueue<Message> getMessagesToSendDestination(InetSocketAddress dest) {
		return this.toSend.computeIfAbsent(dest, k -> new PriorityQueue<Message>());
	}
	
	public String getOutput() {
		return this.output.toString();
	}
	
	public int getMessageCount() {
		return this.messageCount;
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
	
	// Used to time execution
	public void setElapsed(long time) {
		this.elapsedTime = time;
	}
	
	public long getElapsed() {
		return this.elapsedTime;
	}
	
	// Used to make sure correct number of messages have been BEB/PL delivered
	public int countDelivered() {
		return this.delivered.size();
	}
}
