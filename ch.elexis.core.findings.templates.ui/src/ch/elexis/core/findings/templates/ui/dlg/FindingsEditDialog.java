package ch.elexis.core.findings.templates.ui.dlg;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.ui.views.FindingsView;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;

public class FindingsEditDialog extends TitleAreaDialog {
	
	private final IFinding iFinding;
	private ICompositeSaveable iCompositeSaveable;
	
	private boolean hasFocus;
	
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
		String title = FindingsView.findingsTemplateService
			.getTypeAsText(FindingsView.findingsTemplateService.getType(iFinding));
		setTitle(title + " editieren");
		this.hasFocus = false;
		iCompositeSaveable = new CompositeGroup(parent, iFinding, false, false, 10, 0);
		iCompositeSaveable.getChildComposites()
			.add(createDynamicContent(iFinding, iCompositeSaveable, 1));
		return (Control) iCompositeSaveable;
	}
	
	private ICompositeSaveable createDynamicContent(IFinding iFinding, ICompositeSaveable current,
		int depth){
		
		if (iFinding instanceof IObservation) {
			IObservation item = (IObservation) iFinding;
			List<IObservation> refChildrens = item.getTargetObseravtions(ObservationLinkType.REF);
			List<ObservationComponent> compChildrens = item.getComponents();
			if (refChildrens.isEmpty() && compChildrens.isEmpty()) {
				current = new CompositeTextUnit((Composite) current, item, null);
			} else {
				if (!refChildrens.isEmpty()) {
					current =
						new CompositeGroup((Composite) current, item, true, false, 0, depth);
					for (IObservation child : refChildrens) {
						ICompositeSaveable childComposite =
							createDynamicContent(child, current, ++depth);
						current.getChildComposites().add(childComposite);
					}
				}
				if (!compChildrens.isEmpty()) {
					// show as component
					current =
						new CompositeGroup((Composite) current, item, false, false, 0, depth);
					
					Group group = new Group((Composite) current, SWT.NONE);
					group.setText(current.getText());
					
					GridLayout gd = new GridLayout(2, false);
					gd.marginHeight = 0;
					gd.marginBottom = 10;
					gd.verticalSpacing = 0;
					group.setLayout(gd);
					group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					addToolbar(group, 2);
					
					boolean allUnitsSame = checkIfAllUnitsSame(compChildrens);
					int i = 0;
					
					for (ObservationComponent child : compChildrens) {
						i++;
						ICompositeSaveable childComposite =
							new CompositeTextUnit(group, iFinding, child);
						current.getChildComposites().add(childComposite);
						if (allUnitsSame) {
							if (childComposite instanceof CompositeTextUnit) {
								childComposite.hideLabel(i < compChildrens.size());
							}
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
	private boolean checkIfAllUnitsSame(List<ObservationComponent> iObservations){
		Set<String> units = new HashSet<>();
		for (ObservationComponent child : iObservations) {
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
			Optional<String> text = iCompositeSaveable.saveContents().getText();
			if (iFinding instanceof IObservation) {
				((IObservation) iFinding).setEffectiveTime(LocalDateTime.now());
			}
			iFinding.setText(text.orElse(""));
		}
		super.okPressed();
	}
	
	class CompositeTextUnit extends Composite implements ICompositeSaveable {
		private Text fieldText;
		private IFinding iFinding;
		private ObservationComponent backboneComponent;
		private Label lblUnit;
		private Label lbl;
		private boolean plainText;
		
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
			plainText = false;
			String unit = null;
			String numeric = null;
			List<ICoding> codings = null;
			String title = null;
			String textValue = null;
			
			if (backboneComponent != null) {
				
				ObservationType observationType =
					backboneComponent.getTypeFromExtension(ObservationType.class);
				
				if (ObservationType.TEXT.equals(observationType)) {
					textValue = backboneComponent.getStringValue().orElse("");
					plainText = true;
				} else if (ObservationType.NUMERIC.equals(observationType)) {
					unit = backboneComponent.getNumericValueUnit().orElse("");
					numeric = backboneComponent.getNumericValue().isPresent()
							? backboneComponent.getNumericValue().get().toPlainString() : "";
				}
				codings = backboneComponent.getCoding();
			} else if (iFinding instanceof IObservation) {
				IObservation iObservation = (IObservation) iFinding;
				if (ObservationType.TEXT.equals(iObservation.getObservationType())) {
					textValue = iObservation.getStringValue().orElse("");
					plainText = true;
				} else if (ObservationType.NUMERIC.equals(iObservation.getObservationType())) {
					unit = iObservation.getNumericValueUnit().orElse("");
					numeric = iObservation.getNumericValue().isPresent()
							? iObservation.getNumericValue().get().toPlainString() : "";
				}
				codings = iObservation.getCoding();
			}
			
			if (title == null && codings != null) {
				Optional<ICoding> coding = FindingsView.findingsTemplateService.findOneCode(codings,
					CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
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
			GridLayout gd = new GridLayout(2, false);
			gd.marginTop = 0;
			gd.marginBottom = 0;
			gd.horizontalSpacing = 0;
			gd.verticalSpacing = 0;
			gd.marginHeight = 0;
			c.setLayout(gd);
			c.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			
			lbl = new Label(c, SWT.NONE);
			lbl.setText(title);
			
			GridData minGD = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1);
			lbl.setLayoutData(minGD);
			
			if (numeric != null && unit != null) {
				if (!componentChild) {
					addToolbar(c, 1);
				}
				fieldText = new Text(this, SWT.BORDER);
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				fieldText.setText(numeric);
				
				fieldText.addVerifyListener(new VerifyListener() {
					@Override
					public void verifyText(VerifyEvent e){
						String txt = e.text;
						if (!txt.isEmpty()) {
							if (NumberUtils.isDigits(txt) || txt.equals(".")) {
								if (txt.equals(".")) {
									// check if the whole text contains max one "."
									String input = ((Text) e.widget).getText();
									if (StringUtils.countMatches(input, ".") == 1) {
										e.doit = false;
									}
								}
							} else {
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
			
			if (!hasFocus) {
				hasFocus = true;
				fieldText.forceFocus();
			}
		}
		
		@Override
		public IFinding saveContents(){
			StringBuilder stringBuilder = new StringBuilder();
			
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			if (plainText) {
				IObservation iObservation = (IObservation) iFinding;
				String text = fieldText.getText();
				if (backboneComponent != null) {
					backboneComponent.setStringValue(Optional.of(text));
					
					iObservation.updateComponent(backboneComponent);
					stringBuilder.append(" ");
					stringBuilder.append(backboneComponent.getStringValue().get());
					stringBuilder.append(" ");
				} else {
					iObservation.setStringValue(text);
					stringBuilder.append(" ");
					stringBuilder.append(iObservation.getStringValue().orElse(""));
					stringBuilder.append(" ");
				}
			} else if (lblUnit != null && lbl != null) {
				IObservation iObservation = (IObservation) iFinding;
				stringBuilder.append(lbl.getText());
				try {
					if (backboneComponent != null) {
						String text = fieldText.getText();
						
						BigDecimal number =
							NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						backboneComponent.setNumericValue(
							number != null ? Optional.of(number) : Optional.empty());
						iObservation.updateComponent(backboneComponent);
						
						stringBuilder.append(" ");
						stringBuilder.append(backboneComponent.getNumericValue().isPresent()
								? backboneComponent.getNumericValue().get().toPlainString() : "");
						stringBuilder.append(" ");
						stringBuilder.append(backboneComponent.getNumericValueUnit().get());
						stringBuilder.append(" ");
					} else {
						String text = fieldText.getText();
						BigDecimal number =
							NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						iObservation.setNumericValue(number, lblUnit.getText());
						
						stringBuilder.append(" ");
						stringBuilder.append(iObservation.getNumericValue().isPresent()
								? iObservation.getNumericValue().get().toPlainString() : "");
						stringBuilder.append(" ");
						stringBuilder.append(iObservation.getNumericValueUnit().get());
						stringBuilder.append(" ");
					}
					
				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(FindingsEditDialog.class)
						.warn("cannot save number illegal format", e);
				}
				
			} else {
				stringBuilder.append(fieldText.getText());
			}
			iFinding.setText(stringBuilder.toString());
			return iFinding;
		}
		
		@Override
		public List<ICompositeSaveable> getChildComposites(){
			return Collections.emptyList();
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
		public String getText(){
			return null;
		}
	}
	
	class CompositeGroup extends Composite implements ICompositeSaveable {
		private IFinding iFinding;
		private Label lbl;
		private List<ICompositeSaveable> childComposites = new ArrayList<>();
		private String txt;
		
		public CompositeGroup(Composite parent, IFinding iFinding, boolean showTitle,
			boolean showBorder, int marginWidth, int depthIndex){
			super((Composite) parent, showBorder || depthIndex == 1 ? SWT.BORDER : SWT.NONE);
			this.iFinding = iFinding;
			
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginWidth = marginWidth;
			gridLayout.marginTop = 10;
			gridLayout.marginBottom = 10;
			gridLayout.marginHeight = 0;
			gridLayout.verticalSpacing = 0;
			
			if (iFinding instanceof IObservation) {
				Optional<ICoding> coding = FindingsView.findingsTemplateService.findOneCode(
					((IObservation) iFinding).getCoding(), CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
				txt = coding.isPresent() ? coding.get().getDisplay() : "";
			} else {
				txt = iFinding.getText().orElse(null);
			}
			if (showTitle && txt != null) {
				lbl = new Label(this, SWT.NONE);
				FontData fontData = lbl.getFont().getFontData()[0];
				if (depthIndex == 1) {
					gridLayout.marginRight = 10;
					gridLayout.marginLeft = 10;
					lbl.setFont(
						UiDesk.getFont(fontData.getName(), fontData.getHeight() + 3, SWT.BOLD));
				} else if (depthIndex > 1) {
					gridLayout.marginTop = 15;
					lbl.setFont(
						UiDesk.getFont(fontData.getName(), fontData.getHeight() + 1, SWT.BOLD));
					
				}
				
				lbl.setText(txt);
				GridData minGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
				minGD.horizontalIndent = -50;
				lbl.setLayoutData(minGD);
			}
			
			if (depthIndex > 0) {
				((Composite) iCompositeSaveable).setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			}

			
			setLayout(gridLayout);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		}
		
		@Override
		public String getText(){
			return txt;
		}
		
		@Override
		public List<ICompositeSaveable> getChildComposites(){
			return childComposites;
		}
		
		@Override
		public IFinding saveContents(){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
				if (iFinding instanceof IObservation) {
					((IObservation) iFinding).setEffectiveTime(LocalDateTime.now());
				}
			}
			StringBuilder builder = new StringBuilder();
			StringBuilder builderInner = new StringBuilder();
			if (lbl != null) {
				builder.append(lbl.getText() + ": ");
			}
			
			for (ICompositeSaveable iCompositeSaveable : getChildComposites()) {
				if (builderInner.length() > 0) {
					builderInner.append(", ");
				}
				builderInner.append(iCompositeSaveable.saveContents().getText().orElse(""));
			}
			builder.append(builderInner);
			builder.append(" ");
			iFinding.setText(builder.toString());
			return iFinding;
		}
		
		@Override
		public void hideLabel(boolean all){
			if (lbl != null) {
				lbl.setText("");
			}
		}
		
	}
	
	interface ICompositeSaveable {
		public IFinding saveContents();
		
		public List<ICompositeSaveable> getChildComposites();
		
		public void hideLabel(boolean all);
		
		public String getText();
	}
	
	public void addToolbar(Composite c, int horizontalGrap){
		ToolBarManager menuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		menuManager.add(new Action("", Action.AS_PUSH_BUTTON) {
			{
				setImageDescriptor(Images.IMG_VIEW_CONSULTATION_DETAIL.getImageDescriptor());
				setToolTipText("TEST COMMENT");
			}
			
			@Override
			public void run(){
				super.run();
			}
		});
		menuManager.createControl(c)
			.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, horizontalGrap, 1));
	}
}
