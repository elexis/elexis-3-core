package ch.elexis.core.fhir.model.dto;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IMandatorDto extends IPersonDto implements IMandator {

	boolean active;
	IContact biller;

}
