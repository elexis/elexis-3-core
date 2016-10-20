package ch.elexis.core.ui.contacts.dialogs;

import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.ui.contacts.controls.ConditionComposite;

public class ConditionEditDialog extends TitleAreaDialog {
	
	private ConditionComposite conditionComposite;
	
	private Optional<ICondition> condition = Optional.empty();
	private ConditionCategory category;
	
	public ConditionEditDialog(ConditionCategory category, Shell parentShell){
		super(parentShell);
		this.category = category;
	}
	
	public ConditionEditDialog(ICondition condition, Shell parentShell){
		super(parentShell);
		this.condition = Optional.of(condition);
		this.category = condition.getCategory();
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(category + " Daten " + (condition.isPresent() ? "editieren" : "anlegen") + ".");
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new FillLayout());
		conditionComposite = new ConditionComposite(category, ret, SWT.NONE);
		condition.ifPresent(c -> conditionComposite.setCondition(c));
		return ret;
	}
	
	@Override
	protected void okPressed(){
		conditionComposite.udpateModel();
		condition = conditionComposite.getCondition();
		super.okPressed();
	}
	
	public Optional<ICondition> getCondition(){
		return condition;
	}
}
