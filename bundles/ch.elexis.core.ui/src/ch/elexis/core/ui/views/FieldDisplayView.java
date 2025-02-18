/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.views;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * This view displays the content of an arbitrary field.
 *
 * @author gerry
 *
 */
public class FieldDisplayView extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.dbfielddisplay"; //$NON-NLS-1$

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	private IAction newViewAction, editDataAction;
	Text text;
	Class<? extends PersistentObject> myClass;
	Class<?> myModelInterface;
	String myField;
	boolean bCanEdit;
	ScrolledForm form;
	FormToolkit tk = UiDesk.getToolkit();
	String subid;
	String NODE = "FeldAnzeige"; //$NON-NLS-1$

	private MyRunAndTrack currentRunAndTrack;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		form = tk.createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.getBody().setLayout(new GridLayout());
		text = tk.createText(form.getBody(), StringUtils.EMPTY, SWT.MULTI | SWT.V_SCROLL);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if (bCanEdit) {
					IPersistentObject mine = ElexisEventDispatcher.getSelected(myClass);
					if (mine != null) {
						mine.set(myField, text.getText());
					}
				}
			}

		});
		text.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				arg0.doit = bCanEdit;

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}
		});
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newViewAction, editDataAction);
		String nx = "Patient.Diagnosen"; //$NON-NLS-1$
		Integer canEdit = null;
		subid = getViewSite().getSecondaryId();
		if (subid == null) {
			subid = "defaultData"; //$NON-NLS-1$
		}
		nx = ConfigServiceHolder.getUser("FieldDisplayViewData/" + subid, null); //$NON-NLS-1$
		canEdit = ConfigServiceHolder.getUser("FieldDisplayViewCanEdit/" + subid, 0); //$NON-NLS-1$
		setField(nx == null ? "Patient.Diagnosen" : nx, canEdit == null ? false //$NON-NLS-1$
				: (canEdit != 0));
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		if (currentRunAndTrack != null) {
			currentRunAndTrack.stop();
		}
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	@SuppressWarnings("unchecked")
	private void setField(String field, boolean canEdit) {
		String[] def = field.split("\\."); //$NON-NLS-1$
		if (def.length != 2) {
			SWTHelper.showError(Messages.FieldDisplayView_BadDefinitionCaption, // $NON-NLS-1$
					Messages.FieldDisplayView_BadDefinitionBody); // $NON-NLS-1$
		} else {
			myClass = resolveName(def[0]);
			if (myClass != null) {
				myField = def[1];
				bCanEdit = canEdit;
				setPartName(myField);
				ConfigServiceHolder.setUser("FieldDisplayViewData/" + subid, myClass //$NON-NLS-1$
						.getSimpleName() + "." + myField); //$NON-NLS-1$
				ConfigServiceHolder.setUser("FieldDisplayViewCanEdit/" + subid, canEdit); //$NON-NLS-1$

			}
		}
		if (myClass != null) {
			myModelInterface = ElexisEventDispatcher.getCoreModelInterfaceForElexisClass(myClass).orElse(null);
			if (currentRunAndTrack != null) {
				currentRunAndTrack.stop();
			}
			currentRunAndTrack = new MyRunAndTrack(myClass, myModelInterface);
			ContextServiceHolder.get().getRootContext().setNamed("AddRunAndTrackToE4Context", currentRunAndTrack);
		}
	}

	@SuppressWarnings("unchecked")
	private Class resolveName(String k) {
		Class ret = null;

		// resolve with classic names
		try {
			String fqname = "ch.elexis.data." + k; //$NON-NLS-1$
			ret = Class.forName(fqname);
		} catch (ClassNotFoundException ex) {
			ret = null;
		}

		// fall back to new schema (there should not exist any)
		if (ret == null) {
			try {
				String fqname = "ch.elexis.core.data." + k; //$NON-NLS-1$
				ret = Class.forName(fqname);
			} catch (ClassNotFoundException ex) {
				ret = null;
			}
		}

		if (ret == null) {
			SWTHelper.showError(Messages.FieldDisplayView_WrongTypeCaption, // $NON-NLS-1$
					Messages.FieldDisplayView_WrongTypeBody + k + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return ret;
	}

	private void makeActions() {
		newViewAction = new Action(Messages.Core_New_Window) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
				setToolTipText(Messages.FieldDisplayView_NewWindowToolTip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				try {
					String fieldtype = new SelectDataDialog().run();
					FieldDisplayView n = (FieldDisplayView) getViewSite().getPage().showView(ID,
							ElexisIdGenerator.generateId(), IWorkbenchPage.VIEW_VISIBLE);
					n.setField(fieldtype, false);
					refresh();
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		};
		editDataAction = new Action(Messages.FieldDisplayView_DataTypeAction) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.FieldDisplayView_DataTypeToolTip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				SelectDataDialog sdd = new SelectDataDialog();
				if (sdd.open() == Dialog.OK) {
					setField(sdd.result, sdd.bEditable);
					refresh();
				}
			}
		};
	}

	class SelectDataDialog extends TitleAreaDialog {
		private final String DATATYPE = Messages.Core_Data_Type; // $NON-NLS-1$
		String[] nodes;
		Combo cbNodes;
		Button btEditable;
		String result;
		boolean bEditable;

		SelectDataDialog() {
			super(getViewSite().getShell());
		}

		String run() {
			create();
			if (nodes.length > 1) {
				if (open() == Dialog.OK) {
					return result;
				}

			}
			return nodes[0];
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cbNodes = new Combo(ret, SWT.SINGLE);
			cbNodes.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			nodes = CoreHub.localCfg.get(NODE, "Patient.Diagnosen").split(","); //$NON-NLS-1$ //$NON-NLS-2$
			cbNodes.setItems(nodes);
			btEditable = new Button(ret, SWT.CHECK);
			btEditable.setText(Messages.FieldDisplayView_FieldCanBeChanged); // $NON-NLS-1$
			return ret;
		}

		@Override
		public void create() {
			super.create();
			setTitle(DATATYPE);
			setMessage(Messages.FieldDisplayView_EnterExpression, // $NON-NLS-1$
					IMessageProvider.INFORMATION);
		}

		@Override
		protected void okPressed() {
			String tx = cbNodes.getText();
			if (StringTool.getIndex(nodes, tx) == -1) {
				String tm = StringTool.join(nodes, ",") + "," + tx; //$NON-NLS-1$ //$NON-NLS-2$
				CoreHub.localCfg.set(NODE, tm);
			}
			result = tx;
			bEditable = btEditable.getSelection();
			super.okPressed();
		}

	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void refresh() {
		if (currentRunAndTrack != null) {
			ContextServiceHolder.get().getTyped(IEclipseContext.class).ifPresent(ec -> currentRunAndTrack.changed(ec));
		}
	}

	private class MyRunAndTrack extends RunAndTrack {

		private Class<? extends PersistentObject> myClass;
		private Class<?> myModelInterface;

		private boolean stop;

		public MyRunAndTrack(Class<? extends PersistentObject> myClass, Class<?> myModelInterface) {
			this.myClass = myClass;
			this.myModelInterface = myModelInterface;
			stop = false;
		}

		@Override
		public boolean changed(IEclipseContext context) {
			PersistentObject po = null;
			if (myModelInterface != null) {
				Object identifiable = context.get(myModelInterface);
				if (identifiable instanceof Identifiable) {
					po = NoPoUtil.loadAsPersistentObject((Identifiable) identifiable);
				}
			}
			if (po == null) {
				po = context.get(myClass);
			}
			if (po != null) {
				PersistentObject runnablePo = po;
				UiDesk.asyncExec(new Runnable() {
					@Override
					public void run() {
						String val = runnablePo.get(myField);
						if (val == null) {
							SWTHelper.showError(Messages.FieldDisplayView_ErrorFieldCaption, // $NON-NLS-1$
									Messages.FieldDisplayView_ErrorFieldBody + myField);
							text.setText(StringTool.leer);
						} else {
							text.setText(runnablePo.get(myField));
						}
					}
				});
			}
			return !stop;
		}

		public void stop() {
			stop = true;
		}
	}
}
