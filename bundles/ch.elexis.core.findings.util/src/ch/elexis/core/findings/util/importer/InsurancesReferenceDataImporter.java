package ch.elexis.core.findings.util.importer;

import java.io.InputStream;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.interfaces.AbstractReferenceDataImporter;
import ch.elexis.core.interfaces.IReferenceDataImporter;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;

@Component(property = IReferenceDataImporter.REFERENCEDATAID + "=insurances", service = IReferenceDataImporter.class)
public class InsurancesReferenceDataImporter extends AbstractReferenceDataImporter {

	private static final String REFERENCEDATA_MANAGEDINSURANCE_VERSION = "referencedata/managedinsurance/version";

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference(target = "(" + (IReferenceDataImporter.REFERENCEDATAID + "=fhirbundle)"))
	private IReferenceDataImporter fhirBundleImporter;

	@Reference
	private IAccessControlService accessControlService;

	@Reference
	private IStickerService stickerService;

	private ISticker sticker;

	private Optional<ISticker> readOnlySticker;

	@Override
	public int getCurrentVersion() {
		return ConfigServiceHolder.get().get(REFERENCEDATA_MANAGEDINSURANCE_VERSION, 0);
	}

	@Override
	public IStatus performImport(IProgressMonitor ipm, InputStream input, Integer newVersion) {
		accessControlService.doPrivileged(() -> {
			sticker = getOrCreateInsuranceSticker();
			readOnlySticker = coreModelService.load(IStickerService.STICKER_ID_READONLY, ISticker.class);
		});

		// perform import with update consumer
		IStatus ret = ((FhirBundleReferenceDataImporter) fhirBundleImporter).performImport(ipm, input, newVersion,
				(o) -> {
					if (o instanceof IOrganization) {
						IOrganization insurance = (IOrganization) o;
						if (!stickerService.hasSticker(insurance, sticker)) {
							stickerService.addSticker(sticker, insurance);
						}
						if (readOnlySticker.isPresent()
								&& !StickerServiceHolder.get().hasSticker(insurance, readOnlySticker.get())) {
							stickerService.addSticker(readOnlySticker.get(), insurance);
						}
					}
				});
		if (ret.isOK()) {
			if (newVersion != null) {
				ConfigServiceHolder.get().set(REFERENCEDATA_MANAGEDINSURANCE_VERSION, newVersion);
			}
		}
		return ret;
	}

	private ISticker getOrCreateInsuranceSticker() {
		ISticker insuranceSticker = coreModelService.load("managedinsurance", ISticker.class).orElse(null);
		if (insuranceSticker == null) {
			insuranceSticker = coreModelService.create(ISticker.class);
			insuranceSticker.setId("managedinsurance");
			insuranceSticker.setName("Versicherung");
			insuranceSticker.setBackground("e0e1e8");
			coreModelService.save(insuranceSticker);

			stickerService.setStickerAddableToClass(IOrganization.class, insuranceSticker);
		}
		return insuranceSticker;
	}
}
