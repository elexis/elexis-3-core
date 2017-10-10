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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import ch.elexis.core.ui.actions.CommentAction;
import ch.elexis.core.ui.dialogs.DateTimeSelectorDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.TimeTool;

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
		setTitle("Befund");
		setMessage(title);
		this.hasFocus = false;
		int depth = 0;
		iCompositeSaveable = new CompositeGroup(parent, iFinding, false, false, 10, 10, depth++);
		iCompositeSaveable.getChildComposites()
			.add(createDynamicContent(iFinding, iCompositeSaveable, depth));
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
						new CompositeGroup((Composite) current, item, true, false, 0, 10, depth);
					for (IObservation child : refChildrens) {
						ICompositeSaveable childComposite =
							createDynamicContent(child, current, ++depth);
						current.getChildComposites().add(childComposite);
					}
				}
				if (!compChildrens.isEmpty()) {
					// show as component
					current =
						new CompositeGroup((Composite) current, item, false, false, 0, 5, depth);
					
					Group group = new Group((Composite) current, SWT.NONE);
					group.setText("");
					
					GridLayout gd = new GridLayout(2, false);
					gd.marginHeight = 0;
					gd.marginBottom = 10;
					gd.verticalSpacing = 0;
					gd.marginTop = 0;
					group.setLayout(gd);
					group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					Composite groupComposite = new Composite(group, SWT.NONE);
					GridLayout gd2 = new GridLayout(2, false);
					gd2.marginHeight = 0;
					gd2.marginBottom = 5;
					gd2.verticalSpacing = 0;
					gd2.marginTop = 0;
					groupComposite.setLayout(gd2);
					groupComposite
						.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
					Label lblTitle = new Label(groupComposite, SWT.NONE);
					lblTitle.setText(current.getText());
					lblTitle.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
					current.setToolbarActions(createToolbarSubComponents(groupComposite, item, 1));
					
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
			LocalDateTime localDateTime = LocalDateTime.now();
			List<Action> actions = iCompositeSaveable.getToolbarActions();
			if (actions != null) {
				for (Action action : actions) {
					if (action instanceof DateAction) {
						localDateTime = ((DateAction) action).getLocalDateTime();
					}
				}
			}
			
			Optional<String> text = iCompositeSaveable.saveContents(localDateTime).getText();
			iFinding.setText(text.orElse(""));
		}
		super.okPressed();
	}
	
	/**
	 * Returns all actions from the main toolbar
	 * 
	 * @param c
	 * @param iObservation
	 * @param horizontalGrap
	 * @return
	 */
	private List<Action> createToolbarMainComponent(Composite c, IObservation iObservation,
		int horizontalGrap){
		
		Composite toolbarComposite = new Composite(c, SWT.NONE);
		toolbarComposite.setLayout(SWTHelper.createGridLayout(true, 2));
		toolbarComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		List<Action> actions = new ArrayList<>();
		LocalDateTime currentDate = iObservation.getEffectiveTime().orElse(LocalDateTime.now());
		
		ToolBarManager menuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		
		Action action = new DateAction(getShell(), currentDate, toolbarComposite);
		menuManager.add(action);
		menuManager.createControl(toolbarComposite)
			.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, horizontalGrap, 1));
		actions.add(action);
		
		return actions;
		
	}
	
	/**
	 * Returns all actions from the sub toolbar
	 * 
	 * @param c
	 * @param iObservation
	 * @param horizontalGrap
	 * @return
	 */
	private List<Action> createToolbarSubComponents(Composite c, IObservation iObservation,
		int horizontalGrap){
		
		List<Action> actions = new ArrayList<>();
		String comment = iObservation.getComment().orElse("");
		
		ToolBarManager menuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.NO_FOCUS);
		Action commentableAction = new CommentAction(getShell(), comment);
		menuManager.add(commentableAction);
		menuManager.createControl(c)
			.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, horizontalGrap, 1));
		actions.add(commentableAction);
		
		return actions;
		
	}
	
	private void saveToolbarActionsResult(IFinding iFinding, List<Action> toolbarActions){
		// save comments for observations
		if (iFinding instanceof IObservation && toolbarActions != null) {
			for (Action a : toolbarActions) {
				if (a instanceof CommentAction) {
					String comment = ((CommentAction) a).getComment();
					((IObservation) iFinding).setComment(comment);
				}
			}
		}
	}
	
	private void saveSimpleResult(IFinding iFinding, String text, LocalDateTime localDateTime){
		// save text
		iFinding.setText(text);
		
		// save effective time
		if (iFinding instanceof IObservation) {
			((IObservation) iFinding).setEffectiveTime(localDateTime);
		}
	}
	
	private interface ICompositeSaveable {
		public IFinding saveContents(LocalDateTime localDateTime);
		
		public List<ICompositeSaveable> getChildComposites();
		
		public void hideLabel(boolean all);
		
		public String getText();
		
		public void setToolbarActions(List<Action> toolbarActions);
		
		public List<Action> getToolbarActions();
	}
	
	private class CompositeTextUnit extends Composite implements ICompositeSaveable {
		private Text fieldText;
		private IFinding iFinding;
		private ObservationComponent backboneComponent;
		private Label lblUnit;
		private Label lbl;
		private boolean plainText;
		private List<Action> toolbarActions;
		
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
			c.setLayout(SWTHelper.createGridLayout(true, 2));
			c.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			
			lbl = new Label(c, SWT.NONE);
			lbl.setText(title);
			
			GridData minGD = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1);
			lbl.setLayoutData(minGD);
			
			if (numeric != null && unit != null) {
				if (!componentChild && iFinding instanceof IObservation) {
					toolbarActions = createToolbarSubComponents(c, (IObservation) iFinding, 1);
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
			
			if (!hasFocus) {
				hasFocus = true;
				fieldText.forceFocus();
			}
		}
		
		@Override
		public IFinding saveContents(LocalDateTime localDateTime){
			StringBuilder stringBuilder = new StringBuilder();
			
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			if (plainText) {
				// text fields inside component
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
						// numeric fields inside component
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
						stringBuilder.append(backboneComponent.getNumericValueUnit().orElse(""));
						stringBuilder.append(" ");
					} else {
						// numeric fields
						String text = fieldText.getText();
						BigDecimal number =
							NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						iObservation.setNumericValue(number, lblUnit.getText());
						
						stringBuilder.append(" ");
						stringBuilder.append(iObservation.getNumericValue().isPresent()
								? iObservation.getNumericValue().get().toPlainString() : "");
						stringBuilder.append(" ");
						stringBuilder.append(iObservation.getNumericValueUnit().orElse(""));
						stringBuilder.append(" ");
					}
					
				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(FindingsEditDialog.class)
						.warn("cannot save number illegal format", e);
				}
				
			} else {
				// text fields
				stringBuilder.append(fieldText.getText());
			}
			saveSimpleResult(iFinding, stringBuilder.toString(), localDateTime);
			saveToolbarActionsResult(iFinding, toolbarActions);
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
		
		@Override
		public void setToolbarActions(List<Action> toolbarActions){
			this.toolbarActions = toolbarActions;
			
		}
		
		@Override
		public List<Action> getToolbarActions(){
			return toolbarActions;
		}
	}
	
	/**
	 * There exists a main group with depth index of 0 all childrens have a depth index > 0
	 * 
	 * @author med1
	 *
	 */
	private class CompositeGroup extends Composite implements ICompositeSaveable {
		private IFinding iFinding;
		private Label lbl;
		private List<ICompositeSaveable> childComposites = new ArrayList<>();
		private String txt;
		private List<Action> toolbarActions;
		
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
			
			if (depthIndex == 0) {
				Composite titleComposite = new Composite(this, SWT.NONE);
				titleComposite.setLayout(SWTHelper.createGridLayout(true, 1));
				titleComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
				// add main toolbar
				this.setToolbarActions(
					createToolbarMainComponent(titleComposite, (IObservation) iFinding, 1));
			}
			else {
				if (iFinding instanceof IObservation) {
					Optional<ICoding> coding = FindingsView.findingsTemplateService.findOneCode(
						((IObservation) iFinding).getCoding(),
						CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
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
				
				if (depthIndex > 0) {
					((Composite) iCompositeSaveable)
						.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
				}
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
		public IFinding saveContents(LocalDateTime localDateTime){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
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
				builderInner
					.append(iCompositeSaveable.saveContents(localDateTime).getText().orElse(""));
			}
			builder.append(builderInner);
			builder.append(" ");
			
			saveSimpleResult(iFinding, builder.toString(), localDateTime);
			saveToolbarActionsResult(iFinding, toolbarActions);
			
			return iFinding;
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
		
	}
	
	private class DateAction extends Action {
		private LocalDateTime localDateTime;
		private final Shell shell;
		private Label lblDateText;
		
		public DateAction(Shell shell, LocalDateTime localDateTime, Composite composite){
			super("", Action.AS_PUSH_BUTTON);
			Assert.isNotNull(shell);
			this.shell = shell;
			this.localDateTime = localDateTime == null ? LocalDateTime.now() : localDateTime;
			this.lblDateText = new Label(composite, SWT.NONE);
			this.lblDateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			init();
		}
		
		@Override
		public void run(){
			DateTimeSelectorDialog inputDialog =
				new DateTimeSelectorDialog(shell, new TimeTool(localDateTime), true);
			if (inputDialog.open() == MessageDialog.OK) {
				TimeTool timeTool = inputDialog.getSelectedDate();
				if (timeTool != null) {
					this.localDateTime = timeTool.toLocalDateTime();
					init();
				}
			}
			super.run();
		}
		
		@Override
		public String getToolTipText(){
			return "Datum ändern";
		}
		
		private void init(){
			if (lblDateText != null) {
				lblDateText.setText(new TimeTool(localDateTime).toString(TimeTool.FULL_GER));
			}
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_CALENDAR.getImageDescriptor();
		}
		
		public LocalDateTime getLocalDateTime(){
			return localDateTime;
		}
	}
}
