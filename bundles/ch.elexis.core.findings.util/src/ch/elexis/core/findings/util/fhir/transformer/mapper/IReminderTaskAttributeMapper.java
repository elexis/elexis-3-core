package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class IReminderTaskAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<IReminder, Task> {

	private IModelService modelService;

	public IReminderTaskAttributeMapper(IModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public void elexisToFhir(IReminder source, Task target, SummaryEnum summaryEnum,
			Set<Include> includes) {
		FhirUtil.setVersionedIdPartLastUpdatedMeta(Task.class, target, source);

		target.setIntent(TaskIntent.UNKNOWN);
		target.addIdentifier(getElexisObjectIdentifier(source));

		target.setStatus(getTaskStatus(source));
		target.getCode().addCoding(getVisibilityCoding(source));

		target.setDescription(source.getSubject());
		if (StringUtils.isNotBlank(source.getMessage())) {
			target.addNote(getMessageNote(source));
		}

		if (source.getDue() != null) {
			target.setExecutionPeriod(getExecutionPeriod(source));
		}

		if (!source.getResponsible().isEmpty()) {
			source.getResponsible().forEach(contact -> {
				if (contact.isOrganization()) {
					Reference organizationReference = new Reference(
							new IdDt(Organization.class.getSimpleName(), contact.getId()));
					target.setOwner(organizationReference);
				} else if (contact.isMandator()) {
					Reference practitionerReference = new Reference(
							new IdDt(Practitioner.class.getSimpleName(), contact.getId()));
					target.setOwner(practitionerReference);
				} else {
					Reference personReference = new Reference(new IdDt(Person.class.getSimpleName(), contact.getId()));
					target.setOwner(personReference);
				}
			});
		} else if (source.getGroup() != null) {
			IUserGroup userGroup = source.getGroup();
			Reference groupReference = new Reference(new IdDt(CareTeam.class.getSimpleName(), userGroup.getId()));
			target.setOwner(groupReference);
		} else if (source.isResponsibleAll()) {
			Reference groupReference = new Reference(new IdDt(CareTeam.class.getSimpleName(), "ALL"));
			target.setOwner(groupReference);
		}

		if (source.getContact() != null) {
			if (source.getContact().isPatient()) {
				Reference patientReference = new Reference(
						new IdDt(Patient.class.getSimpleName(), source.getContact().getId()));
				target.setFor(patientReference);
			} else if (source.getContact().isMandator()) {
				Reference practitionerReference = new Reference(
						new IdDt(Practitioner.class.getSimpleName(), source.getContact().getId()));
				target.setFor(practitionerReference);
			}
		}
	}

	@Override
	public void fhirToElexis(Task source, IReminder target) {

		target.setStatus(getProcessStatus(source));
		target.setVisibility(getVisibility(source));
		target.setSubject(source.getDescription());
		if (source.hasNote()) {
			target.setMessage(getMessage(source));
		}
		if (source.hasExecutionPeriod() && source.getExecutionPeriod().hasEnd()) {
			target.setDue(
					LocalDate.ofInstant(source.getExecutionPeriod().getEnd().toInstant(), ZoneId.systemDefault()));
		}
		if (source.hasOwner()) {
			if (CareTeam.class.getSimpleName().equals(source.getOwner().getReferenceElement().getResourceType())) {
				if ("all".equalsIgnoreCase(source.getOwner().getReferenceElement().getIdPart())) {
					target.setGroup(null);
					target.getResponsible().forEach(c -> {
						target.removeResponsible(c);
					});
					target.setResponsibleAll(true);
				} else {
					Optional<IUserGroup> ownerGroup = CoreModelServiceHolder.get()
							.load(source.getOwner().getReferenceElement().getIdPart(),
							IUserGroup.class);
					if (ownerGroup.isPresent()) {
						target.setResponsibleAll(false);
						target.getResponsible().forEach(c -> {
							target.removeResponsible(c);
						});
						target.setGroup(ownerGroup.get());
					}
				}
			} else {
				Optional<IContact> ownerContact = CoreModelServiceHolder.get()
						.load(source.getOwner().getReferenceElement().getIdPart(),
						IContact.class);
				if (ownerContact.isPresent()) {
					target.setResponsibleAll(false);
					target.setGroup(null);
					target.getResponsible().forEach(c -> {
						target.removeResponsible(c);
					});
					target.addResponsible(ownerContact.get());
				}
			}
		}
		if (source.hasFor()) {
			Optional<IContact> forContact = CoreModelServiceHolder.get()
					.load(source.getFor().getReferenceElement().getIdPart(),
					IContact.class);
			if (forContact.isPresent()) {
				target.setContact(forContact.get());
			}
		}
	}

	private String getMessage(Task source) {
		return source.getNote().stream().map(n -> n.getText()).collect(Collectors.joining("\n\n"));
	}

	private Visibility getVisibility(Task source) {
		if (source.hasCode()) {
			Optional<String> visibilityCode = FhirUtil.getCodeFromCodingList("http://www.elexis.info/task/visibility",
					source.getCode().getCoding());
			if (visibilityCode.isPresent()) {
				return Visibility.valueOf(visibilityCode.get());
			}
		}
		return Visibility.ALWAYS;
	}

	private TaskStatus getTaskStatus(IReminder source) {
		switch (source.getStatus()) {
		case CLOSED:
			return TaskStatus.COMPLETED;
		case IN_PROGRESS:
			return TaskStatus.INPROGRESS;
		case ON_HOLD:
			return TaskStatus.ONHOLD;
		case OPEN:
		case DUE:
		case OVERDUE:
			return TaskStatus.ACCEPTED;
		default:
			return TaskStatus.DRAFT;
		}
	}

	private ProcessStatus getProcessStatus(Task source) {
		switch (source.getStatus()) {
		case DRAFT:
		case ACCEPTED:
		case REQUESTED:
		case RECEIVED:
		case READY:
			return ProcessStatus.OPEN;
		case COMPLETED:
		case CANCELLED:
		case FAILED:
		case ENTEREDINERROR:
		case REJECTED:
			return ProcessStatus.CLOSED;
		case INPROGRESS:
			return ProcessStatus.IN_PROGRESS;
		case ONHOLD:
			return ProcessStatus.ON_HOLD;
		default:
			break;
		}
		return ProcessStatus.OPEN;
	}

	private Coding getVisibilityCoding(IReminder source) {
		return new Coding("http://www.elexis.info/task/visibility", source.getVisibility().name(),
				source.getVisibility().getLocaleText());
	}

	private Annotation getMessageNote(IReminder source) {
		Annotation ret = new Annotation();
		ret.setText(source.getMessage());
		return ret;
	}

	private Period getExecutionPeriod(IReminder source) {
		Period ret = new Period();
		ret.setEnd(Date.from(source.getDue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
		return ret;
	}
}
