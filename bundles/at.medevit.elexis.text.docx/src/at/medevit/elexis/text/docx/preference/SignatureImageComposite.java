package at.medevit.elexis.text.docx.preference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.LoggerFactory;

import at.medevit.elexis.text.docx.dataaccess.SignatureImageDataAccess;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.Images;

public class SignatureImageComposite extends Composite {

	private Label userLbl;
	private Button globalBtn;
	private Button imageBtn;

	private IUser user;
	private IImage signatureImage;

	public SignatureImageComposite(IUser user, Composite parentComposite) {
		super(parentComposite, SWT.NONE);
		this.user = user;
		this.signatureImage = CoreModelServiceHolder.get().load("signature_" + user.getId(), IImage.class).orElse(null);
		createContent();
	}

	private void createContent() {
		setLayout(new GridLayout(3, false));

		userLbl = new Label(this, SWT.NONE);
		userLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		userLbl.setText(user.getId());

		globalBtn = new Button(this, SWT.CHECK);
		globalBtn.setText(" global ");
		globalBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfigServiceHolder.get().set(user.getAssignedContact(),
						SignatureImageDataAccess.CFG_USERSIGNATURE_GLOBAL, globalBtn.getSelection());
			}
		});
		globalBtn.setSelection(ConfigServiceHolder.get().get(user.getAssignedContact(),
				SignatureImageDataAccess.CFG_USERSIGNATURE_GLOBAL, false));

		imageBtn = new Button(this, SWT.PUSH);
		imageBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (signatureImage == null) {
					FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
					String filename = fd.open();
					if (filename != null) {
						try (FileInputStream fis = new FileInputStream(new File(filename))) {
							signatureImage = CoreModelServiceHolder.get().create(IImage.class);
							signatureImage.setId("signature_" + user.getId());
							signatureImage.setTitle("SignatureImage");
							signatureImage.setDate(LocalDate.now());
							signatureImage.setImage(IOUtils.toByteArray(fis));
							MimeType mimeType = MimeType.getByExtension(FilenameUtils.getExtension(filename));
							signatureImage.setMimeType(mimeType);

							CoreModelServiceHolder.get().save(signatureImage);
							imageBtn.setImage(Images.IMG_DELETE.getImage());
						} catch (IOException e1) {
							LoggerFactory.getLogger(getClass()).error("Error setting signature image", e);
						}
					}
				} else {
					CoreModelServiceHolder.get().remove(signatureImage);
					signatureImage = null;
					imageBtn.setImage(Images.IMG_NEW.getImage());
				}
			}
		});
		imageBtn.setImage(signatureImage != null ? Images.IMG_DELETE.getImage() : Images.IMG_NEW.getImage());
	}
}
