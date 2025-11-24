/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.provider.ContactSelectionLabelProvider;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.PoDoubleClickListener;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ContactSelectionDialog extends TitleAreaDialog implements PoDoubleClickListener {

	// Name, Vorname, gebdat, strasse, plz, ort, tel, zusatz, fax, email
	public static final int HINTSIZE = 12;

	public static final int HINT_NAME = 0;
	public static final int HINT_FIRSTNAME = 1;
	public static final int HINT_BIRTHDATE = 2;
	public static final int HINT_STREET = 3;
	public static final int HINT_ZIP = 4;
	public static final int HINT_PLACE = 5;
	public static final int HINT_PHONE = 6;
	public static final int HINT_ADD = 7;
	public static final int HINT_FAX = 8;
	public static final int HINT_MAIL = 9;
	public static final int HINT_SEX = 10;
	public static final int HINT_PATIENT = 11;

	// private Class clazz;
	CommonViewer commonViewer;
	ViewerConfigurer vc;
	private final String title;
	private final String message;
	private Object selection;
	Button bAll, bPersons, bOrgs;
	FilterButtonAdapter fba;
	String[] hints;
	// int type;

	boolean showBezugsKontakt = false;
	String extraText = null;
	private ListViewer bezugsKontaktViewer = null;
	private boolean isSelecting = false;
	private final ContactContentProvider contentProvider;
	private boolean enableEmptyField = false;
	private List<String> allowedContactIds = null;
	private Class<? extends Identifiable> targetClass;

	public ContactSelectionDialog(Shell parentShell, Class<? extends Identifiable> which, String title, String message,
			String... orderFields) {
		super(parentShell);
		targetClass = which;
		commonViewer = new CommonViewer();
		fba = new FilterButtonAdapter();
		this.title = title;
		this.message = message;

		contentProvider = new ContactContentProvider(commonViewer);
		contentProvider.setOrderFields(orderFields);
	}

	private class ContactContentProvider extends CommonViewerContentProvider implements IStructuredContentProvider {

		public ContactContentProvider(CommonViewer commonViewer) {
			super(commonViewer);
		}

		@Override
		protected IQuery<?> getBaseQuery() {
			return CoreModelServiceHolder.get().getQuery(targetClass);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			// CommonViewer inputElement can be ignored
			List<? extends Identifiable> roots = Collections.emptyList();
			IQuery<?> query = getBaseQuery();
			if (hasActiveFilter(fieldFilterValues)) {
				query.startGroup();
				for (String key : fieldFilterValues.keySet()) {
					if (!StringUtils.isBlank(fieldFilterValues.get(key))) {
						query.and(key, COMPARATOR.LIKE, "%" + fieldFilterValues.get(key) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				query.andJoinGroups();
			}
			if (fieldOrderBy != null) {
				query.orderBy(fieldOrderBy, fieldOrder);
			} else if (orderFields != null && orderFields.length > 0) {
				for (String field : orderFields) {
					query.orderBy(field, fieldOrder);
				}
			}
			roots = (List<? extends Identifiable>) query.execute();

			if (allowedContactIds != null && IContact.class.isAssignableFrom(targetClass)) {
				roots = roots.stream().filter(
						ident -> ident instanceof IContact && allowedContactIds.contains(((IContact) ident).getId()))
						.toList();
			}
			return roots.toArray();
		}

		private boolean hasActiveFilter(Map<String, String> fieldFilterValues) {
			if (fieldFilterValues != null && !fieldFilterValues.isEmpty()) {
				for (String key : fieldFilterValues.keySet()) {
					String value = fieldFilterValues.get(key);
					if (StringUtils.isNotBlank(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public ContactSelectionDialog(Shell parentShell, Class<? extends Identifiable> which, String t, String m,
			boolean showBezugsKontakt, String... orderFields) {
		this(parentShell, which, t, m, orderFields);
		this.showBezugsKontakt = showBezugsKontakt;
	}

	public ContactSelectionDialog(Shell parentShell, Class<? extends Identifiable> which, String t, String m,
			String extra, String... orderFields) {
		this(parentShell, which, t, m, orderFields);
		extraText = extra;
	}

	@Override
	public boolean close() {
		commonViewer.removeDoubleClickListener(this);
		commonViewer.dispose();
		return super.close();
	}

	/**
	 * Provide a few hints in case the user clicks "Neu erstellen". The hints is an
	 * array of up to 10 Strings as used in KontaktErfassenDialog
	 *
	 * @param hints Name, Vorname, gebdat, strasse, plz, ort, tel, zusatz, fax,
	 *              email
	 */
	public void setHints(String[] h) {
		this.hints = h;
		for (int i = 0; i < hints.length; i++) {
			if (hints[i] == null) {
				hints[i] = StringUtils.EMPTY;
			}
		}
		if (!StringUtils.isBlank(hints[HINT_BIRTHDATE])) {
			TimeTool tt = new TimeTool();
			if (tt.set(hints[HINT_BIRTHDATE])) {
				hints[HINT_BIRTHDATE] = tt.toString(TimeTool.DATE_GER);
			} else {
				hints[HINT_BIRTHDATE] = StringTool.leer;
			}
		}
		if (!StringTool.isNothing(hints[HINT_SEX])) {
			if (hints[HINT_SEX].toLowerCase().startsWith("m")) { //$NON-NLS-1$
				hints[HINT_SEX] = Gender.MALE.value();
			} else {
				hints[HINT_SEX] = Gender.FEMALE.value();
			}
		}
	}

	private String[] getFilterStrings() {
		String[] ret = new String[0];
		if (targetClass.equals(IContact.class)) {
			ret = new String[] { "code=" //$NON-NLS-1$
					+ Messages.Core_Kuerzel,
					"description1=" //$NON-NLS-1$
							+ Messages.Core_Description };
		} else if (IPerson.class.isAssignableFrom(targetClass)) {
			ret = new String[] { "code=" //$NON-NLS-1$
					+ Messages.Core_Kuerzel,
					"description1=" //$NON-NLS-1$
							+ "Nachname",
					"dob=" //$NON-NLS-1$
							+ Messages.Core_Enter_Birthdate };
		} else if (IOrganization.class.isAssignableFrom(targetClass)) {
			ret = new String[] { "code=" //$NON-NLS-1$
					+ Messages.Core_Kuerzel,
					"description1=" //$NON-NLS-1$
							+ Messages.Core_Description };
		}
		return ret;
	}

	/*
	 * (Kein Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		// SashForm ret=new SashForm(parent,SWT.NONE);
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

		// if (showBezugsKontakt) {
		// new Label(ret, SWT.NONE).setText("Bezugskontakte");
		// bezugsKontaktViewer = new ListViewer(ret, SWT.SINGLE);
		// bezugsKontaktViewer.getControl().setLayoutData(
		// SWTHelper.getFillGridData(1, true, 1, false));
		//
		// bezugsKontaktViewer.setContentProvider(new IStructuredContentProvider() {
		// public Object[] getElements(Object inputElement){
		// Patient patient = ElexisEventDispatcher.getSelectedPatient();
		// if (patient != null) {
		// ArrayList<PersistentObject> elements = new ArrayList<PersistentObject>();
		// ArrayList<String> addedKontakte = new ArrayList<String>();
		//
		// // add the patient itself
		// elements.add(patient);
		// addedKontakte.add(patient.getId());
		//
		// List<BezugsKontakt> bezugsKontakte = patient.getBezugsKontakte();
		// if (bezugsKontakte != null) {
		// for (BezugsKontakt bezugsKontakt : bezugsKontakte) {
		// elements.add(bezugsKontakt);
		// addedKontakte.add(bezugsKontakt.get("otherID"));
		// }
		// }
		//
		// // required contacts of biling system
		// Fall[] faelle = patient.getFaelle();
		// for (Fall fall : faelle) {
		// String reqs = fall.getRequirements();
		// if (reqs != null) {
		// for (String req : reqs.split(";")) {
		// final String[] r = req.split(":");
		//
		// // no valid entry
		// if (r.length < 2) {
		// continue;
		// }
		//
		// // only consider contacts
		// if (r[1].equals("K")) {
		// String kontaktID = fall.getInfoString(r[0]);
		// if (!kontaktID.startsWith("**ERROR")) {
		// Kontakt kontakt = Kontakt.load(kontaktID);
		// if (kontakt.isValid()) {
		// elements.add(kontakt);
		// addedKontakte.add(kontakt.getId());
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// return elements.toArray();
		// }
		//
		// return new Object[] {};
		// }
		//
		// public void dispose(){
		// // nothing to do
		// }
		//
		// public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		// // nothing to do
		// }
		// });
		// bezugsKontaktViewer.setLabelProvider(new KontaktSelektorLabelProvider());
		// bezugsKontaktViewer.setInput(this);
		// bezugsKontaktViewer.addSelectionChangedListener(new
		// ISelectionChangedListener() {
		// public void selectionChanged(SelectionChangedEvent event){
		// if (isSelecting) {
		// return;
		// }
		//
		// IStructuredSelection sel =
		// (IStructuredSelection) commonViewer.getViewerWidget().getSelection();
		// if (sel.size() > 0) {
		// isSelecting = true;
		// commonViewer.getViewerWidget().setSelection(new StructuredSelection(),
		// false);
		// isSelecting = false;
		// }
		// }
		// });
		// } else {
		bezugsKontaktViewer = null;
		// }

		if (showBezugsKontakt) {
			new Label(ret, SWT.NONE).setText("Andere Kontakte");
		}
		if (extraText != null) {
			new Label(ret, SWT.WRAP).setText(extraText);
		}
		vc = new ViewerConfigurer(contentProvider, new ContactSelectionLabelProvider(),
				new DefaultControlFieldProvider(commonViewer, getFilterStrings()),
				new ViewerConfigurer.ButtonProvider() {

					public Button createButton(final Composite parent) {
						Button ret = new Button(parent, SWT.PUSH);
						ret.setText("Neu erstellen...");
						ret.addSelectionListener(new SelectionAdapter() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								if (hints == null) {
									hints = new String[3];
									hints[0] = vc.getControlFieldProvider().getValues()[1];
								}
								KontaktErfassenDialog ked = new KontaktErfassenDialog(parent.getShell(), hints);
								ked.open();
								selection = ked.getResult();
								okPressed();
							}

						});
						return ret;
					}

					public boolean isAlwaysEnabled() {
						return false;
					}
				}, new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, commonViewer));
		Composite types = new Composite(ret, SWT.BORDER);
		types.setLayout(new FillLayout());
		types.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bAll = new Button(types, SWT.RADIO);
		bPersons = new Button(types, SWT.RADIO);
		bOrgs = new Button(types, SWT.RADIO);
		bAll.setText("Alle");
		bPersons.setText("Personen");
		bOrgs.setText("Organisationen");
		bAll.addSelectionListener(fba);
		bPersons.addSelectionListener(fba);
		bOrgs.addSelectionListener(fba);
		initContactTypeSelection();

		commonViewer.create(vc, ret, SWT.NONE, "1"); //$NON-NLS-1$
		GridData gd = SWTHelper.getFillGridData(1, true, 1, true);
		gd.heightHint = 100;
		commonViewer.getViewerWidget().getControl().setLayoutData(gd);
		setTitle(title);
		setMessage(message);
		vc.getContentProvider().startListening();
		commonViewer.addDoubleClickListener(this);

		if (showBezugsKontakt) {
			commonViewer.getViewerWidget().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (isSelecting) {
						return;
					}

					if (bezugsKontaktViewer != null) {
						IStructuredSelection sel = (IStructuredSelection) bezugsKontaktViewer.getSelection();
						if (sel.size() > 0) {
							isSelecting = true;
							bezugsKontaktViewer.setSelection(new StructuredSelection(), false);
							isSelecting = false;
						}
					}
				}
			});
		}
		return ret;
	}

	private void initContactTypeSelection() {
		if (IPerson.class.isAssignableFrom(targetClass)) {
			bPersons.setSelection(true);
		} else if (IOrganization.class.isAssignableFrom(targetClass)) {
			bOrgs.setSelection(true);
		} else {
			bAll.setSelection(true);
		}

	}

	public Object getSelection() {
		return selection;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.Core_Select_Contact);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (enableEmptyField) {
			parent.setLayout(new GridLayout(3, false));
			Button btnClear = createButton(parent, IDialogConstants.NO_ID, Messages.KontaktSelector_clearField, false);
			btnClear.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selection = null;
					vc.getContentProvider().stopListening();
					close();
				}
			});
		} else {
			parent.setLayout(new GridLayout(2, false));
		}

		createButton(parent, IDialogConstants.OK_ID, "OK", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
	}

	/*
	 * (Kein Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		selection = null;
		vc.getContentProvider().stopListening();
		super.cancelPressed();
	}

	private Object getBezugsKontaktSelection() {
		Object bezugsKontakt = null;

		if (bezugsKontaktViewer != null) {
			IStructuredSelection sel = (IStructuredSelection) bezugsKontaktViewer.getSelection();
			if (sel.size() > 0) {
				bezugsKontakt = sel.getFirstElement();
			}
		}

		return bezugsKontakt;
	}

	/*
	 * (Kein Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {

		Object bKSel = getBezugsKontaktSelection();
		if (bKSel instanceof Kontakt) {
			selection = bKSel;
		} else if (bKSel instanceof BezugsKontakt) {
			BezugsKontakt bezugsKontakt = (BezugsKontakt) bKSel;
			Kontakt kontakt = Kontakt.load(bezugsKontakt.get("otherID")); //$NON-NLS-1$
			if (kontakt.exists()) {
				selection = kontakt;
			}
		} else {
			if (selection == null) {
				Object[] sel = commonViewer.getSelection();
				if ((sel != null) && (sel.length > 0)) {
					selection = sel[0];
				} else {
					Table tbl = (Table) commonViewer.getViewerWidget().getControl();
					tbl.setSelection(0);
					if (commonViewer.getSelection().length > 0) {
						selection = commonViewer.getSelection()[0];
					}
				}
			}
		}
		vc.getContentProvider().stopListening();
		commonViewer.removeDoubleClickListener(this);
		super.okPressed();
	}

	public void doubleClicked(PersistentObject obj, CommonViewer cv) {
		okPressed();
	}

	class FilterButtonAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (((Button) e.getSource()).getSelection()) {
				if (bPersons.getSelection()) {
					targetClass = IPerson.class;
				} else if (bOrgs.getSelection()) {
					targetClass = IOrganization.class;
				} else {
					targetClass = IContact.class;
				}
				ControlFieldProvider controlFieldProvider = commonViewer.getConfigurer().getControlFieldProvider();
				if (controlFieldProvider instanceof DefaultControlFieldProvider) {
					((DefaultControlFieldProvider) controlFieldProvider).updateFields(getFilterStrings(), true);
					ContactSelectionDialog.this.getShell().layout(true, true);
				}
				commonViewer.notify(CommonViewer.Message.update);
			}
		}
	}

	public static IContact showInSync(Class<? extends IContact> clazz, String title, String message, String extra) {
		InSync rn = new InSync(clazz, title, message, extra, null);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;

	}

	public static IContact showInSync(Class<? extends IContact> clazz, String title, String message) {
		InSync rn = new InSync(clazz, title, message, null, null);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;

	}

	public static IContact showInSync(Class<? extends IContact> clazz, String title, String message, String extra,
			String[] hints) {
		InSync rn = new InSync(clazz, title, message, extra, hints);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;

	}

	public static IContact showInSync(Class<? extends IContact> clazz, String title, String message, String[] hints) {
		InSync rn = new InSync(clazz, title, message, null, hints);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;

	}

	private static class InSync implements Runnable {
		IContact ret;
		String title, message;
		Class<? extends IContact> clazz;
		String extra;
		String[] hints;
		private String[] orderFields;

		InSync(Class<? extends IContact> clazz, String title, String message, String extra, String[] hints,
				String... orderFields) {
			this.title = title;
			this.message = message;
			this.clazz = clazz;
			this.extra = extra;
			this.hints = hints;
			this.orderFields = orderFields;
			if (orderFields == null) {
				this.orderFields = new String[] { ModelPackage.Literals.ICONTACT__DESCRIPTION1.getName(),
						ModelPackage.Literals.ICONTACT__DESCRIPTION2.getName(),
						ModelPackage.Literals.ICONTACT__STREET.getName(),
						ModelPackage.Literals.ICONTACT__CITY.getName() };
			}
		}

		public void run() {
			Shell shell = UiDesk.getDisplay().getActiveShell();
			ContactSelectionDialog ksl = new ContactSelectionDialog(shell, clazz, title, message, extra, orderFields);
			if (hints != null) {
				ksl.setHints(hints);
			}
			if (ksl.open() == Dialog.OK) {
				ret = (IContact) ksl.getSelection();
			} else {
				ret = null;
			}
		}

	}

	public void enableEmptyFieldButton() {
		enableEmptyField = true;
	}

	public void setAllowedContacts(List<IContact> allowedContacts) {
		if (allowedContacts == null) {
			this.allowedContactIds = null;
		} else {
			this.allowedContactIds = allowedContacts.stream().filter(Objects::nonNull).map(IContact::getId)
					.collect(Collectors.toList());
		}
	}
}
