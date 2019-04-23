package ch.elexis.core.application;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IElexisDataSource;

@Component
public class ElexisDatasourceHolder {
	
	private static IElexisDataSource datasource;
	
	@Reference
	public void setElexisDataSource(IElexisDataSource datasource){
		ElexisDatasourceHolder.datasource = datasource;
	}
	
	public static Optional<IElexisDataSource> get(){
		return Optional.ofNullable(datasource);
	}
}
