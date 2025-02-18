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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.data.interfaces.IOutputter;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.builder.IRecipeBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.MediDetailDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.GenericObjectDragSource;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.codesystems.CodeSystemDescription;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.Artikel;
import ch.elexis.data.Brief;
import ch.elexis.data.OutputLog;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rezept;
import ch.rgw.tools.ExHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Eine View zum Anzeigen von Rezepten. Links wird eine Liste mit allen Rezepten
 * des aktuellen Patienten angezeigt, rechts die Prescriptions des aktuellen
 * Rezepts.
 *
 * @author Gerry
 */
public class RezepteView extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.Rezepte"; //$NON-NLS-1$
	private final FormToolkit tk = UiDesk.getToolkit();
	private Form master;
	TableViewer tv;
	// Label ausgestellt;
	ListViewer lvRpLines;
	private Action newRpAction, deleteRpAction;
	private Action addLineAction, removeLineAction, changeMedicationAction;
	private ViewMenus menus;
	private Action printAction;
	private GenericObjectDropTarget dropTarget;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	private IPatient actPatient;
	private RecipeLoader loader;

	@Inject
	private ICodeElementService codeElementService;

	@Inject
	void activePatient(@Optional IPatient patient) {
		actPatient = patient;
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(tv.getControl())) {
				tv.setInput(Collections.emptyList());
			}
			ContextServiceHolder.get().getRootContext().removeTyped(IRecipe.class);
			if (patient != null) {
				addLineAction.setEnabled(false);
				printAction.setEnabled(false);
				master.setText(patient.getLabel());
			}
			refresh();
		});
	}

	@Inject
	void activeRecipe(@Optional IRecipe recipe) {
		Display.getDefault().asyncExec(() -> {
			if (tv != null && tv.getControl() != null && !tv.getControl().isDisposed()) {
				refreshRecipe();
			}
		});
	}

	@Optional
	@Inject
	void udpateRecipe(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IRecipe recipe) {
		if (tv != null && tv.getControl() != null && !tv.getControl().isDisposed()) {
			tv.refresh(true);
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		setTitleImage(Images.IMG_VIEW_RECIPES.getImage());
		parent.setLayout(new GridLayout());
		master = tk.createForm(parent);
		master.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		master.getBody().setLayout(new FillLayout());
		SashForm sash = new SashForm(master.getBody(), SWT.NONE);
		tv = new TableViewer(sash, SWT.V_SCROLL | SWT.FULL_SELECTION);
		tv.setContentProvider(ArrayContentProvider.getInstance());
		tv.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				if (element instanceof IRecipe) {
					IRecipe rp = (IRecipe) element;
					return rp.getLabel();
				}
				return element.toString();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object )
			 */
			@Override
			public Image getImage(Object element) {
				if (element instanceof IRecipe) {
					element = Rezept.load(((IRecipe) element).getId());
				}
				List<OutputLog> outputs = OutputLog.getOutputs((PersistentObject) element);
				if (outputs != null && !outputs.isEmpty()) {
					OutputLog o = outputs.get(0);
					String outputterID = o.getOutputterID();
					IOutputter io = OutputLog.getOutputter(outputterID);
					if (io != null) {
						return (Image) io.getSymbol();
					}
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IRecipe) {
					element = Rezept.load(((IRecipe) element).getId());
				}
				List<OutputLog> outputs = OutputLog.getOutputs((PersistentObject) element);
				if (outputs != null && !outputs.isEmpty()) {
					OutputLog o = outputs.get(0);
					String outputterID = o.getOutputterID();
					IOutputter io = OutputLog.getOutputter(outputterID);
					if (io != null) {
						return io.getInfo(element).orElse(null);
					}
				}
				return null;
			}
		});
		ColumnViewerToolTipSupport.enableFor(tv, ToolTip.NO_RECREATE);
		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getStructuredSelection().getFirstElement() instanceof IRecipe) {
					ContextServiceHolder.get().getRootContext()
							.setTyped(event.getStructuredSelection().getFirstElement());
				}
			}
		});
		lvRpLines = new ListViewer(sash);
		makeActions();
		menus = new ViewMenus(getViewSite());
		// menus.createToolbar(newRpAction, addLineAction, printAction );
		menus.createMenu(newRpAction, addLineAction, printAction, deleteRpAction);
		menus.createViewerContextMenu(lvRpLines, removeLineAction, changeMedicationAction);
		// make selection of prescription viewer available for commands of
		// context menu
		getSite().setSelectionProvider(lvRpLines);
		IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
		List<IAction> importers = Extensions.getClasses(Extensions.getExtensions(ExtensionPointConstantsUi.REZEPT_HOOK), // $NON-NLS-1$
				"RpToolbarAction", false); //$NON-NLS-1$
		for (IAction ac : importers) {
			tm.add(ac);
		}
		if (!importers.isEmpty()) {
			tm.add(new Separator());
		}
		tm.add(newRpAction);
		tm.add(addLineAction);
		tm.add(printAction);
		tv.setInput(getViewSite());

		/* Implementation Drag&Drop */
		GenericObjectDropTarget.IReceiver dtr = new GenericObjectDropTarget.IReceiver() {

			@Override
			public void dropped(List<Object> list, DropTargetEvent e) {
				for (Object obj : list) {
					IRecipe recipe = ContextServiceHolder.get().getTyped(IRecipe.class).orElse(null);
					if (recipe == null) {
						SWTHelper.showError(Messages.RezepteView_NoPrescriptionSelected, // $NON-NLS-1$
								Messages.RezepteView_PleaseChoosaAPrescription); // $NON-NLS-1$
						return;
					}

					if (obj instanceof IArticle) {
						IArticle art = (IArticle) obj;
						IPrescription ret = new IPrescriptionBuilder(CoreModelServiceHolder.get(),
								ContextServiceHolder.get(), art, recipe.getPatient(), StringUtils.EMPTY).build();
						ret.setRemark(StringUtils.EMPTY);
						ret.setEntryType(EntryType.RECIPE);
						ret.setRecipe(recipe);
						CoreModelServiceHolder.get().save(ret);

						refreshRecipe();
					} else if (obj instanceof IPrescription) {
						IPrescription pre = (IPrescription) obj;

						IPrescription ret = new IPrescriptionBuilder(CoreModelServiceHolder.get(),
								ContextServiceHolder.get(), pre.getArticle(), recipe.getPatient(),
								pre.getDosageInstruction()).build();
						ret.setRemark(pre.getRemark());
						ret.setEntryType(EntryType.RECIPE);
						ret.setRecipe(recipe);
						CoreModelServiceHolder.get().save(ret);

						refreshRecipe();
					}
				}
			}

			@Override
			public boolean accept(List<Object> list) {
				return true;
			}
		};

		// final TextTransfer textTransfer = TextTransfer.getInstance();
		// Transfer[] types = new Transfer[] {textTransfer};
		dropTarget = new GenericObjectDropTarget("Rezept", lvRpLines.getControl(), dtr); //$NON-NLS-1$

		lvRpLines.setContentProvider(new RezeptContentProvider());
		lvRpLines.setLabelProvider(new RezeptLabelProvider());
		lvRpLines.getControl().setToolTipText(Messages.RezepteView_DragMedicamentsHere); // $NON-NLS-1$
		/* lvRpLines.addDragSupport(DND.DROP_COPY,types, */
		new GenericObjectDragSource(lvRpLines);
		lvRpLines.setInput(getViewSite());
		addLineAction.setEnabled(false);
		printAction.setEnabled(false);

		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				try {
					RezeptBlatt rp = (RezeptBlatt) getViewSite().getPage().showView(RezeptBlatt.ID);
					if (event.getSelection() instanceof StructuredSelection) {
						IRecipe recipe = (IRecipe) ((StructuredSelection) event.getSelection()).getFirstElement();
						if (recipe != null) {
							Rezept rezept = Rezept.load(recipe.getId());
							IDocumentLetter document = recipe.getDocument();
							if (document != null) {
								// existing - just reads prescriptiom and opens
								// RezeptBlatt
								rp.loadRezeptFromDatabase(rezept, Brief.load(document.getId()));
							} else {
								// not existing - create prescription and opens
								// RezeptBlatt
								ElexisEventDispatcher.fireSelectionEvent(rezept);
								rp.createRezept(rezept);
								CoreModelServiceHolder.get().refresh(recipe, true);
								refresh();
							}
						}
					}
				} catch (Throwable ex) {
					ExHandler.handle(ex);
				}
			}

		});
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
	}

	@Override
	public void refresh() {
		if (CoreUiUtil.isActiveControl(tv.getControl())) {
			if (loader != null) {
				loader.cancel();
			}
			loader = new RecipeLoader(tv, actPatient);
			loader.schedule();
		}
	}

	public void refreshRecipe() {
		IRecipe recipe = ContextServiceHolder.get().getTyped(IRecipe.class).orElse(null);
		if (recipe == null) {
			lvRpLines.refresh(true);
			addLineAction.setEnabled(false);
			printAction.setEnabled(false);
		} else {
			lvRpLines.refresh(true);
			addLineAction.setEnabled(true);
			printAction.setEnabled(true);
		}
	}

	private void makeActions() {
		newRpAction = new Action(Messages.RezepteView_newPrescriptionAction) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.RezepteView_newPrescriptonTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				IPatient patient = ContextServiceHolder.get().getActivePatient().orElse(null);
				if (patient == null) {
					MessageBox mb = new MessageBox(getViewSite().getShell(), SWT.ICON_INFORMATION | SWT.OK);
					mb.setText(Messages.RezepteView_newPrescriptionError); // $NON-NLS-1$
					mb.setMessage(Messages.RezepteView_noPatientSelected); // $NON-NLS-1$
					mb.open();
					return;
				}
				ICoverage coverage = ContextServiceHolder.get().getActiveCoverage().orElse(null);
				if (coverage == null) {
					IEncounter k = EncounterServiceHolder.get().getLatestEncounter(patient).orElse(null);
					if (k == null) {
						SWTHelper.alert(Messages.Core_No_case_selected, // $NON-NLS-1$
								Messages.RezepteView_pleaseCreateOrChooseCase); // $NON-NLS-1$
						return;
					}
				}
				IRecipe recipe = new IRecipeBuilder(CoreModelServiceHolder.get(), patient,
						ContextServiceHolder.get().getActiveMandator().orElse(null)).buildAndSave();
				refresh();
				doSelectNewRezept(recipe);
				doAddLine();
			}
		};
		deleteRpAction = new Action(Messages.RezepteView_deletePrescriptionActiom) {
			@Override
			public void run() {
				IRecipe recipe = ContextServiceHolder.get().getTyped(IRecipe.class).orElse(null);
				if (MessageDialog.openConfirm(getViewSite().getShell(), Messages.RezepteView_deletePrescriptionActiom,
						MessageFormat.format(Messages.RezepteView_deletePrescriptionConfirm, recipe.getDate()))) {
					CoreModelServiceHolder.get().delete(recipe);
					refresh();
				}
			}
		};
		removeLineAction = new Action(Messages.RezepteView_deleteLineAction) { // $NON-NLS-1$
			@Override
			public void run() {
				IRecipe rp = ContextServiceHolder.get().getTyped(IRecipe.class).orElse(null);
				IStructuredSelection sel = (IStructuredSelection) lvRpLines.getSelection();
				IPrescription p = (IPrescription) sel.getFirstElement();
				if ((rp != null) && (p != null)) {
					rp.removePrescription(p);
					lvRpLines.refresh();
				}
				/*
				 * RpZeile z=(RpZeile)sel.getFirstElement(); if((rp!=null) && (z!=null)){
				 * rp.removeLine(z); lvRpLines.refresh(); }
				 */
			}
		};
		addLineAction = new Action(Messages.RezepteView_newLineAction) { // $NON-NLS-1$
			@Override
			public void run() {
				doAddLine();
			}
		};
		printAction = new Action(Messages.Core_Print) { // $NON-NLS-1$
			@Override
			public void run() {
				try {
					IRecipe recipe = ContextServiceHolder.get().getTyped(IRecipe.class).orElse(null);
					if (recipe != null) {
						Rezept rezept = Rezept.load(recipe.getId());
						RezeptBlatt rp = (RezeptBlatt) getViewSite().getPage().showView(RezeptBlatt.ID);
						IDocumentLetter document = recipe.getDocument();
						if (document == null) {
							// not yet created - just create a new Rezept
							ElexisEventDispatcher.fireSelectionEvent(rezept);
							rp.createRezept(rezept);
						} else {
							Brief rpBrief = Brief.load(document.getId());
							// Brief for Rezept already exists:
							// ask if it should be recreated or just shown
							String[] dialogButtonLabels = { Messages.RezepteView_RecreatePrescription,
									Messages.RezepteView_ShowPrescription, Messages.Core_Abort };
							MessageDialog msg = new MessageDialog(null, Messages.RezepteView_CreatePrescription, // $NON-NLS-1$
									null, Messages.RezepteView_ReallyWantToRecreatePrescription, // $NON-NLS-1$
									MessageDialog.WARNING, dialogButtonLabels, 2);
							int result = msg.open();
							switch (result) {
							case 0: // recreate rezept
								ElexisEventDispatcher.fireSelectionEvent(rezept);
								rp.createRezept(rezept);
								break;
							case 1: // open rezept
								rp.loadRezeptFromDatabase(rezept, rpBrief);
								break;
							case 2: // cancel or closebox - do nothing
								break;
							}
						}
						CoreModelServiceHolder.get().refresh(recipe, true);
						refresh();
					}
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		};
		changeMedicationAction = new RestrictedAction(EvACE.of(IPrescription.class, Right.UPDATE),
				Messages.Core_DoChange_ellipsis) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.RezepteView_ChangeTooltip); // $NON-NLS-1$
			}

			@Override
			public void doRun() {
				IStructuredSelection sel = (IStructuredSelection) lvRpLines.getSelection();
				IPrescription pr = (IPrescription) sel.getFirstElement();
				if (pr != null) {
					new MediDetailDialog(getViewSite().getShell(), pr).open();
					CoreModelServiceHolder.get().save(pr);
					refreshRecipe();
				}
			}
		};
		addLineAction.setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
		printAction.setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
		deleteRpAction.setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
	}

	private void doSelectNewRezept(IRecipe rezept) {
		tv.getTable().setFocus();
		tv.setSelection(new StructuredSelection(rezept), true);
		ContextServiceHolder.get().getRootContext().setTyped(rezept);
	}

	private void doAddLine() {
		try {
			LeistungenView lv1 = (LeistungenView) getViewSite().getPage().showView(LeistungenView.ID);
			CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
			CTabItem[] tabItems = lv1.ctab.getItems();

			List<ICodeElementServiceContribution> articleCodeContributions = codeElementService
					.getContributionsByTyp(CodeElementTyp.ARTICLE);

			for (CTabItem tab : tabItems) {
				if (tab.getData() instanceof ICodeElement) {
					ICodeElement ics = (ICodeElement) tab.getData();
					if (ics instanceof Artikel) {
						lv1.ctab.setSelection(tab);
						lv1.setSelected(tab);
						break;
					}
				} else if (tab.getData() instanceof CodeSystemDescription) {
					CodeSystemDescription desc = (CodeSystemDescription) tab.getData();
					if (articleCodeContributions.stream().filter(ac -> ac.getSystem().equals(desc.getCodeSystemName()))
							.findFirst().isPresent()) {
						lv1.ctab.setSelection(tab);
						lv1.setSelected(tab);
						break;
					}
				}
			}
			lv1.setFocus();
		} catch (PartInitException ex) {
			ExHandler.handle(ex);
		}
	}

	private static class RezeptContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(final Object inputElement) {
			IRecipe recipe = ContextServiceHolder.get().getTyped(IRecipe.class).orElse(null);
			if (recipe == null) {
				return new Prescription[0];
			}
			List<IPrescription> list = recipe.getPrescriptions();
	
			return sortPrescriptions(list).toArray();
		}
		
		private List<IPrescription> sortPrescriptions(List<IPrescription> prescriptions) {
			Collections.sort(prescriptions, new Comparator<IPrescription>() {
		      @Override
		      public int compare(IPrescription pres1, IPrescription pres2) {
		          return pres1.getLabel().compareTo(pres2.getLabel());
		       }
		    });
			return prescriptions;
		}

		@Override
		public void dispose() { /* leer */
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) { /* leer */
		}
	}

	private static class RezeptLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof IPrescription) {
				IPrescription z = (IPrescription) element;
				return z.getLabel();
			}
			return "?"; //$NON-NLS-1$
		}

	}

	public void clearEvent(final Class<? extends PersistentObject> template) {
		lvRpLines.refresh();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	private static class RecipeLoader extends Job {
		private Viewer viewer;
		private IPatient patient;

		private List<ch.elexis.core.model.IRecipe> loaded;

		public RecipeLoader(Viewer viewer, IPatient patient) {
			super("Recipe loading ...");
			this.viewer = viewer;
			this.patient = patient;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Recipe loading ...", IProgressMonitor.UNKNOWN);
			if (patient != null) {
				INamedQuery<IRecipe> query = CoreModelServiceHolder.get().getNamedQuery(IRecipe.class, "patient"); //$NON-NLS-1$
				loaded = query.executeWithParameters(query.getParameterMap("patient", patient)); //$NON-NLS-1$
			} else {
				loaded = Collections.emptyList();
			}

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.setInput(loaded);
					}
				}
			});
			return Status.OK_STATUS;
		}
	}
}
