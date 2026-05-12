package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Task;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IReminderTaskAttributeMapper;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.IModelService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@Component
public class TaskReminderTransformer implements IFhirTransformer<Task, IReminder> {

	@Inject
	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	IModelService coreModelService;

	IReminderTaskAttributeMapper attributeMapper;

	@PostConstruct
	@Activate
	private void activate() {
		attributeMapper = new IReminderTaskAttributeMapper(coreModelService);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Task.class.equals(fhirClazz) && IReminder.class.equals(localClazz);
	}

	@Override
	public Optional<IReminder> getLocalObject(Task fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IReminder.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IReminder> createLocalObject(Task fhirObject) {
		if (!fhirObject.hasStatus()) {
			LoggerFactory.getLogger(getClass()).warn("Create Task failed, has no status set.");
			return Optional.empty();
		}
		IReminder create = coreModelService.create(IReminder.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		coreModelService.save(create);
		return Optional.of(create);
	}

	@Override
	public Optional<Task> getFhirObject(IReminder localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Task task = new Task();
		attributeMapper.elexisToFhir(localObject, task, summaryEnum, includes);
		return Optional.of(task);
	}

	@Override
	public Optional<IReminder> updateLocalObject(Task fhirObject, IReminder localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		coreModelService.save(localObject);
		return Optional.of(localObject);
	}
}
