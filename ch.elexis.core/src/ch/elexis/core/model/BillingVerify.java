package ch.elexis.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.types.VerifyType;

public class BillingVerify implements IVerify {
	
	private double count;
	private IStatus iStatus;
	private IBillable iBillable;
	private VerifyType verifyType;
	private String validatorId;
	
	private Map<String, String> info = new HashMap<>();
	
	private BillingVerify(){
	}
	
	public static IVerify create(IBillable verrechenbar, VerifyType verifyType, double count){
		IVerify ret = new BillingVerify();
		ret.setCount(count);
		ret.setStatus(Status.OK_STATUS);
		ret.setBillable(verrechenbar);
		ret.setVerifyType(verifyType);
		return ret;
	}
	
	@Override
	public double getCount(){
		return count;
	}
	
	@Override
	public void setCount(double d){
		this.count = d;
	}
	
	@Override
	public IStatus getStatus(){
		return iStatus;
	}
	
	@Override
	public void setStatus(IStatus value){
		this.iStatus = value;
	}
	
	@Override
	public IBillable getBillable(){
		return iBillable;
	}
	
	@Override
	public void setBillable(IBillable value){
		this.iBillable = value;
	}
	
	@Override
	public Map<String, String> getInfo(){
		return info;
	}
	
	@Override
	public String toString(){
		return "Verify [count=" + count + ", iStatus=" + iStatus + ", iBillable=" + iBillable
			+ ", info=" + info.keySet().toArray() + "]";
	}
	
	@Override
	public VerifyType getVerifyType(){
		return verifyType;
	}
	
	@Override
	public void setVerifyType(VerifyType value){
		this.verifyType = value;
	}
	
	@Override
	public String getValidatorId(){
		return validatorId;
	}
	
	@Override
	public void setValidatorId(String value){
		this.validatorId = value;
		
	}
}