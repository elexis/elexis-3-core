package ch.elexis.core.ui.contacts.wizard;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;

public class ManagedInsurancesUniqueIdsSupplier implements Supplier<Set<String>> {

	@Override
	public Set<String> get() {
		if (getInsuranceSticker() != null) {
			List<IOrganization> list = StickerServiceHolder.get().getObjectsWithSticker(getInsuranceSticker(),
					IOrganization.class);
			Set<String> ret = new HashSet<String>();
			list.forEach(o -> ret.add(o.getId()));
			return ret;
		}
		LoggerFactory.getLogger(getClass()).warn("No managedinsurance sticker found");
		return Collections.emptySet();
	}

	private ISticker getInsuranceSticker() {
		ISticker insuranceSticker = CoreModelServiceHolder.get().load("managedinsurance", ISticker.class).orElse(null);
		return insuranceSticker;
	}
}
