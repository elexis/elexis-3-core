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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
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
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.data.UiSticker;
import ch.elexis.core.ui.dialogs.AssignStickerDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.events.ElexisUiSyncEventListenerImpl;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockedAction;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.locks.ToggleCurrentKonsultationLockHandler;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.FallComparator;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Anwender;
import ch.elexis.data.Artikel;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.Sticker;
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
	private Konsultation actKons;
	FormToolkit tk;
	Form form;
	Patient actPat;
	Color defaultBackground;
	
	private DiagnosenDisplay dd;
	private VerrechnungsDisplay vd;
	private Action versionBackAction;
	private LockedAction<Konsultation> saveAction;
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
	
	private final ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
		ElexisEvent.EVENT_UPDATE | ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_RELOAD) {
		@Override
		public void runInUi(ElexisEvent ev){
			actPat = null; // make sure patient will be updated
			setPatient((Patient) ev.getObject());
		};
	};
	
	private final ElexisEventListener eeli_user =
		new ElexisUiEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			@Override
			public void runInUi(ElexisEvent ev){
				adaptMenus();
			}
		};
	
	private final ElexisEventListener eeli_fall = new ElexisUiEventListenerImpl(Fall.class,
		ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_DELETE) {
		@Override
		public void runInUi(ElexisEvent ev){
			updateFallCombo();
		};
	};
	
	private final ElexisEventListener eeli_kons_sync =
		new ElexisUiSyncEventListenerImpl(Konsultation.class, ElexisEvent.EVENT_LOCK_PRERELEASE) {
			@Override
			public void runInUi(ElexisEvent ev){
				Konsultation kons = (Konsultation) ev.getObject();
				if (kons.equals(actKons)) {
					save();
				}
			}
		};
	
	private final ElexisEventListener eeli_kons =
		new ElexisUiEventListenerImpl(Konsultation.class,
			ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED
				| ElexisEvent.EVENT_LOCK_AQUIRED | ElexisEvent.EVENT_LOCK_RELEASED
				| ElexisEvent.EVENT_UPDATE) {
			@Override
			public void runInUi(ElexisEvent ev){
				Konsultation kons = (Konsultation) ev.getObject();
				Konsultation deselectedKons = null;
				switch (ev.getType()) {
				case ElexisEvent.EVENT_SELECTED:
					deselectedKons = actKons;
					setKons(kons);
					releaseAndRefreshLock(deselectedKons,
						ToggleCurrentKonsultationLockHandler.COMMAND_ID);
					break;
				case ElexisEvent.EVENT_UPDATE:
					if (kons != null && kons.equals(actKons)) {
						setKons(kons);
					}
					break;
				case ElexisEvent.EVENT_DESELECTED:
					deselectedKons = actKons;
					setKons(null);
					releaseAndRefreshLock(deselectedKons,
						ToggleCurrentKonsultationLockHandler.COMMAND_ID);
					break;
				case ElexisEvent.EVENT_LOCK_AQUIRED:
				case ElexisEvent.EVENT_LOCK_RELEASED:
					if (kons.equals(actKons)) {
						setUnlocked(ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED);
					}
					break;
				default:
					break;
				}
			}
		};
	
	private void releaseAndRefreshLock(IPersistentObject object, String commandId){
		if (object != null && CoreHub.getLocalLockService().isLockedLocal(object)) {
			CoreHub.getLocalLockService().releaseLock(object);
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
		boolean hlMandantEnabled = actKons != null && actKons.isEditable(false)
			&& CoreHub.acl.request(AccessControlDefaults.KONS_REASSIGN) && unlocked;
		hlMandant.setEnabled(hlMandantEnabled);
		boolean cbFallEnabled = actKons != null && actKons.isEditable(false) && unlocked;
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
				KontaktSelektor ksl = new KontaktSelektor(getSite().getShell(), Mandant.class,
					Messages.KonsDetailView_SelectMandatorCaption, // $NON-NLS-1$
					Messages.KonsDetailView_SelectMandatorBody, new String[] {
						Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2
				}); // $NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					actKons.setMandant((Mandant) ksl.getSelection());
					setKons(actKons);
				}
			}
			
		});
		hlMandant.setBackground(p.getBackground());
		
		comboViewerFall = new ComboViewer(form.getBody(), SWT.SINGLE);
		comboViewerFall.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerFall.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((Fall) element).getLabel();
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
		
		dd = new DiagnosenDisplay(getSite().getPage(), botleft, SWT.NONE);
		dd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		vd = new VerrechnungsDisplay(getSite().getPage(), botright, SWT.NONE);
		vd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		getSite().registerContextMenu(ID + ".VerrechnungsDisplay", vd.contextMenuManager,
			vd.viewer);
		getSite().setSelectionProvider(vd.viewer);
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
		GlobalEventDispatcher.addActivationListener(this, this);
		ElexisEventDispatcher.getInstance().addListeners(eeli_kons, eeli_kons_sync, eeli_pat,
			eeli_user, eeli_fall);
		text.connectGlobalActions(getViewSite());
		adaptMenus();
		setKons((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
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
		GlobalEventDispatcher.removeActivationListener(this, this);
		ElexisEventDispatcher.getInstance().removeListeners(eeli_kons, eeli_kons_sync, eeli_pat,
			eeli_user, eeli_fall);
		text.disconnectGlobalActions(getViewSite());
		// emFont.dispose();
		super.dispose();
	}
	
	/** Aktuellen patient setzen */
	private synchronized void setPatient(Patient pat){
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
		if (pat == null) {
			pat = ElexisEventDispatcher.getSelectedPatient();
		}
		actPat = pat;
		if (pat != null) {
			form.setText(pat.getPersonalia() + StringTool.space + "(" + pat.getAlter() + ")");
			List<ISticker> etis = pat.getStickers();
			if (etis != null && etis.size() > 0) {
				// Point size = form.getHead().getSize();
				for (ISticker et : etis) {
					if (et != null) {
						new UiSticker((Sticker) et).createForm(cEtiketten);
					}
				}
			}
			updateFallCombo();
		}
		form.layout();
	}
	
	private void updateFallCombo(){
		Patient pat = ElexisEventDispatcher.getSelectedPatient();
		if (pat != null && comboViewerFall != null) {
			Fall[] faelle = pat.getFaelle();
			Arrays.sort(faelle, new FallComparator());
			comboViewerFall.setInput(faelle);
			if (actKons != null) {
				comboFallSelectionListener.ignoreSelectionEventOnce();
				comboViewerFall.setSelection(new StructuredSelection(actKons.getFall()));
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
	private synchronized void setKons(final Konsultation kons){
		
		if (actKons != null && text.isDirty()) {
			actKons.updateEintrag(text.getContentsAsXML(), false);
		}
		
		if (kons != null) {
			Fall act = kons.getFall();
			setPatient(act.getPatient());
			setKonsText(kons, kons.getHeadVersion());
			
			comboFallSelectionListener.ignoreSelectionEventOnce();
			comboViewerFall.setSelection(new StructuredSelection(act));
			comboViewerFall.getCombo().setEnabled(act.isOpen());
			Mandant m = kons.getMandant();
			lBeh.setText(kons.getDatum() + " (" //$NON-NLS-1$
				+ new TimeTool(kons.getDatum()).getDurationToNowString() + ")"); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			if (m == null) {
				sb.append(Messages.KonsDetailView_NotYours); // $NON-NLS-1$
				hlMandant.setBackground(hlMandant.getParent().getBackground());
			} else {
				Rechnungssteller rs = m.getRechnungssteller();
				if (rs.getId().equals(m.getId())) {
					sb.append("(").append(m.getLabel()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sb.append("(").append(m.getLabel()).append("/").append( //$NON-NLS-1$ //$NON-NLS-2$
						rs.getLabel()).append(")"); //$NON-NLS-1$
				}
				hlMandant.setBackground(UiMandant.getColorForMandator(m));
			}
			hlMandant.setText(sb.toString());
			
			boolean hlMandantEnabled =
				kons.isEditable(false) && CoreHub.acl.request(AccessControlDefaults.KONS_REASSIGN);
			hlMandant.setEnabled(hlMandantEnabled);
			dd.setDiagnosen(kons);
			vd.setLeistungen(kons);
			vd.setEnabled(true);
			dd.setEnabled(true);
			if (kons.isEditable(false)) {
				text.setEnabled(true);
				text.setToolTipText("");
				lBeh.setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
				lBeh.setBackground(defaultBackground);
			} else {
				text.setToolTipText("Konsultation geschlossen oder nicht von Ihnen");
				lBeh.setForeground(UiDesk.getColor(UiDesk.COL_GREY60));
				lBeh.setBackground(UiDesk.getColor(UiDesk.COL_GREY20));
			}
			if (isKonsToday(kons)) {
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
			dd.clear();
			vd.clear();
			text.setText(""); //$NON-NLS-1$
			text.setEnabled(false);
			vd.setEnabled(false);
			dd.setEnabled(false);
		}
		actKons = kons;
		cDesc.layout();
		
		if (actKons == null) {
			setUnlocked(false);
		} else {
			setUnlocked(CoreHub.getLocalLockService().isLockedLocal(actKons));
		}
	}
	
	private boolean isKonsToday(Konsultation kons){
		TimeTool konsDate = new TimeTool(kons.getDatum());
		return konsDate.toLocalDate().isEqual(LocalDate.now());
	}
	
	void setKonsText(final Konsultation b, final int version){
		String ntext = ""; //$NON-NLS-1$
		if ((version >= 0) && (version <= b.getHeadVersion())) {
			VersionedResource vr = b.getEintrag();
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
		text.setKons(b);
		displayedVersion = version;
		versionBackAction.setEnabled(version != 0);
		versionFwdAction.setEnabled(version != b.getHeadVersion());
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
		
		purgeAction = new LockedRestrictedAction<Konsultation>(AccessControlDefaults.AC_PURGE,
			Messages.KonsDetailView_PurgeOldEntries) {
			
			@Override
			public Konsultation getTargetedObject(){
				return actKons;
			}
			
			@Override
			public void doRun(Konsultation element){
				element.purgeEintrag();
				ElexisEventDispatcher.fireSelectionEvent(element);
			}
			
		};
		versionBackAction = new Action(Messages.KonsDetailView_PreviousEntry) { // $NON-NLS-1$
			
			@Override
			public void run(){
				if (actKons == null) {
					return;
				}
				if (MessageDialog.openConfirm(getViewSite().getShell(),
					Messages.KonsDetailView_ReplaceKonsTextCaption, // $NON-NLS-1$
					Messages.KonsDetailView_ReplaceKonsTextBody)) { // $NON-NLS-1$
					setKonsText(actKons, displayedVersion - 1);
					text.setDirty(true);
				}
			}
			
		};
		versionFwdAction = new Action(Messages.KonsDetailView_nextEntry) { // $NON-NLS-1$
			@Override
			public void run(){
				if (actKons == null) {
					return;
				}
				if (MessageDialog.openConfirm(getViewSite().getShell(),
					Messages.KonsDetailView_ReplaceKonsTextCaption, // $NON-NLS-1$
					Messages.KonsDetailView_ReplaceKonsTextBody2)) { // $NON-NLS-1$
					setKonsText(actKons, displayedVersion + 1);
					text.setDirty(true);
				}
			}
		};
		saveAction = new LockedAction<Konsultation>(Messages.KonsDetailView_SaveEntry) {
			{
				setImageDescriptor(Images.IMG_DISK.getImageDescriptor());
				setToolTipText(Messages.KonsDetailView_SaveExplicit); // $NON-NLS-1$
			}
			
			@Override
			public Konsultation getTargetedObject(){
				return actKons;
			}
			
			@Override
			public void doRun(Konsultation element){
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
				if (actKons != null) {
					AssignStickerDialog asd =
						new AssignStickerDialog(getViewSite().getShell(), actKons);
					asd.open();
				}
			}
		};
	}
	
	public void save(){
		if (actKons != null) {
			if (text.isDirty()) {
				actKons.updateEintrag(text.getContentsAsXML(), false);
				text.setDirty(false);
			}
			setKons(actKons);
		} else {
			log.warn("Save() actKons == null");
		}
	}
	
	public void adaptMenus(){
		vd.tVerr.getMenu().setEnabled(CoreHub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN));
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
	
	public void addToVerechnung(Artikel artikel){
		vd.addPersistentObject(artikel);
	}
	
	@Override
	public void activation(boolean mode){
		if (mode == false) {
			// save entry on deactivation if text was edited
			if (actKons != null && (text.isDirty())) {
				actKons.updateEintrag(text.getContentsAsXML(), false);
				text.setDirty(false);
			}
		} else {
			// load newest version on activation, if there are no local changes
			if (actKons != null && !text.isDirty()) {
				setKonsText(actKons, actKons.getHeadVersion());
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
						Fall nFall = (Fall) ((StructuredSelection) selection).getFirstElement();
						
						Fall actFall = null;
						String fallId = "";
						String fallLabel = "Current Case NOT found!!";//$NON-NLS-1$
						if (actKons != null) {
							actFall = actKons.getFall();
							fallId = actFall.getId();
							fallLabel = actFall.getLabel();
						}
						
						if (!nFall.getId().equals(fallId)) {
							if (!nFall.isOpen()) {
								SWTHelper.alert(Messages.KonsDetailView_CaseClosedCaption, // $NON-NLS-1$
									Messages.KonsDetailView_CaseClosedBody); // $NON-NLS-1$
							} else {
								MessageDialog msd = new MessageDialog(getViewSite().getShell(),
									Messages.KonsDetailView_ChangeCaseCaption, // $NON-NLS-1$
									Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize),
									MessageFormat.format(
										Messages.KonsDetailView_ConfirmChangeConsToCase,
										new Object[] {
											fallLabel, nFall.getLabel()
										}), MessageDialog.QUESTION, new String[] {
											Messages.KonsDetailView_Yes, // $NON-NLS-1$
											Messages.KonsDetailView_No
									}, 0); // $NON-NLS-1$
								if (msd.open() == Window.OK) {
									actKons.transferToFall(nFall, false, false);
								} else {
									ignoreSelectionEventOnce();
									comboViewerFall.setSelection(new StructuredSelection(actFall));
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
