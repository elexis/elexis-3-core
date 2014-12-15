package ch.elexis.core.ui.views.controls;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;

import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;

public class ArticleDefaultSignatureComposite extends Composite {
	
	private WritableValue signatureItem = new WritableValue(null, ArticleDefaultSignature.class);
	private DataBindingContext databindingContext;
	
	private Text txtSignatureMorning;
	private Text txtSignatureNoon;
	private Text txtSignatureEvening;
	private Text txtSignatureNight;
	private Text txtSignatureComment;
	
	/**
	 * Create the composite.
	 * 
	 * @param this
	 * @param style
	 */
	public ArticleDefaultSignatureComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(7, false));
		
		txtSignatureMorning = new Text(this, SWT.BORDER);
		txtSignatureMorning.setMessage("morgens");
		txtSignatureMorning.setToolTipText("");
		txtSignatureMorning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label = new Label(this, SWT.None);
		label.setText("-");
		
		txtSignatureNoon = new Text(this, SWT.BORDER);
		txtSignatureNoon.setMessage("mittags");
		txtSignatureNoon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(this, SWT.None);
		label.setText("-");
		
		txtSignatureEvening = new Text(this, SWT.BORDER);
		txtSignatureEvening.setMessage("abends");
		txtSignatureEvening.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(this, SWT.None);
		label.setText("-");
		
		txtSignatureNight = new Text(this, SWT.BORDER);
		txtSignatureNight.setMessage("nachts");
		txtSignatureNight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtSignatureComment = new Text(this, SWT.BORDER);
		txtSignatureComment.setMessage("Kommentar");
		txtSignatureComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
	}
	
	public DataBindingContext initDataBindings(DataBindingContext dbc){
		if (dbc == null) {
			databindingContext = new DataBindingContext();
		} else {
			databindingContext = dbc;
		}
		
		IObservableValue observeTextTextSignatureMorningObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureMorning);
		IObservableValue itemSignatureMorningObserveDetailValue =
			PojoProperties.value(ArticleDefaultSignature.class, "signatureMorning", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureMorningObserveWidget,
			itemSignatureMorningObserveDetailValue);
		
		IObservableValue observeTextTextSignatureNoonObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureNoon);
		IObservableValue itemSignatureNoonObserveDetailValue =
			PojoProperties.value(ArticleDefaultSignature.class, "signatureNoon", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureNoonObserveWidget,
			itemSignatureNoonObserveDetailValue);
		
		IObservableValue observeTextTextSignatureEveningObserveWidget =
				WidgetProperties.text(new int[] {
					SWT.Modify, SWT.FocusOut
				}).observeDelayed(100, txtSignatureEvening);
			IObservableValue itemSignatureEveningObserveDetailValue =
				PojoProperties.value(ArticleDefaultSignature.class, "signatureEvening", String.class)
					.observeDetail(signatureItem);
			databindingContext.bindValue(observeTextTextSignatureEveningObserveWidget,
				itemSignatureEveningObserveDetailValue);
		
		IObservableValue observeTextTextSignatureNightObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureNight);
		IObservableValue itemSignatureNightObserveDetailValue =
			PojoProperties.value(ArticleDefaultSignature.class, "signatureNight", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureNightObserveWidget,
			itemSignatureNightObserveDetailValue);
		
		IObservableValue observeTextTextSignatureCommentObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureComment);
		IObservableValue itemSignatureCommentObserveDetailValue =
			PojoProperties.value(ArticleDefaultSignature.class, "signatureComment", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureCommentObserveWidget,
			itemSignatureCommentObserveDetailValue);
		
		return databindingContext;
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void setArticleToBind(Artikel article){
		ArticleDefaultSignature defSignature =
			ArticleDefaultSignature.getDefaultsignatureForArticle(article);
		this.setEnabled(defSignature != null);
		signatureItem.setValue(defSignature);
	}
	
	public String getSignatureMorning(){
		return txtSignatureMorning.getText();
	}
	
	public void setSignatureMorning(String signatureMorning){
		txtSignatureMorning.setText(signatureMorning);
	}
	
	public String getSignatureNoon(){
		return txtSignatureNoon.getText();
	}
	
	public void setSignatureNoon(String signatureNoon){
		txtSignatureNoon.setText(signatureNoon);
	}
	
	public String getSignatureEvening(){
		return txtSignatureEvening.getText();
	}
	
	public void setSignatureEvening(String signatureEvening){
		txtSignatureEvening.setText(signatureEvening);
	}
	
	public String getSignatureNight(){
		return txtSignatureNight.getText();
	}
	
	public void setSignatureNight(String signatureNight){
		txtSignatureNight.setText(signatureNight);
	}
	
	public String getSignatureComment(){
		return txtSignatureComment.getText();
	}
	
	public void setSignatureComment(String signatureComment){
		txtSignatureComment.setText(signatureComment);
	}
}
