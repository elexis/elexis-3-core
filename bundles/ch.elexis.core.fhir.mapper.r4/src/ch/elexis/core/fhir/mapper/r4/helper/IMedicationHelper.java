package ch.elexis.core.fhir.mapper.r4.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.SimpleQuantity;

import ch.elexis.core.findings.codes.MedicamentCoding;
import ch.elexis.core.model.IArticle;

public class IMedicationHelper {

	public Coding getGtinCoding(String gtin) {
		if (StringUtils.isNumeric(gtin)) {
			return new Coding(MedicamentCoding.GTIN.getOid(), gtin, null);
		}
		return null;
	}

	public List<Coding> getAtcCodings(String atcCodes) {
		if (atcCodes != null && !atcCodes.isEmpty()) {
			List<Coding> ret = new ArrayList<>();
			String[] codes = atcCodes.split(";");
			for (String string : codes) {
				// TODO translate ATC code to display text
				ret.add(new Coding(MedicamentCoding.ATC.getOid(), string, null));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	public Coding getNameCoding(String name) {
		if (StringUtils.isNotBlank(name)) {
			return new Coding(MedicamentCoding.NAME.getUrl(), name, null);
		}
		return null;
	}

	public Coding getTypeCoding(IArticle localObject) {
		return new Coding(MedicamentCoding.TYPE.getUrl(), localObject.getTyp().getCodeSystemName(), null);
	}

	public Ratio determineAmount(IArticle localObject) {
		SimpleQuantity quantity = new SimpleQuantity();
		quantity.setUnit(localObject.getPackageUnit());
		try {
			quantity.setValue(localObject.getPackageSize());
		} catch (NumberFormatException e) {
			// encode into unit if non numeric
			quantity.setUnit(localObject.getPackageSize() + ", " + localObject.getPackageUnit());
		}
		Ratio r = new Ratio();
		r.setDenominator(quantity);
		r.setNumerator(quantity);
		return r;

	}

}
