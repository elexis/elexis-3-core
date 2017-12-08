package ch.elexis.core.lock.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LockRequest {

	public enum Type {
		ACQUIRE, RELEASE, INFO
	};

	private Type requestType;
	private LockInfo lockInfo;

	public LockRequest() {
	}

	public LockRequest(Type requestType, LockInfo lockInfo) {
		this.requestType = requestType;
		this.lockInfo = lockInfo;
	}

	public Type getRequestType() {
		return requestType;
	}

	public void setRequestType(Type requestType) {
		this.requestType = requestType;
	}

	public LockInfo getLockInfo() {
		return lockInfo;
	}

	public void setLockInfo(LockInfo lockInfo) {
		this.lockInfo = lockInfo;
	}

}
