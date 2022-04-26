package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;

public class ICoverageHelper extends AbstractHelper {

	public String getDependent(ICoverage coverage) {
		String ret = coverage.getInsuranceNumber();
		if (ret == null) {
			ret = (String) coverage.getExtInfo("Versicherungsnummer");
		}
		return ret;
	}

	public void setBin(ICoverage coverage, String bin) {
		String billingSystem = coverage.getBillingSystem().getName();
		if (billingSystem != null && !billingSystem.isEmpty()) {
			if (billingSystem.equals("UVG")) {
				coverage.setExtInfo("Unfallnummer", bin);
			} else {
				coverage.setExtInfo("Versicherungsnummer", bin);
			}
		}
	}

	public Reference getBeneficiaryReference(ICoverage fall) {
		IPatient patient = fall.getPatient();
		if (patient != null) {
			return new Reference(new IdDt("Patient", patient.getId()));
		}
		return null;
	}

	public Reference getIssuerReference(ICoverage fall) {
		IContact kostenTr = fall.getCostBearer();
		if (kostenTr != null) {
			if (kostenTr.isOrganization()) {
				return new Reference(new IdDt("Organization", kostenTr.getId()));
			} else if (kostenTr.isPatient()) {
				return new Reference(new IdDt("Patient", kostenTr.getId()));
			}
		}
		return null;
	}

	public Period getPeriod(ICoverage coverage) {
		Period period = new Period();
		LocalDate startDate = coverage.getDateFrom();
		if (startDate != null) {
			period.setStart(getDate(startDate.atStartOfDay()));
		}
		LocalDate endDate = coverage.getDateTo();
		if (endDate != null) {
			period.setEnd(getDate(endDate.atStartOfDay()));
		}
		return period;
	}

	public void setPeriod(ICoverage coverage, Period period) {
		if (period.getStart() != null) {
			coverage.setDateFrom(getLocalDateTime(period.getStart()).toLocalDate());
		}
	}

	public String getFallText(ICoverage coverage) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$
		String grund = coverage.getReason();
		String bezeichnung = coverage.getDescription();
		LocalDate dateFrom = coverage.getDateFrom();
		LocalDate dateTo = coverage.getDateTo();
		String billingSystem = coverage.getBillingSystem().getName();

		if (dateFrom == null) {
			dateFrom = LocalDate.of(1970, 1, 1);
		}
		StringBuilder ret = new StringBuilder();
		if (dateTo != null) {
			ret.append("-GESCHLOSSEN-");
		}
		ret.append(billingSystem).append(": ").append(grund).append(" - "); //$NON-NLS-1$ //$NON-NLS-2$
		ret.append(bezeichnung).append("("); //$NON-NLS-1$
		String ed;
		if (dateTo == null) {
			ed = "offen";
		} else {
			ed = dateTo.format(dateFormat);
		}
		ret.append(dateFrom.format(dateFormat)).append("-").append(ed).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return ret.toString();
	}

	public Optional<CodeableConcept> getType(ICoverage coverage) {
		CodeableConcept ret = new CodeableConcept();
		String billingSystem = coverage.getBillingSystem().getName();
		if (billingSystem != null) {
			Coding coding = new Coding();
			coding.setSystem(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem());
			coding.setCode(billingSystem);
			ret.addCoding(coding);
		}
		return Optional.of(ret);
	}

	public Optional<String> getType(Coverage fhirObject) {
		CodeableConcept fhirType = fhirObject.getType();
		for (Coding coding : fhirType.getCoding()) {
			if (coding.getSystem().equals(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem())) {
				return Optional.ofNullable(coding.getCode());
			}
		}
		return Optional.empty();
	}
}
