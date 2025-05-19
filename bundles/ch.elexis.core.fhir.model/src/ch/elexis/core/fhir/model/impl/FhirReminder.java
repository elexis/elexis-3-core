package ch.elexis.core.fhir.model.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.hl7.fhir.r4.model.Task.TaskPriority;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;

public class FhirReminder extends AbstractFhirModelAdapter<Task> implements IReminder {

	private IContact creator;

	private IContact contact;

	public FhirReminder(Task fhirResource) {
		super(fhirResource);
	}

	@Override
	public IContact getCreator() {
		if (creator == null) {
			if (getFhirResource().hasRequester() && Practitioner.class.getSimpleName()
					.equals(getFhirResource().getRequester().getReferenceElement().getResourceType())) {
				Optional<IContact> requestorContact = FhirModelServiceHolder.get()
						.load(getFhirResource().getRequester().getReferenceElement().getIdPart(), IContact.class);
				if (requestorContact.isPresent()) {
					creator = requestorContact.get();
				}
			}
		}
		return creator;
	}

	@Override
	public void setCreator(IContact value) {
		if (value != null) {
			if (value.isPatient()) {
				Reference patientReference = new Reference(new IdDt(Patient.class.getSimpleName(), value.getId()));
				getFhirResource().setRequester(patientReference);
				creator = value;
			} else if (value.isMandator()) {
				Reference practitionerReference = new Reference(
						new IdDt(Practitioner.class.getSimpleName(), value.getId()));
				getFhirResource().setRequester(practitionerReference);
				creator = value;
			}
		} else {
			creator = null;
			getFhirResource().setRequester(null);
		}
	}

	@Override
	public IContact getContact() {
		if (contact == null) {
			if (getFhirResource().hasFor()) {
				if (Patient.class.getSimpleName()
						.equals(getFhirResource().getFor().getReferenceElement().getResourceType())) {
					Optional<IPatient> forContact = FhirModelServiceHolder.get()
							.load(getFhirResource().getFor().getReferenceElement().getIdPart(), IPatient.class);
					if (forContact.isPresent()) {
						contact = forContact.get();
					}
				} else if (Practitioner.class.getSimpleName()
						.equals(getFhirResource().getFor().getReferenceElement().getResourceType())) {
					Optional<IContact> forContact = FhirModelServiceHolder.get()
							.load(getFhirResource().getFor().getReferenceElement().getIdPart(), IContact.class);
					if (forContact.isPresent()) {
						contact = forContact.get();
					}
				}
			}
		}
		return contact;
	}

	@Override
	public void setContact(IContact value) {
		if (value != null) {
			if (value.isPatient()) {
				Reference patientReference = new Reference(new IdDt(Patient.class.getSimpleName(), value.getId()));
				getFhirResource().setFor(patientReference);
				contact = value;
			} else if (value.isMandator()) {
				Reference practitionerReference = new Reference(
						new IdDt(Practitioner.class.getSimpleName(), value.getId()));
				getFhirResource().setFor(practitionerReference);
				contact = value;
			}
		} else {
			contact = null;
			getFhirResource().setFor(null);
		}
	}

	@Override
	public LocalDate getDue() {
		if (getFhirResource().hasExecutionPeriod() && getFhirResource().getExecutionPeriod().hasEnd()) {
			return LocalDate.ofInstant(getFhirResource().getExecutionPeriod().getEnd().toInstant(),
					ZoneId.systemDefault());
		}
		return null;
	}

	@Override
	public void setDue(LocalDate value) {
		if (value != null) {
			getFhirResource().setExecutionPeriod(
					new Period().setEnd(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant())));
		} else {
			getFhirResource().setExecutionPeriod(null);
		}
	}

	@Override
	public ProcessStatus getStatus() {
		switch (getFhirResource().getStatus()) {
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

	@Override
	public void setStatus(ProcessStatus value) {
		switch (value) {
		case CLOSED:
			getFhirResource().setStatus(TaskStatus.COMPLETED);
			return;
		case IN_PROGRESS:
			getFhirResource().setStatus(TaskStatus.INPROGRESS);
			return;
		case ON_HOLD:
			getFhirResource().setStatus(TaskStatus.ONHOLD);
			return;
		case OPEN:
		case DUE:
		case OVERDUE:
			getFhirResource().setStatus(TaskStatus.ACCEPTED);
			return;
		}
		getFhirResource().setStatus(TaskStatus.DRAFT);
	}

	@Override
	public Visibility getVisibility() {
		if (getFhirResource().hasCode()) {
			Optional<String> visibilityCode = FhirUtil.getCodeFromCodingList("http://www.elexis.info/task/visibility",
					getFhirResource().getCode().getCoding());
			if (visibilityCode.isPresent()) {
				return Visibility.valueOf(visibilityCode.get());
			}
		}
		return Visibility.ALWAYS;
	}

	@Override
	public void setVisibility(Visibility value) {
		getFhirResource().getCode().setCoding(new ArrayList<>(getFhirResource().getCode().getCoding().stream()
				.filter(c -> !"http://www.elexis.info/task/visibility".equals(c.getSystem())).toList()));
		if(value != null) {
			getFhirResource().getCode().addCoding(
					new Coding("http://www.elexis.info/task/visibility", value.name(), value.getLocaleText()));
		}
	}

	@Override
	public String getSubject() {
		return getFhirResource().getDescription();
	}

	@Override
	public void setSubject(String value) {
		getFhirResource().setDescription(value);
	}

	@Override
	public String getMessage() {
		return getFhirResource().getNote().stream().map(n -> n.getText()).collect(Collectors.joining("\n\n"));
	}

	@Override
	public void setMessage(String value) {
		getFhirResource().setNote(List.of(new Annotation().setText(value)));

	}

	@Override
	public Priority getPriority() {
		switch (getFhirResource().getPriority()) {
		case STAT:
		case ASAP:
		case URGENT:
			return Priority.HIGH;
		case ROUTINE:
			return Priority.MEDIUM;
		default:
			break;
		}
		return Priority.MEDIUM;
	}

	@Override
	public void setPriority(Priority value) {
		getFhirResource().setPriority(getTaskPriority(value));
	}

	private TaskPriority getTaskPriority(Priority value) {
		switch (value) {
		case HIGH:
			return TaskPriority.URGENT;
		case LOW:
		case MEDIUM:
			return TaskPriority.ROUTINE;
		default:
			break;
		}
		return TaskPriority.ROUTINE;
	}

	@Override
	public Type getType() {
		if (getFhirResource().hasCode()) {
			Optional<String> visibilityCode = FhirUtil.getCodeFromCodingList("http://www.elexis.info/task/type",
					getFhirResource().getCode().getCoding());
			if (visibilityCode.isPresent()) {
				return Type.valueOf(visibilityCode.get());
			}
		}
		return Type.COMMON;
	}

	@Override
	public void setType(Type value) {
		getFhirResource().getCode().setCoding(new ArrayList<>(getFhirResource().getCode().getCoding().stream()
				.filter(c -> !"http://www.elexis.info/task/type".equals(c.getSystem())).toList()));
		if (value != null) {
			getFhirResource().getCode()
					.addCoding(new Coding("http://www.elexis.info/task/type", value.name(), value.getLocaleText()));
		}
	}

	@Override
	public boolean isResponsibleAll() {
		if (getFhirResource().hasOwner()) {
			if (CareTeam.class.getSimpleName()
					.equals(getFhirResource().getOwner().getReferenceElement().getResourceType())) {
				return "all".equalsIgnoreCase(getFhirResource().getOwner().getReferenceElement().getIdPart());
			}
		}
		return false;
	}

	@Override
	public void setResponsibleAll(boolean value) {
		if (value) {
			Reference groupReference = new Reference(new IdDt(CareTeam.class.getSimpleName(), "ALL"));
			getFhirResource().setOwner(groupReference);
		} else if (getFhirResource().hasOwner()
				&& getFhirResource().getOwner().getReferenceElement().getIdPart().equalsIgnoreCase("ALL")) {
			getFhirResource().setOwner(null);
		}
	}

	@Override
	public IUserGroup getGroup() {
		if (getFhirResource().hasOwner()) {
			if (CareTeam.class.getSimpleName()
					.equals(getFhirResource().getOwner().getReferenceElement().getResourceType())) {
				if (!"all".equalsIgnoreCase(getFhirResource().getOwner().getReferenceElement().getIdPart())) {
					Optional<IUserGroup> ownerGroup = FhirModelServiceHolder.get()
							.load(getFhirResource().getOwner().getReferenceElement().getIdPart(),
							IUserGroup.class);
					if (ownerGroup.isPresent()) {
						return ownerGroup.get();
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setGroup(IUserGroup value) {
		if (value != null) {
			Reference groupReference = new Reference(new IdDt(CareTeam.class.getSimpleName(), value.getId()));
			getFhirResource().setOwner(groupReference);
		} else {
			getFhirResource().setOwner(null);
		}
	}

	@Override
	public List<IContact> getResponsible() {
		if (getFhirResource().hasOwner()) {
			if(Practitioner.class.getSimpleName().equals(getFhirResource().getOwner().getReferenceElement().getResourceType())) {
				Optional<IContact> ownerContact = FhirModelServiceHolder.get()
						.load(getFhirResource().getOwner().getReferenceElement().getIdPart(), IContact.class);
				if (ownerContact.isPresent()) {
					return Collections.singletonList(ownerContact.get());
				}				
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void addResponsible(IContact responsible) {
		if (responsible.isOrganization()) {
			Reference organizationReference = new Reference(
					new IdDt(Organization.class.getSimpleName(), responsible.getId()));
			getFhirResource().setOwner(organizationReference);
		} else if (responsible.isMandator()) {
			Reference practitionerReference = new Reference(
					new IdDt(Practitioner.class.getSimpleName(), responsible.getId()));
			getFhirResource().setOwner(practitionerReference);
		} else {
			Reference personReference = new Reference(new IdDt(Person.class.getSimpleName(), responsible.getId()));
			getFhirResource().setOwner(personReference);
		}
	}

	@Override
	public void removeResponsible(IContact responsible) {
		getFhirResource().setOwner(null);
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getExtInfo(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Object, Object> getMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<Task> getFhirType() {
		return Task.class;
	}

	@Override
	public Class<?> getModelType() {
		return IReminder.class;
	}
}
