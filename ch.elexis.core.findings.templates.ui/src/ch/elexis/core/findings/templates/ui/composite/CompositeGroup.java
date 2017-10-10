package ch.elexis.core.findings.templates.ui.composite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
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
import ch.elexis.core.findings.templates.ui.util.FindingsTemplateUtil;
import ch.elexis.core.findings.templates.ui.views.FindingsView;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;

/**
 * There exists a main group with depth index of 0 all childrens have a depth index > 0
 * 
 * @author med1
 *
 */
public class CompositeGroup extends Composite implements ICompositeSaveable {
	private IFinding iFinding;
	private Label lbl;
	private List<ICompositeSaveable> childReferences = new ArrayList<>();
	private List<ICompositeSaveable> childComponents = new ArrayList<>();
	private String txt;
	private List<Action> toolbarActions = new ArrayList<>();
	private ObservationType observationType;
	
	public CompositeGroup(Composite parent, IFinding iFinding, boolean showTitle,
		boolean showBorder, int marginWidth, int marginTop, int depthIndex){
		super((Composite) parent, showBorder || depthIndex == 0 ? SWT.BORDER : SWT.NONE);
		this.iFinding = iFinding;
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = marginWidth;
		gridLayout.marginTop = marginTop;
		gridLayout.marginBottom = 10;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		
		if (iFinding instanceof IObservation) {
			this.observationType = ((IObservation) iFinding).getObservationType();
		}
		
		if (depthIndex == 0) {
			setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			Composite titleComposite = new Composite(this, SWT.NONE);
			titleComposite.setLayout(SWTHelper.createGridLayout(true, 1));
			titleComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
			// add main toolbar
			this.setToolbarActions(FindingsTemplateUtil.
				createToolbarMainComponent(titleComposite, (IObservation) iFinding, 1));
		} else {
			if (iFinding instanceof IObservation) {
				Optional<ICoding> coding = FindingsView.findingsTemplateService.findOneCode(
					((IObservation) iFinding).getCoding(), CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
				txt = coding.isPresent() ? coding.get().getDisplay() : "";
			} else {
				txt = iFinding.getText().orElse(null);
			}
			if (showTitle && txt != null) {
				GridData gdLbl = new GridData(SWT.CENTER, SWT.BOTTOM, true, false);
				lbl = new Label(this, SWT.NONE);
				FontData fontData = lbl.getFont().getFontData()[0];
				
				if (depthIndex == 1) {
					gdLbl.horizontalIndent = -40;
					gridLayout.marginRight = 10;
					gridLayout.marginLeft = 10;
					lbl.setFont(
						UiDesk.getFont(fontData.getName(), fontData.getHeight() + 3, SWT.BOLD));
				} else if (depthIndex > 1) {
					gridLayout.marginTop = 15;
					gridLayout.marginBottom = 0;
					gdLbl.horizontalIndent = -40;
					lbl.setFont(
						UiDesk.getFont(fontData.getName(), fontData.getHeight() + 1, SWT.BOLD));
				}
				lbl.setText(txt);
				lbl.setLayoutData(gdLbl);
			}
		}
		setLayout(gridLayout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
	}
	
	@Override
	public String getText(){
		return FindingsTemplateUtil.getGroupText(this);
	}
	
	@Override
	public String getFieldTextValue(){
		return null;
	}
	
	@Override
	public List<ICompositeSaveable> getChildComponents(){
		return childComponents;
	}
	
	@Override
	public List<ICompositeSaveable> getChildReferences(){
		return childReferences;
	}
	
	@Override
	public IFinding saveContents(LocalDateTime localDateTime){
		if (iFinding.getId() == null) {
			iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
		}
		
		return FindingsTemplateUtil.saveObservation((IObservation) iFinding, this, localDateTime);
	}
	
	@Override
	public void hideLabel(boolean all){
		if (lbl != null) {
			lbl.setText("");
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
		return lbl != null ? lbl.getText() : txt;
	}
	
	@Override
	public IFinding getFinding(){
		return iFinding;
	}
	
	@Override
	public ObservationComponent getObservationComponent(){
		return null;
	}
	
	@Override
	public ObservationType getObservationType(){
		return observationType;
	}
	
}
