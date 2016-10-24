package ch.elexis.core.findings.ui.composites;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class ConditionComposite extends Composite {
	
	private ConditionCategory category;
	
	private Optional<ICondition> condition;
	private WritableValue transientConditionValue;
	
	private ComboViewer statusViewer;
	
	private Text textTxt;
	
	public ConditionComposite(ConditionCategory category, Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		this.category = category;
		condition = Optional.empty();
		
		statusViewer = new ComboViewer(this);
		statusViewer.setContentProvider(new ArrayContentProvider());
		statusViewer.setInput(ConditionStatus.values());
		statusViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		textTxt = new Text(this, SWT.BORDER);
		textTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		initDataBinding();
	}
	
	private void initDataBinding(){
		transientConditionValue = new WritableValue();
		DataBindingContext bindingContext = new DataBindingContext();
		
		IObservableValue targetObservable =
			ViewersObservables.observeSingleSelection(statusViewer);
		IObservableValue modelObservable = PojoObservables
			.observeDetailValue(transientConditionValue, "status", TransientCondition.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		targetObservable = SWTObservables.observeText(textTxt, SWT.Modify);
		modelObservable =
			PojoObservables.observeDetailValue(transientConditionValue, "text",
				TransientCondition.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		TransientCondition initialCondition = new TransientCondition();
		initialCondition.setStatus(ConditionStatus.ACTIVE);
		initialCondition.setDateRecorded(LocalDate.now());
		transientConditionValue.setValue(initialCondition);
	}
	
	public Optional<ICondition> getCondition(){
		return condition;
	}
	
	public void setCondition(ICondition condition){
		this.condition = Optional.ofNullable(condition);
		transientConditionValue.setValue(TransientCondition.fromCondition(condition));
	}
	
	/**
	 * Creates a new ICondition if none set, and updates it with the actual values.
	 */
	public void udpateModel(){
		if (!condition.isPresent()) {
			condition = Optional
				.of(FindingsServiceComponent.getService().getFindingsFactory().createCondition());
			condition.get().setCategory(category);
		}
		if (transientConditionValue.getValue() instanceof TransientCondition) {
			((TransientCondition) transientConditionValue.getValue()).toCondition(condition.get());
		}
	}
	
	private static class TransientCondition {
		
		private String text;
		private List<ICoding> coding = new ArrayList<>();
		private ConditionStatus status;
		private LocalDate dateRecorded;
		
		public List<ICoding> getCoding(){
			return coding;
		}
		

		public void setCoding(List<ICoding> coding){
			this.coding = coding;
		}
		
		public String getText(){
			return text;
		}
		

		public void setText(String text){
			this.text = text;
		}
		
		
		public ConditionStatus getStatus(){
			return status;
		}
		

		public void setStatus(ConditionStatus status){
			this.status = status;
		}
		
		public void setDateRecorded(LocalDate dateRecorded){
			this.dateRecorded = dateRecorded;
		}
		
		public LocalDate getDateRecorded(){
			return dateRecorded;
		}
		
		public static TransientCondition fromCondition(ICondition condition){
			TransientCondition ret = new TransientCondition();
			ret.setStatus(condition.getStatus());
			ret.setCoding(ret.getCoding());
			condition.getDateRecorded().ifPresent(d -> ret.setDateRecorded(d));
			condition.getText().ifPresent(t -> ret.setText(t));
			return ret;
		}
		
		public void toCondition(ICondition condition){
			condition.setStatus(getStatus());
			condition.setCoding(getCoding());
			condition.setDateRecorded(getDateRecorded());
			condition.setText(getText());
		}
	}
}
