package ch.elexis.core.findings.ui.composites;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.findings.util.ModelUtil;

public class CompositeDate extends Composite implements ICompositeSaveable {
	private IFinding iFinding;
	private ObservationComponent backboneComponent;
	private Label lbl;
	private CDateTime dateTime;
	private List<Action> toolbarActions = new ArrayList<>();
	
	private ObservationType observationType;
	
	public CompositeDate(Composite parent, IFinding iFinding,
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
		List<ICoding> codings = null;
		String title = null;
		Date value = null;
		
		if (backboneComponent != null) {
			
			this.observationType = backboneComponent.getTypeFromExtension(ObservationType.class);
			
			value = backboneComponent.getDateTimeValue().orElse(new Date());
			
			codings = backboneComponent.getCoding();
		} else if (iFinding instanceof IObservation) {
			IObservation iObservation = (IObservation) iFinding;
			
			this.observationType = iObservation.getObservationType();
			
			value = iObservation.getDateTimeValue().orElse(new Date());
			
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
		
		createContents(title, value, backboneComponent != null);
	}
	
	private void createContents(String title, Date value,
		boolean componentChild){
		lbl = new Label(this, SWT.NONE);
		lbl.setText(title);
		
		GridData minGD = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
		lbl.setLayoutData(minGD);
		
		if (dateTime == null) {
			dateTime = new CDateTime(this,
				CDT.HORIZONTAL | CDT.DATE_SHORT | CDT.DROP_DOWN | SWT.BORDER | CDT.TAB_FIELDS);
			GridData gdFieldText = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			dateTime.setLayoutData(gdFieldText);
			dateTime.setSelection(value);

			
			dateTime.addTraverseListener(new TraverseListener() {
				
				@Override
				public void keyTraversed(TraverseEvent e){
					if (e.detail == SWT.TRAVERSE_TAB_NEXT
						|| e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
						e.doit = true;
					}
				}
			});
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
		if (lbl != null) {
			dateTime.setToolTipText(lbl.getText());
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
		return dateTime != null ? LocalDateTime
			.ofInstant(dateTime.getSelection().toInstant(), ZoneId.systemDefault()).toString() : "";
	}
	
	@Override
	public ObservationComponent getObservationComponent(){
		return backboneComponent;
	}
	
	@Override
	public ObservationType getObservationType(){
		return observationType;
	}
}
