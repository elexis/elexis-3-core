package ch.elexis.core.spotlight.ui.controls.detail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.utils.CoreUtil;
import ch.rgw.tools.Money;

public class PatientDetailCompositeUtil {

	private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EE, dd.MM.yy HH:MM");
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	String getAppointmentLabel(IAppointment appointment) {
		if (appointment == null) {
			return "-";
		}
		return dayFormatter.format(appointment.getStartTime()) + " - " + appointment.getReason() + " bei "
				+ appointment.getSchedule();
	}

	void clearComposite(Composite composite, Control... exclude) {
		Control[] children = composite.getChildren();
		for (Control control : children) {
			if (exclude == null) {
				control.dispose();
			} else {
				boolean excluded = false;
				for (int i = 0; i < exclude.length; i++) {
					if (exclude[i].equals(control)) {
						excluded = true;
						break;
					}
				}
				if (!excluded) {
					control.dispose();
				}
			}
		}
		composite.layout();
	}

	public String formatDate(LocalDate date) {
		return dateFormatter.format(date);
	}

	String getFormattedFamilyDoctor(IPatient patient) {
		if (patient != null) {
			IContact familyDoctor = patient.getFamilyDoctor();
			if (familyDoctor != null) {
				String label = familyDoctor.getDescription1() + ", " + familyDoctor.getDescription2();
				if (StringUtils.isNotEmpty(familyDoctor.getCode())) {
					label += " (" + familyDoctor.getCode() + ")";
				}
				return label;
			}
		}
		return "-";
	}

	String getFormattedInsurance(IModelService coreModelService, IPatient patient) {
		if (patient != null) {
			IQuery<ICoverage> firstKvgQuery = coreModelService.getQuery(ICoverage.class);
			firstKvgQuery.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
			firstKvgQuery.and("gesetz", COMPARATOR.EQUALS, "KVG");
			firstKvgQuery.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
			firstKvgQuery.limit(1);
			ICoverage firstKvg = firstKvgQuery.executeSingleResult().orElse(null);
			IContact guarantor = firstKvg != null ? firstKvg.getGuarantor() : null;
			if (guarantor != null) {
				return guarantor.getLabel();
			}
		}
		return "-";
	}

	String getFormattedPatientBalance(IModelService coreModelService, IPatient patient) {
		if (patient != null) {
			List<Number> balanceResult = Collections.emptyList();
			if (!"PostgreSQL".equalsIgnoreCase(CoreUtil.getDatabaseProductName())) {
				INamedQuery<Number> namedQuery = coreModelService.getNamedQuery(Number.class, IAccountTransaction.class,
						true, "balance.patient");
				balanceResult = namedQuery.executeWithParameters(namedQuery.getParameterMap("patient", patient));
			}
			if (!balanceResult.isEmpty()) {
				int _balance = balanceResult.get(0).intValue();
				return "CHF " + new Money(_balance);
			}
		}
		return "-";
	}

	String getFormattedFixedMedication(IModelService coreModelService, IPatient patient) {
		if (patient != null) {
			List<IPrescription> fixedMedication = patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
			if (!fixedMedication.isEmpty()) {
				return fixedMedication.stream().map(m -> m.getLabel()).reduce((u, t) -> u + "\r\n" + t).get();
			}
		}
		return "-";
	}

	IAppointment getNextAppointment(IModelService coreModelService, IPatient patient) {
		if (patient != null) {
			IQuery<IAppointment> futureAppointmentsQuery = coreModelService.getQuery(IAppointment.class);
			futureAppointmentsQuery.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT, COMPARATOR.EQUALS,
					patient.getId());
			futureAppointmentsQuery.and("tag", COMPARATOR.GREATER, LocalDate.now());
			futureAppointmentsQuery.orderBy("tag", ORDER.ASC);
			futureAppointmentsQuery.limit(1);
			return futureAppointmentsQuery.executeSingleResult().orElse(null);
		}
		return null;
	}

	IAppointment getPreviousAppointment(IModelService coreModelService, IPatient patient) {
		if (patient != null) {
			IQuery<IAppointment> lastAppointmentQuery = coreModelService.getQuery(IAppointment.class);
			lastAppointmentQuery.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT, COMPARATOR.EQUALS,
					patient.getId());
			lastAppointmentQuery.and("tag", COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			lastAppointmentQuery.orderBy("tag", ORDER.DESC);
			lastAppointmentQuery.limit(1);
			return lastAppointmentQuery.executeSingleResult().orElse(null);
		}
		return null;
	}

	String getFormattedLatestLaboratoryDate(IModelService coreModelService, IPatient patient) {
		if (patient != null) {
			IQuery<ILabResult> labResultQuery = coreModelService.getQuery(ILabResult.class);
			labResultQuery.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS, patient);
			labResultQuery.orderBy(ModelPackage.Literals.ILAB_RESULT__DATE, ORDER.DESC);
			labResultQuery.limit(1);
			ILabResult latestResult = labResultQuery.executeSingleResult().orElse(null);
			if (latestResult != null) {
				return formatDate(latestResult.getDate());
			}
		}
		return "-";
	}

}
