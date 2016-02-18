package info.elexis.server.elexis.common.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LockResponse {

	public enum Type {
		OK, DENIED
	};

	private Type requestType;
	private List<LockInfo> lockInfos;

	public LockResponse() {}
	
	public LockResponse(Type requestType, List<LockInfo> lockInfos) {
		this.requestType = requestType;
		this.lockInfos = lockInfos;
	}

	public Type getRequestType() {
		return requestType;
	}

	public void setRequestType(Type requestType) {
		this.requestType = requestType;
	}

	public List<LockInfo> getLockInfos() {
		return lockInfos;
	}

	public void setLockInfos(List<LockInfo> lockInfos) {
		this.lockInfos = lockInfos;
	}

}
