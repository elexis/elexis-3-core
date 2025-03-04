/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.BriefDocumentStoreHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.commands.BriefNewHandler;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.Message;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;
import ch.rgw.tools.ExHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class BriefAuswahl extends ViewPart implements IRefreshable {

	public final static String ID = "ch.elexis.BriefAuswahlView"; //$NON-NLS-1$
	private final FormToolkit tk;
	private Form form;
	private Action briefNeuAction, briefLadenAction, editNameAction, startLocalEditAction, endLocalEditAction,
			cancelLocalEditAction;
	private Action deleteAction;
	private ViewMenus menus;
	private ArrayList<sPage> pages = new ArrayList<>();
	CTabFolder ctab;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	@Inject
	void activePatient(@Optional IPatient patient) {
		ContextServiceHolder.get().getRootContext().removeTyped(IDocumentLetter.class);
		Display.getDefault().asyncExec(() -> {
			if (form != null && !form.isDisposed()) {
				if (patient == null) {
					form.setText(Messages.Core_No_patient_selected); // $NON-NLS-1$
				} else {
					form.setText(patient.getLabel());
				}
			}
			refresh();
		});
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IDocumentLetter.class.equals(clazz)) {
			relabel();
		}
	}

	@Override
	public void refresh() {
		Display.getDefault().asyncExec(() -> {
			refreshSelectedViewer();
		});
	}

	public void refreshSelectedViewer() {
		if (CoreUiUtil.isActiveControl(ctab)) {
			CTabItem sel = ctab.getSelection();
			if ((sel != null)) {
				CommonViewer cv = (CommonViewer) sel.getData();
				cv.notify(CommonViewer.Message.update);
			}
		}
	}

	public void refreshCV(Message updateKeeplabels) {
		if (ctab != null && CoreUiUtil.isActiveControl(ctab)) {
			CTabItem sel = ctab.getSelection();
			if (sel != null) {
				CommonViewer cv = (CommonViewer) sel.getData();
				cv.notify(updateKeeplabels);
			}
		}
	}

	// private ViewMenus menu;
	// private IAction delBriefAction;
	public BriefAuswahl() {
		tk = UiDesk.getToolkit();
	}

	@Override
	public void createPartControl(final Composite parent) {
		List<String> _categories = BriefDocumentStoreHolder.get().getCategories().stream().map(ICategory::getName)
				.filter(c -> !BriefConstants.TEMPLATE.equalsIgnoreCase(c)).toList();

		List<String> categories = new ArrayList<>();
		categories.add(Messages.BriefAuswahlAllLetters);
		categories.addAll(_categories);

		parent.setLayout(new GridLayout());

		form = tk.createForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.setBackground(parent.getBackground());

		// Grid layout with zero margins
		GridLayout slimLayout = new GridLayout();
		slimLayout.marginHeight = 0;
		slimLayout.marginWidth = 0;

		Composite body = form.getBody();
		body.setLayout(slimLayout);
		body.setBackground(parent.getBackground());

		ctab = new CTabFolder(body, SWT.BOTTOM);
		ctab.setLayout(slimLayout);
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ctab.setBackground(parent.getBackground());
		makeActions();
		menus = new ViewMenus(getViewSite());

		for (String cat : categories) { // $NON-NLS-1$
			CTabItem ct = new CTabItem(ctab, SWT.NONE);
			ct.setText(cat);
			sPage page = new sPage(ctab, cat);
			pages.add(page);
			if (LocalConfigService.get(Preferences.P_TEXT_EDIT_LOCAL, false)) {
				menus.createViewerContextMenu(page.cv.getViewerWidget(), editNameAction, deleteAction,
						startLocalEditAction, endLocalEditAction, cancelLocalEditAction);
			} else {
				menus.createViewerContextMenu(page.cv.getViewerWidget(), editNameAction, deleteAction);
			}
			ct.setData(page.cv);
			ct.setControl(page);
			page.cv.getViewerWidget().addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					briefLadenAction.run();
				}
			});
		}

		ctab.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				relabel();
			}

		});

		menus.createMenu(briefNeuAction, briefLadenAction, editNameAction, deleteAction);
		menus.createToolbar(briefNeuAction, briefLadenAction, deleteAction);
		ctab.setSelection(0);
		// relabel();
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		for (sPage page : pages) {
			page.getCommonViewer().getConfigurer().getContentProvider().stopListening();
		}
	}

	@Override
	public void setFocus() {

	}

	public void relabel() {
		UiDesk.asyncExec(new Runnable() {
			@Override
			public void run() {
				Patient pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				if (form != null && !form.isDisposed()) {
					if (pat == null) {
						form.setText(Messages.Core_No_patient_selected); // $NON-NLS-1$
					} else {
						form.setText(pat.getLabel());
						CTabItem sel = ctab.getSelection();
						if (sel != null) {
							CommonViewer cv = (CommonViewer) sel.getData();
							cv.notify(CommonViewer.Message.update);
						}
					}
				}
			}
		});

	}

	class sPage extends Composite {
		private TableViewer tableViewer;
		private LetterViewerComparator comparator;
		private final CommonViewer cv;
		private final ViewerConfigurer vc;

		public CommonViewer getCommonViewer() {
			return cv;
		}

		sPage(final Composite parent, final String cat) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout());
			cv = new CommonViewer();
			DefaultControlFieldProvider controlFieldProvider = new DefaultControlFieldProvider(cv,
					new String[] { "subject=Titel" //$NON-NLS-1$
					});
			CommonViewerContentProvider contentProvider = new ch.elexis.core.ui.util.viewers.CommonViewerContentProvider(
					cv) {

				private static final int QUERY_LIMIT = 500;

				@Override
				public Object[] getElements(final Object inputElement) {
					java.util.Optional<IPatient> actPat = ContextServiceHolder.get().getActivePatient();
					if (actPat.isPresent()) {
						IQuery<?> query = getBaseQuery();
						query.and(ModelPackage.Literals.IDOCUMENT__PATIENT, COMPARATOR.EQUALS, actPat.get());
						if (cat.equals(Messages.Core_All)) { // $NON-NLS-1$
							query.and(ModelPackage.Literals.IDOCUMENT__CATEGORY, COMPARATOR.NOT_EQUALS,
									BriefConstants.TEMPLATE);
						} else {
							query.and(ModelPackage.Literals.IDOCUMENT__CATEGORY, COMPARATOR.EQUALS, cat);
						}
						// apply filters from control field provider
						controlFieldProvider.setQuery(query);
						List<?> elements = query.execute();
						return elements.toArray(new Object[elements.size()]);
					} else {
						return new Object[0];
					}
				}

				@Override
				protected IQuery<?> getBaseQuery() {
					IQuery<IDocumentLetter> ret = CoreModelServiceHolder.get().getQuery(IDocumentLetter.class);
					if (!ignoreLimit) {
						ret.limit(QUERY_LIMIT);
					}
					return ret;
				}

			};
			vc = new ViewerConfigurer(contentProvider, new DefaultLabelProvider(), controlFieldProvider,
					new ViewerConfigurer.DefaultButtonProvider(),
					new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TABLE, SWT.V_SCROLL | SWT.FULL_SELECTION, cv));
			vc.setContentType(ContentType.GENERICOBJECT);
			cv.create(vc, this, SWT.NONE, getViewSite());

			tableViewer = (TableViewer) cv.getViewerWidget();
			tableViewer.getTable().setHeaderVisible(true);
			createColumns();
			comparator = new LetterViewerComparator();
			tableViewer.setComparator(comparator);
			if (LocalConfigService.get(Preferences.P_TEXT_RENAME_WITH_F2, false)) {
				tableViewer.getTable().addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						if (e.keyCode == SWT.F2) {
							editNameAction.run();
						}
					}
				});
			}

			vc.getContentProvider().startListening();
			Button bLoad = tk.createButton(this, Messages.BriefAuswahlLoadButtonText, SWT.PUSH); // $NON-NLS-1$
			bLoad.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					briefLadenAction.run();
				}

			});
			bLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		}

		// create the columns for the table
		private void createColumns() {
			// first column - date
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
			col.getColumn().setText(Messages.Core_Date);
			col.getColumn().setWidth(100);
			col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 0));
			col.setLabelProvider(new ColumnLabelProvider() {
				private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$

				@Override
				public String getText(Object element) {
					IDocumentLetter b = (IDocumentLetter) element;
					return dateFormat.format(b.getCreated());
				}
			});

			// second column - title
			col = new TableViewerColumn(tableViewer, SWT.NONE);
			col.getColumn().setText(Messages.Core_Title);
			col.getColumn().setWidth(300);
			col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 1));
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					IDocumentLetter b = (IDocumentLetter) element;
					return b.getTitle();
				}

				@Override
				public Image getImage(Object element) {
					if (LocalDocumentServiceHolder.getService().isPresent()) {
						if (LocalDocumentServiceHolder.getService().get().contains(element)) {
							return Images.IMG_EDIT.getImage();
						}
					}
					return super.getImage(element);
				}
			});
		}

		private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
			SelectionAdapter selectionAdapter = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					comparator.setColumn(index);
					tableViewer.getTable().setSortDirection(comparator.getDirection());
					tableViewer.getTable().setSortColumn(column);
					tableViewer.refresh();
				}
			};
			return selectionAdapter;
		}

		class LetterViewerComparator extends ViewerComparator {
			private int propertyIndex;
			private boolean direction = true;

			public LetterViewerComparator() {
				this.propertyIndex = 0;
			}

			/**
			 * for sort direction
			 *
			 * @return SWT.DOWN or SWT.UP
			 */
			public int getDirection() {
				return direction ? SWT.DOWN : SWT.UP;
			}

			public void setColumn(int column) {
				if (column == this.propertyIndex) {
					// Same column as last sort; toggle the direction
					direction = !direction;
				} else {
					// New column; do an ascending sort
					this.propertyIndex = column;
					direction = true;
				}
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof IDocumentLetter && e2 instanceof IDocumentLetter) {
					IDocumentLetter b1 = (IDocumentLetter) e1;
					IDocumentLetter b2 = (IDocumentLetter) e2;
					int rc = 0;

					switch (propertyIndex) {
					case 0:
						rc = b1.getCreated().compareTo(b2.getCreated());
						if (rc == 0) {
							rc = b2.getLastupdate().compareTo(b1.getLastupdate());
							return rc;
						}
						break;
					case 1:
						rc = b1.getTitle().compareTo(b2.getTitle());
						break;
					default:
						rc = 0;
					}
					// If descending order, flip the direction
					if (direction) {
						rc = -rc;
					}
					return rc;
				}
				return 0;
			}
		}
	}

	private void makeActions() {
		briefNeuAction = new Action(Messages.Core_New_ellipsis) { // $NON-NLS-1$
			@Override
			public void run() {
				IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(BriefNewHandler.CMD_ID, null);
				} catch (Exception e) {
					LoggerFactory.getLogger(BriefAuswahl.class).error("cannot execute cmd", e); //$NON-NLS-1$
				}
			}
		};

		briefLadenAction = new Action(Messages.Core_Open) { // $NON-NLS-1$
			@Override
			public void run() {
				try {
					Brief brief = getSelectedBrief();
					if (brief != null) {
						if (LocalConfigService.get(Preferences.P_TEXT_EDIT_LOCAL, false)) {
							startLocalEditAction.run();
						} else {
							TextView tv = (TextView) getViewSite().getPage().showView(TextView.ID);
							if (brief.getMimeType().equalsIgnoreCase("pdf")) { //$NON-NLS-1$
								try {
									File temp = File.createTempFile("letter_", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
									temp.deleteOnExit();
									try (FileOutputStream fos = new FileOutputStream(temp)) {
										fos.write(brief.loadBinary());
									}
									Program.launch(temp.getAbsolutePath());
								} catch (IOException e) {
									ExHandler.handle(e);
									SWTHelper.alert(Messages.Core_Error, // $NON-NLS-1$
											Messages.BriefAuswahlCouldNotLoadText); // $NON-NLS-1$
								}
							} else if (tv.openDocument(brief) == false) {
								SWTHelper.alert(Messages.Core_Error, // $NON-NLS-1$
										Messages.BriefAuswahlCouldNotLoadText); // $NON-NLS-1$
							}
						}
					} else {
						TextView tv = (TextView) getViewSite().getPage().showView(TextView.ID);
						tv.createDocument(null, null);
					}
					CommonViewer cv = (CommonViewer) ctab.getSelection().getData();
					cv.notify(CommonViewer.Message.update);
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}

			}
		};
		deleteAction = new LockRequestingAction<Brief>(Messages.Core_Delete) { // $NON-NLS-1$
			@Override
			public void doRun(Brief brief) {
				if (brief != null && SWTHelper.askYesNo(Messages.Core_Delete_Document, // $NON-NLS-1$
						Messages.BriefAuswahlDeleteConfirmText)) {
					brief.delete();
					CTabItem sel = ctab.getSelection();
					CommonViewer cv = (CommonViewer) sel.getData();
					cv.notify(CommonViewer.Message.update);
				}
			}

			@Override
			public Brief getTargetedObject() {
				return getSelectedBrief();
			}
		};
		editNameAction = new LockRequestingAction<Brief>(Messages.BriefAuswahlRenameButtonText) { // $NON-NLS-1$
			@Override
			public void doRun(Brief brief) {
				if (brief != null) {
					InputDialog id = new InputDialog(getViewSite().getShell(), Messages.BriefAuswahlNewSubjectHeading, // $NON-NLS-1$
							Messages.BriefAuswahlNewSubjectText, // $NON-NLS-1$
							brief.getBetreff(), null);
					if (id.open() == Dialog.OK) {
						brief.setBetreff(id.getValue());
						// refresh the model
						CoreModelServiceHolder.get().load(brief.getId(), IDocumentLetter.class, false, true);
					}
					CTabItem sel = ctab.getSelection();
					CommonViewer cv = (CommonViewer) sel.getData();
					cv.notify(CommonViewer.Message.update);
				}
			}

			@Override
			public Brief getTargetedObject() {
				return getSelectedBrief();
			}
		};
		startLocalEditAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_EDIT.getImageDescriptor();
			}

			@Override
			public String getText() {
				return Messages.BriefAuswahl_actionlocaledittext;
			}

			@Override
			public void run() {
				IDocumentLetter brief = getSelected();
				if (brief != null) {
					ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
					Command command = commandService.getCommand("ch.elexis.core.ui.command.startEditLocalDocument"); //$NON-NLS-1$
					PlatformUI.getWorkbench().getService(IEclipseContext.class)
							.set(command.getId().concat(".selection"), new StructuredSelection(brief)); //$NON-NLS-1$
					try {
						command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, this, null));
					} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
						MessageDialog.openError(getSite().getShell(), Messages.Core_Error,
								Messages.Core_Document_Not_Opened_Locally);
					}
					refreshSelectedViewer();
				}
			}
		};
		endLocalEditAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_EDIT_DONE.getImageDescriptor();
			}

			@Override
			public String getText() {
				return Messages.BriefAuswahl_actionlocaleditstopmessage;
			}

			@Override
			public void run() {
				IDocumentLetter brief = getSelected();
				if (brief != null) {
					ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
					Command command = commandService.getCommand("ch.elexis.core.ui.command.endLocalDocument"); //$NON-NLS-1$

					PlatformUI.getWorkbench().getService(IEclipseContext.class)
							.set(command.getId().concat(".selection"), new StructuredSelection(brief)); //$NON-NLS-1$
					try {
						command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, this, null));
					} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
						MessageDialog.openError(getSite().getShell(), Messages.Core_Error,
								Messages.Core_Could_not_reread_correctly_document);
					}
				}
				refreshSelectedViewer();
			}
		};
		cancelLocalEditAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_EDIT_ABORT.getImageDescriptor();
			}

			@Override
			public String getText() {
				return Messages.Core_Abort;
			}

			@Override
			public void run() {
				IDocumentLetter brief = getSelected();
				if (brief != null) {
					ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
					Command command = commandService.getCommand("ch.elexis.core.ui.command.abortLocalDocument"); //$NON-NLS-1$

					PlatformUI.getWorkbench().getService(IEclipseContext.class)
							.set(command.getId().concat(".selection"), new StructuredSelection(brief)); //$NON-NLS-1$
					try {
						command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, this, null));
					} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
						MessageDialog.openError(getSite().getShell(), Messages.Core_Error,
								Messages.Core_Could_not_abort_editing);
					}
				}
				refreshSelectedViewer();
			}
		};
		/*
		 * importAction=new Action("Importieren..."){ public void run(){
		 *
		 * } };
		 */
		briefLadenAction.setImageDescriptor(Images.IMG_DOCUMENT_TEXT.getImageDescriptor());
		briefLadenAction.setToolTipText(Messages.BriefAuswahlOpenLetterForEdit); // $NON-NLS-1$
		briefNeuAction.setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
		briefNeuAction.setToolTipText(Messages.BriefAuswahlCreateNewDocument); // $NON-NLS-1$
		editNameAction.setImageDescriptor(Images.IMG_DOCUMENT_WRITE.getImageDescriptor());
		editNameAction.setToolTipText(Messages.BriefAuswahlRenameDocument); // $NON-NLS-1$
		deleteAction.setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
		deleteAction.setToolTipText(Messages.Core_Delete_Document); // $NON-NLS-1$
	}

	public IDocumentLetter getSelected() {
		CTabItem sel = ctab.getSelection();
		if ((sel != null)) {
			CommonViewer cv = (CommonViewer) sel.getData();
			Object[] o = cv.getSelection();
			if ((o != null) && (o.length > 0)) {
				if (o[0] instanceof IDocumentLetter) {
					return (IDocumentLetter) o[0];
				}
			}
		}
		return null;
	}

	public Brief getSelectedBrief() {
		CTabItem sel = ctab.getSelection();
		if ((sel != null)) {
			CommonViewer cv = (CommonViewer) sel.getData();
			Object[] o = cv.getSelection();
			if ((o != null) && (o.length > 0)) {
				if (o[0] instanceof IDocumentLetter) {
					return (Brief) NoPoUtil.loadAsPersistentObject((Identifiable) o[0]);
				}
			}
		}
		return null;
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
