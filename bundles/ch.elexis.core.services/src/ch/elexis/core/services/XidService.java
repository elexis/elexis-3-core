package ch.elexis.core.services;

import static ch.elexis.core.constants.XidConstants.ASSIGNMENT_REGIONAL;
import static ch.elexis.core.constants.XidConstants.CH_AHV;
import static ch.elexis.core.constants.XidConstants.CH_AHV_QUALITY;
import static ch.elexis.core.constants.XidConstants.DOMAIN_BSVNUM;
import static ch.elexis.core.constants.XidConstants.DOMAIN_EAN;
import static ch.elexis.core.constants.XidConstants.DOMAIN_KSK;
import static ch.elexis.core.constants.XidConstants.DOMAIN_NIF;
import static ch.elexis.core.constants.XidConstants.DOMAIN_OID;
import static ch.elexis.core.constants.XidConstants.DOMAIN_RECIPIENT_EAN;
import static ch.elexis.core.constants.XidConstants.ELEXIS;
import static ch.elexis.core.constants.XidConstants.ELEXIS_QUALITY;
import static ch.elexis.core.constants.XidConstants.QUALITY_GUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.XidQuality;
import ch.elexis.core.services.internal.TransientXid;

@Component
public class XidService implements IXidService {

	private Logger logger = LoggerFactory.getLogger(XidService.class);

	private Map<String, XidDomain> domains;
	private Map<String, String> domainMap;

	public XidService() {
		domains = new HashMap<>();
		domainMap = new HashMap<>();
	}

	@Reference
	private IConfigService configService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IAccessControlService accessControlService;

	@Activate
	private void activate() {
		accessControlService.doPrivileged(() -> {
			loadDomains();
		});
	}

	@Override
	public IXidDomain getDomain(String name) {
		String dom = domainMap.get(name);
		if (dom != null) {
			name = dom;
		}
		return domains.get(name);
	}

	@Override
	public List<IXidDomain> getDomains() {
		return new ArrayList<>(domains.values());
	}

	private void loadDomains() {
		String storedDomains = configService.get("LocalXIDDomains", null);
		if (storedDomains != null) {
			loadDomainsSplitString(storedDomains);
		}

		localRegisterXIDDomainIfNotExists(ELEXIS, "UUID", ELEXIS_QUALITY, "ch.elexis.data.PersistentObject");
		localRegisterXIDDomainIfNotExists(CH_AHV, "AHV", CH_AHV_QUALITY, "ch.elexis.data.Person");
		localRegisterXIDDomainIfNotExists(DOMAIN_OID, "OID", XidConstants.ASSIGNMENT_GLOBAL | QUALITY_GUID,
				"ch.elexis.data.PersistentObject");
		localRegisterXIDDomainIfNotExists(DOMAIN_EAN, "EAN", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Kontakt,ch.elexis.data.Person,ch.elexis.data.Organisation");
		localRegisterXIDDomainIfNotExists(DOMAIN_BSVNUM, "BSV-Nummer", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Organisation");
		localRegisterXIDDomainIfNotExists(DOMAIN_RECIPIENT_EAN, "rEAN", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Kontakt,ch.elexis.data.Person,ch.elexis.data.Organisation");
		localRegisterXIDDomainIfNotExists(DOMAIN_KSK, "KSK/ZSR-Nr", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Kontakt,ch.elexis.data.Person,ch.elexis.data.Organisation");
		localRegisterXIDDomainIfNotExists(DOMAIN_NIF, "IV/NIF-Nr", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Kontakt,ch.elexis.data.Person,ch.elexis.data.Organisation");

		localRegisterXIDDomainIfNotExists(XidConstants.XID_KONTAKT_ANREDE, "Anrede", ASSIGNMENT_REGIONAL);
		localRegisterXIDDomainIfNotExists(XidConstants.XID_KONTAKT_KANTON, "Kanton", ASSIGNMENT_REGIONAL);
		localRegisterXIDDomainIfNotExists(XidConstants.XID_KONTAKT_SPEZ, "Spezialität", ASSIGNMENT_REGIONAL);
		localRegisterXIDDomainIfNotExists(XidConstants.XID_KONTAKT_ROLLE, "Rolle", ASSIGNMENT_REGIONAL);
		localRegisterXIDDomainIfNotExists(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, "Sendende Institution",
				ASSIGNMENT_REGIONAL);
	}

	private void loadDomainsSplitString(String storedDomains) {
		for (String domainString : storedDomains.split(";")) {
			String[] spl = domainString.split("#");
			if (spl.length < 2) {
				logger.error("Fehler in XID-Domain " + domainString);
			}
			String simpleName = StringUtils.EMPTY;
			if (spl.length >= 3) {
				simpleName = spl[2];
			}
			String displayOptions = "Kontakt";
			if (spl.length >= 4) {
				displayOptions = spl[3];
			}
			domains.put(spl[0], new XidDomain(spl[0], simpleName, Integer.parseInt(spl[1]), displayOptions));
			domainMap.put(simpleName, spl[0]);
		}
	}

	private void storeDomains() {
		accessControlService.doPrivileged(() -> {
			StringBuilder sb = new StringBuilder();
			for (String k : domains.keySet()) {
				XidDomain xd = domains.get(k);
				sb.append(k).append("#").append(xd.getQuality()).append("#").append(xd.getSimpleName()).append("#")
						.append(xd.getDisplayOptions()).append(";");
			}
			configService.set("LocalXIDDomains", sb.toString());
		});
	}

	@Override
	public IXidDomain localRegisterXIDDomain(String domainName, String simpleName, int quality) {
		return localRegisterXIDDomain(domainName, simpleName, quality, "");
	}

	private IXidDomain localRegisterXIDDomain(String domainName, String simpleName, int quality,
			String displayOptions) {
		if (domains.containsKey(domainName)) {
			logger.error("XID Domain " + domainName + " bereits registriert");
		} else {
			if (domainName.matches(".*[;#].*")) {
				logger.error("XID Domain " + domainName + " ungültig");
			} else {
				XidDomain created = new XidDomain(domainName, simpleName == null ? StringUtils.EMPTY : simpleName,
						quality, displayOptions);
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
	public IXidDomain localRegisterXIDDomainIfNotExists(String domainName, String simpleName, int quality) {
		return localRegisterXIDDomainIfNotExists(domainName, simpleName, quality, "");
	}

	private IXidDomain localRegisterXIDDomainIfNotExists(String domainName, String simpleName, int quality,
			String displayOptions) {
		XidDomain existing = domains.get(domainName);
		if (existing == null) {
			return localRegisterXIDDomain(domainName, simpleName, quality, displayOptions);
		}
		return existing;
	}

	@Override
	public <T> Optional<T> findObject(String domainName, String domainId, Class<T> clazz) {
		List<T> ret = findObjects(domainName, domainId, clazz);
		if (ret.size() == 1) {
			return Optional.of(ret.get(0));
		} else if (ret.size() > 1) {
			throw new IllegalStateException("Found more than one object for [" + domainName + "] [" + domainId + "]");
		}
		return Optional.empty();
	}

	@Override
	public <T> List<T> findObjects(String domainName, String domainId, Class<T> clazz) {
		String dom = domainMap.get(domainName);
		if (dom != null) {
			domainName = dom;
		}

		List<IXid> xids;
		if (Objects.equals(CH_AHV, domainName)) {
			INamedQuery<IXid> namedQuery = coreModelService.getNamedQuery(IXid.class, "ahvdomainid");
			xids = namedQuery.executeWithParameters(namedQuery.getParameterMap("ahvdomainid", domainId));
		} else {
			// the type is unknown here
			INamedQuery<IXid> query = coreModelService.getNamedQuery(IXid.class, "domain", "domainid");
			xids = query.executeWithParameters(query.getParameterMap("domain", domainName, "domainid", domainId));
		}

		// filter results, getObject will filter by type
		List<T> ret = xids.parallelStream().map(iXid -> iXid.getObject(clazz)).filter(Objects::nonNull)
				.collect(Collectors.toList());
		return ret;
	}

	@Override
	public String getDomainName(String domainName) {
		String dom = domainMap.get(domainName);
		if (dom != null) {
			return dom;
		}
		return domainName;
	}

	@Override
	public boolean addXid(Identifiable identifiable, String domain, String id, boolean updateIfExists) {
		domain = getDomainName(domain);
		IXid existing = getXid(identifiable, domain);
		if (existing != null) {
			if (updateIfExists) {
				IXid xid = existing;
				xid.setDomain(domain);
				xid.setDomainId(id);
				xid.setObject(identifiable);
				coreModelService.save(xid);
				return true;
			}
		} else {
			IXid xid = coreModelService.create(IXid.class);
			xid.setDomain(domain);
			xid.setDomainId(id);
			xid.setObject(identifiable);

			XidDomain xidDomain = domains.get(domain);
			if (xidDomain == null) {
				logger.info("XID Domain " + domain + " not found reload domains");
				loadDomains();
				xidDomain = domains.get(domain);
			}
			if (xidDomain != null) {
				int val = xidDomain.getQuality();
				if (val > 9) {
					val = (val & 7) + 4;
				}
				xid.setQuality(XidQuality.ofValue(val));
			} else {
				logger.error("XID Domain " + domain + " is not registered");
				return false;
			}

			coreModelService.save(xid);
			return true;
		}
		return false;
	}

	@Override
	public IXid getXid(Identifiable identifiable, String domain) {
		if (ELEXIS.equals(domain)) {
			return new TransientXid(ELEXIS, identifiable.getId(), XidQuality.ASSIGNMENT_LOCAL_QUALITY_GUID,
					identifiable);
		}
		// ignore type here, as Kontakt and subtypes lead to not finding the Xid
		// objectid should be unique
		INamedQuery<IXid> query = coreModelService.getNamedQuery(IXid.class, "domain", "objectid");
		List<IXid> xids = query
				.executeWithParameters(query.getParameterMap("domain", domain, "objectid", identifiable.getId()));
		if (!xids.isEmpty()) {
			if (xids.size() > 1) {
				LoggerFactory.getLogger(getClass()).error(
						"XID [" + domain + "] [" + identifiable.getId() + "] on multiple objects, returning first.");
			}
			return xids.get(0);
		}
		return null;
	}

	@Override
	public List<IXid> getXids(Identifiable identifiable) {
		// ignore type here, as Kontakt and subtypes lead to not finding the Xid
		// objectid should be unique
		INamedQuery<IXid> query = coreModelService.getNamedQuery(IXid.class, "objectid");
		return query.executeWithParameters(query.getParameterMap("objectid", identifiable.getId()));
	}
}
