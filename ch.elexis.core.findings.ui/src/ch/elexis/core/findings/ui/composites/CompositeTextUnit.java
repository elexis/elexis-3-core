package ch.elexis.core.findings.ui.composites;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.ui.util.SWTHelper;

public class CompositeTextUnit extends Composite implements ICompositeSaveable {
	private Text fieldText;
	private IFinding iFinding;
	private ObservationComponent backboneComponent;
	private Label lblUnit;
	private Label lbl;
	private List<Action> toolbarActions = new ArrayList<>();
	
	private ObservationType observationType;
	
	public CompositeTextUnit(Composite parent, IFinding iFinding,
		ObservationComponent backboneComponent){
		super((Composite) parent, SWT.NONE);
		this.iFinding = iFinding;
		this.backboneComponent = backboneComponent;
		GridLayout gd = new GridLayout(2, false);
		gd.marginTop = 5;
		gd.marginBottom = 0;
		gd.marginHeight = 0;
		gd.verticalSpacing = 0;
		
		setLayout(gd);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		String unit = null;
		String numeric = null;
		List<ICoding> codings = null;
		String title = null;
		String textValue = null;
		
		if (backboneComponent != null) {
			
			this.observationType = backboneComponent.getTypeFromExtension(ObservationType.class);
			
			if (ObservationType.TEXT.equals(observationType)) {
				textValue = backboneComponent.getStringValue().orElse("");
			} else if (ObservationType.NUMERIC.equals(observationType)) {
				unit = backboneComponent.getNumericValueUnit().orElse("");
				numeric = backboneComponent.getNumericValue().isPresent()
						? backboneComponent.getNumericValue().get().toPlainString() : "";
			}
			codings = backboneComponent.getCoding();
		} else if (iFinding instanceof IObservation) {
			IObservation iObservation = (IObservation) iFinding;
			
			this.observationType = iObservation.getObservationType();
			
			if (ObservationType.TEXT.equals(iObservation.getObservationType())) {
				textValue = iObservation.getStringValue().orElse("");
			} else if (ObservationType.NUMERIC.equals(iObservation.getObservationType())) {
				unit = iObservation.getNumericValueUnit().orElse("");
				numeric = iObservation.getNumericValue().isPresent()
						? iObservation.getNumericValue().get().toPlainString() : "";
			}
			codings = iObservation.getCoding();
		}
		
		if (title == null && codings != null) {
			Optional<ICoding> coding =
				ModelUtil.getCodeBySystem(codings, CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
			title = coding.isPresent() ? coding.get().getDisplay() : "";
		}
		if (title == null) {
			title = iFinding.getText().orElse("");
		}
		
		createContents(title, textValue, unit, numeric, backboneComponent != null);
	}
	
	private void createContents(String title, String textValue, String unit, String numeric,
		boolean componentChild){
		Composite c = new Composite(this, SWT.NONE);
		c.setLayout(SWTHelper.createGridLayout(true, 2));
		c.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		
		lbl = new Label(c, SWT.NONE);
		lbl.setText(title);
		
		GridData minGD = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1);
		lbl.setLayoutData(minGD);
		
		if (numeric != null && unit != null) {
			if (!componentChild && iFinding instanceof IObservation) {
				toolbarActions.addAll(
					FindingsUiUtil.createToolbarSubComponents(c, (IObservation) iFinding, 1));
			}
			fieldText = new Text(this, SWT.BORDER);
			fieldText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			fieldText.setText(numeric);
			
			fieldText.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e){
					// checks if a numeric text is inserted
					String txt = e.text;
					if (!txt.isEmpty()) {
						StringBuilder builder = new StringBuilder(((Text) e.widget).getText());
						if (e.start == e.end) {
							builder.insert(e.start, txt);
						} else {
							builder.replace(e.start, e.end, txt);
						}
						if (!builder.toString().matches("-?(\\d+\\.)?\\d*$")) {
							e.doit = false;
						}
					}
					
				}
			});
			lblUnit = new Label(this, SWT.NONE);
			GridData gdUnit = new GridData(SWT.FILL, SWT.TOP, false, false);
			gdUnit.widthHint = 40;
			lblUnit.setLayoutData(gdUnit);
			lblUnit.setAlignment(SWT.CENTER);
			lblUnit.setText(unit);
		}
		
		if (fieldText == null) {
			fieldText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			GridData gdFieldText = new GridData(SWT.FILL, SWT.TOP, true, false);
			gdFieldText.heightHint = 40;
			fieldText.setLayoutData(gdFieldText);
			fieldText.setText(textValue != null ? textValue : "");
			
			Label lblTmp = new Label(this, SWT.NONE);
			lblTmp.setText("");
			GridData gdUnit = new GridData(SWT.FILL, SWT.TOP, false, false);
			gdUnit.widthHint = 40;
			lblTmp.setLayoutData(gdUnit);
		}
	}
	
	@Override
	public IFinding saveContents(LocalDateTime localDateTime){
		if (iFinding.getId() == null) {
			iFinding = FindingsServiceComponent.getService().create(iFinding.getClass());
		}
		return FindingsUiUtil.saveObservation((IObservation) iFinding, this, localDateTime);
	}
	
	@Override
	public void hideLabel(boolean all){
		if (lblUnit != null && all) {
			lblUnit.setVisible(false);
			GridData minGD = new GridData(SWT.FILL, SWT.TOP, false, false);
			minGD.widthHint = 0;
			minGD.heightHint = 0;
			lblUnit.setLayoutData(minGD);
		}
		if (lbl != null) {
			fieldText.setToolTipText(lbl.getText());
			fieldText.setMessage(lbl.getText());
			lbl.setVisible(false);
			((GridData) lbl.getLayoutData()).widthHint = 0;
			((GridData) lbl.getLayoutData()).heightHint = 0;
		}
	}
	
	@Override
	public void setToolbarActions(List<Action> toolbarActions){
		this.toolbarActions = toolbarActions;
		
	}
	
	@Override
	public List<Action> getToolbarActions(){
		return toolbarActions;
	}
	
	@Override
	public String getTitle(){
		return lbl != null ? lbl.getText() : "";
	}
	
	@Override
	public IFinding getFinding(){
		return iFinding;
	}
	
	@Override
	public List<ICompositeSaveable> getChildReferences(){
		return Collections.emptyList();
	}
	
	@Override
	public List<ICompositeSaveable> getChildComponents(){
		return Collections.emptyList();
	}
	
	@Override
	public String getFieldTextValue(){
		return fieldText != null ? fieldText.getText() : "";
	}
	
	@Override
	public ObservationComponent getObservationComponent(){
		return backboneComponent;
	}
	
	@Override
	public ObservationType getObservationType(){
		// TODO Auto-generated method stub
		return observationType;
	}
}
