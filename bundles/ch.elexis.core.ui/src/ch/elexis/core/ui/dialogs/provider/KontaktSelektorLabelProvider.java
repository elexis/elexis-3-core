package ch.elexis.core.ui.dialogs.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.OrganizationConstants;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;

public class KontaktSelektorLabelProvider extends DefaultLabelProvider implements ITableColorProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof Kontakt) {
			Kontakt k = (Kontakt) element;

			String label = k.getLabel();
			if (k.istPerson()) {
				label = label + " (" + k.get(Person.BIRTHDATE) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (StringConstants.ONE.equals(k.get(Kontakt.FLD_IS_USER))) {
				label = k.get(Kontakt.FLD_NAME1) + StringUtils.SPACE + k.get(Kontakt.FLD_NAME2) + " - " + label; //$NON-NLS-1$
			}
			if (k.istOrganisation() && StringUtils.isNotBlank(
					(String) k.getExtInfoStoredObjectByKey(OrganizationConstants.FLD_EXT_ALLOWED_BILLINGLAW))) {
				label = label + " (" //$NON-NLS-1$
						+ (String) k.getExtInfoStoredObjectByKey(OrganizationConstants.FLD_EXT_ALLOWED_BILLINGLAW)
						+ ")"; //$NON-NLS-1$
			}
			return label;
		} else if (element instanceof PersistentObject) {
			PersistentObject po = (PersistentObject) element;
			return po.getLabel();
		}
		return element.toString();
	}

	public String getColumnText(Object element, int columnIndex) {
		return getText(element);
	}

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		if (element instanceof Kontakt) {
			IContact pat = ((Kontakt) element).toIContact();
			ISticker sticker = StickerServiceHolder.get().getSticker(pat).orElse(null);
			if (sticker != null && sticker.getImage() != null) {
				return CoreUiUtil.getImageAsIcon(sticker.getImage());
			}
		}
		return super.getColumnImage(element, columnIndex);
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof Kontakt) {
			IContact pat = ((Kontakt) element).toIContact();
			ISticker sticker = StickerServiceHolder.get().getSticker(pat).orElse(null);
			if (sticker != null && StringUtils.isNotBlank(sticker.getForeground())) {
				return CoreUiUtil.getColorForString(sticker.getForeground());
			}
		}
		return null;
	}

	@Override
	public Color getBackground(final Object element, final int columnIndex) {
		if (element instanceof Kontakt) {
			IContact pat = ((Kontakt) element).toIContact();
			ISticker sticker = StickerServiceHolder.get().getSticker(pat).orElse(null);
			if (sticker != null && StringUtils.isNotBlank(sticker.getBackground())) {
				return CoreUiUtil.getColorForString(sticker.getBackground());
			}
		}
		return null;
	}
}
