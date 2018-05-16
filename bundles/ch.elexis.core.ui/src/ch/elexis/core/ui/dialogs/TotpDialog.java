package ch.elexis.core.ui.dialogs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Kontakt;
import ch.elexis.data.User;

public class TotpDialog extends TitleAreaDialog {
	
	protected static Logger log = LoggerFactory.getLogger(TotpDialog.class);
	
	private Image image;
	private Text text;
	private Label lblVerificationResult;
	private User user;
	private Label lblImage;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public TotpDialog(Shell parentShell){
		super(parentShell);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public TotpDialog(Shell topShell, User user){
		this(topShell);
		this.user = user;
		
		createOtpQRCodeImage();
	}
	
	private void createOtpQRCodeImage(){
		if (image != null) {
			image.dispose();
		}
		
		String issuer = "Elexis";
		
		String selfContactId = CoreHub.globalCfg.get(Preferences.SELFCONTACT_ID, "");
		if (!StringUtils.isEmpty(selfContactId)) {
			Kontakt selfContact = Kontakt.load(selfContactId);
			if (selfContact.isAvailable()) {
				try {
					issuer =
						URLEncoder.encode(selfContact.get(Kontakt.FLD_NAME1), "UTF-8").toString();
				} catch (UnsupportedEncodingException e) {
					log.error("Error encoding issuer", e);
				}
			}
		}
		
		String otpAuthString = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", "Elexis",
			user.getId(), user.getTotp(), issuer);
		Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		try {
			BitMatrix bitMatrix =
				qrCodeWriter.encode(otpAuthString, BarcodeFormat.QR_CODE, 164, 164, hintMap);
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			
			ImageData data =
				new ImageData(width, height, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					data.setPixel(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
				}
			}
			image = new Image(Display.getDefault(), data);
		} catch (WriterException ex) {
			LoggerFactory.getLogger(getClass()).error("Error creating QR", ex);
			
		}
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		
		setTitle("Validate one-time password");
		setMessage(String.format("Scan and verify One-Time Password QR code for user %s",
			user.getLabel()));
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, true));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		lblImage = new Label(container, SWT.NONE);
		lblImage.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		lblImage.setImage(image);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setBounds(0, 0, 64, 64);
		
		Label lblEnterCurrentCode = new Label(composite, SWT.WRAP);
		lblEnterCurrentCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblEnterCurrentCode.setText(
			"Scan the QR code with an app like Google Authenticator.\n\nEnter code to verify.");
		
		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e){
				String otpToken = text.getText();
				if (otpToken.length() > 5 && StringUtils.isNumeric(otpToken)) {
					if (user.verifyTotp(otpToken)) {
						lblVerificationResult.setText("Correct");
						lblVerificationResult.setForeground(UiDesk.getColor(UiDesk.COL_DARKGREEN));
						return;
					}
				}
				lblVerificationResult.setText("Incorrect");
				lblVerificationResult.setForeground(UiDesk.getColor(UiDesk.COL_RED));
			}
		});
		
		lblVerificationResult = new Label(composite, SWT.NONE);
		lblVerificationResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Link lnkResetOtp = new Link(container, SWT.NONE);
		lnkResetOtp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lnkResetOtp.setText("<a>reset</a> (Invalidates existing)");
		lnkResetOtp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e){
				user.resetTotp();
				createOtpQRCodeImage();
				lblImage.setImage(image);
			}
		});
		
		new Label(container, SWT.NONE);
		
		return area;
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	@Override
	protected void okPressed(){
		image.dispose();
		super.okPressed();
	}
}
