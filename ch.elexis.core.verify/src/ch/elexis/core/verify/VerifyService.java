package ch.elexis.core.verify;


import ch.elexis.core.model.IVerify;
import ch.elexis.core.model.IVerifyContext;
import ch.elexis.core.model.IVerifyService;
import ch.elexis.core.verify.internal.ElexisVerifyService;
import ch.elexis.core.verify.internal.SumexVerifyService;

public class VerifyService implements IVerifyService {
	
	SumexVerifyService sumexVerifyService = new SumexVerifyService();
	ElexisVerifyService elexisVerifyService = new ElexisVerifyService();
	
	public VerifyService(){
		
	}
	
	@Override
	public IVerify validate(IVerifyContext iVerifyContext, IVerify iVerify){
		IVerify sumexResult = sumexVerifyService.validate(iVerifyContext,
			iVerify);
		if (sumexResult != null) {
			sumexResult.setValidatorId(sumexVerifyService.getValidatorId());
			return sumexResult;
		}
		IVerify tarmedResult = elexisVerifyService.validate(iVerifyContext, iVerify);
		tarmedResult.setValidatorId(elexisVerifyService.getValidatorId());
		return tarmedResult;
	}
	
	@Override
	public String getValidatorId(){
		return null;
	}
}
