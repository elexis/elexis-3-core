package ch.elexis.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.types.VerificationType;

public class BillingVerification {
	
	private double count;
	private IStatus iStatus;
	private IBillable iBillable;
	private VerificationType verificationType;
	private String validatorId;
	
	private Map<String, String> info = new HashMap<>();
	
	private BillingVerification(){
	}
	
	public static BillingVerification create(IBillable verrechenbar,
		VerificationType verificationType,
		double count){
		BillingVerification ret = new BillingVerification();
		ret.setCount(count);
		ret.setStatus(Status.OK_STATUS);
		ret.setBillable(verrechenbar);
		ret.setVerificationType(verificationType);
		return ret;
	}
	

	public double getCount(){
		return count;
	}
	

	public void setCount(double d){
		this.count = d;
	}
	

	public IStatus getStatus(){
		return iStatus;
	}
	

	public void setStatus(IStatus value){
		this.iStatus = value;
	}
	

	public IBillable getBillable(){
		return iBillable;
	}
	

	public void setBillable(IBillable value){
		this.iBillable = value;
	}
	

	public Map<String, String> getInfo(){
		return info;
	}
	
	public void setVerificationType(VerificationType verificationType){
		this.verificationType = verificationType;
	}
	
	public VerificationType getVerificationType(){
		return verificationType;
	}
	

	public String getValidatorId(){
		return validatorId;
	}
	

	public void setValidatorId(String value){
		this.validatorId = value;
		
	}
	
	@Override
	public String toString(){
		return "BillingVerification [count=" + count + ", iStatus=" + iStatus + ", iBillable="
			+ iBillable + ", verificationType=" + verificationType + ", validatorId=" + validatorId
			+ ", info=" + info + "]";
	}
}