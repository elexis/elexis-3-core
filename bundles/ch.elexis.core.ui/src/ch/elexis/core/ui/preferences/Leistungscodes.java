/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    H. Marlovits - added more field types (multiple lines text, styled text, combos, checkboxes, lists)
 *    				 added optional and unused/deleted fields editor
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.data.util.MultiplikatorList;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.provider.ILocalizedEnumLabelProvider;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.inputs.MultiplikatorEditor;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Fall;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Leistungscodes extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DEFINITIONSDELIMITER = ";"; //$NON-NLS-1$
	private static final String ARGUMENTSSDELIMITER = ":"; //$NON-NLS-1$
	private static final String ITEMDELIMITER = "\t"; //$NON-NLS-1$
	private static final String FOURLINESPLACEHOLDER = "\n\n\n\nd"; //$NON-NLS-1$
	List<IConfigurationElement> lo =
		Extensions.getExtensions(ExtensionPointConstantsData.RECHNUNGS_MANAGER); //$NON-NLS-1$
	List<IConfigurationElement> ll =
		Extensions.getExtensions(ExtensionPointConstantsUi.VERRECHNUNGSCODE); //$NON-NLS-1$
	String[] systeme = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
	Table table;
	String[] tableCols = {
		Messages.Leistungscodes_nameOfBillingSystem, Messages.Leistungscodes_billingSystem,
		Messages.Leistungscodes_defaultOutput, Messages.Leistungscodes_multiplier
	};
	int[] tableWidths = {
		60, 120, 120, 70
	};
	Button bCheckZero;
	Button bStrictCheck;
	
	@Override
	protected Control createContents(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		
		// *** set label on top
		Label l1 = new Label(ret, SWT.NONE);
		l1.setText(Messages.Leistungscodes_billingSystems);
		l1.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		// *** hyperlinks new and delete
		SWTHelper.createHyperlink(ret, Messages.Leistungscodes_new, new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				AbrechnungsTypDialog at = new AbrechnungsTypDialog(getShell(), null);
				if (at.open() == Dialog.OK) {
					String[] result = at.getResult();
					String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + result[0]; //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/name", result[0]); //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/leistungscodes", result[1]); //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/standardausgabe", result[2]); //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/bedingungen", result[3]); //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/fakultativ", result[4]); //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/unused", result[5]); //$NON-NLS-1$
					CoreHub.globalCfg.set(key + "/disabled", result[6]); //$NON-NLS-1$
					BillingSystem.setConfigurationValue(result[0], BillingSystem.CFG_BILLINGLAW,
						result[7]);
					BillingSystem.setConfigurationValue(result[0], BillingSystem.CFG_NOCOSTBEARER,
						result[8]);
					systeme = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
					reload();
				}
			}
		});
		SWTHelper.createHyperlink(ret, Messages.Leistungscodes_delete, new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e){
				TableItem sel = table.getSelection()[0];
				String bName = sel.getText(0);
				if (SWTHelper.askYesNo(
					MessageFormat.format(Messages.Leistungscodes_reallyDelete, bName),
					Messages.Leistungscodes_notUndoable)) {
					BillingSystem.removeAbrechnungssystem(bName);
					systeme = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
					reload();
				}
			}
		});
		
		// *** table with definitions of systems
		table = new Table(ret, SWT.FULL_SELECTION | SWT.SINGLE);
		for (int i = 0; i < tableCols.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE);
			tc.setText(tableCols[i]);
			tc.setWidth(tableWidths[i]);
		}
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e){
				int idx = table.getSelectionIndex();
				if (idx != -1) {
					TableItem sel = table.getItem(idx);
					String ssel = sel.getText(0);
					for (String s1 : systeme) {
						if (s1.equals(ssel)) {
							String[] pre = new String[9];
							
							pre[0] = s1;
							pre[1] = BillingSystem.getCodeSystem(s1);
							pre[2] = BillingSystem.getDefaultPrintSystem(s1);
							pre[3] = BillingSystem.getRequirements(s1);
							pre[4] = BillingSystem.getOptionals(s1);
							pre[5] = BillingSystem.getUnused(s1);
							pre[6] = Boolean.toString(BillingSystem.isDisabled(s1));
							pre[7] = BillingSystem.getConfigurationValue(s1,
								BillingSystem.CFG_BILLINGLAW, BillingLaw.KVG.name());
							pre[8] = BillingSystem.getConfigurationValue(s1,
								BillingSystem.CFG_NOCOSTBEARER, Boolean.FALSE.toString());
							
							AbrechnungsTypDialog at = new AbrechnungsTypDialog(getShell(), pre);
							if (at.open() == Dialog.OK) {
								String[] result = at.getResult();
								String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + result[0]; //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/name", result[0]); //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/leistungscodes", result[1]); //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/standardausgabe", result[2]); //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/bedingungen", result[3]); //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/fakultativ", result[4]); //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/unused", result[5]); //$NON-NLS-1$
								CoreHub.globalCfg.set(key + "/disabled", result[6]); //$NON-NLS-1$
								BillingSystem.setConfigurationValue(result[0],
									BillingSystem.CFG_BILLINGLAW, result[7]);
								BillingSystem.setConfigurationValue(result[0],
									BillingSystem.CFG_NOCOSTBEARER, result[8]);
								systeme =
									CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
								reload();
							}
						}
					}
				}
			}
		});
		table.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		
		// *** checkboxes check zero, strict check and optify check
		bCheckZero = new Button(ret, SWT.CHECK);
		bCheckZero.setText(Messages.Leistungscodes_checkZero);
		bCheckZero.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_BILLING_ZERO_CHECK,
					bCheckZero.getSelection());
			}
		});
		bCheckZero.setSelection(
			CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_ZERO_CHECK, false));
		bCheckZero.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		bStrictCheck = new Button(ret, SWT.CHECK);
		bStrictCheck.setText(Messages.Leistungscodes_strictValidityCheck);
		bStrictCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_BILLING_STRICT,
					bStrictCheck.getSelection());
			}
		});
		bStrictCheck
			.setSelection(CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, true));
		bStrictCheck.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		final Button bOptify = new Button(ret, SWT.CHECK);
		bOptify.setText(Messages.Leistungscodes_checkPositions);
		bOptify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_OPTIFY, bOptify.getSelection());
			}
			
		});
		bOptify.setSelection(CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_OPTIFY, true));
		bOptify.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		final Button bOptifyXray = new Button(ret, SWT.CHECK);
		bOptifyXray.setText(Messages.Leistungscodes_optifyXrayPositions);
		bOptifyXray.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_OPTIFY_XRAY,
					bOptifyXray.getSelection());
			}
			
		});
		bOptifyXray.setSelection(CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_OPTIFY_XRAY, true));
		bOptifyXray.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		// *** checkbox for enforcing separate Fall for obligations and non obligations
		final Button bObligation = new Button(ret, SWT.CHECK);
		bObligation.setText(Messages.Leistungscodes_separateObligations);
		bObligation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_OBLIGATION,
					bObligation.getSelection());
			}
			
		});
		bObligation.setSelection(CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_OBLIGATION, false));
		bObligation.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		// *** checkbox for removing open reminders if bill is fully payed
		final Button bRemoveOpenReminders = new Button(ret, SWT.CHECK);
		bRemoveOpenReminders.setText(Messages.Leistungscodes_removeOpenReminders);
		bRemoveOpenReminders.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				CoreHub.globalCfg.set(Preferences.RNN_REMOVE_OPEN_REMINDER,
					bRemoveOpenReminders.getSelection());
			}
			
		});
		bRemoveOpenReminders
			.setSelection(CoreHub.globalCfg.get(Preferences.RNN_REMOVE_OPEN_REMINDER, false));
		bRemoveOpenReminders.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		// *** populate the table with items
		reload();
		
		return ret;
	}
	
	/**
	 * (re)build the table for the billing systems, populate the table with items
	 */
	public void reload(){
		// *** remove all items first
		table.removeAll();
		// *** and rebuild the table contents
		if (systeme != null) {
			for (String s : systeme) {
				String cfgkey = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + s + "/"; //$NON-NLS-1$ //$NON-NLS-2$
				TableItem it = new TableItem(table, SWT.NONE);
				String name = CoreHub.globalCfg.get(cfgkey + "name", "default"); //$NON-NLS-1$ //$NON-NLS-2$
				it.setText(0, name);
				it.setText(1, CoreHub.globalCfg.get(cfgkey + "leistungscodes", "?")); //$NON-NLS-1$ //$NON-NLS-2$
				it.setText(2, CoreHub.globalCfg.get(cfgkey + "standardausgabe", "?")); //$NON-NLS-1$ //$NON-NLS-2$
				StringBuilder sql = new StringBuilder();
				TimeTool actdat = new TimeTool();
				MultiplikatorList multis = new MultiplikatorList("VK_PREISE", name); //$NON-NLS-1$
				String tp = Double.toString(multis.getMultiplikator(actdat));
				if (StringTool.isNothing(tp)) {
					if (CoreHub.getSystemLogLevel() > Log.INFOS) {
						SWTHelper.alert(Messages.Leistungscodes_didNotFindMulitplier,
							Messages.Leistungscodes_query + sql.toString());
					}
					tp = "1.0"; //$NON-NLS-1$
				}
				it.setText(3, tp);
			}
		}
	}
	
	public void init(final IWorkbench workbench){
		// *** at the moment do just nothing
	}
	
	/**
	 * show an input dialog for entering the name for the new case-field, <br>
	 * additionally different options to specify: <br>
	 * <br>
	 * <b> for text: </b>
	 * <ul>
	 * <li>checkbox for multiline text input</li>
	 * <li>checkbox for styled text input</li>
	 * </ul>
	 * <b> for date: </b>
	 * <ul>
	 * <li>simple text input</li>
	 * </ul>
	 * <b> for combo, list, radiogroup: </b>
	 * <ul>
	 * <li>field displayed for entering item values</li>
	 * <li>checkbox for saving as numeric value</li>
	 * </ul>
	 * <b> for checkboxes: </b>
	 * <ul>
	 * <li>field displayed for entering item values</li>
	 * </ul>
	 * <br>
	 * 
	 * @author Harald Marlovits
	 * 
	 */
	class AbrechnungsTypDialog_InputDialog extends Dialog {
		String cDialogTitle;
		String cDialogMessage;
		ListDisplay<String>[] cNoDuplicatesList;
		Text tName = null; // *** text field for field name
		String cInitialValue;
		Text tTextEditor = null; // *** text field for entering item values
		boolean cHasTextEditor;
		String cTextEditorValue; // *** initial value for this text field
		boolean cTextNeeded;
		Button chNumeric = null; // *** checkbox numeric
		boolean cHasNumericCheckbox;
		boolean cIsNumericChecked;
		Button chMultiline = null; // *** checkbox multiline
		boolean cHasStyledCheckbox;
		boolean cIsStyledChecked;
		Button chStyled = null; // *** checkbox styled text
		boolean cHasMultilineCheckbox;
		boolean cIsMultilineChecked;
		String[] result = null; // *** String Array for result
		Combo changeCombo;
		String[] cChangeTypeItems;
		String cCurrentFieldType;
		String cBilllingSystemDisabled;
		
		/**
		 * 
		 * @param parentShell
		 *            Shell, the parentShell...
		 * @param dialogTitle
		 *            String, the text displayed in the window bar
		 * @param dialogMessage
		 *            String, the message displayed at the top of the window
		 * @param initialValue
		 *            String, the value displayed for the name of the field
		 * @param noDuplicatesList
		 *            ListDisplay<String>[], array of ListDisplay's containing item names which
		 *            should not be duplicated
		 * @param text
		 *            String, the initial text value for the text editor field (for the item values)
		 * @param isNumericChecked
		 *            Boolean, should the checkbox be checked
		 * @param isStyledChecked
		 *            Boolean, should the checkbox be checked
		 * @param isMultilineChecked
		 *            Boolean, should the checkbox be checked
		 * @param changeTypeItems
		 *            String[], list of types to which this field can be changed to - if null, then
		 *            display no combo
		 */
		public AbrechnungsTypDialog_InputDialog(Shell parentShell, final String dialogTitle,
			final String dialogMessage, final String initialValue,
			ListDisplay<String>[] noDuplicatesList, String text, boolean isNumericChecked,
			boolean isStyledChecked, boolean isMultilineChecked, String[] changeTypeItems){
			super(parentShell);
			cCurrentFieldType = dialogTitle.replaceAll("\\.\\.\\..*", StringTool.leer) + "..."; //$NON-NLS-1$ //$NON-NLS-2$;
			cDialogTitle = dialogTitle.replaceAll("\\.\\.\\.", StringTool.leer); //$NON-NLS-1$
			cDialogMessage = dialogMessage;
			cInitialValue = initialValue;
			cNoDuplicatesList = noDuplicatesList;
			cTextEditorValue = text;
			cIsNumericChecked = isNumericChecked;
			cIsStyledChecked = isStyledChecked;
			cIsMultilineChecked = isMultilineChecked;
			if (changeTypeItems != null) {
				for (int i = 0; i < changeTypeItems.length; i++) {
					changeTypeItems[i] =
						changeTypeItems[i].replaceAll("\\.\\.\\.", StringTool.leer); //$NON-NLS-1$
				}
			}
			cChangeTypeItems = changeTypeItems;
			calcFieldPresence();
		}
		
		protected void calcFieldPresence(){
			cHasTextEditor = false;
			cTextNeeded = true;
			cHasNumericCheckbox = false;
			cHasStyledCheckbox = false;
			cHasMultilineCheckbox = false;
			if (cCurrentFieldType
				.equalsIgnoreCase(Messages.Leistungscodes_contactHL)) {} else if (cCurrentFieldType
					.equalsIgnoreCase(Messages.Leistungscodes_textHL)) {
				cHasStyledCheckbox = true;
				cHasMultilineCheckbox = true;
			} else if (cCurrentFieldType
				.equalsIgnoreCase(Messages.Leistungscodes_dateHL)) {} else if (cCurrentFieldType
					.equalsIgnoreCase(Messages.Leistungscodes_comboHL)) {
				cHasTextEditor = true;
				cHasNumericCheckbox = true;
			} else if (cCurrentFieldType.equalsIgnoreCase(Messages.Leistungscodes_listHL)) {
				cHasTextEditor = true;
				cHasNumericCheckbox = true;
			} else if (cCurrentFieldType.equalsIgnoreCase(Messages.Leistungscodes_checkboxHL)) {
				cHasTextEditor = true;
				cTextNeeded = false;
			} else if (cCurrentFieldType.equalsIgnoreCase(Messages.Leistungscodes_radioHL)) {
				cHasTextEditor = true;
				cHasNumericCheckbox = true;
			}
		}
		
		/**
		 * @parentShell the parent shell, or null to create a top-level shell
		 * @dialogTitle the dialog title, or null if none
		 * @dialogMessage the dialog message, or null if none
		 * @initialValue the initial input value, or null if none (equivalent to the empty string)
		 */
		protected Control createDialogArea(final Composite parent){
			// *** remove any existing controls - used if refreshing for
			// changing field type
			Control[] controls = parent.getChildren();
			for (Control c : controls) {
				c.dispose();
			}
			
			// *** create top composite
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = 5;
			gridLayout.marginTop = 7;
			gridLayout.marginBottom = 5;
			gridLayout.marginLeft = 5;
			gridLayout.marginRight = 5;
			ret.setLayout(gridLayout);
			
			// *** message on top of the window
			Label label = new Label(ret, SWT.NONE);
			label.setText(cDialogMessage);
			label.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// *** field for entering field name
			tName = new Text(ret, SWT.BORDER);
			tName.setText(cInitialValue);
			tName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// *** spacer...
			Label spacer = new Label(ret, SWT.NONE);
			spacer.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// *** display text editor if needed
			if (cHasTextEditor) {
				Label teLabel = new Label(ret, SWT.NONE);
				teLabel.setText(Messages.Leistungscodes_EnterItems);
				teLabel.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
				tTextEditor = new Text(ret, SWT.BORDER + SWT.MULTI + SWT.V_SCROLL);
				tTextEditor.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
				tTextEditor.setText(FOURLINESPLACEHOLDER); // force 4 lines
				// size...
				tTextEditor.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			}
			
			// *** display checkbox for numeric/multiline/styled
			if (cHasNumericCheckbox) {
				chNumeric = new Button(ret, SWT.CHECK);
				chNumeric.setText(Messages.Leistungscodes_SaveAsNumeric);
				chNumeric.setSelection(cIsNumericChecked);
				chNumeric.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			}
			if (cHasMultilineCheckbox) {
				chMultiline = new Button(ret, SWT.CHECK);
				chMultiline.setText(Messages.Leistungscodes_SaveAsMultiplelinesText);
				chMultiline.setSelection(cIsMultilineChecked);
				chMultiline.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			}
			// *** +++++ styled text not yet ok - just for testing now
			if (cHasStyledCheckbox & (CoreHub.actUser.getLabel().equalsIgnoreCase("a"))) { //$NON-NLS-1$
				chStyled = new Button(ret, SWT.CHECK);
				chStyled.setText(Messages.Leistungscodes_SaveAsStyledText);
				chStyled.setSelection(cIsStyledChecked);
				chStyled.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			}
			
			// *** add change fieldType combo
			if (cChangeTypeItems != null) {
				// *** placeholder
				new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL)
					.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
				Label currentTypeLabel = new Label(ret, SWT.NONE);
				currentTypeLabel.setText("Aktueller Feldtyp: "); //$NON-NLS-1$);
				Label currentType = new Label(ret, SWT.NONE);
				currentType.setText(cCurrentFieldType.replaceAll("\\.\\.\\.", StringTool.leer)); //$NON-NLS-1$);
				Label changeLabel = new Label(ret, SWT.NONE);
				changeLabel.setText(Messages.Leistungscodes_changeFieldTypeTo);
				changeCombo = new Combo(ret, SWT.NONE);
				changeCombo.setItems(cChangeTypeItems);
				changeCombo.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e){
						Combo selCombo = (Combo) e.widget;
						String selectedFieldType = selCombo.getText();
						cCurrentFieldType = selectedFieldType + "..."; //$NON-NLS-1$
						cDialogTitle = cCurrentFieldType.replaceAll("\\.\\.\\.", StringTool.leer); //$NON-NLS-1$
						calcFieldPresence();
						createDialogArea(parent); // *** create the dialog area
						// again
						createButtonBar(parent); // *** draw the button bar
						// again
						parent.layout(true); // *** force layout
						initializeBounds(); // *** recalc and set size of window
						if (tTextEditor != null)
							tTextEditor.setText(cTextEditorValue); // force 4
						// lines
						// end..
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e){}
				});
			}
			
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(cDialogTitle);
			if (tTextEditor != null)
				tTextEditor.setText(cTextEditorValue); // force 4 lines end...
		}
		
		/**
		 * validate input if not ok then display error message and return false, <br>
		 * else create result as String[] containing:
		 * <ul>
		 * <li>[0] fieldName</li>
		 * <li>[1] options for combo/list/checkboxes/radiogroup</li>
		 * <li>[2] boolean numeric</li>
		 * <li>[3] boolean multiline</li>
		 * <li>[4] boolean styled text</li>
		 * <li>[5] String new field type</li>
		 * <li>[6] boolean billing system disabled</li>
		 * <li>[7] the {@link BillingLaw} defined for this biling system</li>
		 * <li>[8] whether to exclude the definition of a cost bearer</li>
		 */
		@Override
		protected void okPressed(){
			// *** build result String Array
			result = new String[9];
			result[0] = tName.getText();
			result[1] = (tTextEditor == null || tTextEditor.isDisposed()) ? StringTool.leer
					: tTextEditor.getText();
			result[2] = (chNumeric == null || chNumeric.isDisposed()) ? "0" //$NON-NLS-1$
					: ((chNumeric.getSelection()) ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
			result[3] = (chMultiline == null || chMultiline.isDisposed()) ? "0" //$NON-NLS-1$
					: ((chMultiline.getSelection()) ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
			result[4] = (chStyled == null || chStyled.isDisposed()) ? "0" //$NON-NLS-1$
					: ((chStyled.getSelection()) ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
			result[5] = cCurrentFieldType;
			result[6] = cBilllingSystemDisabled;
			
			// *** do some validation: needs a name, some characters are not
			// allowed, no duplicates are allowed
			String errorString = StringTool.leer;
			if (result[0].isEmpty()) {
				errorString = errorString + Messages.Leistungscodes_ErrorNameMissing;
			}
			if ((result[0].indexOf(ARGUMENTSSDELIMITER) >= 0)
				|| (result[0].indexOf(DEFINITIONSDELIMITER) >= 0)
				|| (result[0].indexOf(ITEMDELIMITER) >= 0)) {
				errorString = errorString + Messages.Leistungscodes_ErrorNameNoSpecialChars;
			}
			if ((!cInitialValue.equalsIgnoreCase(result[0]))
				&& (fieldExistsAlready(result[0], cNoDuplicatesList))) {
				errorString = errorString + Messages.Leistungscodes_ErrorFieldAlreadyExists;
			}
			if (cHasTextEditor) {
				if (cTextNeeded) {
					if ((result[1].length() > 4)
						&& (result[1].substring(0, 4).equalsIgnoreCase("SQL:"))) { //$NON-NLS-1$
						// *** SQL-variant, is directly passed through
					} else if ((result[1].length() > 7)
						&& (result[1].substring(0, 7).equalsIgnoreCase("SCRIPT:"))) { //$NON-NLS-1$
						// *** SCRIPT-variant, is directly passed through
					} else {
						// *** simple listing, line by line
						String tmp = result[1].replaceAll("\r\n", DEFINITIONSDELIMITER); //$NON-NLS-1$
						tmp = tmp.replaceAll("\n", DEFINITIONSDELIMITER); //$NON-NLS-1$
						tmp = tmp.replaceAll("\r", DEFINITIONSDELIMITER); //$NON-NLS-1$
						if ((tmp.isEmpty()) || (tmp.split(DEFINITIONSDELIMITER).length < 2)) {
							errorString = errorString + Messages.Leistungscodes_ErrorAtLeast2Items;
						}
						if (!tmp.isEmpty()) {
							if ((tmp.substring(0, 1).equalsIgnoreCase(DEFINITIONSDELIMITER)) || (tmp
								.indexOf(DEFINITIONSDELIMITER + DEFINITIONSDELIMITER) >= 0)) {
								errorString =
									errorString + Messages.Leistungscodes_ErrorNoEmptyItemsAllowed;
							}
						}
						if ((result[1].indexOf(ARGUMENTSSDELIMITER) >= 0)
							|| (result[1].indexOf(DEFINITIONSDELIMITER) >= 0)
							|| (result[1].indexOf(ITEMDELIMITER) >= 0)) {
							errorString =
								errorString + Messages.Leistungscodes_ErrorItemsNoSpecialChars;
						}
					}
				}
			}
			
			// *** return if ok, else stay open
			if (errorString.isEmpty()) {
				super.okPressed();
			} else {
				SWTHelper.alert(Messages.Leistungscodes_ErrorMessageTitlebar, errorString);
			}
		}
		
		public String[] getResult(){
			return result;
		}
	}
	
	/**
	 * class for entering billing systems
	 * 
	 * @author G. Weirich / Harald Marlovits
	 * 
	 */
	class AbrechnungsTypDialog extends TitleAreaDialog {
		Text tName;
		Combo cbLstg;
		Combo cbRechn;
		ComboViewer cbLaw;
		Button cbDisabled;
		Button bNoCostBearer;
		Label lbTaxp;
		String[] result;
		MultiplikatorEditor mke;
		ListDisplay<String> ldConstants;
		ListDisplay<String> ldRequirements;
		ListDisplay<String> ldOptional;
		ListDisplay<String> ldUnused;
		private Button bUseMultiForEigenleistung;
		
		/**
		 * the constructor,
		 * 
		 * @param shell
		 * @param abrdef
		 *            String, the name of the billing system
		 */
		AbrechnungsTypDialog(final Shell shell, final String[] abrdef){
			super(shell);
			result = abrdef;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(final Composite parent){
			ScrolledComposite scroller = new ScrolledComposite(parent, SWT.V_SCROLL);
			
			Composite ret = new Composite(scroller, SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
			ret.setLayout(new GridLayout(1, false));
			
			// ****** upper part: three combos and a checkbox, has separator below
			Composite upperPartComp = new Composite(ret, SWT.NONE);
			upperPartComp.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
			upperPartComp.setLayout(new GridLayout(2, false));
			
			// *** label/text field for LeistungsSystem name
			new Label(upperPartComp, SWT.NONE).setText(Messages.Leistungscodes_nameLabel);
			tName = new Text(upperPartComp, SWT.BORDER);
			tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tName.setTextLimit(20);
			tName.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e){
					mke.reload(tName.getText());
					super.focusLost(e);
				}
			});
			
			// *** label/combo for billingcode system
			new Label(upperPartComp, SWT.NONE).setText(Messages.Leistungscodes_billingSystemLabel);
			cbLstg = new Combo(upperPartComp, SWT.READ_ONLY);
			cbLstg.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			for (IConfigurationElement ic : ll) {
				cbLstg.add(ic.getAttribute("name")); //$NON-NLS-1$
			}
			
			// *** label/combo for default output for bills
			new Label(upperPartComp, SWT.NONE).setText(Messages.Leistungscodes_defaultOutputLabel);
			cbRechn = new Combo(upperPartComp, SWT.READ_ONLY);
			cbRechn.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			for (IConfigurationElement ic : lo) {
				cbRechn.add(ic.getAttribute("name")); //$NON-NLS-1$
			}
			
			// *** label/combo for law
			new Label(upperPartComp, SWT.NONE).setText(Messages.Leistungscodes_defaultLawLabel);
			cbLaw = new ComboViewer(upperPartComp, SWT.READ_ONLY);
			cbLaw.getCombo().setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			cbLaw.setContentProvider(ArrayContentProvider.getInstance());
			cbLaw.setLabelProvider(ILocalizedEnumLabelProvider.getInstance());
			cbLaw.setInput(BillingLaw.values());
			
			// *** checkbox if system is disabled
			new Label(upperPartComp, SWT.NONE);
			cbDisabled = new Button(upperPartComp, SWT.CHECK);
			cbDisabled.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			cbDisabled.setText(Messages.Leistungscodes_systemDisabled);
			
			new Label(upperPartComp, SWT.NONE);
			bNoCostBearer = new Button(upperPartComp, SWT.CHECK);
			bNoCostBearer.setText(Messages.Leistungscodes_maskCostBearer);
			bNoCostBearer.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			
			// *** separator
			new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// *** setting the values
			String name = "default"; //$NON-NLS-1$
			if (result != null) {
				tName.setText(result[0]);
				cbLstg.setText(result[1]);
				cbRechn.setText(result[2]);
				boolean checked = true;
				if ((result[6] == null) || (result[6].isEmpty())
					|| (result[6].equalsIgnoreCase("0")) || (result[6].equalsIgnoreCase("false"))) //$NON-NLS-1$ //$NON-NLS-2$
					checked = false;
				cbDisabled.setSelection(checked);
				cbLaw.setSelection(new StructuredSelection(BillingLaw.valueOf(result[7])));
				bNoCostBearer.setSelection(Boolean.valueOf(result[8]));
				name = result[0];
			}
			
			GridLayout grid1 = new GridLayout(1, false);
			grid1.marginWidth = 0;
			grid1.marginTop = 0;
			grid1.marginBottom = 0;
			grid1.marginRight = 5;
			
			// ****** middle part: multiplyer in left column, case constants in
			// right column
			Composite middlePartComp = new Composite(ret, SWT.NONE);
			middlePartComp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			middlePartComp.setLayout(new GridLayout(2, true));
			Composite leftMiddlePart = new Composite(middlePartComp, SWT.NONE);
			leftMiddlePart.setLayout(grid1);
			leftMiddlePart.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			Composite rightMiddlePart = new Composite(middlePartComp, SWT.NONE);
			rightMiddlePart.setLayout(grid1);
			rightMiddlePart.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			// *** label/editor field/button for multiplyer
			lbTaxp = new Label(leftMiddlePart, SWT.NONE);
			lbTaxp.setText(Messages.Leistungscodes_multiplierLabel);
			
			mke = new MultiplikatorEditor(leftMiddlePart, name);
			mke.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			bUseMultiForEigenleistung = new Button(leftMiddlePart, SWT.CHECK);
			bUseMultiForEigenleistung
				.setText(Messages.Leistungscodes_useMultiplierForCustomServices);
			bUseMultiForEigenleistung
				.setSelection(MultiplikatorList.isEigenleistungUseMulti(tName.getText()));
			
			// *** label/editor for case constants
			new Label(rightMiddlePart, SWT.NONE).setText(Messages.Leistungscodes_caseConstants);
			ldConstants =
				new ListDisplay<String>(rightMiddlePart, SWT.NONE, new ListDisplay.LDListener() {
					public String getLabel(Object o){
						return (String) o;
					}
					
					public void hyperlinkActivated(String l){
						String msg = Messages.Leistungscodes_pleaseEnterNameAndValue;
						InputDialog inp = new InputDialog(getShell(),
							l + Messages.Leistungscodes_add, msg, StringTool.leer, null); //$NON-NLS-1$
						if (inp.open() == Dialog.OK) {
							String[] req = inp.getValue().split("="); //$NON-NLS-1$
							if (req.length != 2) {
								SWTHelper.showError(Messages.Leistungscodes_badEntry,
									Messages.Leistungscodes_explainEntry);
							} else {
								ldConstants.add(inp.getValue());
								String bs = result[0];
								if (bs == null) {
									bs = tName.getText();
								}
								if (StringTool.isNothing(bs)) {
									SWTHelper.showError(Messages.Leistungscodes_badEntryCaptiob,
										Messages.Leistungscodes_badEntryText);
								} else {
									BillingSystem.addBillingSystemConstant(bs, inp.getValue());
								}
							}
						}
					}
				});
			ldConstants.addHyperlinks(Messages.Leistungscodes_constantHL);
			if (result != null) {
				for (String con : BillingSystem.getBillingSystemConstants(result[0])) {
					ldConstants.add(con);
				}
			}
			ldConstants.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			Action actionDel = new Action() {
				@Override
				public String getText(){
					return Messages.Leistungscodes_delText;
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					String sel = ldConstants.getSelection();
					ldConstants.remove(sel);
					BillingSystem.removeBillingSystemConstant(result[0], sel);
				}
			};
			
			ldConstants.setMenu(actionDel);
			ldConstants.addListenerToSelectionList(SWT.KeyDown, (event) -> {
				if (event.keyCode == 0x6b) {
					// CTRL + K #6105 move cost bearer from extinfo to table
					removeRequiredStringGesetzFromFallExtInfo();
				}
			});
			
			// *** separator
			Label separator = new Label(middlePartComp, SWT.SEPARATOR | SWT.HORIZONTAL);
			separator.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// ****** lower part: required, optional and unused fields
			Composite lowerPartComp = new Composite(ret, SWT.NONE);
			lowerPartComp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			lowerPartComp.setLayout(new GridLayout(2, false));
			
			// *** label/editor field for required fields
			String[] data = null;
			if ((result != null) && (result.length > 3) && (result[3] != null))
				data = result[3].split(DEFINITIONSDELIMITER);
			FieldDefsDisplay fdReq = new FieldDefsDisplay(lowerPartComp, SWT.BORDER, data);
			fdReq.setLabel(Messages.Leistungscodes_necessaryData);
			fdReq.setData(tName.getText());
			ldRequirements = fdReq.getListDisplay();
			
			// *** label/editor field for optional fields
			data = null;
			if ((result != null) && (result.length > 4) && (result[4] != null))
				data = result[4].split(DEFINITIONSDELIMITER);
			FieldDefsDisplay fdOpt = new FieldDefsDisplay(lowerPartComp, SWT.BORDER, data);
			fdOpt.setLabel(Messages.Leistungscodes_optionalData);
			ldOptional = fdOpt.getListDisplay();
			
			// *** label/editor field for unused fields
			if (CoreHub.acl.request(AccessControlDefaults.CASE_DEFINE_SPECIALS) == true) {
				data = null;
				if ((result != null) && (result.length > 5) && (result[5] != null))
					data = result[5].split(DEFINITIONSDELIMITER);
				FieldDefsDisplay fdUnused = new FieldDefsDisplay(lowerPartComp, SWT.BORDER, data);
				fdUnused.setLabel(Messages.Leistungscodes_unusedData);
				ldUnused = fdUnused.getListDisplay();
				// *** adding action items for unused fields list
				fdUnused.addMoveToAction(ldRequirements,
					Messages.Leistungscodes_moveItemToRequiredData,
					Images.IMG_MOVETOUPPERLIST.getImageDescriptor(), true);
				fdUnused.addMoveToAction(ldOptional, Messages.Leistungscodes_moveItemToOptionalData,
					Images.IMG_MOVETOLOWERLIST.getImageDescriptor(), true);
				fdUnused.setNoDuplicatesList(ldRequirements, ldOptional);
				fdUnused.setNoDuplicatesCreateList(ldRequirements, ldOptional);
			}
			
			// *** adding action items for required fields list
			fdReq.addMoveToAction(ldRequirements, Messages.Leistungscodes_moveItemToRequiredData,
				Images.IMG_MOVETOUPPERLIST.getImageDescriptor(), false);
			fdReq.addMoveToAction(ldOptional, Messages.Leistungscodes_moveItemToOptionalData,
				Images.IMG_MOVETOLOWERLIST.getImageDescriptor(), true);
			fdReq.setDeletedList(ldUnused);
			fdReq.setNoDuplicatesList(ldOptional);
			fdReq.setNoDuplicatesCreateList(ldRequirements, ldOptional);
			
			// *** adding action items for optional fields list
			fdOpt.addMoveToAction(ldRequirements, Messages.Leistungscodes_moveItemToRequiredData,
				Images.IMG_MOVETOUPPERLIST.getImageDescriptor(), true);
			fdOpt.addMoveToAction(ldOptional, Messages.Leistungscodes_moveItemToOptionalData,
				Images.IMG_MOVETOLOWERLIST.getImageDescriptor(), false);
			fdOpt.setDeletedList(ldUnused);
			fdOpt.setNoDuplicatesList(ldRequirements);
			fdOpt.setNoDuplicatesCreateList(ldRequirements, ldOptional);
			
			// *** calc size of full-height-composite, make it correctly scroll
			Point retSize = ret.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			ret.setSize(retSize.x, retSize.y);
			scroller.setContent(ret);
			scroller.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ScrollBar sbY = scroller.getVerticalBar();
			if (sbY != null) {
				sbY.setPageIncrement(ret.getSize().y);
				sbY.setIncrement(20);
			}
			
			// *** return composite
			return scroller;
		}
		
		// #6105
		private void removeRequiredStringGesetzFromFallExtInfo(){
			String selection = ldConstants.getSelection();
			if (selection != null && StringUtils.containsIgnoreCase(selection, "Gesetz")) {
				String[] split = selection.split("=");
				String message = MessageFormat
					.format("Remove the selected field [{0}] from all Faelle?\nPlease validate that a law is set!", split[0]);
				boolean performDelete =
					MessageDialog.openQuestion(UiDesk.getTopShell(), "Remove from Faelle", message);
				if (performDelete) {
					BusyIndicator.showWhile(UiDesk.getDisplay(), () -> {
						BillingSystem.removeExtInfoValueForAllFaelleOfBillingSystem(tName.getText(),
							split[0]);
						ldConstants.remove(selection);
						BillingSystem.removeBillingSystemConstant(result[0], selection);
					});
				}
			}
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.Leistungscodes_defineBillingSystem);
			setMessage(Messages.Leistungscodes_pleaseEnterDataForBillingSystem);
			getShell().setText(Messages.Leistungscodes_billingSystemCaption);
		}
		
		/**
		 * create the result String array
		 */
		@Override
		protected void okPressed(){
			result = new String[9];
			result[0] = tName.getText();
			result[1] = cbLstg.getText();
			result[2] = cbRechn.getText();
			result[3] = StringTool.join(ldRequirements.getAll(), DEFINITIONSDELIMITER);
			result[4] = StringTool.join(ldOptional.getAll(), DEFINITIONSDELIMITER);
			if (ldUnused != null) {
				result[5] = StringTool.join(ldUnused.getAll(), DEFINITIONSDELIMITER);
			}
			result[6] = (cbDisabled.getSelection() == true) ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
			if (bUseMultiForEigenleistung.getSelection()) {
				if (!MultiplikatorList.isEigenleistungUseMulti(tName.getText())) {
					MultiplikatorList.setEigenleistungUseMulti(tName.getText());
				}
			} else {
				if (MultiplikatorList.isEigenleistungUseMulti(tName.getText())) {
					MultiplikatorList.removeEigenleistungUseMulti(tName.getText());
				}
			}
			result[7] = ((BillingLaw) cbLaw.getStructuredSelection().getFirstElement()).name();
			result[8] = Boolean.toString(bNoCostBearer.getSelection());
			super.okPressed();
		}
		
		public String[] getResult(){
			return result;
		}
	}
	
	/**
	 * test if a field already exists in the database table (faelle) or in any existing field
	 * definitions
	 * 
	 * @param fieldName
	 * @param ldNoDuplicates
	 * @return
	 */
	private boolean fieldExistsAlready(final String fieldName,
		ListDisplay<String>... ldNoDuplicates){
		final String tempCaseID = "marlovits-14x@8w1"; //$NON-NLS-1$
		
		JdbcLink j = PersistentObject.getConnection();
		String minID = ""; //$NON-NLS-1$
		try {
			// *** get just any case
			minID = j.queryString("select id from faelle limit 1"); //$NON-NLS-1$
			Fall fall = Fall.load(minID);
			if (!fall.exists()) {
				// *** there is no case yet created -> create temp dummy case
				j.exec("insert into faelle (id) values(" + JdbcLink.wrap(tempCaseID) + ")"); //$NON-NLS-1$  //$NON-NLS-2$
				minID = tempCaseID;
				fall = Fall.load(minID);
			}
			
			// *** try to find a field in the db or in the mapping
			// (case-sensitive!!!)
			String mapped = fall.map(fieldName);
			if (mapped.equalsIgnoreCase(fieldName)) {
				return true;
			}
			if (!mapped.substring(0, 8).equalsIgnoreCase("**ERROR:")) { //$NON-NLS-1$
				return true;
			}
			
			// *** try to find in existing lists
			for (ListDisplay<String> ld : ldNoDuplicates) {
				for (String str : ld.getAll()) {
					if (str.split(ARGUMENTSSDELIMITER)[0].equalsIgnoreCase(fieldName)) {
						return true;
					}
				}
			}
		} finally {
			// *** clean up: delete any created dummy case before leaving
			if (minID.equalsIgnoreCase(tempCaseID))
				j.exec("delete from faelle where id = " + JdbcLink.wrap(tempCaseID)); //$NON-NLS-1$
		}
		
		return false;
		
	}
	
	/**
	 * creates a composite with a label on the top, <br>
	 * below there is a ListDisplay on the left side and a navigator on the right side. <br>
	 * in the navigator there are some items preconfigured: <br>
	 * moveItemUp, moveItemDown, editItem
	 * 
	 * @author Harald Marlovits
	 * 
	 */
	class FieldDefsDisplay extends Composite {
		final static int MOVEITEMUP = 0;
		final static int MOVEITEMDOWN = 1;
		final static int DELETEITEM = 3;
		final static int EDITITEM = 4;
		final static int LASTFIXEDITEM = 5;
		
		Label label; // *** Label on top of this list
		ListDisplay<String> listDisplay; // *** the ListDisplay
		org.eclipse.swt.widgets.List list; // *** the List of the ListDisplay
		ToolBar toolBar; // *** the navigator on the right side
		
		ListDisplay<String> deletedList = null; // *** if items are deleted,
		// they are moved to this list
		ListDisplay<String>[] noDuplicatesList = null; // *** for move to other
		// list, this contains
		// the Lists containing
		// items not to be
		// duplicated
		ListDisplay<String>[] noDuplicatesListCreate = null; // *** for creating
		// new fields,
		// this contains
		// the Lists
		// containing
		// items not to
		// be duplicated
		
		// *** additional action items for toolbar and popupmenu
		Short additionalItemsCount = 0;
		List<ListDisplay<String>> moveTo_DestinationLists = new ArrayList<ListDisplay<String>>(30);
		IAction[] actions = new IAction[LASTFIXEDITEM + 1];
		
		/**
		 * set the noDuplicatesList value, which contains the Lists containing items not to be
		 * duplicated for the "move to other list action"
		 * 
		 * @param listArray
		 */
		public void setNoDuplicatesList(ListDisplay<String>... listArray){
			noDuplicatesList = listArray;
		}
		
		/**
		 * set the noDuplicatesList value, which contains the Lists containing items not to be
		 * duplicated for the "create new field action"
		 * 
		 * @param listArray
		 */
		public void setNoDuplicatesCreateList(ListDisplay<String>... listArray){
			noDuplicatesListCreate = listArray;
		}
		
		/**
		 * sets the label on top of this List
		 * 
		 * @param labelText
		 */
		public void setLabel(String labelText){
			label.setText(labelText);
		}
		
		/**
		 * returns the ListDisplay of this composite
		 * 
		 * @return
		 */
		public ListDisplay<String> getListDisplay(){
			return listDisplay;
		}
		
		/**
		 * sets the list to which items should be moved to when deleted
		 * 
		 * @param unusedList
		 */
		public void setDeletedList(ListDisplay<String> unusedList){
			deletedList = unusedList;
		}
		
		/**
		 * adds an item at the bottom of the popupmenu and the toolbar
		 * 
		 * @param destinationList
		 * @param toolTipText
		 * @param imageDescriptor
		 */
		public void addMoveToAction(ListDisplay<String> destinationList, String toolTipText,
			ImageDescriptor imageDescriptor, boolean enabled){
			if (toolBar != null) {
				Short newItemIx = (short) (LASTFIXEDITEM + additionalItemsCount + 1);
				moveTo_DestinationLists.add(destinationList);
				addToolItem(toolBar, imageDescriptor.createImage(), toolTipText, newItemIx,
					enabled);
				ListPopUpMenuAction newAction = new ListPopUpMenuAction(toolTipText,
					imageDescriptor, toolTipText, newItemIx, destinationList, enabled);
				IAction[] extended = new IAction[LASTFIXEDITEM + additionalItemsCount + 1 + 1];
				System.arraycopy(actions, 0, extended, 0, LASTFIXEDITEM + additionalItemsCount + 1);
				extended[LASTFIXEDITEM + additionalItemsCount + 1] = newAction;
				actions = extended;
				listDisplay.setMenu(actions);
				additionalItemsCount++;
			}
		}
		
		private FieldDefsDisplay(Composite parent, int style, String[] listItems){
			super(parent, SWT.NONE);
			this.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
			GridLayout navGridMain = new GridLayout(2, false);
			navGridMain.marginWidth = 0;
			navGridMain.marginHeight = 0;
			this.setLayout(navGridMain);
			
			// ****** create the label on top
			label = new Label(this, SWT.NONE);
			label.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// ****** create the ListDisplay
			listDisplay = new ListDisplay<String>(this, SWT.NONE, new ListDisplay.LDListener() {
				public void hyperlinkActivated(final String l){
					_FieldsHyperlinkActivated(l, StringTool.leer);
				}
				
				public String getLabel(final Object o){
					return _FieldsGetLabel(o);
				}
			});
			// *** add/fill hyperlinks on top
			listDisplay.addHyperlinks(Messages.Leistungscodes_contactHL,
				Messages.Leistungscodes_textHL, Messages.Leistungscodes_dateHL,
				Messages.Leistungscodes_comboHL, Messages.Leistungscodes_listHL,
				Messages.Leistungscodes_checkboxHL, Messages.Leistungscodes_radioHL);
			listDisplay.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			// *** add data
			if (listItems != null) {
				for (String item : listItems) {
					listDisplay.add(item);
				}
			}
			
			// ****** create toolbar on the right side
			Composite navigator = new Composite(this, SWT.NONE);
			GridLayout navGrid = new GridLayout(1, false);
			navGrid.marginWidth = 0;
			navGrid.marginHeight = 0;
			navGrid.marginTop = 0;
			navigator.setLayout(navGrid);
			navigator.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
			
			toolBar = new ToolBar(navigator, SWT.BORDER | SWT.FLAT | SWT.VERTICAL);
			// toolBar = new ToolBar(navigator, SWT.BORDER | SWT.FLAT |
			// SWT.VERTICAL);
			toolBar.setData("listDisplay", listDisplay); //$NON-NLS-1$
			listDisplay.setData("toolbar", toolBar); //$NON-NLS-1$
			
			addToolItem(toolBar, Images.IMG_ARROWUP.getImage(), Messages.Leistungscodes_moveItemUp,
				MOVEITEMUP, true);
			addToolItem(toolBar, Images.IMG_ARROWDOWN.getImage(),
				Messages.Leistungscodes_moveItemDown, MOVEITEMDOWN, true);
			addToolItem(toolBar, null, Messages.Leistungscodes_moveItemDown, -1, false);
			addToolItem(toolBar, Images.IMG_REMOVEITEM.getImage(),
				Messages.Leistungscodes_deleteItem, DELETEITEM, true);
			addToolItem(toolBar, Images.IMG_EDIT.getImage(), Messages.Leistungscodes_editItem,
				EDITITEM, true);
			addToolItem(toolBar, null, Messages.Leistungscodes_moveItemDown, -1, false);
			
			// ****** create popupmenu for this list (same actions)
			ListPopUpMenuAction moveItemUpAction = new ListPopUpMenuAction(
				Messages.Leistungscodes_moveItemUp, Images.IMG_ARROWUP.getImageDescriptor(),
				Messages.Leistungscodes_moveItemUp, MOVEITEMUP, listDisplay, true);
			ListPopUpMenuAction moveItemDownAction = new ListPopUpMenuAction(
				Messages.Leistungscodes_moveItemDown, Images.IMG_ARROWDOWN.getImageDescriptor(),
				Messages.Leistungscodes_moveItemDown, MOVEITEMDOWN, listDisplay, true);
			ListPopUpMenuAction delItemAction = new ListPopUpMenuAction(
				Messages.Leistungscodes_deleteAction, Images.IMG_REMOVEITEM.getImageDescriptor(),
				Messages.Leistungscodes_removeConstraintTT, DELETEITEM, listDisplay, true);
			ListPopUpMenuAction changeItemAction = new ListPopUpMenuAction(
				Messages.Leistungscodes_editItem, Images.IMG_EDIT.getImageDescriptor(),
				Messages.Leistungscodes_editItem, EDITITEM, listDisplay, true);
			actions[MOVEITEMUP] = moveItemUpAction;
			actions[MOVEITEMDOWN] = moveItemDownAction;
			actions[2] = null;
			actions[DELETEITEM] = delItemAction;
			actions[EDITITEM] = changeItemAction;
			actions[LASTFIXEDITEM] = null;
			listDisplay.setMenu(actions);
			
			enableDisableItems();
			
			// ****** handling clicks inside list
			final org.eclipse.swt.widgets.List child = getListPart(listDisplay);
			if (child != null) {
				child.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e){
						// *** enable or disable items
						enableDisableItems();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				child.addMouseListener(new MouseListener() {
					@Override
					// *** double click opens the change dialog
					public void mouseDoubleClick(MouseEvent e){
						String sel = listDisplay.getSelection();
						_FieldsHyperlinkActivated(StringTool.leer, sel);
					}
					
					@Override
					public void mouseDown(MouseEvent e){
						// *** mouse down right button selects item
						if (e.button != 1) {
							int itemIx = e.y / child.getItemHeight() + child.getTopIndex();
							if (itemIx >= child.getItemCount() - 1)
								itemIx = child.getItemCount() - 1;
							child.setSelection(itemIx);
						}
						// *** enable or disable items
						enableDisableItems();
					}
					
					@Override
					public void mouseUp(MouseEvent e){}
				});
				child.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e){
						if (e.keyCode == 0x6b) {
							// CTRL + K #6105 move cost bearer from extinfo to table
							moveCostBearerFromExtInfoToDBRow(child);
						}
					}
					
					// #6105
					private void moveCostBearerFromExtInfoToDBRow(
						org.eclipse.swt.widgets.List child){
						String[] selection = child.getSelection();
						if (selection != null && selection.length == 1) {
							String[] fields = selection[0].split(ARGUMENTSSDELIMITER);
							String fieldType = fields[0];
							final String fieldName =
								(StringUtils.isNotBlank(fields[1])) ? fields[1].trim() : null;
							// contains e.g. Kontakt:  Kostenträger\
							if ("K".equals(fieldType.substring(0, 1)) && fieldName != null
								&& getData() != null) {
								String message = MessageFormat.format(
									"Move the selected field [{0}] to cost bearer table for billing systems [{1}]?",
									fieldName, (String) getData());
								boolean performMove = MessageDialog.openQuestion(
									UiDesk.getTopShell(), "Move to cost bearer table", message);
								if (performMove) {
									BusyIndicator.showWhile(UiDesk.getDisplay(), () -> {
										BillingSystem.moveCostBearerFromExtinfoToDBRow(
											(String) getData(), fieldName);
										deleteListItem();
									});
								}
							}
						}
					}
					
					@Override
					public void keyReleased(KeyEvent e){}
				});
			}
		}
		
		/**
		 * adds a button/toolItem to the bottom of the navigator
		 * 
		 * @param parent
		 *            the toolBar/navigator
		 * @param image
		 *            the image to be used
		 * @param toolTipText
		 *            the tooltip to be shown
		 * @param actionType
		 *            the action type (MOVEITEMUP, MOVEITEMDOWN, DELETEITEM, EDITITEM,
		 *            LASTFIXEDITEM, ...)
		 * @param enabled
		 *            enabled or disabled...
		 * @since 3.0.0
		 */
		private void addToolItem(ToolBar parent, Image image, String toolTipText, int actionType,
			boolean enabled){
			if (actionType == -1) {
				ToolItem toolItem = new ToolItem(parent, SWT.SEPARATOR);
				toolItem.setEnabled(false);
			} else {
				ImageRegistry imageRegistry = UiDesk.getImageRegistry();
				ToolItem toolItem = new ToolItem(parent, SWT.PUSH);
				toolItem.setToolTipText(toolTipText);
				toolItem.setImage(image);
				toolItem.setData("type", actionType); //$NON-NLS-1$
				toolItem.addSelectionListener(new toolBarSelectionListener());
				toolItem.setEnabled(enabled);
			}
		}
		
		/**
		 * enable or disable the items in the toolbar and in the popupmenu
		 * 
		 * @param listDisplayToBeChanged
		 */
		private void enableDisableItems(){
			ToolBar toolBar = (ToolBar) listDisplay.getData("toolbar"); //$NON-NLS-1$
			org.eclipse.swt.widgets.List list = getListPart(listDisplay);
			
			Menu menu = list.getMenu();
			boolean menuHasItems = menu.getItems().length > 0;
			
			int selIx = list.getSelectionIndex();
			int maxIx = list.getItemCount();
			// *** move up item
			boolean enabled = false;
			if (selIx > 0)
				enabled = true;
			toolBar.getItem(MOVEITEMUP).setEnabled(enabled);
			if (menuHasItems)
				menu.getItem(MOVEITEMUP).setEnabled(enabled);
			// *** move down item
			enabled = false;
			if (selIx < (maxIx - 1))
				enabled = true;
			if (selIx == -1)
				enabled = false;
			toolBar.getItem(MOVEITEMDOWN).setEnabled(enabled);
			if (menuHasItems)
				menu.getItem(MOVEITEMDOWN).setEnabled(enabled);
			// *** delete and edit item
			enabled = true;
			if (selIx == -1)
				enabled = false;
			toolBar.getItem(DELETEITEM).setEnabled(enabled);
			toolBar.getItem(EDITITEM).setEnabled(enabled);
			if (menuHasItems)
				menu.getItem(DELETEITEM).setEnabled(enabled);
			if (menuHasItems)
				menu.getItem(EDITITEM).setEnabled(enabled);
		}
		
		/**
		 * find the list of ListDisplay composite - ugly but working find...
		 * 
		 * @param listDisplay
		 * @return
		 */
		private org.eclipse.swt.widgets.List getListPart(ListDisplay<String> listDisplay){
			Control[] children = listDisplay.getChildren();
			for (int li = 0; li < children.length; li++) {
				Control child = children[li];
				if (child.getClass().toString()
					.equalsIgnoreCase("class org.eclipse.swt.widgets.List")) { //$NON-NLS-1$
					return (org.eclipse.swt.widgets.List) child;
				}
			}
			return null;
		}
		
		/**
		 * change the type of an item
		 */
		public void changeItemType(){
			if (listDisplay.getSelection() == null) {
				SWTHelper.alert(StringTool.leer, Messages.Leistungscodes_mustSelectALine);
			} else {}
		}
		
		/**
		 * move an item up or down in the list
		 * 
		 * @param step
		 *            positive for moving down, negative for moving up
		 */
		public void moveListItem(int step){
			if (listDisplay.getSelection() == null) {
				SWTHelper.alert(StringTool.leer, Messages.Leistungscodes_mustSelectALine);
			} else {
				if (step == 0)
					return;
				String sel = listDisplay.getSelection();
				List<String> allRequirements = listDisplay.getAll();
				int selIx = allRequirements.indexOf(sel);
				int listSize = allRequirements.size();
				boolean conditionOk = false;
				if (step < 0) {
					if (selIx > (-step) - 1)
						conditionOk = true;
				}
				if (step > 0) {
					if (selIx <= listSize - step)
						conditionOk = true;
				}
				if (conditionOk) {
					allRequirements.set(selIx, allRequirements.get(selIx + step));
					allRequirements.set(selIx + step, sel);
					Object[] tmp = allRequirements.toArray();
					for (int i = 0; i < listSize; i++) {
						listDisplay.remove(allRequirements.get(0));
					}
					for (int i = 0; i < tmp.length; i++) {
						listDisplay.add((String) tmp[i]);
					}
					listDisplay.setSelection(selIx + step);
				}
			}
		}
		
		/**
		 * delete an item from the list, move to deletedList. <br>
		 * if this IS the deletedList, then delete definitively
		 */
		public void deleteListItem(){
			String sel = listDisplay.getSelection();
			if (sel == null) {
				SWTHelper.alert(StringTool.leer, Messages.Leistungscodes_mustSelectALine);
			} else {
				if (SWTHelper.askYesNo(StringTool.leer,
					Messages.Leistungscodes_reallyWantToDeleteItem)) {
					if ((deletedList != null) && (!deletedList.equals(listDisplay))) {
						deletedList.add(sel);
					}
					listDisplay.remove(sel);
				}
			}
		}
		
		/**
		 * selection listener for the toolbar/navigator
		 * 
		 * @author Harald Marlovits
		 * 
		 */
		class toolBarSelectionListener implements SelectionListener {
			@Override
			public void widgetSelected(SelectionEvent e){
				int type = (Integer) (((ToolItem) (e.getSource())).getData("type")); //$NON-NLS-1$
				
				switch (type) {
				case MOVEITEMUP:
					moveListItem(-1);
					break;
				case MOVEITEMDOWN:
					moveListItem(+1);
					break;
				case DELETEITEM:
					deleteListItem();
					break;
				case EDITITEM:
					String sel = listDisplay.getSelection();
					_FieldsHyperlinkActivated(StringTool.leer, sel);
					break;
				default:
					if (type > LASTFIXEDITEM) {
						moveItemToOtherList(listDisplay,
							moveTo_DestinationLists.get(type - LASTFIXEDITEM - 1),
							noDuplicatesList);
					}
					break;
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
			
		}
		
		/**
		 * selection listener for the popupmenu
		 * 
		 * @author Harald Marlovits
		 * 
		 */
		class ListPopUpMenuAction extends Action {
			int actionType = 0;
			
			/**
			 * 
			 * @param actionName
			 * @param imageDescriptor
			 * @param toolTipText
			 * @param actionType
			 * @param listDisplay
			 * @param enabled
			 * @since 3.0.0 constructor signature changed to ImageDescriptor
			 */
			public ListPopUpMenuAction(String actionName, ImageDescriptor imageDescriptor,
				String toolTipText, int actionType, ListDisplay<String> listDisplay,
				boolean enabled){
				super(actionName);
				this.actionType = actionType;
				setImageDescriptor(imageDescriptor);
				setToolTipText(toolTipText);
				this.setEnabled(enabled);
			}
			
			@Override
			public void run(){
				// this.setEnabled(false);
				switch (actionType) {
				case MOVEITEMUP:
					moveListItem(-1);
					break;
				case MOVEITEMDOWN:
					moveListItem(+1);
					break;
				case DELETEITEM:
					deleteListItem();
					break;
				case EDITITEM:
					String sel = listDisplay.getSelection();
					_FieldsHyperlinkActivated(StringTool.leer, sel);
					break;
				default:
					if (actionType > LASTFIXEDITEM) {
						moveItemToOtherList(listDisplay,
							moveTo_DestinationLists.get(actionType - LASTFIXEDITEM - 1),
							noDuplicatesList);
					}
					break;
				}
			}
		};
		
		/**
		 * return the label for the supplied object <br>
		 * the object is the String in the form <fieldName>:<fieldType>:<itemsList>
		 * 
		 * @param o
		 * @return
		 */
		public String _FieldsGetLabel(final Object o){
			String[] l = ((String) o).split(ARGUMENTSSDELIMITER); //$NON-NLS-1$
			if (l.length > 1) {
				String type = Messages.Leistungscodes_date;
				if (l[1].equals("T")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_text;
				} else if (l[1].equals("K")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_contact;
				} else if (l[1].equals("D")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_date;
				} else if (l[1].equals("TM")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_textMultipleLines;
				} else if (l[1].equals("TS")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_textStyled;
				} else if (l[1].equals("CS")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_combo;
				} else if (l[1].equals("CN")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_comboNumeric;
				} else if (l[1].equals("LS")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_list;
				} else if (l[1].equals("LN")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_ListNumeric;
				} else if (l[1].equals("X")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_checkbox;
				} else if (l[1].equals("RS")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_radiogroup;
				} else if (l[1].equals("RN")) { //$NON-NLS-1$
					type = Messages.Leistungscodes_radiogroupNumeric;
				}
				String opt = (l.length >= 3) ? "   (" + l[2].replaceAll(ITEMDELIMITER, "; ") + ")" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						: StringTool.leer;
				if (opt.trim().equalsIgnoreCase("(SQL)")) { //$NON-NLS-1$
					opt = (l.length >= 4) ? "   (SQL: " + l[3].replaceAll(ITEMDELIMITER, "; ") + ")" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
							: StringTool.leer;
					return type + " " + l[0] + opt; //$NON-NLS-1$
				} else {
					return type + " " + l[0] + opt; //$NON-NLS-1$
				}
			} else {
				String opt = (l.length >= 3) ? "   (" + l[2].replaceAll(ITEMDELIMITER, "; ") + ")" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						: StringTool.leer;
				return "? " + " " + l[0] + opt; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		/**
		 * 
		 * @param l
		 *            if creating a field, then this contains the field type name
		 * @param fieldToBeChanged
		 *            if changing a field, then this contains the full definition for the field to
		 *            be changed [fieldName]:[fieldType]:[itemsList]
		 */
		public void _FieldsHyperlinkActivated(final String l, String fieldToBeChanged){
			String ll = l;
			boolean isChanging = false;
			String fieldName = StringTool.leer;
			String fieldType = StringTool.leer;
			String optionsIn = StringTool.leer;
			boolean isNumericChecked = false;
			boolean isStyledChecked = false;
			boolean isMultilineChecked = false;
			if (ll.isEmpty()) {
				isChanging = true;
				String[] fields = fieldToBeChanged.split(ARGUMENTSSDELIMITER);
				fieldName = fields[0];
				fieldType = fields[1];
				optionsIn = fields.length > 2 ? fields[2] : StringTool.leer;
				optionsIn = optionsIn.replaceAll(ITEMDELIMITER, "\n"); //$NON-NLS-1$
				
				if (fieldType.equalsIgnoreCase("K")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_contactHL;
				} else if (fieldType.substring(0, 1).equalsIgnoreCase("T")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_textHL;
				} else if (fieldType.substring(0, 1).equalsIgnoreCase("D")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_dateHL;
				} else if (fieldType.substring(0, 1).equalsIgnoreCase("C")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_comboHL;
				} else if (fieldType.substring(0, 1).equalsIgnoreCase("L")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_listHL;
				} else if (fieldType.substring(0, 1).equalsIgnoreCase("X")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_checkboxHL;
				} else if (fieldType.substring(0, 1).equalsIgnoreCase("R")) { //$NON-NLS-1$
					ll = Messages.Leistungscodes_radioHL;
				}
				if (fieldType.length() >= 2) {
					if (fieldType.equalsIgnoreCase("TM")) { //$NON-NLS-1$
						isMultilineChecked = true;
					} else if (fieldType.equalsIgnoreCase("TS")) { //$NON-NLS-1$
						isStyledChecked = true;
					} else if (fieldType.substring(1, 2).equalsIgnoreCase("N")) { //$NON-NLS-1$
						isNumericChecked = true;
					}
				}
			}
			String msg = Messages.Leistungscodes_pleaseEnterName;
			String[] changeItems = {
				Messages.Leistungscodes_contactHL, Messages.Leistungscodes_textHL,
				Messages.Leistungscodes_dateHL, Messages.Leistungscodes_comboHL,
				Messages.Leistungscodes_listHL, Messages.Leistungscodes_checkboxHL,
				Messages.Leistungscodes_radioHL
			};
			AbrechnungsTypDialog_InputDialog inputDlg =
				new AbrechnungsTypDialog_InputDialog(getShell(),
					ll + (isChanging ? Messages.Leistungscodes_changeTextInTitleBar
							: Messages.Leistungscodes_add),
					msg, fieldName, noDuplicatesListCreate, optionsIn, isNumericChecked,
					isStyledChecked, isMultilineChecked, isChanging ? changeItems : null);
			if (inputDlg.open() == Dialog.OK) {
				String[] result = inputDlg.getResult();
				String req = result[0];
				String options = result[1];
				ll = result[5];
				// *** if options contains an sql-statement, then read from db
				if ((options.length() > 4) && (options.substring(0, 4).equalsIgnoreCase("SQL:"))) { //$NON-NLS-1$
					// *** this is calculated on the fly in the view
					// FallDetailBlatt2
				}
				if ((options.length() > 4)
					&& (options.substring(0, 4).equalsIgnoreCase("SCRIPT:"))) { //$NON-NLS-1$
					// *** this is calculated on the fly in the view
					// FallDetailBlatt2
				}
				boolean hasNumeric = result[2].equalsIgnoreCase("0") ? false : true; //$NON-NLS-1$
				boolean hasMultiline = result[3].equalsIgnoreCase("0") ? false : true; //$NON-NLS-1$
				boolean hasStyled = result[4].equalsIgnoreCase("0") ? false : true; //$NON-NLS-1$
				if (ll.equalsIgnoreCase(Messages.Leistungscodes_contactHL)) {
					req += ":K"; // Kontakt //$NON-NLS-1$
				} else if (ll.equalsIgnoreCase(Messages.Leistungscodes_textHL)) {
					if (hasStyled) {
						req += ":TS"; // Styled Text //$NON-NLS-1$
					} else if (hasMultiline) {
						req += ":TM"; // Multiline Text //$NON-NLS-1$
					} else {
						req += ":T"; // normal Text //$NON-NLS-1$
					}
				} else if (ll.equalsIgnoreCase(Messages.Leistungscodes_dateHL)) {
					req += ":D"; // Date //$NON-NLS-1$
				} else if (ll.equalsIgnoreCase(Messages.Leistungscodes_comboHL)) {
					if (hasNumeric) {
						req += ":CN"; // Combo numeric //$NON-NLS-1$
					} else {
						req += ":CS"; // Combo //$NON-NLS-1$
					}
				} else if (ll.equalsIgnoreCase(Messages.Leistungscodes_listHL)) {
					if (hasNumeric) {
						req += ":LN"; // List numeric //$NON-NLS-1$
					} else {
						req += ":LS"; // List //$NON-NLS-1$
					}
				} else if (ll.equalsIgnoreCase(Messages.Leistungscodes_checkboxHL)) {
					req += ":X"; // Checkboxes  //$NON-NLS-1$
				} else if (ll.equalsIgnoreCase(Messages.Leistungscodes_radioHL)) {
					if (hasNumeric) {
						req += ":RN"; // Radiobuttons numeric //$NON-NLS-1$
					} else {
						req += ":RS"; // Radiobuttons //$NON-NLS-1$
					}
				}
				
				// *** append options as ;-delimited list
				options = options.replaceAll("\r\n", ITEMDELIMITER); //$NON-NLS-1$
				options = (options.replaceAll("\n", ITEMDELIMITER)).replaceAll("\r", ITEMDELIMITER); //$NON-NLS-1$ //$NON-NLS-2$
				req = req + ARGUMENTSSDELIMITER + options;
				
				// *** return result
				if (isChanging) {
					List<String> allRequirements = listDisplay.getAll();
					int selIx = allRequirements.indexOf(fieldToBeChanged);
					if (selIx >= 0) {
						Object[] tmp = allRequirements.toArray();
						int listSize = allRequirements.size();
						List<String> currRequirements = listDisplay.getAll();
						for (int i = 0; i < listSize; i++) {
							listDisplay.remove(currRequirements.get(0));
						}
						for (int i = 0; i < tmp.length; i++) {
							String toBeAdded = (String) tmp[i];
							if (i == selIx)
								toBeAdded = req;
							listDisplay.add(toBeAdded);
						}
					}
				} else {
					listDisplay.add(req);
				}
			}
		}
		
		/**
		 * moving items from one list to another list the moved item MUST NOT be in the destination
		 * list - will not be allowed the moved item MUST NOT be in one of the noDuplicatesDisplays
		 * list - will not be allowed
		 * 
		 * @param listDisplay
		 *            the current list from which the item should be moved away
		 * @param destDisplay
		 *            the destination list to which the item should be moved to
		 * @param noDuplicatesDisplays
		 *            an array of ListDisplay's which contain the values which should not be
		 *            duplicated
		 */
		@SuppressWarnings("unchecked")
		public void moveItemToOtherList(ListDisplay<String> listDisplay,
			ListDisplay<String> destDisplay, ListDisplay<String>... noDuplicatesDisplays){
			// no duplicates allowed for destination list
			String sel = listDisplay.getSelection();
			if (sel.isEmpty())
				return;
			java.util.ArrayList<String> allItems =
				(ArrayList<String>) ((ArrayList<String>) destDisplay.getAll()).clone();
			for (int li = 0; li < allItems.size(); li++) {
				allItems.set(li, allItems.get(li).split(ARGUMENTSSDELIMITER)[0]);
			}
			if (allItems.contains(sel.split(ARGUMENTSSDELIMITER)[0])) {
				SWTHelper.alert(StringTool.leer,
					Messages.Leistungscodes_definitionAlreadyExistsInDestination);
				return;
			}
			
			// *** no duplicates in noDuplicatesDisplays
			for (ListDisplay<String> noDuplicatesList : noDuplicatesDisplays) {
				java.util.ArrayList<String> allItems2 =
					(ArrayList<String>) ((ArrayList<String>) noDuplicatesList.getAll()).clone();
				for (int li = 0; li < allItems2.size(); li++) {
					allItems2.set(li, allItems2.get(li).split(ARGUMENTSSDELIMITER)[0]);
				}
				if (allItems2.contains(sel.split(ARGUMENTSSDELIMITER)[0])) {
					SWTHelper.alert(StringTool.leer,
						Messages.Leistungscodes_definitionAlreadyExistsSomewhere);
					return;
				}
			}
			
			// *** moving is ok
			destDisplay.add(sel);
			listDisplay.remove(sel);
		}
	}
	
}
