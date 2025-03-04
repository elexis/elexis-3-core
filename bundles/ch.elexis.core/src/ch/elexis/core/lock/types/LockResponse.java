package ch.elexis.core.lock.types;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LockResponse {

	public static final LockResponse OK = new LockResponse(Status.OK, null);
	public static final LockResponse ERROR = new LockResponse(Status.ERROR, null);

	public enum Status {
		OK, DENIED, DENIED_PERMANENT, ERROR, NOINFO
	};

	private Status status;
	private LockInfo lockInfo;
	private LockRequest.Type lockRequestType;

	public LockResponse() {
		status = Status.OK;
		lockInfo = new LockInfo();
	}

	public LockResponse(Status status, LockInfo lockInfo) {
		this(status, lockInfo, null);
	}

	public LockResponse(Status status, LockInfo lockInfo, LockRequest.Type lockRequestType) {
		this.status = status;
		this.lockInfo = (lockInfo != null) ? lockInfo : new LockInfo();
		this.lockRequestType = lockRequestType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public LockInfo getLockInfo() {
		return lockInfo;
	}

	public void setLockInfo(LockInfo lockInfo) {
		this.lockInfo = lockInfo;
	}

	/**
	 * @return the request type this response answers to
	 */
	public LockRequest.Type getLockRequestType() {
		return lockRequestType;
	}

	public static LockResponse OK(LockInfo lie) {
		return new LockResponse(Status.OK, lie);
	}

	public static LockResponse DENIED(LockInfo lie) {
		return new LockResponse(Status.DENIED, lie);
	}

	public static LockResponse NOINFO(LockInfo lie) {
		return new LockResponse(Status.NOINFO, lie);
	}

	public boolean isOk() {
		return getStatus() == Status.OK;
	}
}
