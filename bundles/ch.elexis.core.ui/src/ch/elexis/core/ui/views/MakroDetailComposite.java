package ch.elexis.core.ui.views;

import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.data.dto.MakroDTO;

public class MakroDetailComposite extends Composite {

	private WritableValue<MakroDTO> value;
	private DataBindingContext bindingContext;

	private Text makroName;
	private boolean nameDirty;
	private StyledText makroContent;
	private boolean contentDirty;

	public MakroDetailComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, true));

		createContent();
	}

	@SuppressWarnings("unchecked")
	private void createContent() {
		value = new WritableValue<>();
		bindingContext = new DataBindingContext();

		makroName = new Text(this, SWT.BORDER);
		makroName.setMessage("Makro Name");
		makroName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(makroName),
				PojoProperties.value("makroName").observeDetail(value));
		makroName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameDirty = true;
			}
		});
		makroName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (nameDirty) {
					save(getMakro());
				}
				super.focusLost(e);
			}
		});

		makroContent = new StyledText(this, SWT.BORDER | SWT.MULTI);
		makroContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(makroContent),
				PojoProperties.value("makroContent").observeDetail(value));
		makroContent.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				contentDirty = true;
			}
		});
		makroContent.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (contentDirty) {
					save(getMakro());
				}
				super.focusLost(e);
			}
		});
	}

	public void setMakro(MakroDTO makro) {
		value.setValue(makro);
		nameDirty = false;
		contentDirty = false;
	}

	public MakroDTO getMakro() {
		return value.getValue();
	}

	private void save(MakroDTO makro) {
		if (nameDirty) {
			// remove old entry first, and update the param value
			removeMakro(makro);
			StringBuilder name = new StringBuilder(makro.getMakroName());
			makro.setMakroParam("makros/" + name.reverse());
		}
		saveMakro(makro);
		nameDirty = false;
		contentDirty = false;
	}

	/**
	 * Utility method to save a makro to the USERCONFIG.
	 * 
	 * @param makro
	 */
	public static void saveMakro(MakroDTO makro) {

		Optional<IContact> userContact = CoreModelServiceHolder.get().load(makro.getMakroUserId(), IContact.class);
		if (userContact.isPresent()) {
			ConfigServiceHolder.get().set(userContact.get(), makro.getMakroParam(), makro.getMakroContent());
		} else {
			LoggerFactory.getLogger(MakroDetailComposite.class).warn(
					"No user to save makro [" + makro.getMakroName() + "] userid [" + makro.getMakroUserId() + "]");
		}
	}

	/**
	 * Utility method to remove a makro from the USERCONFIG.
	 * 
	 * @param makro
	 */
	public static void removeMakro(MakroDTO makro) {

		Optional<IContact> userContact = CoreModelServiceHolder.get().load(makro.getMakroUserId(), IContact.class);
		if (userContact.isPresent()) {
			ConfigServiceHolder.get().set(userContact.get(), makro.getMakroParam(), null);
		} else {
			LoggerFactory.getLogger(MakroDetailComposite.class).warn(
					"No user to remove makro [" + makro.getMakroName() + "] userid [" + makro.getMakroUserId() + "]");
		}
	}
}
