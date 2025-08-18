package ch.elexis.core.ui.medication.views;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.DateAndTimeObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.handlers.ApplyCustomSortingHandler;
import ch.elexis.core.ui.medication.views.MedicationTableViewerContentProvider.MedicationContentProviderComposite;
import ch.elexis.core.ui.medication.views.provider.MedicationFilter;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;
import ch.elexis.core.ui.util.GenericObjectDragSource;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.views.controls.InteractionLink;

public class MedicationComposite extends Composite implements ISelectionProvider, ISelectionChangedListener {

	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

	private Composite compositeSearchFilter;
	private Text txtSearch;
	private MedicationFilter medicationHistoryFilter;
	private GridData compositeSearchFilterLayoutData;

	private Composite compositeMedicationDetail;
	private Composite stackCompositeDosage;
	private Composite compositeDayTimeDosage;
	private Composite compositeFreeTextDosage;
	private StackLayout stackLayoutDosage;
	private Text txtMorning, txtNoon, txtEvening, txtNight, txtFreeText;

	private Composite tablesComposite;
	private StackLayout tablesLayout;
	private MedicationTableComposite medicationTableComposite;
	private MedicationHistoryTableComposite medicationHistoryTableComposite;

	private GridData compositeMedicationDetailLayoutData;
	private Button btnConfirm;

	private String[] signatureArray;
	private Button btnShowHistory;
	private StackLayout stackLayout;
	private Composite compositeMedicationTextDetails;
	private Composite compositeStopMedicationTextDetails;
	private Composite stackedMedicationDetailComposite;

	private WritableValue<MedicationTableViewerItem> selectedMedication = new WritableValue<>(null,
			MedicationTableViewerItem.class);
	private WritableValue<Identifiable> lastDisposal = new WritableValue<>(null, Identifiable.class);

	private DateTime dateStart;
	private DateTime timeStopped;
	private DateTime dateStopped;
	private Button btnStopMedication;
	private Button btnToggleDetailComposite;
	private Label lblLastDisposalLink;
	private Label lblDailyTherapyCost;
	private Label lblLastModified;

	private Color tagBtnColor = UiDesk.getColor(UiDesk.COL_GREEN);
	private Color stopBGColor = UiDesk.getColorFromRGB("FF7256"); //$NON-NLS-1$
	private Color defaultBGColor = UiDesk.getColorFromRGB("F0F0F0"); //$NON-NLS-1$
	private ControlDecoration ctrlDecor;
	private IPatient patient;
	private GenericObjectDropTarget dropTarget;
	private Text txtIntakeOrder;
	private Text txtDisposalComment;
	private Text txtStopComment;
	private MedicationContentProviderComposite contentProviderComp;
	private InteractionLink interactionLink;

	private ViewerSortOrder sortOrder;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param partSite
	 */
	public MedicationComposite(Composite parent, int style, IWorkbenchPartSite partSite) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		searchFilterComposite();
		medicationTableComposite(partSite);
		stateComposite();
		medicationDetailComposite();

		showSearchFilterComposite(false);
		showMedicationDetailComposite(null);

		dropTarget = new GenericObjectDropTarget("Medication", this, new DropMedicationReceiver(getShell()));//$NON-NLS-1$
	}

	private void searchFilterComposite() {
		compositeSearchFilter = new Composite(this, SWT.NONE);
		compositeSearchFilter.setLayout(new GridLayout(2, false));
		compositeSearchFilterLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		compositeSearchFilter.setLayoutData(compositeSearchFilterLayoutData);

		ToolBar toolBar = new ToolBar(compositeSearchFilter, SWT.HORIZONTAL);
		ToolItem tiClear = new ToolItem(toolBar, SWT.PUSH);
		tiClear.setImage(Images.IMG_CLEAR.getImage());
		tiClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearSearchFilter();
			}
		});

		txtSearch = new Text(compositeSearchFilter, SWT.BORDER);
		txtSearch.setMessage(Messages.Core_DoSearch);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				medicationHistoryFilter.setSearchText(txtSearch.getText());
			};
		});
	}

	private void clearSearchFilter() {
		txtSearch.setText(StringUtils.EMPTY);
		medicationHistoryFilter.setSearchText(StringUtils.EMPTY);
	}

	private void medicationTableComposite(IWorkbenchPartSite partSite) {
		tablesComposite = new Composite(this, SWT.NONE);
		tablesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tablesLayout = new StackLayout();
		tablesComposite.setLayout(tablesLayout);

		medicationTableComposite = new MedicationTableComposite(tablesComposite, SWT.NONE | SWT.VIRTUAL);
		medicationTableComposite.setMedicationComposite(this);
		MedicationViewerHelper.addContextMenu(medicationTableComposite.getTableViewer(), this, partSite);
		// this composite manages selection of both tables
		medicationTableComposite.getTableViewer().addSelectionChangedListener(this);

		medicationHistoryTableComposite = new MedicationHistoryTableComposite(tablesComposite, SWT.NONE | SWT.VIRTUAL);
		medicationHistoryTableComposite.setMedicationComposite(this);
		MedicationViewerHelper.addContextMenu(medicationHistoryTableComposite.getTableViewer(), this, partSite);
		// this composite manages selection of both tables
		medicationHistoryTableComposite.getTableViewer().addSelectionChangedListener(this);
		medicationHistoryFilter = new MedicationFilter(medicationHistoryTableComposite.getTableViewer());
		medicationHistoryTableComposite.getTableViewer().addFilter(medicationHistoryFilter);

		addDragSource(medicationTableComposite.getTableViewer());

		tablesLayout.topControl = medicationTableComposite;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addDragSource(TableViewer tableViewer) {
		new GenericObjectDragSource(tableViewer, () -> {
			List selection = tableViewer.getStructuredSelection().toList();
			return selection.stream().filter(MedicationTableViewerItem.class::isInstance)
					.map(p -> ((MedicationTableViewerItem) p).getArticle())
					.toList();
		});
	}

	public void setViewerSortOrder(ViewerSortOrder vso) {
		sortOrder = vso;
		medicationTableComposite.getTableViewer().setComparator(vso.vc);
		medicationHistoryTableComposite.getTableViewer().setComparator(vso.vc);

		ICommandService service = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand(ApplyCustomSortingHandler.CMD_ID);
		command.getState(ApplyCustomSortingHandler.STATE_ID).setValue(vso.equals(ViewerSortOrder.MANUAL));
	}

	public ViewerSortOrder getViewerSortOrder() {
		return sortOrder;
	}

	private void stateComposite() {
		Composite compositeState = new Composite(this, SWT.NONE);
		GridLayout gl_compositeState = new GridLayout(8, false);
		gl_compositeState.marginWidth = 0;
		gl_compositeState.marginHeight = 0;
		gl_compositeState.horizontalSpacing = 0;
		compositeState.setLayout(gl_compositeState);
		compositeState.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		contentProviderComp = new MedicationContentProviderComposite(compositeState, SWT.NONE);
		contentProviderComp.setContentProvider(
				(MedicationTableViewerContentProvider) medicationTableComposite.getTableViewer().getContentProvider());
		contentProviderComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button bestaetigenButton = new Button(compositeState, SWT.FLAT | SWT.TOGGLE);
		bestaetigenButton.setImage(Images.IMG_TICK.getImage());
		bestaetigenButton.setToolTipText(Messages.MedicationComposite_btnConfirm_toolTipText);
		GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData2.widthHint = 20;
		gridData2.heightHint = 20;
		bestaetigenButton.setLayoutData(gridData2);

		lblLastModified = new Label(compositeState, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.heightHint = 15;
		gridData.horizontalIndent = 5;
		lblLastModified.setLayoutData(gridData);

		bestaetigenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (patient != null && !patient.getId().isEmpty()) {
					String info = CoreHub.getLoggedInContact().getLabel() + " " + setCurrentDateTime();
					patient.setExtInfo("lastModifiedInfo", info);
					CoreModelServiceHolder.get().save(patient);
					lblLastModified.setText(setCurrentDate());
					lblLastModified.setToolTipText(info);
				} else {
					lblLastModified.setText("Kein Patient ausgewählt");
				}
			}
		});

		Label lblLastDisposal = new Label(compositeState, SWT.NONE);
		lblLastDisposal.setText(Messages.MedicationComposite_lastReceived);
		GridData gd_lblLastDisposal = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblLastDisposal.horizontalIndent = 5;
		lblLastDisposal.setLayoutData(gd_lblLastDisposal);

		lblLastDisposalLink = new Label(compositeState, SWT.NONE);
		lblLastDisposalLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Identifiable identifiable = lastDisposal.getValue();
				if (identifiable == null)
					return;
			}
		});
		lblLastDisposalLink.setForeground(UiDesk.getColor(UiDesk.COL_BLUE));
		lblLastDisposalLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblDailyTherapyCost = new Label(compositeState, SWT.NONE);
		lblDailyTherapyCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblDailyTherapyCost.setText(Messages.FixMediDisplay_DailyCost);
		interactionLink = new InteractionLink(this, SWT.NONE);
		interactionLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnShowHistory = new Button(compositeState, SWT.TOGGLE | SWT.FLAT);
		btnShowHistory.setToolTipText(Messages.MedicationComposite_btnShowHistory_toolTipText);
		btnShowHistory.setText(Messages.MedicationComposite_btnCheckButton_text);
		btnShowHistory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnShowHistory.getSelection()) {
					showSearchFilterComposite(true);
					tablesLayout.topControl = medicationHistoryTableComposite;
					medicationHistoryTableComposite.setPendingInput();
					medicationHistoryTableComposite.getTableViewer().refresh();
					setViewerSortOrder(ViewerSortOrder.DEFAULT);
					contentProviderComp
							.setContentProvider((MedicationTableViewerContentProvider) medicationHistoryTableComposite
									.getTableViewer().getContentProvider());
				} else {
					showSearchFilterComposite(false);
					tablesLayout.topControl = medicationTableComposite;
					medicationTableComposite.setPendingInput();
					medicationTableComposite.getTableViewer().refresh();
					contentProviderComp
							.setContentProvider((MedicationTableViewerContentProvider) medicationTableComposite
									.getTableViewer().getContentProvider());

				}
				tablesComposite.layout();

				updateUi(patient, false);
			}
		});

		btnToggleDetailComposite = new Button(compositeState, SWT.FLAT | SWT.TOGGLE);
		btnToggleDetailComposite.setImage(Images.IMG_LOCK_CO.getImage());
		btnToggleDetailComposite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnToggleDetailComposite.getSelection()) {
					Object selectedObj = selectedMedication.getValue();
					if (selectedObj instanceof MedicationTableViewerItem) {
						showMedicationDetailComposite(selectedMedication.getValue());
					}
				}
			}
		});
	}

	public boolean isShowingHistory() {
		if (btnShowHistory != null && !btnShowHistory.isDisposed()) {
			return btnShowHistory.getSelection();
		}
		return false;
	}

	private void medicationDetailComposite() {
		compositeMedicationDetail = new Composite(this, SWT.BORDER);
		GridLayout gl_compositeMedicationDetail = new GridLayout(7, false);
		gl_compositeMedicationDetail.marginBottom = 5;
		gl_compositeMedicationDetail.marginRight = 5;
		gl_compositeMedicationDetail.marginLeft = 5;
		gl_compositeMedicationDetail.marginTop = 5;
		gl_compositeMedicationDetail.marginWidth = 0;
		gl_compositeMedicationDetail.marginHeight = 0;
		compositeMedicationDetail.setLayout(gl_compositeMedicationDetail);
		compositeMedicationDetailLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		compositeMedicationDetail.setLayoutData(compositeMedicationDetailLayoutData);

		{
			dateStart = new DateTime(compositeMedicationDetail, SWT.DATE);
			dateStart.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					activateConfirmButton(true);
				}
			});
			dateStart.setToolTipText("Startdatum");

			stackCompositeDosage = new Composite(compositeMedicationDetail, SWT.NONE);
			stackCompositeDosage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			stackLayoutDosage = new StackLayout();
			stackCompositeDosage.setLayout(stackLayoutDosage);

			compositeDayTimeDosage = new Composite(stackCompositeDosage, SWT.NONE);
			compositeDayTimeDosage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			GridLayout gl_compositeDayTimeDosage = new GridLayout(7, false);
			gl_compositeDayTimeDosage.marginWidth = 0;
			gl_compositeDayTimeDosage.marginHeight = 0;
			gl_compositeDayTimeDosage.verticalSpacing = 1;
			gl_compositeDayTimeDosage.horizontalSpacing = 0;
			compositeDayTimeDosage.setLayout(gl_compositeDayTimeDosage);

			txtMorning = new Text(compositeDayTimeDosage, SWT.BORDER);
			txtMorning.setTextLimit(60); // varchar 255 divided by 4 minus 3 '-'
			txtMorning.setMessage("morn");
			GridData gd_txtMorning = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_txtMorning.widthHint = 40;
			txtMorning.setLayoutData(gd_txtMorning);
			txtMorning.addModifyListener(new SignatureArrayModifyListener(0));

			Label lblStop = new Label(compositeDayTimeDosage, SWT.HORIZONTAL);
			lblStop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblStop.setText("-"); //$NON-NLS-1$

			txtNoon = new Text(compositeDayTimeDosage, SWT.BORDER);
			txtNoon.setTextLimit(60);
			txtNoon.setMessage("noon");
			GridData gd_txtNoon = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_txtNoon.widthHint = 40;
			txtNoon.setLayoutData(gd_txtNoon);
			txtNoon.addModifyListener(new SignatureArrayModifyListener(1));

			Label lblStop2 = new Label(compositeDayTimeDosage, SWT.NONE);
			lblStop2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblStop2.setText("-"); //$NON-NLS-1$

			txtEvening = new Text(compositeDayTimeDosage, SWT.BORDER);
			txtEvening.setTextLimit(60);
			txtEvening.setMessage("eve");
			GridData gd_txtEvening = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_txtEvening.widthHint = 40;
			txtEvening.setLayoutData(gd_txtEvening);
			txtEvening.addModifyListener(new SignatureArrayModifyListener(2));
			txtEvening.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					switch (e.keyCode) {
					case SWT.CR:
						if (btnConfirm.isEnabled())
							applyDetailChanges();
						break;
					default:
						break;
					}
				};
			});

			Label lblStop3 = new Label(compositeDayTimeDosage, SWT.NONE);
			lblStop3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblStop3.setText("-"); //$NON-NLS-1$

			txtNight = new Text(compositeDayTimeDosage, SWT.BORDER);
			txtNight.setTextLimit(60);
			txtNight.setMessage("night");
			GridData gd_txtNight = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_txtNight.widthHint = 40;
			txtNight.setLayoutData(gd_txtNight);
			txtNight.addModifyListener(new SignatureArrayModifyListener(3));
			txtNight.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					switch (e.keyCode) {
					case SWT.CR:
						if (btnConfirm.isEnabled())
							applyDetailChanges();
						break;
					default:
						break;
					}
				};
			});

			compositeFreeTextDosage = new Composite(stackCompositeDosage, SWT.NONE);
			compositeFreeTextDosage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			GridLayout gl_compositeFreeTextDosage = new GridLayout(1, false);
			gl_compositeFreeTextDosage.marginWidth = 0;
			gl_compositeFreeTextDosage.marginHeight = 0;
			gl_compositeFreeTextDosage.verticalSpacing = 1;
			gl_compositeFreeTextDosage.horizontalSpacing = 0;
			compositeFreeTextDosage.setLayout(gl_compositeFreeTextDosage);

			txtFreeText = new Text(compositeFreeTextDosage, SWT.BORDER);
			txtFreeText.setMessage(Messages.Core_Freetext);
			GridData gd_txtFreeText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_txtFreeText.widthHint = 210;
			txtFreeText.setLayoutData(gd_txtFreeText);
			txtFreeText.setTextLimit(255);
			txtFreeText.addModifyListener(new SignatureArrayModifyListener(0));
			txtFreeText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					switch (e.keyCode) {
					case SWT.CR:
						if (btnConfirm.isEnabled())
							applyDetailChanges();
						break;
					default:
						break;
					}
				};
			});

			stackLayoutDosage.topControl = compositeDayTimeDosage;
			stackCompositeDosage.layout();
		}

		Button btnDoseSwitch = new Button(compositeMedicationDetail, SWT.PUSH);
		btnDoseSwitch.setImage(Images.IMG_SYNC.getImage());
		btnDoseSwitch.setToolTipText(Messages.Core_change_freetext_activation);
		btnDoseSwitch.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stackLayoutDosage.topControl == compositeDayTimeDosage) {
					stackLayoutDosage.topControl = compositeFreeTextDosage;
				} else {
					stackLayoutDosage.topControl = compositeDayTimeDosage;
				}
				stackCompositeDosage.layout();
			};
		});

		timeStopped = new DateTime(compositeMedicationDetail, SWT.TIME);
		timeStopped.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				activateConfirmButton(true);
			}
		});
		dateStopped = new DateTime(compositeMedicationDetail, SWT.DATE);
		dateStopped.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				activateConfirmButton(true);
			}
		});
		DataBindingContext dbc = new DataBindingContext();
		IObservableValue dateTimeStopObservable = PojoProperties.value("endTime", Date.class) //$NON-NLS-1$
				.observeDetail(selectedMedication);
		IObservableValue<Date> timeObservable = WidgetProperties.dateTimeSelection().observe(timeStopped);
		IObservableValue<Date> dateObservable = WidgetProperties.dateTimeSelection().observe(dateStopped);
		dbc.bindValue(new DateAndTimeObservableValue(dateObservable, timeObservable), dateTimeStopObservable);

		btnStopMedication = new Button(compositeMedicationDetail, SWT.FLAT | SWT.TOGGLE);
		btnStopMedication.setImage(Images.IMG_STOP.getImage());
		btnStopMedication.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnStopMedication.setToolTipText(Messages.Core_DoStop);
		btnStopMedication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnStopMedication.getSelection()) {
					stackLayout.topControl = compositeStopMedicationTextDetails;
					// change color
					compositeMedicationDetail.setBackground(stopBGColor);
					compositeStopMedicationTextDetails.setBackground(stopBGColor);

					dateStopped.setEnabled(true);
					timeStopped.setEnabled(true);

					// highlight confirm button
					activateConfirmButton(true);
				} else {
					// change color
					compositeMedicationDetail.setBackground(defaultBGColor);
					compositeStopMedicationTextDetails.setBackground(defaultBGColor);
					stackLayout.topControl = compositeMedicationTextDetails;

					dateStopped.setEnabled(false);
					timeStopped.setEnabled(false);

					// set confirm button defaults
					activateConfirmButton(false);
				}
				stackedMedicationDetailComposite.layout();
			}
		});

		btnConfirm = new Button(compositeMedicationDetail, SWT.FLAT);
		btnConfirm.setText(Messages.MedicationComposite_btnConfirm);
		GridData gdBtnConfirm = new GridData();
		gdBtnConfirm.horizontalIndent = 5;
		gdBtnConfirm.horizontalAlignment = SWT.RIGHT;
		btnConfirm.setLayoutData(gdBtnConfirm);
		btnConfirm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyDetailChanges();
			}
		});
		btnConfirm.setEnabled(false);

		stackedMedicationDetailComposite = new Composite(compositeMedicationDetail, SWT.NONE);
		stackedMedicationDetailComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
		stackLayout = new StackLayout();
		stackedMedicationDetailComposite.setLayout(stackLayout);

		// --- medication detail
		compositeMedicationTextDetails = new Composite(stackedMedicationDetailComposite, SWT.NONE);
		GridLayout gl_compositeMedicationTextDetails = new GridLayout(1, false);
		gl_compositeMedicationTextDetails.marginWidth = 0;
		gl_compositeMedicationTextDetails.horizontalSpacing = 0;
		gl_compositeMedicationTextDetails.marginHeight = 0;
		compositeMedicationTextDetails.setLayout(gl_compositeMedicationTextDetails);
		compositeMedicationTextDetails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

		txtIntakeOrder = new Text(compositeMedicationTextDetails, SWT.BORDER);
		txtIntakeOrder.setMessage(Messages.Prescription_Instruction);
		txtIntakeOrder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		IObservableValue<String> txtIntakeOrderObservable = WidgetProperties.text(SWT.Modify).observeDelayed(100,
				txtIntakeOrder);
		IObservableValue<String> intakeOrderObservable = PojoProperties.value("remark", String.class) //$NON-NLS-1$
				.observeDetail(selectedMedication);
		dbc.bindValue(txtIntakeOrderObservable, intakeOrderObservable,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
		txtIntakeOrderObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				activateConfirmButton(true);
			}
		});

		txtDisposalComment = new Text(compositeMedicationTextDetails, SWT.BORDER);
		txtDisposalComment.setMessage(Messages.Prescription_Reason);
		txtDisposalComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		IObservableValue txtCommentObservable = WidgetProperties.text(SWT.Modify).observeDelayed(100,
				txtDisposalComment);
		IObservableValue commentObservable = PojoProperties.value("disposalComment", String.class) //$NON-NLS-1$
				.observeDetail(selectedMedication);
		dbc.bindValue(txtCommentObservable, commentObservable,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
		txtCommentObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				activateConfirmButton(true);
			}
		});

		stackLayout.topControl = compositeMedicationTextDetails;

		// --- stop medication detail
		compositeStopMedicationTextDetails = new Composite(stackedMedicationDetailComposite, SWT.NONE);
		GridLayout gl_compositeStopMedicationTextDetails = new GridLayout(1, false);
		gl_compositeStopMedicationTextDetails.marginWidth = 0;
		gl_compositeStopMedicationTextDetails.horizontalSpacing = 0;
		gl_compositeStopMedicationTextDetails.marginHeight = 0;
		compositeStopMedicationTextDetails.setLayout(gl_compositeStopMedicationTextDetails);
		compositeStopMedicationTextDetails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

		txtStopComment = new Text(compositeStopMedicationTextDetails, SWT.BORDER);
		txtStopComment.setMessage(Messages.MedicationComposite_stopReason);
		txtStopComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		IObservableValue<String> txtStopCommentObservableUi = WidgetProperties.text(SWT.Modify).observeDelayed(100,
				txtStopComment);
		IObservableValue<String> txtStopCommentObservable = PojoProperties.value("stopReason", String.class) //$NON-NLS-1$
				.observeDetail(selectedMedication);
		dbc.bindValue(txtStopCommentObservableUi, txtStopCommentObservable,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));

		txtStopCommentObservableUi.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				activateConfirmButton(true);
			}
		});

	}

	private void applyDetailChanges() {
		MedicationTableViewerItem pres = selectedMedication.getValue();
		if (pres == null)
			return; // prevent npe

		AcquireLockUi.aquireAndRun(pres.getPrescription(), new ILockHandler() {

			@Override
			public void lockFailed() {
				// do nothing
			}

			@Override
			public void lockAcquired() {
				IPrescription oldPrescription = pres.getPrescription();
				String endDate = pres.getEndDate();
				if (!btnStopMedication.getSelection()) {
					IPrescription newPrescription = MedicationServiceHolder.get()
							.createPrescriptionCopy(oldPrescription);
					newPrescription.setDosageInstruction(getDosisStringFromSignatureTextArray());
					newPrescription.setRemark(txtIntakeOrder.getText());
					newPrescription.setDisposalComment(txtDisposalComment.getText());

					LocalDate startDate = LocalDate.of(dateStart.getYear(), dateStart.getMonth() + 1,
							dateStart.getDay());
					// compare with old prescription for changed test, new prescription has now as
					// start
					if (!oldPrescription.getDateFrom().toLocalDate().equals(startDate)) {
						newPrescription.setDateFrom(startDate.atTime(newPrescription.getDateFrom().toLocalTime()));
					}
					CoreModelServiceHolder.get().save(newPrescription);
				}
				// change always stops
				if (btnStopMedication.getSelection()) {
					MedicationServiceHolder.get().stopPrescription(oldPrescription,
							LocalDateTime.ofInstant(pres.getEndTime().toInstant(), ZoneId.systemDefault()));
				} else {
					MedicationServiceHolder.get().stopPrescription(oldPrescription, LocalDateTime.now());
				}
				if (endDate != null && !endDate.isEmpty()) {
					// create new stopped prescription
					IPrescription newStoppedPrescription = MedicationServiceHolder.get()
							.createPrescriptionCopy(oldPrescription);
					newStoppedPrescription.setDateFrom(oldPrescription.getDateFrom());
					MedicationServiceHolder.get().stopPrescription(oldPrescription,
							LocalDateTime.ofInstant(pres.getEndTime().toInstant(), ZoneId.systemDefault()));
					newStoppedPrescription.setStopReason("Änderung des Stop Datums von " + endDate);
					// stop the old prescription with current time
					MedicationServiceHolder.get().stopPrescription(oldPrescription, LocalDateTime.now());
					CoreModelServiceHolder.get().save(newStoppedPrescription);
				} else {
					// apply stop reason if set
					if (txtStopComment.getText() == null || txtStopComment.getText().isEmpty()) {
						oldPrescription.setStopReason("Geändert durch " + CoreHub.getLoggedInContact().getLabel());
					} else {
						oldPrescription.setStopReason(txtStopComment.getText());
					}
				}
				CoreModelServiceHolder.get().save(oldPrescription);

			}
		});
		activateConfirmButton(false);
		if (btnStopMedication.isEnabled())
			showMedicationDetailComposite(null);

		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, pres.getPrescription());
	}

	/**
	 * show the medication detail composite; if the medication is stopped, show the
	 * stop relevant information, else show the default information
	 *
	 * A medication can not be stopped, if it is not a fixed medication, or was
	 * already stopped
	 *
	 * @param presc
	 */
	public void showMedicationDetailComposite(MedicationTableViewerItem presc) {
		// freezes the toggle for composite detail view
		if (presc == null && btnToggleDetailComposite != null && btnToggleDetailComposite.getSelection()) {
			return;
		}
		boolean showDetailComposite = presc != null;

		compositeMedicationDetail.setVisible(showDetailComposite);
		compositeMedicationDetailLayoutData.exclude = !(showDetailComposite);

		if (showDetailComposite && presc != null) {
			boolean stopped = presc.getEndDate().length() > 1;
			if (stopped) {
				stackLayout.topControl = compositeStopMedicationTextDetails;
			} else {
				stackLayout.topControl = compositeMedicationTextDetails;
			}
			txtEvening.setEnabled(!stopped);
			txtMorning.setEnabled(!stopped);
			txtNight.setEnabled(!stopped);
			txtNoon.setEnabled(!stopped);
			txtIntakeOrder.setEnabled(!stopped);
			txtDisposalComment.setEnabled(!stopped);
			txtStopComment.setEnabled(!stopped);
			txtFreeText.setEnabled(!stopped);
			dateStart.setEnabled(!stopped);
			dateStopped.setEnabled(false);
			timeStopped.setEnabled(false);

			LocalDateTime dateFrom = presc.getPrescription().getDateFrom();
			dateStart.setDate(dateFrom.getYear(), dateFrom.getMonthValue() - 1, dateFrom.getDayOfMonth());

			btnStopMedication.setEnabled(presc.isActiveMedication());
			stackedMedicationDetailComposite.layout();

			// set default color
			compositeMedicationDetail.setBackground(defaultBGColor);
			compositeStopMedicationTextDetails.setBackground(defaultBGColor);
			btnStopMedication.setSelection(false);
		}

		this.layout(true);
	}

	private void showSearchFilterComposite(boolean show) {
		medicationHistoryFilter.setSearchText(StringUtils.EMPTY);
		compositeSearchFilterLayoutData.exclude = !show;
		compositeSearchFilter.setVisible(show);
		this.layout(true);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void updateUi(IPatient patient, boolean forceUpdate) {
		if ((this.patient == patient || (this.patient != null && this.patient.equals(patient)))
				&& !forceUpdate) {
			return;
		}
		this.patient = patient;

		clearSearchFilter();
		lastDisposal.setValue(null);

		if (patient == null) {
			medicationTableComposite.setInput(Collections.emptyList());
			medicationHistoryTableComposite.setInput(Collections.emptyList());
			return;
		}

		List<IPrescription> medicationInput = MedicationViewHelper.loadInputData(false, patient);
		medicationTableComposite.setInput(medicationInput);

		List<IPrescription> medicationHistoryInput = MedicationViewHelper.loadInputData(true, patient);
		medicationHistoryTableComposite.setInput(medicationHistoryInput);

		contentProviderComp.refresh();
		selectedMedication.setValue(null);
		lblLastDisposalLink.setText(StringUtils.EMPTY);
		showMedicationDetailComposite(null);

		if (medicationInput != null) {
			String dailyCost = MedicationViewHelper.calculateDailyCostAsString(medicationInput);
			lblDailyTherapyCost.setText(dailyCost);
			interactionLink.updateAtcs(MedicationViewHelper.getAllGtins(medicationInput));

		} else {
			lblDailyTherapyCost.setText(StringUtils.EMPTY);
			interactionLink.updateAtcs(Collections.emptyList());
		}
		if (patient != null && !patient.getId().isEmpty()) {
			Object extInfoObj = patient.getExtInfo("lastModifiedInfo");
			String lastModifiedInfo = extInfoObj != null ? extInfoObj.toString() : StringUtils.EMPTY;
			if (lastModifiedInfo != null && !lastModifiedInfo.isEmpty()) {
				Pattern pattern = Pattern.compile("\\d{2}.\\d{2}.\\d{4}");
				Matcher matcher = pattern.matcher(lastModifiedInfo);
				if (matcher.find()) {
					String datum = matcher.group();
					lblLastModified.setText(datum);
					lblLastModified.setToolTipText(lastModifiedInfo);
				}
			} else {
				lblLastModified.setText(StringUtils.EMPTY);
				lblLastModified.setToolTipText(StringUtils.EMPTY);
			}
		} else {
			lblLastModified.setText(StringUtils.EMPTY);
			lblLastModified.setToolTipText(StringUtils.EMPTY);
		}
	}

	@Override
	public boolean setFocus() {
		if (medicationTableComposite != null && !medicationTableComposite.isDisposed()
				&& medicationTableComposite.isVisible()) {
			medicationTableComposite.setPendingInput();
		}
		if (medicationHistoryTableComposite != null && !medicationHistoryTableComposite.isDisposed()
				&& medicationHistoryTableComposite.isVisible()) {
			medicationHistoryTableComposite.setPendingInput();
		}

		return super.setFocus();
	}

	private void setValuesForTextSignature(String[] signatureArray) {
		boolean isFreetext = !signatureArray[0].isEmpty() && signatureArray[1].isEmpty() && signatureArray[2].isEmpty()
				&& signatureArray[3].isEmpty();
		if (isFreetext) {
			txtFreeText.setText(signatureArray[0]);
			txtMorning.setText(StringUtils.EMPTY);
			txtNoon.setText(StringUtils.EMPTY);
			txtEvening.setText(StringUtils.EMPTY);
			txtNight.setText(StringUtils.EMPTY);

			if (stackLayoutDosage.topControl == compositeDayTimeDosage) {
				stackLayoutDosage.topControl = compositeFreeTextDosage;
				stackCompositeDosage.layout();
			}
		} else {
			txtFreeText.setText(StringUtils.EMPTY);
			txtMorning.setText(signatureArray[0]);
			txtNoon.setText(signatureArray[1]);
			txtEvening.setText(signatureArray[2]);
			txtNight.setText(signatureArray[3]);

			if (stackLayoutDosage.topControl == compositeFreeTextDosage) {
				stackLayoutDosage.topControl = compositeDayTimeDosage;
				stackCompositeDosage.layout();
			}
		}
	}

	/**
	 * @return the values in the signature text array as dose string
	 */
	private String getDosisStringFromSignatureTextArray() {
		if (stackLayoutDosage.topControl == compositeDayTimeDosage) {
			String[] values = new String[4];
			values[0] = txtMorning.getText().isEmpty() ? "0" : txtMorning.getText(); //$NON-NLS-1$
			values[1] = txtNoon.getText().isEmpty() ? "0" : txtNoon.getText(); //$NON-NLS-1$
			values[2] = txtEvening.getText().isEmpty() ? "0" : txtEvening.getText(); //$NON-NLS-1$
			values[3] = txtNight.getText().isEmpty() ? "0" : txtNight.getText(); //$NON-NLS-1$

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < values.length; i++) {
				String string = values[i];
				if (string.length() > 0) {
					if (i > 0) {
						sb.append("-"); //$NON-NLS-1$
					}
					sb.append(string);
				}
			}
			return sb.toString();
		} else {
			return txtFreeText.getText();
		}
	}

	private void activateConfirmButton(boolean activate) {
		if (ctrlDecor == null)
			initControlDecoration();

		MedicationTableViewerItem pres = selectedMedication.getValue();
		if (pres == null || pres.isStopped()) {
			// activate = false;
		}

		if (activate) {
			btnConfirm.setBackground(tagBtnColor);
			btnConfirm.setEnabled(true);
			ctrlDecor.show();
		} else {
			btnConfirm.setBackground(defaultBGColor);
			btnConfirm.setEnabled(false);
			ctrlDecor.hide();
		}

	}

	private void initControlDecoration() {
		ctrlDecor = new ControlDecoration(btnConfirm, SWT.TOP);
		ctrlDecor.setDescriptionText(Messages.MedicationComposite_decorConfirm);
		Image imgWarn = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING)
				.getImage();
		ctrlDecor.setImage(imgWarn);
	}

	/**
	 * detects a change in the signature text array (i.e. txtMorning, txtNoon,
	 * txtEvening, txtNight)
	 */
	private class SignatureArrayModifyListener implements ModifyListener {

		final int index;

		public SignatureArrayModifyListener(int index) {
			this.index = index;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			String text = ((Text) e.getSource()).getText();
			activateConfirmButton(!signatureArray[index].equals(text));
		}
	}

	public void resetSelectedMedication() {
		selectedMedication.setValue(null);
	}

	public void setLastDisposal(Identifiable identifiable) {
		lastDisposal.setValue(identifiable);
		if (identifiable != null) {
			String label = StringUtils.EMPTY;
			if (identifiable instanceof IRecipe) {
				IRecipe rp = (IRecipe) identifiable;
				label = MessageFormat.format(Messages.MedicationComposite_recipeFrom,
						dateFormatter.format(rp.getDate()));
			} else if (identifiable instanceof IBilled) {
				IBilled billed = (IBilled) identifiable;
				if (billed.getEncounter() == null) {
					label = Messages.MedicationComposite_consMissing;
				} else {
					label = MessageFormat.format(Messages.MedicationComposite_consFrom,
							dateFormatter.format(billed.getEncounter().getDate()));
				}
			}
			lblLastDisposalLink.setText(label);
		} else {
			lblLastDisposalLink.setText(StringUtils.EMPTY);
		}
	}

	public void setSelectedMedication(MedicationTableViewerItem presc) {
		if (btnConfirm.getEnabled() && selectedMedication.getValue() != null) {
			if (MessageDialog.openQuestion(getShell(), "Speichern", "Sollen die Änderungen an "
					+ selectedMedication.getValue().getArtikelLabel() + " gespeichert werden?")) {
				applyDetailChanges();
			}
		}
		selectedMedication.setValue(presc);
		showMedicationDetailComposite(presc);

		signatureArray = MedicationServiceHolder.get()
				.getSignatureAsStringArray((presc != null) ? presc.getDosis() : null);
		setValuesForTextSignature(signatureArray);
	}

	public void refresh() {
		medicationTableComposite.getTableViewer().refresh();
		medicationHistoryTableComposite.getTableViewer().refresh();
	}

	public TableViewer getActiveTableViewer() {
		Control topControl = tablesLayout.topControl;
		if (topControl instanceof MedicationTableComposite) {
			return ((MedicationTableComposite) topControl).getTableViewer();
		} else if (topControl instanceof MedicationHistoryTableComposite) {
			return ((MedicationHistoryTableComposite) topControl).getTableViewer();
		}
		return null;
	}

	/**
	 * List of selection change listeners (element type:
	 * <code>ISelectionChangedListener</code>).
	 *
	 * @see #fireSelectionChanged
	 */
	private ListenerList selectionChangedListeners = new ListenerList();
	private IPrescription dropChangePrescription;

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		if (tablesLayout.topControl instanceof MedicationTableComposite) {
			return ((MedicationTableComposite) tablesLayout.topControl).getTableViewer().getSelection();
		} else if (tablesLayout.topControl instanceof MedicationHistoryTableComposite) {
			return ((MedicationHistoryTableComposite) tablesLayout.topControl).getTableViewer().getSelection();
		}
		return null;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (tablesLayout.topControl instanceof MedicationTableComposite) {
			((MedicationTableComposite) tablesLayout.topControl).getTableViewer().setSelection(selection);
		} else if (tablesLayout.topControl instanceof MedicationHistoryTableComposite) {
			((MedicationHistoryTableComposite) tablesLayout.topControl).getTableViewer().setSelection(selection);
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object[] listeners = selectionChangedListeners.getListeners();
		for (Object listener : listeners) {
			((ISelectionChangedListener) listener).selectionChanged(event);
		}
	}

	public ICodeSelectorTarget getDropTarget() {
		return dropTarget;
	}

	public void setDropChangePrescription(IPrescription changePrescription) {
		this.dropChangePrescription = changePrescription;
	}

	/**
	 * waits for dropps/double-clicks on a medication
	 *
	 */
	private final class DropMedicationReceiver implements GenericObjectDropTarget.IReceiver {

		private Shell parentShell;

		public DropMedicationReceiver(Shell parentShell) {
			this.parentShell = parentShell;
		}

		@Override
		public void dropped(List<Object> list, DropTargetEvent e) {
			for (Object object : list) {
				if (object instanceof IArticle) {
					IArticle article = (IArticle) object;
					if (isVaccination(article)) {
						MessageDialog.openWarning(parentShell, Messages.MedicationComposite_isVaccinationTitle,
								Messages.MedicationComposite_isVaccinationText);
						return;
					}
					if (dropChangePrescription == null) {
						CreatePrescriptionHelper prescriptionHelper = new CreatePrescriptionHelper(article,
								parentShell);
						prescriptionHelper.createPrescription();
					} else {
						IPrescription changedPrescription = MedicationServiceHolder.get()
								.createPrescriptionCopy(dropChangePrescription);
						changedPrescription.setArticle(article);
						MedicationServiceHolder.get().stopPrescription(dropChangePrescription, LocalDateTime.now());
						dropChangePrescription.setStopReason("Ersetzt durch " + article.getName());
						CoreModelServiceHolder.get().save(Arrays.asList(dropChangePrescription, changedPrescription));
						dropChangePrescription = null;
					}
					refresh();
				}
			}
		}

		private boolean isVaccination(IArticle article) {
			return article.isVaccination();
		}

		@Override
		public boolean accept(List<Object> list) {
			for (Object object : list) {
				if (!(object instanceof IArticle))
					return false;
				// we do not accept vaccination articles
				return !isVaccination((IArticle) object);
			}
			return true;
		}
	}

	public String setCurrentDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		String currentDateTime = formatter.format(LocalDateTime.now());
		return currentDateTime;
	}

	public String setCurrentDate() {
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		String currentDate = dateFormatter.format(localDate);

		return currentDate;
	}

}
