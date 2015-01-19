package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.views.controls.ArticleDefaultSignatureComposite;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;

public class ArticleDefaultSignatureTitleAreaDialog extends TitleAreaDialog {
	
	private Artikel article;
	private Button btnRadioOnAtcCode;
	private ArticleDefaultSignatureComposite adsc;
	private Prescription prescription;
	private Button btnRadioOnArticle;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public ArticleDefaultSignatureTitleAreaDialog(Shell parentShell, Artikel article){
		super(parentShell);
		
		// TODO find existing?
		this.article = article;
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ArticleDefaultSignatureTitleAreaDialog(Shell parentShell, Prescription pr){
		super(parentShell);
		// TODO find existing
		this.prescription = pr;
		this.article = pr.getArtikel();
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage("Für diesen ATC Code oder diesen Artikel folgende Standard-Signatur hinterlegen");
		setTitle("Standard-Signatur hinterlegen");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.grabExcessVerticalSpace = false;
		container.setLayoutData(gd_container);
		
		btnRadioOnAtcCode = new Button(container, SWT.RADIO);
		btnRadioOnAtcCode.setText("auf ATC Code hinterlegen");
		
		btnRadioOnArticle = new Button(container, SWT.RADIO);
		btnRadioOnArticle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnRadioOnArticle.setText("auf Artikel hinterlegen");
		
		if(article!=null) {
			String atc_code = article.getATC_code();
			boolean atcCode = (atc_code!=null && atc_code.length()>0);
			btnRadioOnAtcCode.setEnabled(atcCode);
			btnRadioOnAtcCode.setSelection(atcCode);
			btnRadioOnArticle.setSelection(!atcCode);
		}
		
		adsc = new ArticleDefaultSignatureComposite(area, SWT.None);
		adsc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if(prescription!=null) {
			// set initial values from prescription
			List<Float> doseAsFloats = Prescription.getDoseAsFloats(prescription.getDosis());
			 for (int i = 0; i < doseAsFloats.size(); i++) {
				String val = trimTrailingZeros(Float.toString(doseAsFloats.get(i)));
				switch (i) {
				case 0:
					adsc.setSignatureMorning(val);
					break;
				case 1:
					adsc.setSignatureNoon(val);
					break;
				case 2:
					adsc.setSignatureEvening(val);
					break;
				case 3:
					adsc.setSignatureNight(val);
					break;
				default:
					break;
				}
			}
			adsc.setSignatureComment(prescription.getBemerkung());		
		}
		
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
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed(){
		ArticleDefaultSignature ads;
		if (btnRadioOnAtcCode.getSelection()) {
			ads = new ArticleDefaultSignature(null, article.getATC_code());
		} else {
			ads = new ArticleDefaultSignature(article, null);
		}
		
		ads.set(new String[] {
			ArticleDefaultSignature.FLD_SIG_MORNING, ArticleDefaultSignature.FLD_SIG_NOON,
			ArticleDefaultSignature.FLD_SIG_EVENING, ArticleDefaultSignature.FLD_SIG_NIGHT,
			ArticleDefaultSignature.FLD_SIG_COMMENT
		},
			new String[] {
				adsc.getSignatureMorning(), adsc.getSignatureNoon(), adsc.getSignatureEvening(),
				adsc.getSignatureNight(), adsc.getSignatureComment()
			});
		
		super.okPressed();
	}
	
	private String trimTrailingZeros(String number) {
	    if(!number.contains(".")) {
	        return number;
	    }

	    return number.replaceAll("\\.?0*$", "");
	}
	
}
