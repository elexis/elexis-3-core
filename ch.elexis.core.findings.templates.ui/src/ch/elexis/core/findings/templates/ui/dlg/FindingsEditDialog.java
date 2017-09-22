package ch.elexis.core.findings.templates.ui.dlg;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
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
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.BackboneComponent;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.codes.CodingSystem;
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
		
		iCompositeSaveable = new CompositeGroup(parent, iFinding, false);
		iCompositeSaveable.getChildComposites()
			.add(createDynamicContent(iFinding, iCompositeSaveable));
		return (Control) iCompositeSaveable;
	}
	
	private ICompositeSaveable createDynamicContent(IFinding iFinding, ICompositeSaveable current){
		
		if (iFinding instanceof IObservation) {
			IObservation item = (IObservation) iFinding;
			List<IObservation> refChildrens = item.getTargetObseravtions(ObservationLinkType.REF);
			List<BackboneComponent> compChildrens = item.getComponents();
			if (refChildrens.isEmpty() && compChildrens.isEmpty()) {
				current = new CompositeTextUnit((Composite) current, item, null);
			} else {
				current = new CompositeGroup((Composite) current, item, true);
				for (IObservation child : refChildrens) {
					ICompositeSaveable childComposite = createDynamicContent(child, current);
					current.getChildComposites().add(childComposite);
				}
				if (!compChildrens.isEmpty()) {
					// show as component
					current.changeLayout(new GridLayout(compChildrens.size() + 1, false));
				}
				
				boolean allUnitsSame = checkIfAllUnitsSame(compChildrens);
				int i = 0;
				for (BackboneComponent child : compChildrens) {
					i++;
					ICompositeSaveable childComposite =
						new CompositeTextUnit((Composite) current, iFinding, child);
					current.getChildComposites().add(childComposite);
					if (allUnitsSame) {
						if (childComposite instanceof CompositeTextUnit) {
							childComposite.hideLabel(i < compChildrens.size());
						}
					}
					
				}
			}
		} else {
			current = new CompositeTextUnit((Composite) current, iFinding, null);
		}
		
		return current;
	}
	
	/**
	 * Checks if all units the same
	 * 
	 * @param iObservations
	 * @return
	 */
	private boolean checkIfAllUnitsSame(List<BackboneComponent> iObservations){
		Set<String> units = new HashSet<>();
		for (BackboneComponent child : iObservations) {
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
			if (iFinding instanceof IObservation) {
				((IObservation) iFinding).setEffectiveTime(LocalDateTime.now());
			}
		}
		super.okPressed();
	}
	
	class CompositeTextUnit extends Composite implements ICompositeSaveable {
		private Text fieldText;
		private IFinding iFinding;
		private BackboneComponent backboneComponent;
		private Label lblUnit;
		private Label lbl;
		
		public CompositeTextUnit(Composite parent, IFinding iFinding,
			BackboneComponent backboneComponent){
			super((Composite) parent, SWT.NONE);
			this.iFinding = iFinding;
			this.backboneComponent = backboneComponent;
			setLayout(new GridLayout(3, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			String unit = null;
			BigDecimal numeric = null;
			List<ICoding> codings = null;
			String title = null;
			
			if (backboneComponent != null) {
				unit = backboneComponent.getNumericValueUnit().orElse(null);
				numeric = backboneComponent.getNumericValue().orElse(null);
				codings = backboneComponent.getCoding();
			} else if (iFinding instanceof IObservation) {
				IObservation iObservation = (IObservation) iFinding;
				unit = iObservation.getNumericValueUnit().orElse(null);
				numeric = iObservation.getNumericValue().orElse(null);
				codings = iObservation.getCoding();
			}
			
			if (codings != null) {
				Optional<ICoding> coding = FindingsView.findingsTemplateService.findOneCode(codings,
					CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
				title = coding.isPresent() ? coding.get().getDisplay() : "";
			}
			if (title == null) {
				title = iFinding.getText().orElse("");
			}
			
			createContents(title, unit, numeric);
		}
		
		private void createContents(String text, String unit, BigDecimal numeric){
			if (numeric != null && unit != null) {
				lbl = new Label(this, SWT.NONE);
				lbl.setText(text);
				
				GridData minGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
				minGD.widthHint = 90;
				lbl.setLayoutData(minGD);
				
				fieldText = new Text(this, SWT.BORDER);
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
				fieldText.setText(numeric.toPlainString());
				lblUnit = new Label(this, SWT.NONE);
				lblUnit.setText(unit);
			}
			
			if (fieldText == null) {
				fieldText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				fieldText.setText(text);
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
					if (backboneComponent != null) {
						String text = fieldText.getText();
						
						BigDecimal number =
							NumberUtils.isDigits(text) ? new BigDecimal(text) : BigDecimal.ZERO;
						backboneComponent.setNumericValue(Optional.of(number));
						iObservation.updateComponent(backboneComponent);
					} else {
						String text = fieldText.getText();
						BigDecimal number =
							NumberUtils.isDigits(text) ? new BigDecimal(text) : BigDecimal.ZERO;
						iObservation.setNumericValue(number, lblUnit.getText());
					}
					
				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(FindingsEditDialog.class)
						.warn("cannot save number illegal format", e);
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
		
		public CompositeGroup(Composite parent, IFinding iFinding, boolean showTitle){
			super((Composite) parent, SWT.NONE);
			this.iFinding = iFinding;
			
			setLayout(new GridLayout(1, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			if (showTitle) {
				String txt = null;
				if (iFinding instanceof IObservation) {
					Optional<ICoding> coding = FindingsView.findingsTemplateService.findOneCode(
						((IObservation) iFinding).getCoding(),
						CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
					txt = coding.isPresent() ? coding.get().getDisplay() : "";
				} else {
					txt = iFinding.getText().orElse(null);
				}
				if (txt != null) {
					lbl = new Label(this, SWT.NONE);
					lbl.setText(txt);
					GridData minGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
					minGD.widthHint = 80;
					lbl.setLayoutData(minGD);
				}
			}
		}
		
		@Override
		public List<ICompositeSaveable> getChildComposites(){
			return childComposites;
		}
		
		@Override
		public void saveContents(){
			if (iFinding.getId() == null) {
				// TODO NEEDED in which cases finding has no id ?
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
				if (iFinding instanceof IObservation) {
					((IObservation) iFinding).setEffectiveTime(LocalDateTime.now());
				}
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
