package ch.elexis.core.findings.ui.composites;

import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;

public class CodingComposite extends Composite {
	
	private WritableValue transientCodingValue;
	
	private Text codeTxt;
	private Text displayTxt;
	
	private Optional<ICoding> coding;
	
	public CodingComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		codeTxt = new Text(this, SWT.BORDER);
		codeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		codeTxt.setMessage("Code");
		
		displayTxt = new Text(this, SWT.BORDER);
		displayTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		displayTxt.setMessage("Text");
		
		initDataBinding();
	}
	
	private void initDataBinding(){
		transientCodingValue = new WritableValue();
		DataBindingContext bindingContext = new DataBindingContext();
		
		IObservableValue targetObservable = SWTObservables.observeText(codeTxt, SWT.Modify);
		IObservableValue modelObservable = PojoObservables.observeDetailValue(transientCodingValue,
			"code", TransientCoding.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		targetObservable = SWTObservables.observeText(displayTxt, SWT.Modify);
		modelObservable = PojoObservables.observeDetailValue(transientCodingValue, "display",
			TransientCoding.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		setCoding(null);
	}
	
	public void setCoding(ICoding iCoding){
		if (iCoding != null) {
			this.coding = Optional.of(iCoding);
		} else {
			this.coding = Optional.of(new TransientCoding("", "", ""));
		}
		transientCodingValue.setValue(this.coding.get());
	}
	
	public Optional<ICoding> getCoding(){
		return coding;
	}
}
