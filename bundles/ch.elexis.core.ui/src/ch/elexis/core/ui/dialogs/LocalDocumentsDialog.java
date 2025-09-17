package ch.elexis.core.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Person;

/**
 * Dialog showing all currently managed local documents of the
 * {@link ILocalDocumentService}. Local editing can be ended or cancelled.
 *
 * @author thomas
 *
 */
public class LocalDocumentsDialog extends TitleAreaDialog {

	private ILocalDocumentService service;
	private TableViewer tableViewer;

	public LocalDocumentsDialog(Shell parentShell, ILocalDocumentService service) {
		super(parentShell);
		this.service = service;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.LocalDocumentsDialog_dialogtitle);
		setMessage(Messages.LocalDocumentsDialog_dialogmessage);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		area.setLayout(new GridLayout(1, false));

		Composite tableComposite = new Composite(area, SWT.NONE);
		tableComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		TableColumnLayout tcLayout = new TableColumnLayout();
		tableComposite.setLayout(tcLayout);

		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tcPatient = tvc.getColumn();
		tcLayout.setColumnData(tcPatient, new ColumnPixelData(150, false, false));
		tcPatient.setText(Messages.Core_Patient);
		tvc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getReflectivePatientText(element);
			}
		});

		tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tcDocument = tvc.getColumn();
		tcLayout.setColumnData(tcDocument, new ColumnPixelData(250, true, true));
		tcDocument.setText(Messages.Core_Document);
		tvc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPersistentObject) {
					return ((IPersistentObject) element).getLabel();
				} else if (element instanceof Identifiable) {
					return ((Identifiable) element).getLabel();
				} else {
					return element.toString();
				}
			}
		});

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		// Create a menu manager and create context menu
		MenuManager menuManager = new MenuManager();
		menuManager.add(new EndLocalEditAction());
		menuManager.add(new AbortLocalEditAction());
		Menu menu = menuManager.createContextMenu(tableViewer.getTable());
		tableViewer.getTable().setMenu(menu);

		tableViewer.setInput(service.getAll());
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		Button abortAllBtn = new Button(parent, SWT.PUSH);
		abortAllBtn.setText(Messages.LocalDocumentsDialog_abortall);
		abortAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (MessageDialog.openQuestion(getShell(), Messages.Core_Abort,
						Messages.LocalDocumentsDialog_abortquestion)) {
					abortLocalEdit(new StructuredSelection(service.getAll()));
					okPressed();
				}
			}
		});
		((GridLayout) parent.getLayout()).numColumns++;
		Button endAllBtn = new Button(parent, SWT.PUSH);
		endAllBtn.setText(Messages.LocalDocumentsDialog_endall);
		endAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (MessageDialog.openQuestion(getShell(), Messages.LocalDocumentsDialog_endttile,
						Messages.LocalDocumentsDialog_endquestion)) {
					endLocalEdit(new StructuredSelection(service.getAll()));
					okPressed();
				}
			}
		});
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	private String getReflectivePatientText(Object element) {
		Method getPatientMethod = getGetPatientMethod(element.getClass(), Person.class);
		if (getPatientMethod != null) {
			try {
				Person patient = (Person) getPatientMethod.invoke(element, new Object[0]);
				if (patient != null) {
					return patient.getLabel(true);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// ignore ? will be returned
			}
		}
		getPatientMethod = getGetPatientMethod(element.getClass(), IPatient.class);
		if (getPatientMethod != null) {
			try {
				IPatient patient = (IPatient) getPatientMethod.invoke(element, new Object[0]);
				if (patient != null) {
					return patient.getLabel();
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// ignore ? will be returned
			}
		}
		return "?"; //$NON-NLS-1$
	}

	private Method getGetPatientMethod(Class<? extends Object> clazz, Class<? extends Object> type) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().equalsIgnoreCase("getpatient") //$NON-NLS-1$
					&& type.isAssignableFrom(method.getReturnType()) && method.getParameterTypes().length == 0) {
				return method;
			}
		}
		return null;
	}

	private void endLocalEdit(StructuredSelection selection) {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("ch.elexis.core.ui.command.endLocalDocument"); //$NON-NLS-1$

		PlatformUI.getWorkbench().getService(IEclipseContext.class).set(command.getId().concat(".selection"), //$NON-NLS-1$
				selection);
		try {
			command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, this, null));
			tableViewer.setInput(service.getAll());
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			MessageDialog.openError(getShell(), Messages.Core_Error,
					Messages.Core_Could_not_reread_correctly_document);
		}
	}

	private void abortLocalEdit(StructuredSelection selection) {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("ch.elexis.core.ui.command.abortLocalDocument"); //$NON-NLS-1$

		PlatformUI.getWorkbench().getService(IEclipseContext.class).set(command.getId().concat(".selection"), //$NON-NLS-1$
				selection);
		try {
			command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, this, null));
			tableViewer.setInput(service.getAll());
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			MessageDialog.openError(getShell(), Messages.Core_Error,
					Messages.Core_Could_not_abort_editing);
		}
	}

	@Override
	public boolean close() {
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IDocumentLetter.class);
		return super.close();
	}

	private class EndLocalEditAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_EDIT_DONE.getImageDescriptor();
		}

		@Override
		public String getText() {
			return Messages.LocalDocumentsDialog_actionendtext;
		}

		@Override
		public void run() {
			StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
			if (selection != null) {
				endLocalEdit(selection);
			}
		}
	}

	private class AbortLocalEditAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_EDIT_ABORT.getImageDescriptor();
		}

		@Override
		public String getText() {
			return Messages.LocalDocumentsDialog_actionaborttext;
		}

		@Override
		public void run() {
			StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
			if (selection != null) {
				abortLocalEdit(selection);
			}
		}
	}
}
