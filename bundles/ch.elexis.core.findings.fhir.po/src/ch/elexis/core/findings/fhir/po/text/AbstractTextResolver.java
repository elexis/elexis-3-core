package ch.elexis.core.findings.fhir.po.text;

import java.util.Optional;

import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.data.interfaces.text.ITextResolver;
import ch.elexis.core.findings.fhir.po.dataaccess.FindingsDataAccessor;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public abstract class AbstractTextResolver implements ITextResolver {
	
	private static FindingsDataAccessor ACCESSOR;
	
	public static synchronized IDataAccess getAccessor(){
		if (ACCESSOR == null) {
			ACCESSOR = new FindingsDataAccessor();
		}
		return ACCESSOR;
	}
	
	protected Optional<String> getFindingsText(Object object, String dataAccessDescriptor){
		Result<Object> result = null;
		if (object instanceof PersistentObject) {
			result = getAccessor().getObject(dataAccessDescriptor, (PersistentObject) object, null,
				null);
		} else {
			result = getAccessor().getObject(dataAccessDescriptor, null, null, null);
		}
		if (result.isOK()) {
			return Optional.ofNullable((String) result.get());
		}
		return Optional.empty();
	}
}
