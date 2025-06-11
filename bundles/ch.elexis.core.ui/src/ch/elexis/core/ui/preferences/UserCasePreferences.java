/*******************************************************************************
 * Copyright (c) 2007-2009, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *
 *  $Id: UserCasePreferences.java 5320 2009-05-27 16:51:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.holder.BillingSystemServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.DiagnoseSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.BillingSystemColorHelper;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.rgw.io.InMemorySettings;
import ch.rgw.tools.StringTool;

/**
 * User specific settings: Case defaults
 */
public class UserCasePreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "ch.elexis.preferences.UserCasePreferences"; //$NON-NLS-1$
	public static final String MENUSEPARATOR = "------------------------------------"; //$NON-NLS-1$
	private static final String PREFSDELIMITER = "`^"; //$NON-NLS-1$
	private static final String PREFSDELIMITER_REGEX = "\\`\\^"; //$NON-NLS-1$
	public static final String USR_AUTOMATIC_STAMMARZT_MANDANT = "usr/automaticStammarztMandant";

	Text diagnoseTxt;
	TableViewer sorterList2Viewer;
	Button btnToManual;
	Button btnToNotPresorted;
	Button btnUp;
	Button btnDown;

	LinkedList<String> topItemsLinkedList = new LinkedList<>();


	public UserCasePreferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(new InMemorySettings()));
		setDescription(Messages.UserCasePreferences_Cases);
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(Preferences.USR_DEFCASELABEL, Messages.UserCasePreferences_DefaultName,
				getFieldEditorParent()));
		addField(new StringFieldEditor(Preferences.USR_DEFCASEREASON, Messages.UserCasePreferences_DefaultReason,
				getFieldEditorParent()));
		addField(new StringFieldEditor(Preferences.USR_DEFLAW, Messages.UserCasePreferences_DefaultBillingSystem,
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		getPreferenceStore().setValue(Preferences.USR_DEFCASELABEL, Fall.getDefaultCaseLabel());
		getPreferenceStore().setValue(Preferences.USR_DEFCASEREASON, Fall.getDefaultCaseReason());
		getPreferenceStore().setValue(Preferences.USR_DEFLAW, Fall.getDefaultCaseLaw());
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = ConfigServiceHolder.getUser(Preferences.USR_TOPITEMSSORTING, StringUtils.EMPTY);
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		topItemsLinkedList = new LinkedList<>(Arrays.asList(topItemsSorting));
	}

	@Override
	public boolean performOk() {
		super.performOk();

		ConfigServiceHolder.setUser(Preferences.USR_DEFCASELABEL,
				getPreferenceStore().getString(Preferences.USR_DEFCASELABEL));
		ConfigServiceHolder.setUser(Preferences.USR_DEFCASEREASON,
				getPreferenceStore().getString(Preferences.USR_DEFCASEREASON));
		ConfigServiceHolder.setUser(Preferences.USR_DEFLAW, getPreferenceStore().getString(Preferences.USR_DEFLAW));
		// convert LinkedList for the sorting to comma delimited list and save to prefs
		String[] topItemsSorting = new String[topItemsLinkedList.size()];
		topItemsLinkedList.toArray(topItemsSorting);
		String topItemsSortingStr = StringTool.join(topItemsSorting, PREFSDELIMITER);
		ConfigServiceHolder.setUser(Preferences.USR_TOPITEMSSORTING, topItemsSortingStr);
		return true;
	}

	@Override
	protected Control createContents(Composite parent) {
		// create the field editors by calling super
		Control suParent = super.createContents(parent);
		// create a composite for selecting the default diagnose
		Composite diagnoseParent = new Composite((Composite) suParent, SWT.NULL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		diagnoseParent.setLayoutData(gd);

		diagnoseParent.setLayout(new FormLayout());
		Label diagnoseLbl = new Label(diagnoseParent, SWT.NONE);
		diagnoseLbl.setText(Messages.UserCasePreferences_DefaultDiagnose);
		diagnoseTxt = new Text(diagnoseParent, SWT.BORDER);
		diagnoseTxt.setEditable(false);
		String diagnoseId = ConfigServiceHolder.getUser(Preferences.USR_DEFDIAGNOSE, StringUtils.EMPTY);
		if (diagnoseId.length() > 1) {
			Identifiable diagnose = StoreToStringServiceHolder.get().loadFromString(diagnoseId).orElse(null);
			if (diagnose != null)
				diagnoseTxt.setText(diagnose.getLabel());
		}
		Button diagnoseBtn = new Button(diagnoseParent, SWT.PUSH);
		diagnoseBtn.setText("Diagnose"); //$NON-NLS-1$
		diagnoseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiagnoseSelektor dsl = new DiagnoseSelektor(getShell());
				if (dsl.open() == Dialog.OK) {
					Object[] sel = dsl.getResult();
					if (sel != null && sel.length > 0) {
						Identifiable diagnose = (Identifiable) sel[0];
						String storeToString = StoreToStringServiceHolder.getStoreToString(diagnose);
						ConfigServiceHolder.setUser(Preferences.USR_DEFDIAGNOSE, storeToString);
						diagnoseTxt.setText(diagnose.getLabel());
					} else {
						ConfigServiceHolder.setUser(Preferences.USR_DEFDIAGNOSE, StringUtils.EMPTY);
						diagnoseTxt.setText(StringUtils.EMPTY);
					}
				}
			}
		});
		Button diagnoseDelBtn = new Button(diagnoseParent, SWT.PUSH);
		diagnoseDelBtn.setImage(Images.IMG_DELETE.getImage());
		diagnoseDelBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfigServiceHolder.setUser(Preferences.USR_DEFDIAGNOSE, StringUtils.EMPTY);
				diagnoseTxt.setText(StringUtils.EMPTY);
			}
		});

		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 0);
		diagnoseLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(diagnoseLbl, 0, SWT.CENTER);
		fd.left = new FormAttachment(diagnoseLbl, 5);
		fd.right = new FormAttachment(diagnoseBtn, -5);
		diagnoseTxt.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(diagnoseLbl, 0, SWT.CENTER);
		fd.right = new FormAttachment(diagnoseDelBtn, -5);
		diagnoseBtn.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(diagnoseLbl, 0, SWT.CENTER);
		fd.right = new FormAttachment(100, -5);
		diagnoseDelBtn.setLayoutData(fd);

		Composite lastConsParent = new Composite((Composite) suParent, SWT.NULL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		lastConsParent.setLayoutData(gd);

		lastConsParent.setLayout(new FormLayout());
		Label lastConsLbl = new Label(lastConsParent, SWT.NONE);
		lastConsLbl.setText(Messages.UserCasePreferences_LoadConsultationAll);

		final Button lastConsBtn = new Button(lastConsParent, SWT.CHECK);
		lastConsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lastConsBtn.getSelection()) {
					ConfigServiceHolder.setUser(Preferences.USR_DEFLOADCONSALL, true);
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_DEFLOADCONSALL, false);
				}
			}
		});
		lastConsBtn.setSelection(ConfigServiceHolder.getUser(Preferences.USR_DEFLOADCONSALL, false));

		Composite stammarztAutoParent = new Composite((Composite) suParent, SWT.NULL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		stammarztAutoParent.setLayoutData(gd);

		stammarztAutoParent.setLayout(new FormLayout());
		Label stammarztAutoLbl = new Label(stammarztAutoParent, SWT.NONE);
		stammarztAutoLbl.setText(Messages.UserCasePreferences_StammarztAutoMandant);

		final Button stammarztAutoBtn = new Button(stammarztAutoParent, SWT.CHECK);
		stammarztAutoBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfigServiceHolder.setUser(Preferences.USR_AUTOMATIC_STAMMARZT_MANDANT,
						stammarztAutoBtn.getSelection());
			}
		});
		stammarztAutoBtn.setSelection(ConfigServiceHolder.getUser(Preferences.USR_AUTOMATIC_STAMMARZT_MANDANT, false));

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 0);
		stammarztAutoLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		stammarztAutoBtn.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 0);
		lastConsLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		lastConsBtn.setLayoutData(fd);

		// ********* section for specifying sorting of billing system
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));

		Label infoTxt = new Label(getFieldEditorParent(), SWT.NONE);
		infoTxt.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		infoTxt.setText(Messages.UserCasePreferences_InfoLabelForSortingBillingSystems);

		Label lllabel2 = new Label(getFieldEditorParent(), SWT.NONE);
		lllabel2.setText(StringUtils.EMPTY);
		Composite sorterListComp = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout sorterListLayout = new GridLayout();
		sorterListLayout.marginWidth = 0;
		sorterListLayout.marginHeight = 0;
		sorterListLayout.numColumns = 2;
		sorterListComp.setLayout(sorterListLayout);

		sorterList2Viewer = new TableViewer(sorterListComp, SWT.BORDER | SWT.FULL_SELECTION);
		Table sorterTable = sorterList2Viewer.getTable();
		sorterTable.setHeaderVisible(false);
		sorterTable.setLinesVisible(true);

		TableColumn nameCol = new TableColumn(sorterTable, SWT.LEFT);
		nameCol.setWidth(180);
		sorterList2Viewer.setContentProvider(ArrayContentProvider.getInstance());
		sorterList2Viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return element == null ? StringUtils.EMPTY : ((IBillingSystem) element).getName();
			}

			@Override
			public Color getBackground(Object element) {
				IBillingSystem bs = (IBillingSystem) element;
				if (bs instanceof SeparatorIBillingSystem) {
					return sorterList2Viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_WHITE);
				}
				String hex = BillingSystemColorHelper.getMixedHexColorForBillingSystem(bs.getName(), 80);
				return UiDesk.getColorFromRGB(hex);
			}
		});

		sorterList2Viewer.getTable().addListener(SWT.MouseDoubleClick, event -> {
			Table table = sorterList2Viewer.getTable();
			Point pt = new Point(event.x, event.y);
			TableItem item = table.getItem(pt);
			if (item != null) {
				String name = item.getText(0);
				if (!UserCasePreferences.MENUSEPARATOR.equals(name)) {
					ColorDialog dlg = new ColorDialog(table.getShell());
					org.eclipse.swt.graphics.RGB rgb = dlg.open();
					if (rgb != null) {
						String value = rgb.red + "," + rgb.green + "," + rgb.blue;
						ConfigServiceHolder.get().set("billingSystemColor_" + name, value);
						sorterList2Viewer.setSelection(new StructuredSelection());
						sorterList2Viewer.refresh();
					}
				}
			}
		});

		setupTableWithColors();

		sorterTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setButtonEnabling();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite sorterListCompButtons = new Composite(sorterListComp, SWT.BORDER);
		GridLayout sorterListCompButtonsLayout = new GridLayout();
		sorterListLayout.marginWidth = 0;
		sorterListLayout.marginHeight = 0;
		sorterListLayout.horizontalSpacing = 0;
		sorterListLayout.verticalSpacing = 0;
		sorterListLayout.marginTop = 0;
		sorterListLayout.marginBottom = 0;
		sorterListLayout.marginLeft = 0;
		sorterListLayout.marginRight = 0;
		sorterListCompButtons.setLayout(sorterListCompButtonsLayout);

		GridData sorterGridData = new GridData();
		sorterGridData.verticalAlignment = SWT.TOP;
		sorterGridData.horizontalSpan = 0;
		sorterGridData.grabExcessVerticalSpace = true;
		sorterListCompButtons.setLayoutData(sorterGridData);

		btnUp = new Button(sorterListCompButtons, SWT.NONE);
		btnUp.setToolTipText(Messages.UserCasePreferences_MoveItemUpInList);
		btnUp.setImage(Images.IMG_ARROWUP.getImage());
		btnUp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveItemUpInPresorted();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnDown = new Button(sorterListCompButtons, SWT.NONE);
		btnDown.setToolTipText(Messages.UserCasePreferences_MoveItemDownInList);
		btnDown.setImage(Images.IMG_ARROWDOWN.getImage());
		btnDown.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveItemDownInPresorted();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnToManual = new Button(sorterListCompButtons, SWT.NONE);
		btnToManual.setToolTipText(Messages.UserCasePreferences_MoveItemToManualSortedList);
		btnToManual.setImage(Images.IMG_MOVETOLOWERLIST.getImage());
		btnToManual.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveItemToPresorted();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnToNotPresorted = new Button(sorterListCompButtons, SWT.NONE);
		btnToNotPresorted.setToolTipText(Messages.UserCasePreferences_MoveItemToAlphabeticallySortedList);
		btnToNotPresorted.setImage(Images.IMG_ARROWDOWNTORECT.getImage());
		btnToNotPresorted.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveItemToNotPresorted();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		setButtonEnabling();

		return suParent;
	}

	private void setupTableWithColors() {
		List<IBillingSystem> allSystems = BillingSystemServiceHolder.get().getBillingSystems();
		List<IBillingSystem> sortedList = new LinkedList<>();
		List<IBillingSystem> alphabeticallySortedList = new LinkedList<>();
		for (String topName : topItemsLinkedList) {
			allSystems.stream().filter(bs -> bs.getName().equals(topName)).findFirst().ifPresent(sortedList::add);
		}
		SeparatorIBillingSystem separator = new SeparatorIBillingSystem();
		sortedList.add(separator);
		for (IBillingSystem bs : allSystems) {
			if (!topItemsLinkedList.contains(bs.getName())) {
				alphabeticallySortedList.add(bs);
			}
		}
		alphabeticallySortedList.sort(Comparator.comparing(IBillingSystem::getName, String.CASE_INSENSITIVE_ORDER));
		sortedList.addAll(alphabeticallySortedList);
		sorterList2Viewer.setInput(sortedList);
		sorterList2Viewer.setComparator(billingSystemComparator);
	}

	void moveItemToPresorted() {
		IStructuredSelection sel = sorterList2Viewer.getStructuredSelection();
		if (sel.isEmpty())
			return;
		IBillingSystem selected = (IBillingSystem) sel.getFirstElement();
		String selName = selected.getName();
		topItemsLinkedList.add(selName);
		topItemsLinkedList.remove(StringUtils.EMPTY);
		setupTableWithColors();
		sorterList2Viewer.setSelection(new StructuredSelection(selected));
		setButtonEnabling();
	}

	void moveItemToNotPresorted() {
		IStructuredSelection sel = sorterList2Viewer.getStructuredSelection();
		if (sel.isEmpty())
			return;
		IBillingSystem selected = (IBillingSystem) sel.getFirstElement();
		String selName = selected.getName();
		topItemsLinkedList.remove(selName);
		topItemsLinkedList.remove(StringUtils.EMPTY);
		setupTableWithColors();
		sorterList2Viewer.setSelection(new StructuredSelection(selected));
		setButtonEnabling();
	}
	void moveItemUpInPresorted() {
		moveItemInPresorted(-1);
	}

	void moveItemDownInPresorted() {
		moveItemInPresorted(+1);
	}

	void moveItemInPresorted(int step) {
		IStructuredSelection sel = sorterList2Viewer.getStructuredSelection();
		if (sel.isEmpty())
			return;
		IBillingSystem selected = (IBillingSystem) sel.getFirstElement();
		String selName = selected.getName();
		int selIx = topItemsLinkedList.indexOf(selName);
		if (selIx < 0)
			return;
		int newIx = selIx + step;
		if (newIx < 0 || newIx >= topItemsLinkedList.size())
			return;
		topItemsLinkedList.remove(selIx);
		topItemsLinkedList.add(newIx, selName);
		topItemsLinkedList.remove(StringConstants.EMPTY);
		setupTableWithColors();
		sorterList2Viewer.setSelection(new StructuredSelection(selected));
		setButtonEnabling();
	}

	void setButtonEnabling() {
		IStructuredSelection sel = sorterList2Viewer.getStructuredSelection();
		if (sel.isEmpty()) {
			btnToManual.setEnabled(false);
			btnToNotPresorted.setEnabled(false);
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
			return;
		}
		IBillingSystem selected = (IBillingSystem) sel.getFirstElement();
		String selName = selected.getName();
		int selIx = topItemsLinkedList.indexOf(selName);
		if (selected instanceof SeparatorIBillingSystem) {
			btnToManual.setEnabled(false);
			btnToNotPresorted.setEnabled(false);
		} else if (selIx >= 0) {
			btnToManual.setEnabled(false);
			btnToNotPresorted.setEnabled(true);
		} else {
			btnToManual.setEnabled(true);
			btnToNotPresorted.setEnabled(false);
		}
		btnUp.setEnabled(selIx > 0);
		btnDown.setEnabled(selIx >= 0 && selIx < topItemsLinkedList.size() - 1);
	}


	public static int getBillingSystemsMenuSeparatorPos(String[] input) {
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = ConfigServiceHolder.getUser(Preferences.USR_TOPITEMSSORTING, StringUtils.EMPTY);
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		LinkedList<String> lTopItemsLinkedList = new LinkedList<>(Arrays.asList(topItemsSorting));
		if ((!lTopItemsLinkedList.isEmpty()) && (!lTopItemsLinkedList.get(0).equalsIgnoreCase(StringUtils.EMPTY)))
			return lTopItemsLinkedList.size();
		else
			return -1;

	}

	/**
	 * sort the input: start with sorting as specified in topItemsSorting found in
	 * the user prefs, add a separator and then sort the rest alphabetically
	 *
	 * @param input String[] of all billing systems, unsorted
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input) {
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = ConfigServiceHolder.getUser(Preferences.USR_TOPITEMSSORTING, StringUtils.EMPTY);
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		LinkedList<String> lTopItemsLinkedList = new LinkedList<>(Arrays.asList(topItemsSorting));
		return sortBillingSystems(input, lTopItemsLinkedList);
	}

	/**
	 * sort the input: start with sorting as specified in parameter topItemsSorting,
	 * add a separator if there are topitems and then sort the rest alphabetically
	 *
	 * @param input           String[] of all billing systems, unsorted
	 * @param topItemsSorting LinkedList<String> Array of billing systems in the
	 *                        order they should appear in the menu/combo
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input, LinkedList<String> topItemsSorting) {
		return sortBillingSystems(input, topItemsSorting, false);
	}

	/**
	 * sort the input: start with sorting as specified in parameter topItemsSorting,
	 * always add a separator and then sort the rest alphabetically
	 *
	 * @param input               String[] of all billing systems, unsorted
	 * @param topItemsSorting     LinkedList<String> Array of billing systems in the
	 *                            order they should appear in the menu/combo
	 * @param alwaysShowSeparator boolean should the separator also be shown when NO
	 *                            topItems are present
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input, LinkedList<String> topItemsSorting,
			boolean alwaysShowSeparator) {
		// create a copy of topItemsSorting - we append the other items to the end later
		LinkedList<String> lTopItemsSorting = (LinkedList<String>) topItemsSorting.clone();

		// create sorted list for the other items
		SortedList<String> sortedList = new SortedList<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return (o1.compareTo(o2));
			}
		});
		for (int i = 0; i < input.length; i++) {
			String item = input[i];
			if (!lTopItemsSorting.contains(item))
				sortedList.add(item);
		}

		// now append the sorted items to the copied top items
		if (alwaysShowSeparator
				|| ((!topItemsSorting.isEmpty()) && (!topItemsSorting.get(0).equalsIgnoreCase(StringUtils.EMPTY)))) {
			lTopItemsSorting.add(MENUSEPARATOR);
		}
		lTopItemsSorting.addAll(sortedList);
		lTopItemsSorting.remove(StringUtils.EMPTY);

		String[] output = new String[lTopItemsSorting.size()];
		lTopItemsSorting.toArray(output);

		return output;
	}

	@Override
	protected void performDefaults() {
		this.initialize();
	}

	private ViewerComparator billingSystemComparator = new ViewerComparator() {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			IBillingSystem bs1 = (IBillingSystem) e1;
			IBillingSystem bs2 = (IBillingSystem) e2;
			int pos1 = topItemsLinkedList.indexOf(bs1.getName());
			int pos2 = topItemsLinkedList.indexOf(bs2.getName());
			if (pos1 != -1 && pos2 != -1) {
				return Integer.compare(pos1, pos2);
			} else if (pos1 != -1) {
				return -1;
			} else if (pos2 != -1) {
				return 1;
			} else if (bs1 instanceof SeparatorIBillingSystem) {
				return -1;
			} else if (bs2 instanceof SeparatorIBillingSystem) {
				return 1;
			} else {
				return bs1.getName().compareToIgnoreCase(bs2.getName());
			}
		}
	};

	public class SeparatorIBillingSystem implements IBillingSystem {

		@Override
		public String getName() {
			return UserCasePreferences.MENUSEPARATOR;
		}

		@Override
		public BillingLaw getLaw() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
