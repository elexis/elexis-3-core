package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;

public class SelectOrCreateOpenKonsDialog extends TitleAreaDialog {
	
	private Patient patient;
	private ComboViewer fallCombo;
	private ComboViewer openKonsCombo;
	
	private Konsultation konsultation;
	private String title;
	
	public SelectOrCreateOpenKonsDialog(Patient patient){
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.patient = patient;
	}
	
	public SelectOrCreateOpenKonsDialog(Patient patient, String title){
		this(patient);
		this.title = title;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText("Konsultation auswählen"); //$NON-NLS-1$
		setTitle(title); //$NON-NLS-1$
		setMessage(String
			.format(
				"Erstellen bzw. wählen Sie eine Konsultation für den Patienten %s aus\n", patient.getLabel())); //$NON-NLS-1$
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Composite areaComposite = new Composite(composite, SWT.NONE);
		areaComposite
			.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		areaComposite.setLayout(new FormLayout());
		
		Label lbl = new Label(areaComposite, SWT.NONE);
		lbl.setText("Konsultation erstellen");
		
		ToolBarManager tbManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		tbManager.add(GlobalActions.neueKonsAction);
		ToolBar toolbar = tbManager.createControl(areaComposite);
		
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 5);
		lbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(30, 5);
		toolbar.setLayoutData(fd);
		
		fallCombo = new ComboViewer(areaComposite);
		
		fallCombo.setContentProvider(new ArrayContentProvider());
		
		fallCombo.setInput(getOpenFall());
		fallCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((Fall) element).getLabel();
			}
		});
		fallCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection selection = (StructuredSelection) fallCombo.getSelection();
				if (!selection.isEmpty()) {
					ElexisEventDispatcher.fireSelectionEvent((PersistentObject) selection
						.getFirstElement());
				}
			}
		});
		fallCombo.setSelection(new StructuredSelection(ElexisEventDispatcher
			.getSelected(Fall.class)));
		
		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(toolbar, 5);
		fallCombo.getControl().setLayoutData(fd);
		
		lbl = new Label(areaComposite, SWT.NONE);
		lbl.setText("Konsultation auswählen");
		
		openKonsCombo = new ComboViewer(areaComposite);
		
		openKonsCombo.setContentProvider(new ArrayContentProvider());
		
		openKonsCombo.setInput(getOpenKons());
		openKonsCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((Konsultation) element).getLabel();
			}
		});
		
		ElexisEventDispatcher.getInstance().addListeners(
			new UpdateKonsComboListener(openKonsCombo, Konsultation.class, 0xff));
		
		fd = new FormData();
		fd.top = new FormAttachment(toolbar, 5);
		fd.left = new FormAttachment(0, 5);
		lbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(toolbar, 5);
		fd.left = new FormAttachment(30, 5);
		fd.right = new FormAttachment(100, -5);
		openKonsCombo.getControl().setLayoutData(fd);
		
		return areaComposite;
	}
	
	private List<Fall> getOpenFall(){
		ArrayList<Fall> ret = new ArrayList<Fall>();
		Fall[] faelle = patient.getFaelle();
		for (Fall f : faelle) {
			if (f.isOpen()) {
				ret.add(f);
			}
		}
		return ret;
	}
	
	protected List<Konsultation> getOpenKons(){
		ArrayList<Konsultation> ret = new ArrayList<Konsultation>();
		Fall[] faelle = patient.getFaelle();
		for (Fall f : faelle) {
			if (f.isOpen()) {
				Konsultation[] consultations = f.getBehandlungen(false);
				for (Konsultation konsultation : consultations) {
					if (konsultation.isEditable(false)) {
						ret.add(konsultation);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public void okPressed(){
		Object obj = ((IStructuredSelection) openKonsCombo.getSelection()).getFirstElement();
		if (obj instanceof Konsultation) {
			konsultation = (Konsultation) obj;
			super.okPressed();
		}
		if (this.getShell() != null && !this.getShell().isDisposed())
			setErrorMessage("Keine Konsultation ausgewählt.");
		return;
	}
	
	public Konsultation getKonsultation(){
		return konsultation;
	}
	
	private class UpdateKonsComboListener extends ElexisUiEventListenerImpl {
		
		ComboViewer viewer;
		
		UpdateKonsComboListener(ComboViewer viewer, final Class<?> clazz, int mode){
			super(clazz, mode);
			this.viewer = viewer;
		}
		
		@Override
		public void runInUi(ElexisEvent ev){
			if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
				viewer.setInput(getOpenKons());
				viewer.refresh();
			}
		}
	}
}
