package ch.elexis.core.ui.eigenartikel;

import org.eclipse.core.databinding.conversion.Converter;

import at.medevit.atc_codes.ATCCode;
import at.medevit.atc_codes.ATCCodeService;
import ch.elexis.core.ui.eigenartikel.consumer.ATCCodeServiceConsumer;

public class ATCCodeToStringConverter extends Converter {
	
	public ATCCodeToStringConverter(){
		super(String.class, String.class);
	}
	
	@Override
	public Object convert(Object fromObject){
		ATCCodeService atcCodeService = ATCCodeServiceConsumer.getATCCodeService();
		if (atcCodeService != null) {
			ATCCode forATCCode = atcCodeService.getForATCCode((String) fromObject);
			if (forATCCode != null) {
				return forATCCode.name_german;
			}
		}
		return "";
	}
	
}
