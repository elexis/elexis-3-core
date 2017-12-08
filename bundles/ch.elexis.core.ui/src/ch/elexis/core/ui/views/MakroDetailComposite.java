package ch.elexis.core.ui.views;

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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.dto.MakroDTO;
import ch.rgw.io.SqlSettings;
import ch.rgw.tools.JdbcLink;

public class MakroDetailComposite extends Composite {
	
	private WritableValue<MakroDTO> value;
	private DataBindingContext bindingContext;
	
	private Text makroName;
	private boolean nameDirty;
	private StyledText makroContent;
	private boolean contentDirty;
	
	public MakroDetailComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, true));
		
		createContent();
	}
	
	@SuppressWarnings("unchecked")
	private void createContent(){
		value = new WritableValue<>();
		bindingContext = new DataBindingContext();
		
		makroName = new Text(this, SWT.BORDER);
		makroName.setMessage("Makro Name");
		makroName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(makroName),
			PojoProperties.value("makroName").observeDetail(value));
		makroName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				nameDirty = true;
			}
		});
		makroName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e){
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
			public void modifyText(ModifyEvent e){
				contentDirty = true;
			}
		});
		makroContent.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e){
				if (contentDirty) {
					save(getMakro());
				}
				super.focusLost(e);
			}
		});
	}
	
	public void setMakro(MakroDTO makro){
		value.setValue(makro);
		nameDirty = false;
		contentDirty = false;
	}
	
	public MakroDTO getMakro(){
		return value.getValue();
	}
	
	private void save(MakroDTO makro){
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
	public static void saveMakro(MakroDTO makro){
		if (isCurrentUser(makro)) {
			// do not lose changes 
			CoreHub.userCfg.flush();
		}
		
		SqlSettings userSettings =
			new SqlSettings(PersistentObject.getDefaultConnection().getJdbcLink(), "USERCONFIG",
				"Param", "Value", "UserID=" + JdbcLink.wrap(makro.getMakroUserId()));
		
		userSettings.set(makro.getMakroParam(), makro.getMakroContent());
		userSettings.flush();
		if (isCurrentUser(makro)) {
			// undo reloads
			CoreHub.userCfg.undo();
		}
	}
	
	/**
	 * Utility method to remove a makro from the USERCONFIG.
	 * 
	 * @param makro
	 */
	public static void removeMakro(MakroDTO makro){
		if (isCurrentUser(makro)) {
			// do not lose changes 
			CoreHub.userCfg.flush();
		}
		
		SqlSettings userSettings =
			new SqlSettings(PersistentObject.getDefaultConnection().getJdbcLink(), "USERCONFIG",
				"Param", "Value", "UserID=" + JdbcLink.wrap(makro.getMakroUserId()));
		
		userSettings.remove(makro.getMakroParam());
		userSettings.flush();
		if (isCurrentUser(makro)) {
			// undo reloads, but keeps changes so remove
			CoreHub.userCfg.undo();
			CoreHub.userCfg.remove(makro.getMakroParam());
		}
	}
	
	private static boolean isCurrentUser(MakroDTO makro){
		return makro.getMakroUserId().equals(CoreHub.actUser.getId());
	}
}
