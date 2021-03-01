package ch.elexis.core.spotlight.ui.internal.ready;

import java.util.function.Supplier;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.internal.CustomLinkWithOptionalImage;
import ch.elexis.core.spotlight.ui.internal.SpotlightShell;
import ch.elexis.core.ui.icons.Images;

public class SpotlightReadyComposite extends Composite {
	
	private SpotlightReadyService spotlightReadyService;
	
	private CustomLinkWithOptionalImage nextAppointment;
	private CustomLinkWithOptionalImage newLabValues;
	private CustomLinkWithOptionalImage newDocuments;
	private ListViewer listViewerLastPatients;
	
	private static final String NEW_LABVALUES_TEMPLATE = "%s neue Laborwerte";
	private static final String NEW_DOCUMENTS_TEMPLATE = "%s neue Dokumente";
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param spotlightReadyService
	 * @param uiUtil
	 */
	public SpotlightReadyComposite(Composite parent, int style, EPartService partService,
		SpotlightReadyService spotlightReadyService){
		super(parent, style);
		
		this.spotlightReadyService = spotlightReadyService;
		
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		
		Font boldDefaultFont = JFaceResources.getFontRegistry().getBold("default");
		
		Composite compositeLeft = new Composite(this, SWT.NONE);
		compositeLeft.setLayout(new GridLayout(1, false));
		compositeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNextAppointment = new Label(compositeLeft, SWT.NONE);
		lblNextAppointment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNextAppointment.setText("Nächster Termin");
		lblNextAppointment.setFont(boldDefaultFont);
		
		nextAppointment = new CustomLinkWithOptionalImage(compositeLeft, SWT.WRAP, null);
		nextAppointment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblLastPatients = new Label(compositeLeft, SWT.NONE);
		lblLastPatients.setText("Patientenverlauf");
		lblLastPatients.setFont(boldDefaultFont);
		
		listViewerLastPatients = new ListViewer(compositeLeft, SWT.V_SCROLL);
		listViewerLastPatients.setContentProvider(ArrayContentProvider.getInstance());
		listViewerLastPatients.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				Object[] _element = (Object[]) element;
				return _element[2].toString() + " (" + _element[1].toString() + ")";
			}
		});
		listViewerLastPatients.addSelectionChangedListener(event -> {
			Object[] firstElement = (Object[]) event.getStructuredSelection().getFirstElement();
			if (firstElement != null) {
				((SpotlightShell) getShell())
					.setSelectedElement(Category.PATIENT.name() + "::" + firstElement[0]);
			} else {
				((SpotlightShell) getShell()).setSelectedElement(null);
			}
			
		});
		List listLastPatients = listViewerLastPatients.getList();
		listLastPatients.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		listLastPatients.setBackground(getBackground());
		listLastPatients.addListener(SWT.FocusIn, event -> {
			int itemCount = listLastPatients.getItemCount();
			if (itemCount >= 1) {
				listViewerLastPatients
					.setSelection(new StructuredSelection(listLastPatients.getItem(0)));
			}
			listLastPatients.setSelection(0);
		});
		listLastPatients.addListener(SWT.FocusOut, event -> {
			listViewerLastPatients.setSelection(null);
		});
		
		compositeLeft.setTabList(new Control[] {
			nextAppointment, listLastPatients
		});
		
		Composite compositeRight = new Composite(this, SWT.NONE);
		compositeRight.setLayout(new GridLayout(1, false));
		compositeRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		CustomLinkWithOptionalImage openReminders = new CustomLinkWithOptionalImage(compositeRight,
			SWT.NONE, Images.IMG_BELL_EXCLAMATION.getImage());
		openReminders.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		openReminders.getLink().setText("? Pendenzen");
		
		//		ListViewer listViewerOpenReminders =
		//			new ListViewer(compositeRight, SWT.NO_FOCUS | SWT.V_SCROLL);
		//		List listOpenReminders = listViewerOpenReminders.getList();
		//		listOpenReminders.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//		listOpenReminders.setBackground(getBackground());
		//		
//		Label lblNewMessages = new Label(compositeRight, SWT.NONE);
//		lblNewMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		lblNewMessages.setText("? neue Nachrichten");
		
		Supplier<Boolean> jumpToInboxViewSupplier = () -> {
			partService.showPart("at.medevit.elexis.inbox.ui.view", PartState.ACTIVATE);
			// showpart already closes this shell
			return false;
		};
		
		newDocuments = new CustomLinkWithOptionalImage(compositeRight, SWT.NONE,
			Images.IMG_DOCUMENT.getImage());
		newDocuments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newDocuments.setData(jumpToInboxViewSupplier);
		
		newLabValues = new CustomLinkWithOptionalImage(compositeRight, SWT.NONE,
			Images.IMG_VIEW_LABORATORY.getImage());
		newLabValues.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newLabValues.setData(jumpToInboxViewSupplier);
		
		compositeRight.setTabList(new Control[] {
			openReminders, newDocuments, newLabValues
		});
		
		Label lblUpdateTime = new Label(this, SWT.None);
		lblUpdateTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblUpdateTime.setText(
			"Aktualisiert vor " + spotlightReadyService.getInfoAgeInSeconds() + " Sekunden.");
		
		setTabList(new Control[] {
			compositeLeft, compositeRight
		});
		
		refresh();
	}
	
	private void refresh(){
		nextAppointment.getLink().setText(spotlightReadyService.getNextAppointmentLabel());
		nextAppointment.setData(spotlightReadyService.getNextAppointment());
		listViewerLastPatients.setInput(spotlightReadyService.getLastPatientSelections());
		newLabValues.getLink().setText(
			String.format(NEW_LABVALUES_TEMPLATE, spotlightReadyService.getNewLabValuesCount()));
		newDocuments.getLink().setText(
			String.format(NEW_DOCUMENTS_TEMPLATE, spotlightReadyService.getNewDocumentsCount()));
	}
	
}
