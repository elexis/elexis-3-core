package ch.elexis.core.ui.dialogs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.User;
import ch.rgw.tools.PasswordValidator;
import ch.rgw.tools.PasswordValidator.PasswordValidationResult;

public class ChangePasswordDialog extends TitleAreaDialog {
	
	private User user;
	private Text txtPassword1;
	private Text txtPassword2;
	
	private Button okButton;
	private PasswordChangeListener pcl = new PasswordChangeListener();
	private Set<String> disallowedPasswords = new HashSet<String>();
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public ChangePasswordDialog(Shell parentShell, User user){
		super(parentShell);
		this.user = user;
		disallowedPasswords.add("Vkwi42Ja");
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage(String.format("Passwort für User %s ändern", user.getId()));
		setTitle("Passwort ändern");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblNewPassword = new Label(container, SWT.NONE);
		lblNewPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewPassword.setText("Neues Passwort");
		
		txtPassword1 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPassword1.addKeyListener(pcl);
		
		Label lblConfirm = new Label(container, SWT.NONE);
		lblConfirm.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConfirm.setText("Bestätigen");
		
		txtPassword2 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblHint = new Label(container, SWT.NONE);
		lblHint.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblHint.setText("Tipp");
		
		Label lblHintText = new Label(container, SWT.NONE);
		lblHintText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblHintText.setText("Denken Sie sich einen kompletten Satz mit einer Zahl.\nVerwenden Sie nun die Anfangsbuchstaben jedes Wortes, \nunter Berücksichtigung der Groß/Kleinschreibung.\n\nEin Beispiel:\nDer Satz „Vor kurzem wurde ich 42 Jahre alt.“ wird zum\nPasswort „Vkwi42Ja“.");
		txtPassword2.addKeyListener(pcl);
		
		return area;
	}
	
	private class PasswordChangeListener extends KeyAdapter {
		
		@Override
		public void keyReleased(KeyEvent e){
			Set<PasswordValidationResult> validateNewPass = PasswordValidator.validateNewPass(
				txtPassword1.getText(), txtPassword2.getText(), disallowedPasswords);
			if (Collections.singleton(PasswordValidationResult.SUCCESS).equals(validateNewPass)) {
				okButton.setEnabled(true);
				setErrorMessage(null);
			} else {
				okButton.setEnabled(false);
				StringBuilder errorMessage = new StringBuilder();
				for (PasswordValidationResult pwvr : validateNewPass) {
					switch (pwvr) {
					case DO_NOT_MATCH:
						errorMessage.append("Passwords do not match.\n");
						break;
					case EXCLUDED_PASSWORD:
						errorMessage.append("Passwords is not allowed.\n");
						break;
					case IS_EMPTY:
						errorMessage.append("Passwords is empty.\n");
						break;
					case MISSING_LOWERCASE:
						errorMessage.append("At least one lowercase character required.\n");
						break;
					case MISSING_UPPERCASE:
						errorMessage.append("At least one uppercase character required.\n");
						break;
					case MISSING_NUMBER:
						errorMessage.append("At least one numeric character required.\n");
						break;
					case TOO_SHORT:
						errorMessage.append("At least 8 characters required.\n");
						break;
					default:
						break;
					}
				}
				setErrorMessage(errorMessage.toString());
			}
		}
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed(){
		user.setPassword(txtPassword1.getText());
		super.okPressed();
	}
	
}
