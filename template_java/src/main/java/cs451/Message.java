package cs451;

import java.net.InetAddress;

public class Message {
	
	private String content;

	private int destPort;
	private int sourcePort;
	private InetAddress destAddr;
	private InetAddress sourceAddr;
	
	private boolean isAck;
	private boolean isSimpleBroadcast;
	
	private int msgId;
	private int senderId;
	private boolean toAck;
	private boolean toBroadcast;
	
	
	public Message(String content, int destPort, int sourcePort, InetAddress destAddr, InetAddress sourceAddr,
			int msgId, int senderId, boolean toAck, boolean toBroadcast) {
		super();
		this.content = content;
		this.destPort = destPort;
		this.sourcePort = sourcePort;
		this.destAddr = destAddr;
		this.sourceAddr = sourceAddr;
		this.msgId = msgId;
		this.senderId = senderId;
		this.toAck = toAck;
		this.toBroadcast = toBroadcast;
	}
	
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
	public boolean isToAck() {
		return toAck;
	}
	public void setToAck(boolean toAck) {
		this.toAck = toAck;
	}
	public boolean isToBroadcast() {
		return toBroadcast;
	}
	public void setToBroadcast(boolean toBroadcast) {
		this.toBroadcast = toBroadcast;
	}
	public int getMsgId() {
		return msgId;
	}
	public boolean isAck() {
		return this.isAck;
	}
	public boolean isSimpleBroadcast() {
		return this.isSimpleBroadcast;
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
		if (sourcePort != other.sourcePort)
			return false;
		return true;
	}
	
}
