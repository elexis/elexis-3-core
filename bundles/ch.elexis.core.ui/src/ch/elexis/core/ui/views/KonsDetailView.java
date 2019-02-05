/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockedAction;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.locks.ToggleCurrentKonsultationLockHandler;
import ch.elexis.core.ui.services.BillingServiceHolder;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.CoverageComparator;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;

/**
 * Behandlungseintrag, Diagnosen und Verrechnung Dg und Verrechnung können wie Drag&Drop aus den
 * entsprechenden Listen.Views auf die Felder gezogen werden.
 * 
 * @author gerry
 * 
 */
public class KonsDetailView extends ViewPart
		implements IActivationListener, ISaveablePart2, IUnlockable {
	private static final String NO_CONS_SELECTED = Messages.KonsDetailView_NoConsSelected; // $NON-NLS-1$
	public static final String ID = "ch.elexis.Konsdetail"; //$NON-NLS-1$
	public static final String CFG_VERTRELATION = "vertrelation"; //$NON-NLS-1$
	public static final String CFG_HORIZRELATION = "horizrelation"; //$NON-NLS-1$
	
	private Logger log = LoggerFactory.getLogger(KonsDetailView.class);
	Hashtable<String, IKonsExtension> hXrefs;
	EnhancedTextField text;
	private Label lBeh;
	Hyperlink hlMandant;
	ComboViewer comboViewerFall;
	private IEncounter actEncounter;
	FormToolkit tk;
	Form form;
	IPatient actPat;
	Color defaultBackground;
	
	private DiagnosenDisplay diagnosesDisplay;
	private VerrechnungsDisplay billedDisplay;
	private Action versionBackAction;
	private LockedAction<IEncounter> saveAction;
	private RestrictedAction purgeAction;
	Action versionFwdAction, assignStickerAction, versionDisplayAction;
	int displayedVersion;
	Font emFont;
	Composite cDesc;
	Composite cEtiketten;
	private int[] sashWeights = null;
	private SashForm sash;
	private int[] diagAndChargeSashWeights = null;
	private SashForm diagAndChargeSash;
	private ComboFallSelectionListener comboFallSelectionListener;
	
	private boolean created = false;
	
	@Inject
	void udpatePatient(@Optional @UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPatient patient){
		if (created) {
			actPat = null; // make sure patient will be updated
			setPatient(patient);
		}
	}
	
	@Inject
	void reloadPatient(@Optional @UIEventTopic(ElexisEventTopics.EVENT_RELOAD) IPatient patient){
		if (created) {
			actPat = null; // make sure patient will be updated
			setPatient(patient);
		}
	}
	
	@Inject
	void activePatient(@Optional @Named(IContext.ACTIVE_PATIENT) IPatient patient){
		if (created) {
			Display.getDefault().asyncExec(() -> {
				actPat = null; // make sure patient will be updated
				setPatient(patient);
			});
		}
	}
	
	//	private final ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
	//		ElexisEvent.EVENT_UPDATE | ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_RELOAD) {
	//		@Override
	//		public void runInUi(ElexisEvent ev){
	//			actPat = null; // make sure patient will be updated
	//			setPatient((Patient) ev.getObject());
	//		};
	//	};
	
	@Inject
	void changedMandator(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_USER_CHANGED) IContact mandator){
		if (created) {
			Display.getDefault().asyncExec(() -> {
				adaptMenus();
			});
		}
	}
	
	//	private final ElexisEventListener eeli_user =
	//		new ElexisUiEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
	//			@Override
	//			public void runInUi(ElexisEvent ev){
	//				adaptMenus();
	//			}
	//		};
	
	@Inject
	void reloadCoverage(@Optional @UIEventTopic(ElexisEventTopics.EVENT_RELOAD) ICoverage coverage){
		if (created) {
			updateFallCombo();
		}
	}
	
	//	private final ElexisEventListener eeli_fall = new ElexisUiEventListenerImpl(Fall.class,
	//		ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_DELETE) {
	//		@Override
	//		public void runInUi(ElexisEvent ev){
	//			updateFallCombo();
	//		};
	//	};
	
	@Inject
	void lockPreRelease(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_LOCK_PRERELEASE) IEncounter encounter){
		if (created) {
			if (encounter.equals(actEncounter)) {
				save();
			}
		}
	}
	
	//	private final ElexisEventListener eeli_kons_sync =
	//		new ElexisUiSyncEventListenerImpl(Konsultation.class, ElexisEvent.EVENT_LOCK_PRERELEASE) {
	//			@Override
	//			public void runInUi(ElexisEvent ev){
	//				Konsultation kons = (Konsultation) ev.getObject();
	//				if (kons.equals(actEncounter)) {
	//					save();
	//				}
	//			}
	//		};
	
	@Inject
	void selectedEncounter(@Optional IEncounter encounter){
		if (created) {
			Display.getDefault().asyncExec(() -> {
				if (encounter != null) {
					// ElexisEvent.EVENT_SELECTED
					IEncounter deselectedKons = actEncounter;
					setKons(encounter);
					releaseAndRefreshLock(deselectedKons,
						ToggleCurrentKonsultationLockHandler.COMMAND_ID);
					setKons(encounter);
				} else {
					// ElexisEvent.EVENT_DESELECTED
					IEncounter deselectedKons = actEncounter;
					setKons(null);
					releaseAndRefreshLock(deselectedKons,
						ToggleCurrentKonsultationLockHandler.COMMAND_ID);
				}
			});
		}
	}
	
	@Inject
	void udpateEncounter(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IEncounter encounter){
		if (created) {
			if (encounter != null && encounter.equals(actEncounter)) {
				setKons(encounter);
			}
		}
	}
	
	@Inject
	void lockedEncounter(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_LOCK_AQUIRED) IEncounter encounter){
		if (created) {
			if (encounter.equals(actEncounter)) {
				setUnlocked(true);
			}
		}
	}
	
	@Inject
	void unlockedEncounter(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_LOCK_RELEASED) IEncounter encounter){
		if (created) {
			if (encounter.equals(actEncounter)) {
				setUnlocked(false);
			}
		}
	}
	
	//	private final ElexisEventListener eeli_kons =
	//		new ElexisUiEventListenerImpl(Konsultation.class,
	//			ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED
	//				| ElexisEvent.EVENT_LOCK_AQUIRED | ElexisEvent.EVENT_LOCK_RELEASED
	//				| ElexisEvent.EVENT_UPDATE) {
	//			@Override
	//			public void runInUi(ElexisEvent ev){
	//				Konsultation kons = (Konsultation) ev.getObject();
	//				Konsultation deselectedKons = null;
	//				switch (ev.getType()) {
	//				case ElexisEvent.EVENT_SELECTED:
	//					deselectedKons = actEncounter;
	//					setKons(kons);
	//					releaseAndRefreshLock(deselectedKons,
	//						ToggleCurrentKonsultationLockHandler.COMMAND_ID);
	//					break;
	//				case ElexisEvent.EVENT_UPDATE:
	//					if (kons != null && kons.equals(actEncounter)) {
	//						setKons(kons);
	//					}
	//					break;
	//				case ElexisEvent.EVENT_DESELECTED:
	//					deselectedKons = actEncounter;
	//					setKons(null);
	//					releaseAndRefreshLock(deselectedKons,
	//						ToggleCurrentKonsultationLockHandler.COMMAND_ID);
	//					break;
	//				case ElexisEvent.EVENT_LOCK_AQUIRED:
	//				case ElexisEvent.EVENT_LOCK_RELEASED:
	//					if (kons.equals(actEncounter)) {
	//						setUnlocked(ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED);
	//					}
	//					break;
	//				default:
	//					break;
	//				}
	//			}
	//		};
	
	private void releaseAndRefreshLock(Object object, String commandId){
		if (object != null && LocalLockServiceHolder.get().isLockedLocal(object)) {
			LocalLockServiceHolder.get().releaseLock(object);
		}
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
	}
	
	@Override
	public void saveState(IMemento memento){
		int[] w = sash.getWeights();
		memento.putString(CFG_VERTRELATION,
			Integer.toString(w[0]) + StringConstants.COMMA + Integer.toString(w[1]));
		
		w = diagAndChargeSash.getWeights();
		memento.putString(CFG_HORIZRELATION,
			Integer.toString(w[0]) + StringConstants.COMMA + Integer.toString(w[1]));
		
		super.saveState(memento);
	}
	
	@Override
	public void setUnlocked(boolean unlocked){
		boolean hlMandantEnabled =
			actEncounter != null && BillingServiceHolder.get().isEditable(actEncounter).isOK()
				&& CoreHub.acl.request(AccessControlDefaults.KONS_REASSIGN) && unlocked;
		hlMandant.setEnabled(hlMandantEnabled);
		boolean cbFallEnabled = actEncounter != null
			&& BillingServiceHolder.get().isEditable(actEncounter).isOK() && unlocked;
		comboViewerFall.getCombo().setEnabled(cbFallEnabled);
		text.setEditable(unlocked);
		
		// update the UI
		IToolBarManager mgr = ((IViewSite) getSite()).getActionBars().getToolBarManager();
		IContributionItem[] items = mgr.getItems();
		for (IContributionItem iContributionItem : items) {
			iContributionItem.update();
		}
	}
	
	@Override
	public void createPartControl(final Composite p){
		setTitleImage(Images.IMG_VIEW_CONSULTATION_DETAIL.getImage());
		sash = new SashForm(p, SWT.VERTICAL);
		
		tk = UiDesk.getToolkit();
		form = tk.createForm(sash);
		form.getBody().setLayout(new GridLayout(1, true));
		form.setText(NO_CONS_SELECTED);
		cEtiketten = new Composite(form.getBody(), SWT.NONE);
		cEtiketten.setLayout(new RowLayout(SWT.HORIZONTAL));
		cEtiketten.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cDesc = new Composite(form.getBody(), SWT.NONE);
		cDesc.setLayout(new RowLayout(SWT.HORIZONTAL));
		cDesc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lBeh = tk.createLabel(cDesc, NO_CONS_SELECTED);
		emFont = UiDesk.getFont("Helvetica", 11, SWT.BOLD); //$NON-NLS-1$
		lBeh.setFont(emFont);
		defaultBackground = p.getBackground();
		// lBeh.setBackground();
		hlMandant = tk.createHyperlink(cDesc, "--", SWT.NONE); //$NON-NLS-1$
		hlMandant.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				// CommonViewer of KontaktSelektor will set Mandant selection of ElexisEventDispatcher
				// we want do reset to current mandant afterwards
				Mandant currentMandant = ElexisEventDispatcher.getSelectedMandator();
				KontaktSelektor ksl = new KontaktSelektor(getSite().getShell(), Mandant.class,
					Messages.KonsDetailView_SelectMandatorCaption, // $NON-NLS-1$
					Messages.KonsDetailView_SelectMandatorBody, new String[] {
						Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2
				}); // $NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					IMandator mandator = CoreModelServiceHolder.get()
						.load(((Mandant) ksl.getSelection()).getId(), IMandator.class).orElse(null);
					if (mandator != null) {
						actEncounter.setMandator(mandator);
					}
					setKons(actEncounter);
				}
				ElexisEventDispatcher.fireSelectionEvent(currentMandant);
			}
			
		});
		hlMandant.setBackground(p.getBackground());
		
		comboViewerFall = new ComboViewer(form.getBody(), SWT.SINGLE);
		comboViewerFall.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerFall.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((ICoverage) element).getLabel();
			}
		});
		
		comboFallSelectionListener = new ComboFallSelectionListener();
		comboViewerFall.addSelectionChangedListener(comboFallSelectionListener);
		
		GridData gdFall = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		comboViewerFall.getCombo().setLayoutData(gdFall);
		
		text = new EnhancedTextField(form.getBody());
		hXrefs = new Hashtable<String, IKonsExtension>();
		@SuppressWarnings("unchecked")
		List<IKonsExtension> xrefs =
			Extensions.getClasses(Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION),
				"KonsExtension", false); //$NON-NLS-1$ //$NON-NLS-2$
		for (IKonsExtension x : xrefs) {
			String provider = x.connect(text);
			hXrefs.put(provider, x);
		}
		text.setXrefHandlers(hXrefs);
		
		@SuppressWarnings("unchecked")
		List<IKonsMakro> makros = Extensions.getClasses(
			Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION), "KonsMakro", false); //$NON-NLS-1$ //$NON-NLS-2$
		text.setExternalMakros(makros);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL
			| GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
		text.setLayoutData(gd);
		tk.adapt(text);
		diagAndChargeSash = new SashForm(sash, SWT.HORIZONTAL);
		
		Composite botleft = tk.createComposite(diagAndChargeSash);
		botleft.setLayout(new GridLayout(1, false));
		Composite botright = tk.createComposite(diagAndChargeSash);
		botright.setLayout(new GridLayout(1, false));
		
		diagnosesDisplay = new DiagnosenDisplay(getSite().getPage(), botleft, SWT.NONE);
		CoreUiUtil.injectServices(diagnosesDisplay);
		diagnosesDisplay.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		billedDisplay = new VerrechnungsDisplay(getSite().getPage(), botright, SWT.NONE);
		CoreUiUtil.injectServices(billedDisplay);
		billedDisplay.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		getSite().registerContextMenu(ID + ".VerrechnungsDisplay", billedDisplay.getMenuManager(),
			billedDisplay.getViewer());
		getSite().setSelectionProvider(billedDisplay.getViewer());
		diagAndChargeSash.setWeights(diagAndChargeSashWeights == null ? new int[] {
			40, 60
		} : diagAndChargeSashWeights);
		
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createMenu(versionDisplayAction, versionFwdAction, versionBackAction,
			GlobalActions.neueKonsAction, GlobalActions.delKonsAction, GlobalActions.redateAction,
			assignStickerAction, purgeAction);
		
		sash.setWeights(sashWeights == null ? new int[] {
			80, 20
		} : sashWeights);
		
		menu.createToolbar(GlobalActions.neueKonsAction, saveAction);
		//		GlobalEventDispatcher.addActivationListener(this, this);
		text.connectGlobalActions(getViewSite());
		adaptMenus();
		// initialize with currently selected encounter
		created = true;
		ContextServiceHolder.get().getTyped(IEncounter.class).ifPresent(e -> selectedEncounter(e));
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException{
		
		if (memento == null) {
			sashWeights = new int[] {
				80, 20
			};
			diagAndChargeSashWeights = new int[] {
				40, 60
			};
		} else {
			String state = memento.getString(CFG_VERTRELATION);
			if (state == null) {
				state = "80,20"; //$NON-NLS-1$
			}
			String[] sw = state.split(StringConstants.COMMA);
			sashWeights = new int[] {
				Integer.parseInt(sw[0]), Integer.parseInt(sw[1])
			};
			
			state = memento.getString(CFG_HORIZRELATION);
			if (state == null) {
				state = "40,60"; //$NON-NLS-1$
			}
			sw = state.split(StringConstants.COMMA);
			diagAndChargeSashWeights = new int[] {
				Integer.parseInt(sw[0]), Integer.parseInt(sw[1])
			};
			
		}
		super.init(site, memento);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose(){
		created = false;
		//		GlobalEventDispatcher.removeActivationListener(this, this);
		if (text != null) {
			text.disconnectGlobalActions(getViewSite());
		}
		// emFont.dispose();
		super.dispose();
	}
	
	/** Aktuellen patient setzen */
	private synchronized void setPatient(IPatient pat){
		if (pat != null && actPat != null) {
			if (pat.getId().equals(actPat.getId())) {
				if (!form.getText().equals(Messages.KonsDetailView_NoConsSelected)) {
					return;
				}
			}
		}
		for (Control cc : cEtiketten.getChildren()) {
			cc.dispose();
		}
		actPat = pat;
		if (pat != null) {
			form.setText(pat.getLabel() + StringTool.space + "(" + pat.getAgeInYears() + ")");
			// TODO enable with new ISticker ...
			//			List<ISticker> etis = pat.getStickers();
			//			if (etis != null && etis.size() > 0) {
			//				// Point size = form.getHead().getSize();
			//				for (ISticker et : etis) {
			//					if (et != null) {
			//						new UiSticker((Sticker) et).createForm(cEtiketten);
			//					}
			//				}
			//			}
			updateFallCombo();
		}
		form.layout();
	}
	
	private void updateFallCombo(){
		IPatient pat = ContextServiceHolder.get().getRootContext().getActivePatient().orElse(null);
		if (pat != null && comboViewerFall != null) {
			List<ICoverage> coverages = pat.getCoverages();
			Collections.sort(coverages, new CoverageComparator());
			comboViewerFall.setInput(coverages);
			if (actEncounter != null) {
				comboFallSelectionListener.ignoreSelectionEventOnce();
				comboViewerFall.setSelection(new StructuredSelection(actEncounter.getCoverage()));
			}
		}
	}
	
	@Override
	public void setFocus(){
		text.setFocus();
	}
	
	/**
	 * Aktuelle Konsultation setzen.
	 */
	private synchronized void setKons(final IEncounter encounter){
		if (actEncounter != null && text.isDirty()) {
			actEncounter.getVersionedEntry().update(text.getContentsAsXML(), getVersionRemark());
			CoreModelServiceHolder.get().save(actEncounter);
		}
		
		if (encounter != null) {
			ICoverage coverage = encounter.getCoverage();
			setPatient(coverage.getPatient());
			setKonsText(encounter, encounter.getVersionedEntry().getHeadVersion());
			
			comboFallSelectionListener.ignoreSelectionEventOnce();
			comboViewerFall.setSelection(new StructuredSelection(coverage));
			comboViewerFall.getCombo().setEnabled(coverage.isOpen());
			IMandator mandator = encounter.getMandator();
			String encounterDate = CoreUtil.defaultDateFormat(encounter.getDate());
			lBeh.setText(encounterDate + " (" //$NON-NLS-1$
				+ new TimeTool(encounter.getDate()).getDurationToNowString() + ")"); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			if (mandator == null) {
				sb.append(Messages.KonsDetailView_NotYours); // $NON-NLS-1$
				hlMandant.setBackground(hlMandant.getParent().getBackground());
			} else {
				IContact biller = mandator.getBiller();
				if (biller.getId().equals(mandator.getId())) {
					sb.append("(").append(mandator.getDescription3()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sb.append("(").append(mandator.getDescription3()).append("/").append( //$NON-NLS-1$ //$NON-NLS-2$
						biller.getDescription3()).append(")"); //$NON-NLS-1$
				}
				hlMandant
					.setBackground(UiMandant.getColorForMandator(Mandant.load(mandator.getId())));
			}
			hlMandant.setText(sb.toString());
			
			boolean hlMandantEnabled = BillingServiceHolder.get().isEditable(encounter).isOK()
				&& CoreHub.acl.request(AccessControlDefaults.KONS_REASSIGN);
			hlMandant.setEnabled(hlMandantEnabled);
			diagnosesDisplay.setEncounter(CoreModelServiceHolder.get()
				.load(encounter.getId(), IEncounter.class).orElse(null));
			billedDisplay.setEncounter(CoreModelServiceHolder.get()
				.load(encounter.getId(), IEncounter.class).orElse(null));
			billedDisplay.setEnabled(true);
			diagnosesDisplay.setEnabled(true);
			if (BillingServiceHolder.get().isEditable(encounter).isOK()) {
				text.setEnabled(true);
				text.setToolTipText("");
				lBeh.setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
				lBeh.setBackground(defaultBackground);
			} else {
				text.setToolTipText("Konsultation geschlossen oder nicht von Ihnen");
				lBeh.setForeground(UiDesk.getColor(UiDesk.COL_GREY60));
				lBeh.setBackground(UiDesk.getColor(UiDesk.COL_GREY20));
			}
			if (encounter.getDate().isEqual(LocalDate.now())) {
				text.setTextBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			} else {
				text.setTextBackground(UiDesk.getColorFromRGB("FAFAFA"));
			}
		} else {
			form.setText(NO_CONS_SELECTED);
			lBeh.setText("-"); //$NON-NLS-1$
			hlMandant.setText("--"); //$NON-NLS-1$
			hlMandant.setEnabled(false);
			hlMandant.setBackground(hlMandant.getParent().getBackground());
			diagnosesDisplay.clear();
			billedDisplay.clear();
			text.setText(""); //$NON-NLS-1$
			text.setEnabled(false);
			billedDisplay.setEnabled(false);
			diagnosesDisplay.setEnabled(false);
		}
		actEncounter = encounter;
		cDesc.layout();
		
		if (actEncounter == null) {
			setUnlocked(false);
		} else {
			setUnlocked(LocalLockServiceHolder.get().isLockedLocal(actEncounter));
		}
	}
	
	private String getVersionRemark(){
		String remark = "edit";
		java.util.Optional<IUser> activeUser =
			ContextServiceHolder.get().getRootContext().getActiveUser();
		if (activeUser.isPresent()) {
			remark = activeUser.get().getLabel();
		}
		return remark;
	}
	
	void setKonsText(final IEncounter encounter, final int version){
		String ntext = ""; //$NON-NLS-1$
		if ((version >= 0) && (version <= encounter.getVersionedEntry().getHeadVersion())) {
			VersionedResource vr = encounter.getVersionedEntry();
			ResourceItem entry = vr.getVersion(version);
			ntext = entry.data;
			StringBuilder sb = new StringBuilder();
			sb.append("rev. ").append(version).append(Messages.KonsDetailView_of) //$NON-NLS-1$
				.append( //$NON-NLS-2$
					new TimeTool(entry.timestamp).toString(TimeTool.FULL_GER))
				.append(" (").append(entry.remark).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			versionDisplayAction.setText(sb.toString());
		} else {
			versionDisplayAction.setText("");
		}
		text.setText(ntext);
		text.setKons(encounter);
		displayedVersion = version;
		versionBackAction.setEnabled(version != 0);
		versionFwdAction.setEnabled(version != encounter.getVersionedEntry().getHeadVersion());
	}
	
	private void makeActions(){
		
		versionDisplayAction = new Action() {
			private String versionText;
			
			@Override
			public String getText(){
				return versionText;
			}
			
			@Override
			public void setText(String text){
				versionText = text;
			}
			
			@Override
			public boolean isEnabled(){
				return false;
			}
		};
		
		purgeAction = new LockedRestrictedAction<IEncounter>(AccessControlDefaults.AC_PURGE,
			Messages.KonsDetailView_PurgeOldEntries) {
			
			@Override
			public IEncounter getTargetedObject(){
				return actEncounter;
			}
			
			@Override
			public void doRun(IEncounter element){
				element.getVersionedEntry().purge();
				ElexisEventDispatcher.fireSelectionEvent(Konsultation.load(element.getId()));
			}
			
		};
		versionBackAction = new Action(Messages.KonsDetailView_PreviousEntry) { // $NON-NLS-1$
			
			@Override
			public void run(){
				if (actEncounter == null) {
					return;
				}
				if (MessageDialog.openConfirm(getViewSite().getShell(),
					Messages.KonsDetailView_ReplaceKonsTextCaption, // $NON-NLS-1$
					Messages.KonsDetailView_ReplaceKonsTextBody)) { // $NON-NLS-1$
					setKonsText(actEncounter, displayedVersion - 1);
					text.setDirty(true);
				}
			}
			
		};
		versionFwdAction = new Action(Messages.KonsDetailView_nextEntry) { // $NON-NLS-1$
			@Override
			public void run(){
				if (actEncounter == null) {
					return;
				}
				if (MessageDialog.openConfirm(getViewSite().getShell(),
					Messages.KonsDetailView_ReplaceKonsTextCaption, // $NON-NLS-1$
					Messages.KonsDetailView_ReplaceKonsTextBody2)) { // $NON-NLS-1$
					setKonsText(actEncounter, displayedVersion + 1);
					text.setDirty(true);
				}
			}
		};
		saveAction = new LockedAction<IEncounter>(Messages.KonsDetailView_SaveEntry) {
			{
				setImageDescriptor(Images.IMG_DISK.getImageDescriptor());
				setToolTipText(Messages.KonsDetailView_SaveExplicit); // $NON-NLS-1$
			}
			
			@Override
			public IEncounter getTargetedObject(){
				return actEncounter;
			}
			
			@Override
			public void doRun(IEncounter element){
				save();
			}
		};
		
		versionFwdAction.setImageDescriptor(Images.IMG_NEXT.getImageDescriptor());
		versionBackAction.setImageDescriptor(Images.IMG_PREVIOUS.getImageDescriptor());
		purgeAction.setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
		assignStickerAction = new Action("Sticker...") {
			{
				setToolTipText("Der Konsultation einer Sticker zuweisen");
			}
			
			@Override
			public void run(){
				if (actEncounter != null) {
					// TODO enable with new ISticker ... 
					//					AssignStickerDialog asd =
					//						new AssignStickerDialog(getViewSite().getShell(), actEncounter);
					//					asd.open();
				}
			}
		};
	}
	
	public void save(){
		if (actEncounter != null) {
			if (text.isDirty()) {
				actEncounter.getVersionedEntry().update(text.getContentsAsXML(),
					getVersionRemark());
				text.setDirty(false);
				CoreModelServiceHolder.get().save(actEncounter);
			}
			setKons(actEncounter);
		} else {
			log.warn("Save() actKons == null");
		}
	}
	
	public void adaptMenus(){
		billedDisplay.adaptMenus();
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir
	 * benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist. Gibt es da keine einfachere Methode?
	 */
	@Override
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	@Override
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	@Override
	public void doSaveAs(){ /* leer */
	}
	
	@Override
	public boolean isDirty(){
		return true;
	}
	
	@Override
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	@Override
	public void activation(boolean mode){
		if (mode == false) {
			// save entry on deactivation if text was edited
			if (actEncounter != null && (text.isDirty())) {
				actEncounter.getVersionedEntry().update(text.getContentsAsXML(),
					getVersionRemark());
				text.setDirty(false);
			}
		} else {
			// load newest version on activation, if there are no local changes
			if (actEncounter != null && !text.isDirty()) {
				setKonsText(actEncounter, actEncounter.getVersionedEntry().getHeadVersion());
			}
		}
	}
	
	@Override
	public void visible(boolean mode){
		if (mode == true) {
			adaptMenus();
		}
	}
	
	private class ComboFallSelectionListener implements ISelectionChangedListener {
		private boolean ignoreEventSelectionChanged;
		
		public void ignoreSelectionEventOnce(){
			this.ignoreEventSelectionChanged = true;
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			if (!ignoreEventSelectionChanged) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					if (!selection.isEmpty()) {
						ICoverage changeToCoverage =
							(ICoverage) ((StructuredSelection) selection).getFirstElement();
						
						ICoverage actCoverage = null;
						String fallLabel = "Current Case NOT found!!";//$NON-NLS-1$
						if (actEncounter != null) {
							actCoverage = actEncounter.getCoverage();
							fallLabel = actCoverage.getLabel();
						}
						
						if (!changeToCoverage.equals(actCoverage)) {
							if (!changeToCoverage.isOpen()) {
								SWTHelper.alert(Messages.KonsDetailView_CaseClosedCaption, // $NON-NLS-1$
									Messages.KonsDetailView_CaseClosedBody); // $NON-NLS-1$
							} else {
								MessageDialog msd = new MessageDialog(getViewSite().getShell(),
									Messages.KonsDetailView_ChangeCaseCaption, // $NON-NLS-1$
									Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize),
									MessageFormat.format(
										Messages.KonsDetailView_ConfirmChangeConsToCase,
										new Object[] {
											fallLabel, changeToCoverage.getLabel()
										}), MessageDialog.QUESTION, new String[] {
											Messages.KonsDetailView_Yes, // $NON-NLS-1$
											Messages.KonsDetailView_No
									}, 0); // $NON-NLS-1$
								if (msd.open() == Window.OK) {
									EncounterServiceHolder.get().transferToCoverage(actEncounter,
										changeToCoverage, false);
								} else {
									ignoreSelectionEventOnce();
									comboViewerFall
										.setSelection(new StructuredSelection(actCoverage));
								}
							}
						}
					}
				}
			}
			ignoreEventSelectionChanged = false;
		}
	}
}
