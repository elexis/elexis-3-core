package ch.elexis.core.findings.util.importer;

import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.interfaces.IReferenceDataImporter;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.holder.StickerServiceHolder;

@Component(property = IReferenceDataImporter.REFERENCEDATAID + "=insurances", service = IReferenceDataImporter.class)
public class InsurancesReferenceDataImporter extends FhirBundleReferenceDataImporter {

	private static final String REFERENCEDATA_MANAGEDINSURANCE_VERSION = "referencedata/managedinsurance/version";

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IStickerService stickerService;

	private ISticker sticker;

	private Optional<ISticker> readOnlySticker;
	
	@Activate
	public void activate() {
		sticker = getOrCreateInsuranceSticker();
		readOnlySticker = coreModelService.load(IStickerService.STICKER_ID_READONLY, ISticker.class);
	}

	@Override
	protected void updateLocalObject(Object object) {
		if (object instanceof IOrganization) {
			IOrganization insurance = (IOrganization) object;
			if (!stickerService.hasSticker(insurance, sticker)) {
				stickerService.addSticker(sticker, insurance);
			}
			if (readOnlySticker.isPresent()
					&& !StickerServiceHolder.get().hasSticker(insurance, readOnlySticker.get())) {
				stickerService.addSticker(readOnlySticker.get(), insurance);
			}
		}
	}

	private ISticker getOrCreateInsuranceSticker() {
		ISticker insuranceSticker = coreModelService.load("managedinsurance", ISticker.class)
				.orElse(null);
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

	@Override
	protected String getVersionConfigString() {
		return REFERENCEDATA_MANAGEDINSURANCE_VERSION;
	}
}
