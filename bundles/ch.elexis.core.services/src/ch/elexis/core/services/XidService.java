package ch.elexis.core.services;

import static ch.elexis.core.constants.XidConstants.CH_AHV;
import static ch.elexis.core.constants.XidConstants.CH_AHV_QUALITY;
import static ch.elexis.core.constants.XidConstants.DOMAIN_EAN;
import static ch.elexis.core.constants.XidConstants.DOMAIN_OID;
import static ch.elexis.core.constants.XidConstants.ELEXIS;
import static ch.elexis.core.constants.XidConstants.ELEXIS_QUALITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.XidQuality;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.internal.TransientXid;

import ch.rgw.tools.StringTool;
@Component
public class XidService implements IXidService {
	
	private static Logger logger = LoggerFactory.getLogger(XidService.class);
	
	private HashMap<String, XidDomain> domains;
	private HashMap<String, String> domainMap;
	
	@Reference
	private IConfigService configService;
	
	@Activate
	private void activate(){
		domains = new HashMap<String, XidDomain>();
		domainMap = new HashMap<String, String>();
		loadDomains();
	}
	
	@Override
	public IXidDomain getDomain(String name){
		String dom = domainMap.get(name);
		if (dom != null) {
			name = dom;
		}
		return domains.get(name);
	}
	
	@Override
	public List<IXidDomain> getDomains(){
		return new ArrayList<>(domains.values());
	}
	
	private void loadDomains(){
		String storedDomains = configService.get("LocalXIDDomains", null);
		if (storedDomains == null) {
			domains.put(ELEXIS,
				new XidDomain(ELEXIS, "UUID", ELEXIS_QUALITY, "ch.elexis.data.PersistentObject"));
			domains.put(CH_AHV,
				new XidDomain(CH_AHV, "AHV", CH_AHV_QUALITY, "ch.elexis.data.Person"));
			domains.put(DOMAIN_OID,
				new XidDomain(DOMAIN_OID, "OID",
					XidConstants.ASSIGNMENT_GLOBAL | XidConstants.QUALITY_GUID,
					"ch.elexis.data.PersistentObject"));
			domains.put(DOMAIN_EAN,
				new XidDomain(DOMAIN_EAN, "EAN", XidConstants.ASSIGNMENT_REGIONAL,
					"ch.elexis.data.Kontakt,ch.elexis.data.Person,ch.elexis.data.Organisation"));
			storeDomains();
		} else {
			for (String domainString : storedDomains.split(";")) {
				String[] spl = domainString.split("#");
				if (spl.length < 2) {
					logger.error("Fehler in XID-Domain " + domainString);
				}
				String simpleName = StringTool.leer;
				if (spl.length >= 3) {
					simpleName = spl[2];
				}
				String displayOptions = "Kontakt";
				if (spl.length >= 4) {
					displayOptions = spl[3];
				}
				domains.put(spl[0],
					new XidDomain(spl[0], simpleName, Integer.parseInt(spl[1]), displayOptions));
				domainMap.put(simpleName, spl[0]);
			}
		}
	}
	
	private void storeDomains(){
		StringBuilder sb = new StringBuilder();
		for (String k : domains.keySet()) {
			XidDomain xd = domains.get(k);
			sb.append(k).append("#").append(xd.getQuality()).append("#").append(xd.getSimpleName())
				.append("#").append(xd.getDisplayOptions()).append(";");
		}
		configService.set("LocalXIDDomains", sb.toString());
	}
	
	@Override
	public IXidDomain localRegisterXIDDomain(String domainName, String simpleName, int quality){
		if (domains.containsKey(domainName)) {
			logger.error("XID Domain " + domainName + " bereits registriert");
		} else {
			if (domainName.matches(".*[;#].*")) {
				logger.error("XID Domain " + domainName + " ungültig");
			} else {
				XidDomain created = new XidDomain(domainName, simpleName == null ? StringTool.leer : simpleName,
						quality, "Kontakt");
				domains.put(domainName, created);
				if (simpleName != null) {
					domainMap.put(simpleName, domainName);
				}
				storeDomains();
				return created;
			}
		}
		return null;
	}
	
	@Override
	public IXidDomain localRegisterXIDDomainIfNotExists(String domainName, String simpleName,
		int quality){
		XidDomain existing = domains.get(domainName);
		if (existing == null) {
			return localRegisterXIDDomain(domainName, simpleName, quality);
		}
		return existing;
	}
	
	@Override
	public <T> Optional<T> findObject(String domainName, String domainId, Class<T> clazz){
		String dom = domainMap.get(domainName);
		if (dom != null) {
			domainName = dom;
		}
		
		// the type is unknown here
		INamedQuery<IXid> query =
			CoreModelServiceHolder.get().getNamedQuery(IXid.class, "domain", "domainid");
		List<IXid> xids = query.executeWithParameters(query.getParameterMap(domainName, domainId));
		// filter results, getObject will filter by type
		List<T> ret = xids.parallelStream().map(iXid -> iXid.getObject(clazz))
			.filter(Objects::nonNull).collect(Collectors.toList());
		if (ret.size() == 1) {
			return Optional.of(ret.get(0));
		} else if (ret.size() > 1) {
			throw new IllegalStateException(
				"Found more than one object for [" + domainName + "] [" + domainId + "]");
		}
		return Optional.empty();
	}
	
	@Override
	public String getDomainName(String domainName){
		String dom = domainMap.get(domainName);
		if (dom != null) {
			return dom;
		}
		return domainName;
	}
	
	@Override
	public boolean addXid(Identifiable identifiable, String domain, String id,
		boolean updateIfExists){
		domain = getDomainName(domain);
		IXid existing = getXid(identifiable, domain);
		if (existing != null) {
			if (updateIfExists) {
				IXid xid = existing;
				xid.setDomain(domain);
				xid.setDomainId(id);
				xid.setObject(identifiable);
				CoreModelServiceHolder.get().save(xid);
				return true;
			}
		} else {
			IXid xid = CoreModelServiceHolder.get().create(IXid.class);
			xid.setDomain(domain);
			xid.setDomainId(id);
			xid.setObject(identifiable);
			CoreModelServiceHolder.get().save(xid);
			return true;
		}
		return false;
	}
	
	@Override
	public IXid getXid(Identifiable identifiable, String domain){
		if (ELEXIS.equals(domain)) {
			return new TransientXid(ELEXIS, identifiable.getId(),
				XidQuality.ASSIGNMENT_LOCAL_QUALITY_GUID, identifiable);
		}
		// ignore type here, as Kontakt and subtypes lead to not finding the Xid
		// objectid should be unique
		INamedQuery<IXid> query =
			CoreModelServiceHolder.get().getNamedQuery(IXid.class, "domain", "objectid");
		List<IXid> xids = query.executeWithParameters(
			query.getParameterMap("domain", domain, "objectid", identifiable.getId()));
		if (xids.size() > 0) {
			if (xids.size() > 1) {
				LoggerFactory.getLogger(getClass()).error("XID [" + domain + "] ["
					+ identifiable.getId() + "] on multiple objects, returning first.");
			}
			return xids.get(0);
		}
		return null;
	}
	
	@Override
	public List<IXid> getXids(Identifiable identifiable){
		// ignore type here, as Kontakt and subtypes lead to not finding the Xid
		// objectid should be unique
		INamedQuery<IXid> query =
			CoreModelServiceHolder.get().getNamedQuery(IXid.class, "objectid");
		return query.executeWithParameters(query.getParameterMap("objectid", identifiable.getId()));
	}
}
