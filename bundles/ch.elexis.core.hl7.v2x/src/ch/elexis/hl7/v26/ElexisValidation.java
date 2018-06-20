package ch.elexis.hl7.v26;

import java.util.Iterator;

import ca.uhn.hl7v2.validation.PrimitiveTypeRule;
import ca.uhn.hl7v2.validation.impl.DefaultValidation;
import ca.uhn.hl7v2.validation.impl.RuleBinding;

/**
 * Elexis Validation removes HL7 rule wich removes leading whitespaces
 * 
 * @author immi
 * 
 */
public class ElexisValidation extends DefaultValidation {
	private static final long serialVersionUID = 2666905258733637592L;
	
	public ElexisValidation(){
		super();
		Iterator<RuleBinding<PrimitiveTypeRule>> iter = getPrimitiveRuleBindings().listIterator();
		for (int i = 0, n = getPrimitiveRuleBindings().size(); i < n; i++) {
			Object o = iter.next();
			if (o instanceof RuleBinding) {
				RuleBinding binding = (RuleBinding) o;
				if (binding.getActive() && binding.appliesToVersion("*")
					&& binding.appliesToScope("TX")) {
					iter.remove();
				} else if (binding.getActive() && binding.appliesToVersion("*")
					&& binding.appliesToScope("TN")) {
					iter.remove();
				}
			}
		}
	}
}
