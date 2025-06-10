package ch.elexis.core.findings.util.fhir.transformer;


import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.hl7.fhir.r4.model.Task;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IReminderTaskAttributeMapper;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.IModelService;

@Component
public class TaskReminderTransformer implements IFhirTransformer<Task, IReminder> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService coreModelService;

	private IReminderTaskAttributeMapper attributeMapper;

	private Cache<String, Task> fhirObjectCache;

	@Activate
	private void activate() {
		attributeMapper = new IReminderTaskAttributeMapper(coreModelService);

		fhirObjectCache = CacheBuilder.newBuilder().maximumSize(1000).build();
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
		Task ret = null;
		try {
			ret = fhirObjectCache.get(getCacheKey(localObject),
					new Callable<Task>() {
						@Override
						public Task call() throws Exception {
							Task ret = new Task();
							attributeMapper.elexisToFhir(localObject, ret, summaryEnum, includes);
							return ret;
						}
					});
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("Error transform to FHIR", e);
			return Optional.empty();
		}
		return Optional.of(ret);
	}

	private String getCacheKey(IReminder localObject) {
		return localObject.getId() + "|" + localObject.getLastupdate();
	}

	@Override
	public Optional<IReminder> updateLocalObject(Task fhirObject, IReminder localObject) {
		fhirObjectCache.invalidate(getCacheKey(localObject));
		attributeMapper.fhirToElexis(fhirObject, localObject);
		coreModelService.save(localObject);
		return Optional.of(localObject);
	}
}
