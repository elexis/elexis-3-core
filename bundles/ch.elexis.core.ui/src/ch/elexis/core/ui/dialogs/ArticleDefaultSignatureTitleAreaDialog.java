package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.views.controls.ArticleDefaultSignatureComposite;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;

public class ArticleDefaultSignatureTitleAreaDialog extends TitleAreaDialog {
	
	private Artikel article;
	private ArticleDefaultSignatureComposite adsc;
	private Prescription prescription;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public ArticleDefaultSignatureTitleAreaDialog(Shell parentShell, Artikel article){
		super(parentShell);
		this.article = article;
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ArticleDefaultSignatureTitleAreaDialog(Shell parentShell, Prescription pr){
		super(parentShell);
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
		setMessage(
			"Für diesen ATC Code oder diesen Artikel folgende Standard-Signatur hinterlegen");
		setTitle("Standard-Signatur hinterlegen");
		Composite area = (Composite) super.createDialogArea(parent);
		
		adsc = new ArticleDefaultSignatureComposite(area, SWT.None);
		adsc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		adsc.setToolbarVisible(false);
		adsc.initDataBindings(null);
		adsc.setArticleToBind(article, false);
		
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
		adsc.createPersistent();
		adsc.updateModelNonDatabinding();
		adsc.safeToDefault();
		
		super.okPressed();
	}
	
	private String trimTrailingZeros(String number) {
	    if(!number.contains(".")) {
	        return number;
	    }

	    return number.replaceAll("\\.?0*$", "");
	}
	
}
