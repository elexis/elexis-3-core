package ch.elexis.core.lock.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LockResponse {

	public static final LockResponse OK = new LockResponse(Status.OK, null);
	public static final LockResponse ERROR = new LockResponse(Status.ERROR, null);
	
	public enum Status{
		OK, DENIED, DENIED_PERMANENT, ERROR
	};

	private Status status;
	private LockInfo lockInfo;

	public LockResponse() {
		status = Status.OK;
		lockInfo = new LockInfo();
	}

	public LockResponse(Status status, LockInfo lockInfo) {
		this.status = status;
		this.lockInfo = (lockInfo!=null) ? lockInfo : new LockInfo();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return if {@link #getStatus()} {@link Status#DENIED} contains the
	 *         {@link LockInfo} the request failed upon, else <code>null</code>
	 */
	public LockInfo getLockInfos() {
		return lockInfo;
	}

	public void setLockInfos(LockInfo lockInfo) {
		this.lockInfo = lockInfo;
	}

	public static LockResponse OK() {
		return new LockResponse();
	}

	public static LockResponse DENIED(LockInfo lie) {
		return new LockResponse(Status.DENIED, lie);
	}

	public boolean isOk() {
		return getStatus()==Status.OK;
	}
}
