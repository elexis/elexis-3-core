package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.fhir.FhirChConstants;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.TimeTool;

public class ICoverageHelper extends AbstractHelper {

	public String getDependent(ICoverage coverage) {
		BillingLaw law = coverage.getBillingSystem().getLaw();
		if (BillingLaw.UVG == law) {
			return (String) coverage.getExtInfo(FallConstants.UVG_UNFALLNUMMER);
		}
		if (BillingLaw.IV == law) {
			return (String) coverage.getExtInfo(FallConstants.IV_FALLNUMMER);
		}
		return null;
	}

	public void setDependent(ICoverage coverage, String bin) {
		String billingSystem = coverage.getBillingSystem().getName();
		if (billingSystem != null && !billingSystem.isEmpty()) {
			if (billingSystem.equals("UVG")) {
				coverage.setExtInfo(FallConstants.UVG_UNFALLNUMMER, bin);
			} else {
				coverage.setExtInfo(FallConstants.IV_FALLNUMMER, bin);
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

	public Reference getPolicyHolderReference(ICoverage coverage) {
		IContact costBearer = coverage.getGuarantor();
		if (costBearer != null) {
			String contactType = costBearer.isOrganization() ? "Organization" : "Patient";
			return new Reference(new IdDt(contactType, costBearer.getId()));
		}
		return null;
	}

	public Optional<IContact> getPolicyHolderByReference(IModelService coreModelService, Reference source) {
		if (source != null) {
			Optional<String> localId = FhirUtil.getLocalId(source.getId());
			if (localId.isPresent()) {
				return coreModelService.load(localId.get(), IContact.class);
			}
		}
		return Optional.empty();
	}

	public Reference getPayor(ICoverage fall) {
		IContact kostenTr = fall.getCostBearer();
		if (kostenTr != null) {
			// TODO Person instances are never directly referenced as actors
			String contactType = kostenTr.isOrganization() ? "Organization" : "Patient";
			return new Reference(new IdDt(contactType, kostenTr.getId()));
		}
		return null;
	}

	public Optional<IContact> getPayorByReference(IModelService coreModelService, Reference payorFirstRep) {
		if (payorFirstRep != null) {
			Optional<String> localId = FhirUtil.getLocalId(payorFirstRep.getId());
			if (localId.isPresent()) {
				return coreModelService.load(localId.get(), IContact.class);
			}
		}
		return Optional.empty();
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
		if (period.getEnd() != null) {
			coverage.setDateTo(getLocalDateTime(period.getEnd()).toLocalDate());
		} else {
			coverage.setDateTo(null);
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

	public Optional<Coding> getType(ICoverage coverage) {
		String billingSystem = coverage.getBillingSystem().getName();
		if (billingSystem != null) {
			Coding coding = new Coding();
			coding.setSystem(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem());
			coding.setCode(billingSystem);
			return Optional.of(coding);
		}
		return Optional.empty();
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

	public Optional<Coding> getReason(ICoverage coverage) {
		String reason = coverage.getReason();
		if (reason != null) {
			Coding coding = new Coding();
			coding.setSystem(CodingSystem.ELEXIS_COVERAGE_REASON.getSystem());
			coding.setCode(reason);
			return Optional.of(coding);
		}
		return Optional.empty();
	}

	public Optional<Identifier> getInsuranceNumber(ICoverage coverage) {
		String insuranceNumber = coverage.getInsuranceNumber();
		if (insuranceNumber != null) {
			Identifier identifier = new Identifier();
			identifier.setSystem(FhirChConstants.OID_VERSICHERTENNUMMER_SYSTEM);
			identifier.setValue(insuranceNumber);
			return Optional.of(identifier);
		}

		return Optional.empty();
	}

	public Optional<Coding> getAccidentDate(ICoverage localObject) {
		if (Objects.equals(BillingLaw.UVG, localObject.getBillingSystem().getLaw())) {
			String accidentDate = (String) localObject.getExtInfo(FallConstants.UVG_UNFALLDATUM);
			if (accidentDate != null) {
				TimeTool timeTool = new TimeTool(accidentDate);
				Coding coding = new Coding();
				coding.setSystem(CodingSystem.ELEXIS_COVERAGE_UVG_ACCIDENTDATE.getSystem());
				coding.setCode(timeTool.toString(TimeTool.DATETIME_XML));
				return Optional.of(coding);
			}
		}
		return Optional.empty();
	}

	public void setInsuranceNumber(Coverage source, ICoverage target) {
		Optional<Identifier> insuranceNumberIdentifier = source.getIdentifier().stream()
				.filter(id -> FhirChConstants.OID_VERSICHERTENNUMMER_SYSTEM.equals(id.getSystem())).findFirst();
		if (insuranceNumberIdentifier.isPresent()) {
			target.setInsuranceNumber(insuranceNumberIdentifier.get().getValue());

			// compatibility - FallDetailBlatt etc. expect value to be contained in ExtInfo
			target.setExtInfo(FallConstants.FLD_EXT_VERSICHERUNGSNUMMER, insuranceNumberIdentifier.get().getValue());
		}
	}
}
