package ch.elexis.core.verification.billing;


import ch.elexis.core.model.BillingVerification;
import ch.elexis.core.model.IVerificationContext;
import ch.elexis.core.model.IVerificationService;
import ch.elexis.core.verification.billing.internal.ElexisVerificationService;
import ch.elexis.core.verification.billing.internal.SumexVerificationService;

public class BillingVerificationService implements IVerificationService<BillingVerification> {
	
	SumexVerificationService sumexVerificationService = new SumexVerificationService();
	ElexisVerificationService elexisVerificationService = new ElexisVerificationService();
	
	public BillingVerificationService(){
		
	}

	@Override
	public String getValidatorId(){
		return null;
	}
	
	@Override
	public BillingVerification validate(
		IVerificationContext<BillingVerification> iVerificationContext,
		BillingVerification verification){
		BillingVerification sumexResult =
			sumexVerificationService.validate(iVerificationContext, verification);
		if (sumexResult != null) {
			sumexResult.setValidatorId(sumexVerificationService.getValidatorId());
			return sumexResult;
		}
		BillingVerification tarmedResult =
			elexisVerificationService.validate(iVerificationContext, verification);
		tarmedResult.setValidatorId(elexisVerificationService.getValidatorId());
		return tarmedResult;
	}
	

}
