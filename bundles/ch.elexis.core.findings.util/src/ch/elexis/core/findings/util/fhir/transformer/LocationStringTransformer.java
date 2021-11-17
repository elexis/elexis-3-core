package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;

@Component
public class LocationStringTransformer implements IFhirTransformer<Location, String> {
	
	// the only location currently accepted
	public static final String MAIN_LOCATION = "mainLocation";
	
	@Override
	public Optional<Location> getFhirObject(String localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		if (Objects.equals(MAIN_LOCATION, localObject)) {
			return getMainLocationOptional();
		}
		return Optional.empty();
	}
	
	private Optional<Location> getMainLocationOptional(){
		Location mainLocation = new Location();
		
		mainLocation.setId(new IdType(Location.class.getSimpleName(), MAIN_LOCATION));
		
		return Optional.of(mainLocation);
	}
	
	@Override
	public Optional<String> getLocalObject(Location fhirObject){
		if (Objects.equals(fhirObject.getId(), MAIN_LOCATION)) {
			return Optional.of(MAIN_LOCATION);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<String> updateLocalObject(Location fhirObject, String localObject){
		return Optional.empty();
	}
	
	@Override
	public Optional<String> createLocalObject(Location fhirObject){
		return Optional.empty();
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Location.class.equals(fhirClazz) && String.class.equals(localClazz);
	}
	
}
