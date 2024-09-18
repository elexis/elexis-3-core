package ch.elexis.core.ui.dbcheck.contributions;

import static ch.elexis.core.constants.XidConstants.DOMAIN_EAN;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IXid;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.services.holder.XidServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;

public class XidsFromExtInfo extends ExternalMaintenance {

	private ISticker insuranceSticker;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		insuranceSticker = CoreModelServiceHolder.get().load("managedinsurance", ISticker.class).orElse(null);

		StringBuilder sb = new StringBuilder();
		int withExtInfoEan = 0;
		int createdEanXid = 0;
		int alreadyEanXid = 0;
		int conflictEanXid = 0;

		IQuery<IOrganization> organizationQuery = CoreModelServiceHolder.get().getQuery(IOrganization.class);
		try (IQueryCursor<IOrganization> allOrganizations = organizationQuery.executeAsCursor()) {
			StringBuilder conflictSb = new StringBuilder();
			while (allOrganizations.hasNext()) {
				IOrganization organization = allOrganizations.next();

				Object extInfoEan = organization.getExtInfo("EAN");
				if (extInfoEan instanceof String && StringUtils.isNotBlank((String) extInfoEan)) {
					withExtInfoEan++;

					List<IOrganization> found = XidServiceHolder.get().findObjects(XidConstants.EAN,
							(String) extInfoEan, IOrganization.class);
					if (found.isEmpty()) {
						organization.addXid(XidConstants.EAN, (String) extInfoEan, false);
						createdEanXid++;
					} else {
						if (found.size() == 1) {
							if (found.get(0).equals(organization)) {
								alreadyEanXid++;
							} else {
								conflictEanXid++;
								conflictSb.append(
										"ExtInfo EAN [" + (String) extInfoEan + "] of org id [" + organization.getId()
												+ "] found in xid of org id [" + found.get(0).getId() + "]\n");
							}
						} else {
							conflictEanXid++;
							conflictSb.append("ExtInfo EAN [" + (String) extInfoEan + "] of org id ["
									+ organization.getId() + "] found in xid of multi org ids ["
									+ found.stream().map(o -> o.getId()).collect(Collectors.joining(",")) + "]\n");
						}
					}
				}
			}
			sb.append("Organization EAN:\n");
			sb.append("found EAN ExtInfo " + withExtInfoEan + "\n");
			sb.append("created EAN XID " + createdEanXid + "\n");
			sb.append("already EAN XID " + alreadyEanXid + "\n");
			sb.append("conflict EAN XID " + conflictEanXid + "\n");
			if(conflictEanXid > 0) {
				sb.append(conflictSb.toString());
			}
		}

		if (insuranceSticker != null) {
			int coverageNoRefOrOrg = 0;
			int coverageNoEanContact = 0;
			int coverageNoManagedContact = 0;
			int coverageManagedContact = 0;
			int coverageChangedToManagedContact = 0;
			IQuery<ICoverage> coverageQuery = CoreModelServiceHolder.get().getQuery(ICoverage.class);
			try (IQueryCursor<ICoverage> allCoverages = coverageQuery.executeAsCursor()) {
				while (allCoverages.hasNext()) {
					ICoverage coverage = allCoverages.next();

					if (coverage.getCostBearer() != null && coverage.getCostBearer().isOrganization()) {
						Optional<String> ean = getEAN(coverage.getCostBearer());
						if (ean.isPresent()) {
							if (!StickerServiceHolder.get().hasSticker(coverage.getCostBearer(), insuranceSticker)) {
								Optional<IOrganization> managedInsurance = getManagedInsuranceWithEan(ean.get());
								if (managedInsurance.isPresent()) {
									coverage.setCostBearer(managedInsurance.get());
									coverageChangedToManagedContact++;
								} else {
									coverageNoManagedContact++;
								}
							} else {
								coverageManagedContact++;
							}
						} else {
							coverageNoEanContact++;
						}
					} else {
						coverageNoRefOrOrg++;
					}
					if (coverage.getGuarantor() != null && coverage.getGuarantor().isOrganization()) {
						Optional<String> ean = getEAN(coverage.getGuarantor());
						if (ean.isPresent()) {
							if (!StickerServiceHolder.get().hasSticker(coverage.getGuarantor(), insuranceSticker)) {
								Optional<IOrganization> managedInsurance = getManagedInsuranceWithEan(ean.get());
								if (managedInsurance.isPresent()) {
									coverage.setGuarantor(managedInsurance.get());
									coverageChangedToManagedContact++;
								} else {
									coverageNoManagedContact++;
								}
							} else {
								coverageManagedContact++;
							}
						} else {
							coverageNoEanContact++;
						}
					} else {
						coverageNoRefOrOrg++;
					}
				}
			}
			sb.append("\nCoverage managedinsurance:");
			sb.append("\nReferences missing or no org:" + coverageNoRefOrOrg);
			sb.append("\nReferences to org no ean:" + coverageNoEanContact);
			sb.append("\nReferences to managedinsurances:" + coverageManagedContact);
			sb.append("\nReferences to not managedinsurances:" + coverageNoManagedContact);
			sb.append("\nChanged References managedinsurances:" + coverageChangedToManagedContact);
		} else {
			sb.append("\nNo managedinsurance sticker\n");
		}

		return sb.toString();
	}

	private Optional<IOrganization> getManagedInsuranceWithEan(String ean) {
		List<IOrganization> found = XidServiceHolder.get().findObjects(DOMAIN_EAN, ean, IOrganization.class);
		if (found != null && !found.isEmpty()) {
			for (IOrganization iOrganization : found) {
				if (StickerServiceHolder.get().hasSticker(iOrganization, insuranceSticker)) {
					return Optional.of(iOrganization);
				}
			}
		}
		return Optional.empty();
	}

	private Optional<String> getEAN(IContact contact) {
		IXid xid = contact.getXid(DOMAIN_EAN);
		if (xid != null && StringUtils.isNotBlank(xid.getDomainId())) {
			return Optional.of(xid.getDomainId());
		}
		return Optional.empty();
	}

	@Override
	public String getMaintenanceDescription() {
		return "XIDs aus ExtInfo Informationen erstellen (z.B. EAN)";
	}

}
