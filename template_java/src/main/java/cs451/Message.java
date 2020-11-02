package cs451;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 14321532143L;

	private String content;

	private int destPort;
	private int sourcePort;
	private InetAddress destAddr;
	private InetAddress sourceAddr;
	
	private boolean isAck;
	private boolean isBroadcastMessage;
	
	private int msgId;
	private int senderId;
	
	// Ack Message constructor - ack message has the same dest/source port/addr
	// and message id, but has the isAck field set to true;
	public Message(Message originalMessage) {
		this.isAck = true;
		this.msgId = originalMessage.getMsgId();
		this.destPort = originalMessage.getDestPort();
		this.destAddr = originalMessage.getDestAddr();
		this.sourcePort = originalMessage.getSourcePort();
		this.sourceAddr = originalMessage.getSourceAddr();
	}
	
	// Message constructor - creates a message object with provided string content, dest/source port/addr
	// and makes it a broadcast message if specified
	public Message(String content, int msgId, int destPort, InetAddress destAddr, int sourcePort, InetAddress sourceAddr, boolean isBroadcast) {
		this.isAck = false;
		this.isBroadcastMessage = isBroadcast;
		this.content = content;
		this.msgId = msgId;
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
	public int getSenderId() {
		return senderId;
	}
	public void setSenderId(int senderId) {
		this.senderId = senderId;
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
		result = prime * result + ((sourceAddr == null) ? 0 : sourceAddr.hashCode());
		result = prime * result + sourcePort;
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
		if (sourceAddr == null) {
			if (other.sourceAddr != null)
				return false;
		} else if (!sourceAddr.equals(other.sourceAddr))
			return false;
		if (destAddr == null) {
			if (other.destAddr != null)
				return false;
		} else if (!destAddr.equals(other.destAddr))
			return false;
		if (sourcePort != other.sourcePort)
			return false;
		if (destPort != other.destPort)
			return false;
		return true;
	}
	
}
