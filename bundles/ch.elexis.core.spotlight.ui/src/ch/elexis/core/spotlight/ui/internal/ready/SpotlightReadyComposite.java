package ch.elexis.core.spotlight.ui.internal.ready;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.spotlight.ui.internal.SpotlightUiUtil;

public class SpotlightReadyComposite extends Composite {
	
	private SpotlightReadyService spotlightReadyService;
	private SpotlightUiUtil uiUtil;
	
	private Label nextAppointment;
	private ListViewer listViewerLastPatients;
	private Label lblNewLabValues;
	
	private static final String NEW_LABVALUES_TEMPLATE = "%s neue Laborwerte";
	private static final String NEW_DOCUMENTS_TEMPLATE = "%s neue Dokumente";
	private Label lblNewDocuments;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param spotlightReadyService
	 * @param uiUtil
	 */
	public SpotlightReadyComposite(Composite parent, int style,
		SpotlightReadyService spotlightReadyService, SpotlightUiUtil uiUtil){
		super(parent, style);
		
		this.spotlightReadyService = spotlightReadyService;
		this.uiUtil = uiUtil;
		
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		
		Composite compositeLeft = new Composite(this, SWT.NONE);
		compositeLeft.setLayout(new GridLayout(1, false));
		compositeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNextAppointment = new Label(compositeLeft, SWT.NONE);
		lblNextAppointment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNextAppointment.setText("Nächster Termin");
		
		nextAppointment = new Label(compositeLeft, SWT.WRAP);
		nextAppointment.addListener(SWT.KeyDown, event -> {
			int keyCode = event.keyCode;
			if (13 == keyCode) {
				// enter, handle and close
				IAppointment appointment = (IAppointment) nextAppointment.getData();
				boolean ok = uiUtil.handleEnter(appointment);
				if (ok) {
					getShell().close();
				}
			}
		});
		
		Label lblLastPatients = new Label(compositeLeft, SWT.NONE);
		lblLastPatients.setText("Patientenverlauf");
		
		listViewerLastPatients = new ListViewer(compositeLeft, SWT.BORDER | SWT.V_SCROLL);
		listViewerLastPatients.setContentProvider(ArrayContentProvider.getInstance());
		listViewerLastPatients.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				Object[] _element = (Object[]) element;
				return _element[2].toString() + " (" + _element[1].toString() + ")";
			}
		});
		List listLastPatients = listViewerLastPatients.getList();
		listLastPatients.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		listLastPatients.setBackground(getBackground());
		compositeLeft.setTabList(new Control[] {
			nextAppointment, listLastPatients
		});
		
		Composite compositeRight = new Composite(this, SWT.NONE);
		compositeRight.setLayout(new GridLayout(1, false));
		compositeRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblOpenReminders = new Label(compositeRight, SWT.NONE);
		lblOpenReminders.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblOpenReminders.setText("? Pendenzen");
		
		ListViewer listViewer = new ListViewer(compositeRight, SWT.BORDER | SWT.V_SCROLL);
		List listOpenReminders = listViewer.getList();
		listOpenReminders.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewMessages = new Label(compositeRight, SWT.NONE);
		lblNewMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewMessages.setText("? neue Nachrichten");
		
		lblNewLabValues = new Label(compositeRight, SWT.NONE);
		lblNewDocuments = new Label(compositeRight, SWT.NONE);
		
		Label lblUpdateTime = new Label(this, SWT.None);
		lblUpdateTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblUpdateTime.setText(
			"Aktualisiert vor " + spotlightReadyService.getInfoAgeInSeconds() + " Sekunden.");
		
		refresh();
	}
	
	private void refresh(){
		nextAppointment.setText(spotlightReadyService.getNextAppointmentLabel());
		nextAppointment.setData(spotlightReadyService.getNextAppointment());
		listViewerLastPatients.setInput(spotlightReadyService.getLastPatientSelections());
		lblNewLabValues.setText(
			String.format(NEW_LABVALUES_TEMPLATE, spotlightReadyService.getNewLabValuesCount()));
		lblNewDocuments.setText(
			String.format(NEW_DOCUMENTS_TEMPLATE, spotlightReadyService.getNewDocumentsCount()));
	}
	
}
