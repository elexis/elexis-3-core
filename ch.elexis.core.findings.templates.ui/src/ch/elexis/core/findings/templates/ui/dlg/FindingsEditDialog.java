package ch.elexis.core.findings.templates.ui.dlg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.templates.ui.views.FindingsView;

public class FindingsEditDialog extends TitleAreaDialog {
	
	private final IFinding iFinding;
	private ICompositeSaveable iCompositeSaveable;
	
	public FindingsEditDialog(Shell parentShell, IFinding iFinding){
		super(parentShell);
		this.iFinding = iFinding;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		String title = FindingsView.findingsTemplateService.getTypeAsText(iFinding);
		setTitle(title + " editieren");
		
		iCompositeSaveable = new CompositeGroup(parent, iFinding, "");
		createDynamicContent(iFinding, iCompositeSaveable);
		return (Control) iCompositeSaveable;
	}
	
	private ICompositeSaveable createDynamicContent(IFinding iFinding, ICompositeSaveable current){
		
		if (iFinding instanceof IObservation) {
			IObservation item = (IObservation) iFinding;
			List<IObservation> refChildrens = item.getTargetObseravtions(ObservationLinkType.REF);
			List<IObservation> compChildrens = item.getTargetObseravtions(ObservationLinkType.COMP);
			if (refChildrens.isEmpty() && compChildrens.isEmpty()) {
				String unit = item.getUnit().orElse("");
				current = new CompositeTextUnit((Composite) current, "", unit, item);
			} else {
				current = new CompositeGroup((Composite) current, item, iFinding.getText().get());
				for (IObservation child : refChildrens) {
					ICompositeSaveable childComposite = createDynamicContent(child, current);
					current.getChildComposites().add(childComposite);
				}
				if (!compChildrens.isEmpty())
				{
					// show as component
					((CompositeGroup) current)
						.setLayout(new GridLayout(compChildrens.size() + 1, false));
				}
				
				boolean allUnitsSame = checkIfAllUnitsSame(compChildrens);
				int i = 0;
				for (IObservation child : compChildrens) {
					i++;
					ICompositeSaveable childComposite = createDynamicContent(child, current);
					current.getChildComposites().add(childComposite);
					if (allUnitsSame) {
						if (childComposite instanceof CompositeTextUnit) {
							if (i < compChildrens.size()) {
								((CompositeTextUnit) childComposite).hideLabelUnit();
							}
						}
					}
					
				}
			}
		} else {
			current = new CompositeTextUnit((Composite) current, "", "", iFinding);
		}
		
		return current;
	}
	
	/**
	 * Checks if all units the same
	 * 
	 * @param iObservations
	 * @return
	 */
	private boolean checkIfAllUnitsSame(List<IObservation> iObservations){
		Set<String> units = new HashSet<>();
		for (IObservation child : iObservations) {
			child.getUnit().ifPresent(item -> units.add(item));
		}
		return units.size() == 1;
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
	protected void buttonPressed(int buttonId){
		super.buttonPressed(buttonId);
	}
	
	@Override
	protected void okPressed(){
		if (iCompositeSaveable != null) {
			iCompositeSaveable.saveContents();
		}
		super.okPressed();
	}
	
	class CompositeTextUnit extends Composite implements ICompositeSaveable {
		private Text fieldText;
		private IFinding iFinding;
		private Label lblUnit;
		
		public CompositeTextUnit(Composite parent, String label, String unit,
			IFinding iFinding){
			super((Composite) parent, SWT.NONE);
			
			setLayout(new GridLayout(3, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			Label lbl = new Label(this, SWT.NONE);
			lbl.setText(label);
			

			if (label.isEmpty() && unit.isEmpty()) {
				fieldText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
				fieldText.setText(iFinding.getText().get());
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}
			else {
				fieldText = new Text(this, SWT.BORDER);
				fieldText.setText(iFinding.getText().get());
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			}
			
			lblUnit = new Label(this, SWT.NONE);
			lblUnit.setText(unit);
			
			this.iFinding = iFinding;
		}
		
		@Override
		public void saveContents(){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			iFinding.setText(fieldText.getText());
		}
		
		@Override
		public List<ICompositeSaveable> getChildComposites(){
			return Collections.emptyList();
		}
		
		public void hideLabelUnit(){
			if (lblUnit != null) {
				lblUnit.setText("");
			}
		}
	}
	
	class CompositeGroup extends Composite implements ICompositeSaveable {
		private IFinding iFinding;
		
		private List<ICompositeSaveable> childComposites = new ArrayList<>();
		
		public CompositeGroup(Composite parent, IFinding iFinding, String label){
			super((Composite) parent, SWT.NONE);
			this.iFinding = iFinding;
			
			setLayout(new GridLayout(1, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			if (!label.isEmpty()) {
				Label lbl = new Label(this, SWT.NONE);
				lbl.setText(label);
			}
		}
		
		@Override
		public List<ICompositeSaveable> getChildComposites(){
			return childComposites;
		}
		@Override
		public void saveContents(){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			
			for (ICompositeSaveable iCompositeSaveable : getChildComposites()) {
				iCompositeSaveable.saveContents();
			}
		}
		
	}
	
	interface ICompositeSaveable {
		public void saveContents();
		
		public List<ICompositeSaveable> getChildComposites();
	}
}
