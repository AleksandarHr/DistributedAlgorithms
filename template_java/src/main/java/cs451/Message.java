package cs451;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {

	private static final long serialVersionUID = 14321532143L;

	private String content;

	private int destPort;
	private int sourcePort;
	private InetAddress destAddr;
	private InetAddress sourceAddr;
	
	private boolean isAck;
	private boolean isBroadcastMessage;
	
	private int msgId;
	private int originalPid;

	// Ack Message constructor - ack message has the same dest/source port/addr
	// and message id, but has the isAck field set to true;
	public Message(Message originalMessage) {
		this.isAck = true;
		this.msgId = originalMessage.getMsgId();
		this.destPort = originalMessage.getDestPort();
		this.destAddr = originalMessage.getDestAddr();
		this.sourcePort = originalMessage.getSourcePort();
		this.sourceAddr = originalMessage.getSourceAddr();
		this.originalPid = originalMessage.getOriginalPid();
	}
	
	// Message constructor - creates a message object with provided string content, dest/source port/addr
	// and makes it a broadcast message if specified
	public Message(String content, int msgId, int senderPid, int destPort, InetAddress destAddr, int sourcePort, InetAddress sourceAddr, boolean isBroadcast) {
		this.isAck = false;
		this.isBroadcastMessage = isBroadcast;
		this.content = content;
		this.msgId = msgId;
		this.originalPid = senderPid;
		this.destPort = destPort;
		this.destAddr = destAddr;
		this.sourcePort = sourcePort;
		this.sourceAddr = sourceAddr;
	}
	
//	public Message(String content, int destPort, int sourcePort, InetAddress destAddr, InetAddress sourceAddr,
//			int msgId, int senderId) {
//		super();
//		this.content = content;
//		this.destPort = destPort;
//		this.sourcePort = sourcePort;
//		this.destAddr = destAddr;
//		this.sourceAddr = sourceAddr;
//		this.msgId = msgId;
//		this.senderId = senderId;
//	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getDestPort() {
		return destPort;
	}
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	public int getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
	public InetAddress getDestAddr() {
		return destAddr;
	}
	public void setDestAddr(InetAddress destAddr) {
		this.destAddr = destAddr;
	}
	public InetAddress getSourceAddr() {
		return sourceAddr;
	}
	public void setSourceAddr(InetAddress sourceAddr) {
		this.sourceAddr = sourceAddr;
	}
	public int getOriginalPid() {
		return this.originalPid;
	}
	public void setOriginalPid(int originalPid) {
		this.originalPid = originalPid;
	}
	public boolean isBroadcastMessage() {
		return this.isBroadcastMessage;
	}
	public void setIsBroadcastMessage(boolean isBcast) {
		this.isBroadcastMessage = isBcast;
	}
	public int getMsgId() {
		return msgId;
	}
	public boolean isAck() {
		return this.isAck;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + msgId;
		result = prime * result + originalPid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (msgId != other.msgId)
			return false;
		if (originalPid != other.originalPid)
			return false;
		return true;
	}
}
