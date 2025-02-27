package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.views.controls.ArticleDefaultSignatureComposite;

public class ArticleDefaultSignatureTitleAreaDialog extends TitleAreaDialog {

	private IArticle article;
	private ArticleDefaultSignatureComposite adsc;
	private IPrescription prescription;

	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public ArticleDefaultSignatureTitleAreaDialog(Shell parentShell, IArticle article) {
		super(parentShell);
		this.article = article;
	}

	/**
	 * @wbp.parser.constructor
	 */
	public ArticleDefaultSignatureTitleAreaDialog(Shell parentShell, IPrescription prescription) {
		super(parentShell);
		this.prescription = prescription;
		this.article = prescription.getArticle();
	}

	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Für diesen ATC Code oder diesen Artikel folgende Standard-Signatur hinterlegen");
		setTitle("Standard-Signatur hinterlegen");
		Composite area = (Composite) super.createDialogArea(parent);

		adsc = new ArticleDefaultSignatureComposite(area, SWT.None);
		adsc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		adsc.setToolbarVisible(false);
		adsc.initDataBindings(null);
		adsc.setArticleToBind(article, false);

		if (prescription != null) {
			// set initial values from prescription
			List<Float> doseAsFloats = MedicationServiceHolder.get().getDosageAsFloats(prescription);
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
			adsc.setSignatureComment(prescription.getDisposalComment());
		}

		return area;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		adsc.updateModelNonDatabinding();
		adsc.save();
		adsc.layout();

		super.okPressed();
	}


	private String trimTrailingZeros(String number) {
		if (!number.contains(".")) { //$NON-NLS-1$
			return number;
		}

		return number.replaceAll("\\.?0*$", StringUtils.EMPTY); //$NON-NLS-1$
	}

}
