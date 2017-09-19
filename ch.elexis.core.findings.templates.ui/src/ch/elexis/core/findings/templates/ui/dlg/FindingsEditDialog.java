package ch.elexis.core.findings.templates.ui.dlg;

import java.math.BigDecimal;
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
		iCompositeSaveable.getChildComposites()
			.add(createDynamicContent(iFinding, iCompositeSaveable));
		return (Control) iCompositeSaveable;
	}
	
	private ICompositeSaveable createDynamicContent(IFinding iFinding, ICompositeSaveable current){
		
		if (iFinding instanceof IObservation) {
			IObservation item = (IObservation) iFinding;
			List<IObservation> refChildrens = item.getTargetObseravtions(ObservationLinkType.REF);
			List<IObservation> compChildrens = item.getTargetObseravtions(ObservationLinkType.COMP);
			if (refChildrens.isEmpty() && compChildrens.isEmpty()) {
				current = new CompositeTextUnit((Composite) current, item);
			} else {
				current = new CompositeGroup((Composite) current, item, iFinding.getText().get());
				for (IObservation child : refChildrens) {
					ICompositeSaveable childComposite = createDynamicContent(child, current);
					current.getChildComposites().add(childComposite);
				}
				if (!compChildrens.isEmpty())
				{
					// show as component
					current.changeLayout(new GridLayout(compChildrens.size() + 1, false));
				}
				
				boolean allUnitsSame = checkIfAllUnitsSame(compChildrens);
				int i = 0;
				for (IObservation child : compChildrens) {
					i++;
					ICompositeSaveable childComposite = createDynamicContent(child, current);
					current.getChildComposites().add(childComposite);
					if (allUnitsSame) {
						if (childComposite instanceof CompositeTextUnit) {
							childComposite.hideLabel(i < compChildrens.size());
						}
					}
					
				}
			}
		} else {
			current = new CompositeTextUnit((Composite) current, iFinding);
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
			child.getNumericValueUnit().ifPresent(item -> units.add(item));
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
		private Label lbl;
		
		public CompositeTextUnit(Composite parent, IFinding iFinding){
			super((Composite) parent, SWT.NONE);
			
			this.iFinding = iFinding;
			
			setLayout(new GridLayout(3, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			String unit = null;
			BigDecimal numeric = null;
			
			if (iFinding instanceof IObservation) {
				IObservation iObservation = (IObservation) iFinding;
				unit = iObservation.getNumericValueUnit().orElse(null);
				numeric = iObservation.getNumericValue().orElse(null);
				
				if (numeric != null && unit != null) {
					lbl = new Label(this, SWT.NONE);
					lbl.setText(iFinding.getText().get());
					
					GridData minGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
					minGD.widthHint = 90;
					lbl.setLayoutData(minGD);
					
					fieldText = new Text(this, SWT.BORDER);
					fieldText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
					fieldText.setText(numeric.toPlainString());
					lblUnit = new Label(this, SWT.NONE);
					lblUnit.setText(unit);
				}
			}
			
			if (fieldText == null) {
				fieldText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				fieldText.setText(iFinding.getText().get());
			}
		}
		
		@Override
		public void saveContents(){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			if (lblUnit != null && lbl != null) {
				IObservation iObservation = (IObservation) iFinding;
				
				try {
					BigDecimal number = new BigDecimal(fieldText.getText());
					iObservation.setNumericValue(number, lblUnit.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
			} else {
				iFinding.setText(fieldText.getText());
			}
		}
		
		@Override
		public List<ICompositeSaveable> getChildComposites(){
			return Collections.emptyList();
		}
		
		@Override
		public void hideLabel(boolean all){
			if (lblUnit != null && all) {
				lblUnit.setVisible(false);
				GridData minGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
				minGD.widthHint = 0;
				lblUnit.setLayoutData(minGD);
			}
			if (lbl != null) {
				fieldText.setToolTipText(lbl.getText());
				lbl.setVisible(false);
				((GridData) lbl.getLayoutData()).widthHint = 0;
			}
		}
		
		@Override
		public void changeLayout(GridLayout gridLayout){
			setLayout(gridLayout);
		}
	}
	
	class CompositeGroup extends Composite implements ICompositeSaveable {
		private IFinding iFinding;
		private Label lbl;
		
		private List<ICompositeSaveable> childComposites = new ArrayList<>();
		
		public CompositeGroup(Composite parent, IFinding iFinding, String label){
			super((Composite) parent, SWT.NONE);
			this.iFinding = iFinding;
			
			setLayout(new GridLayout(1, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			if (!label.isEmpty()) {
				lbl = new Label(this, SWT.NONE);
				lbl.setText(label);
				GridData minGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
				minGD.widthHint = 80;
				lbl.setLayoutData(minGD);
				
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
		
		@Override
		public void hideLabel(boolean all){
			if (lbl != null) {
				lbl.setText("");
			}
		}
		
		@Override
		public void changeLayout(GridLayout gridLayout){
			setLayout(gridLayout);
		}
		
	}
	
	interface ICompositeSaveable {
		public void saveContents();
		
		public List<ICompositeSaveable> getChildComposites();
		
		public void hideLabel(boolean all);
		
		public void changeLayout(GridLayout gridLayout);
	}
}
