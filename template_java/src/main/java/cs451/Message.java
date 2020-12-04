package cs451;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;

public class Message implements Serializable {

	private static final long serialVersionUID = 14321532143L;

	private boolean isAck;
	
	private int msgId;
	private int originalPid;

	private int[] messageVc;
	
	// Ack Message constructor - ack message has the same dest/source port/addr
	// and message id, but has the isAck field set to true;
	public Message(Message originalMessage) {
		this.isAck = true;
		this.msgId = originalMessage.getMsgId();
		this.originalPid = originalMessage.getOriginalPid();
	}
		
	// Message constructor - creates a message object with provided string content, dest/source port/addr
	public Message(int msgId, int senderPid) {
		this.isAck = false;
		this.msgId = msgId;
		this.originalPid = senderPid;
	}

	// Message constructor - creates a message object with provided string content, dest/source port/addr
	// and message vector clock
	public Message(int msgId, int senderPid, int[] messageVc) {
		this.isAck = false;
		this.msgId = msgId;
		this.originalPid = senderPid;
		
		int[] copied = new int[this.messageVc.length];
		System.arraycopy(this.messageVc, 0, copied, 0, this.messageVc.length);
	}
	
	public int getOriginalPid() {
		return this.originalPid;
	}
	public void setOriginalPid(int originalPid) {
		this.originalPid = originalPid;
	}
	public int getMsgId() {
		return msgId;
	}
	public boolean isAck() {
		return this.isAck;
	}
	public int[] getMessageVc() {
		return this.messageVc;
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
