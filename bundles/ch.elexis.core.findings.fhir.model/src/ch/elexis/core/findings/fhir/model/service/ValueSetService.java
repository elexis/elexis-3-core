package ch.elexis.core.findings.fhir.model.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.IValueSetContribution;
import ch.elexis.core.findings.codes.IValueSetService;

@Component
public class ValueSetService implements IValueSetService {
	
	private List<IValueSetContribution> contributions;
	
	private Map<String, IValueSetContribution> idContributionMap;
	
	private Map<String, IValueSetContribution> nameContributionMap;
	
	private Logger getLogger(){
		return LoggerFactory.getLogger(ValueSetService.class);
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindContribution(IValueSetContribution contribution){
		if (contribution.getValueSetIds() != null && !contribution.getValueSetIds().isEmpty()) {
			addContribution(contribution);
		} else {
			getLogger().warn(
				"Contribution " + contribution + " returns no value set ids. It will be ignored.");
		}
	}
	
	private void addContribution(IValueSetContribution contribution){
		if (contributions == null) {
			contributions = new ArrayList<>();
		}
		if (idContributionMap == null) {
			idContributionMap = new HashMap<>();
		}
		if (nameContributionMap == null) {
			nameContributionMap = new HashMap<>();
		}
		contributions.add(contribution);
		List<String> ids = contribution.getValueSetIds();
		for (String id : ids) {
			if (idContributionMap.put(id, contribution) != null) {
				getLogger()
					.warn("Id " + id + " provided by multiple contributions " + contribution);
			}
		}
		List<String> names = contribution.getValueSetNames();
		for (String name : names) {
			if (nameContributionMap.put(name, contribution) != null) {
				getLogger()
					.warn("Name " + name + " provided by multiple contributions " + contribution);
			}
		}
	}
	
	public void unbindContribution(IValueSetContribution contribution){
		if (contributions == null) {
			contributions = new ArrayList<>();
		}
		removeContribution(contribution);
	}
	
	private void removeContribution(IValueSetContribution contribution){
		if (contributions == null) {
			contributions = new ArrayList<>();
		}
		if (idContributionMap == null) {
			idContributionMap = new HashMap<>();
		}
		if (nameContributionMap == null) {
			nameContributionMap = new HashMap<>();
		}
		contributions.remove(contribution);
		
		List<String> ids = contribution.getValueSetIds();
		for (String id : ids) {
			idContributionMap.remove(id);
		}
		List<String> names = contribution.getValueSetNames();
		for (String name : names) {
			nameContributionMap.remove(name);
		}
	}
	
	@Override
	public List<ICoding> getValueSet(String id){
		if (idContributionMap != null && idContributionMap.get(id) != null) {
			return idContributionMap.get(id).getValueSet(id);
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<ICoding> getValueSetByName(String name){
		if (nameContributionMap != null && nameContributionMap.get(name) != null) {
			return nameContributionMap.get(name).getValueSetByName(name);
		}
		return Collections.emptyList();
	}
}
