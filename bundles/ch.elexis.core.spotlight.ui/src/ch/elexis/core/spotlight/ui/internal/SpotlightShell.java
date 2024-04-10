package ch.elexis.core.spotlight.ui.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.pdfbox.ui.parts.PdfPreviewPartLoadHandler;
import ch.elexis.core.pdfbox.ui.parts.handlers.PDFTextHighlighter;
import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.controls.SpotlightResultComposite;
import ch.elexis.core.spotlight.ui.controls.SpotlightSearchHelper;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyComposite;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyService;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;

public class SpotlightShell extends Shell {

	private static boolean ispdfPreviewComp = false;
	private EPartService partService;
	private ISpotlightService spotlightService;
	private ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService;
	private SpotlightReadyService spotlightReadyService;
	private Map<String, String> spotlightContextParameters;

	private Timer timer;
	private Text txtSearchInput;
	private Composite filterComposite;
	private Composite layeredComposite;
	private SpotlightResultComposite resultComposite;
	private SpotlightReadyComposite readyComposite;
	private StackLayout detailCompositeStackLayout;
	private SpotlightUiUtil uiUtil;
	private String searchText;
	private Object selectedElement;
	private Composite previewComposite;
	private GridData layeredCompositeGridData;
	private Point origin;
	private PdfPreviewPartLoadHandler pdfPreviewPartLoadHandler;
	private ScrolledComposite scrolledComposite;
	private Label lblFound;

	public SpotlightShell(Shell shell, EPartService partService, ISpotlightService spotlightService,
			ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService,
			SpotlightReadyService spotlightReadyService, Map<String, String> spotlightContextParameters) {
		super(shell, SWT.NO_TRIM | SWT.TOOL | SWT.BORDER);
		this.partService = partService;
		this.spotlightService = spotlightService;
		this.resultEntryDetailCompositeService = resultEntryDetailCompositeService;
		this.spotlightReadyService = spotlightReadyService;
		this.spotlightContextParameters = spotlightContextParameters;

		// globally handle ESC and ENTER
		addListener(SWT.Traverse, event -> {
			switch (event.detail) {
			// ESC closes the shell
			case SWT.TRAVERSE_ESCAPE:
//				System.out.println(shell.getDisplay().getFocusControl().getClass().getName());
				close();
				event.detail = SWT.TRAVERSE_NONE;
				event.doit = false;
				break;
			// ENTER performs the action defined for the selected element
			case SWT.TRAVERSE_RETURN:
				boolean ok = handleSelectedElement();
				if (ok) {
					if (!isDisposed()) {
						close();
					}
				}
				event.detail = SWT.TRAVERSE_NONE;
				event.doit = true;
				break;
			}
		});

		// clicking outside closes shell
		addListener(SWT.Deactivate, event -> close());

		uiUtil = new SpotlightUiUtil(partService);
		CoreUiUtil.injectServicesWithContext(uiUtil);

		setSize(800, 500);
		createContents();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				origin = new Point(e.x, e.y);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				origin = null;
			}
		});

		addMouseMoveListener(e -> {
			if (origin != null) {
				Point p = toDisplay(e.x, e.y);
				setLocation(p.x - origin.x, p.y - origin.y);
			}
		});
	}

	private final String SEARCH_ICON = "spotlight-search-icon";
	private final String SEARCHTEXT_FONT = "spotlight-searchtext-font";
	private int lableText ;
	private Composite parentComposite;
	private InputStream pdfIDocument;

	/**
	 * Create contents of the shell.
	 *
	 * @param spotlightService
	 */
	protected void createContents() {
		GridLayout gridLayout = new GridLayout(3, false); 
	    setLayout(gridLayout);
		addListener(SWT.Show, event -> adjustShellSize(false));
		filterComposite = new Composite(this, SWT.NO_FOCUS);
		filterComposite.setLayout(new GridLayout(1, false));
		filterComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		filterComposite.setBackground(this.getBackground());
		if (spotlightContextParameters != null) {
			if (spotlightContextParameters.containsKey(ISpotlightService.CONTEXT_FILTER_PATIENT_ID)) {
				Label patientFilter = new Label(filterComposite, SWT.None);
				patientFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				patientFilter.setImage(Images.IMG_PERSON.getImage());
				patientFilter.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
				patientFilter.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			}
		}
		Composite searchComposite = new Composite(this, SWT.NONE);
		searchComposite.setLayout(new GridLayout(2, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label lblIcon = new Label(searchComposite, SWT.NONE);
		lblIcon.setImage(getImage(SEARCH_ICON, "rsc/icons/magnifier-left-24.png"));
		lblIcon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		txtSearchInput = new Text(searchComposite, SWT.NONE);
		GridData txtSearchInputGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		txtSearchInput.setLayoutData(txtSearchInputGridData);
		txtSearchInput.setBackground(this.getBackground());
		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(3, false));
		GridData buttonGridData = new GridData();
		buttonGridData.horizontalAlignment = GridData.FILL;
		buttonGridData.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(buttonGridData);
		GridData gridData = new GridData(SWT.END, SWT.LEFT, false, false);
		gridData.widthHint = 55;
		gridData.heightHint = 20;
		lblFound = new Label(buttonComposite, SWT.RIGHT | SWT.CENTER);
		lblFound.setText("Gefundene Suchwörter:     00 / 00  ");
		lblFound.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button upButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
		upButton.setImage(Images.IMG_PREVIOUS.getImage());
		upButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		upButton.setLayoutData(gridData);
		Button downButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
		downButton.setImage(Images.IMG_NEXT.getImage());
		downButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		downButton.setLayoutData(gridData);
		txtSearchInput.setToolTipText(Messages.SpotlightSerchHelText);
		Font biggerFont;
		if (JFaceResources.getFontRegistry().hasValueFor(SEARCHTEXT_FONT)) {
			biggerFont = JFaceResources.getFontRegistry().get(SEARCHTEXT_FONT);
		} else {
			FontData[] fontData = txtSearchInput.getFont().getFontData();
			fontData[0].setHeight(20);
			JFaceResources.getFontRegistry().put(SEARCHTEXT_FONT, fontData);
			biggerFont = JFaceResources.getFontRegistry().get(SEARCHTEXT_FONT);
		}
		txtSearchInput.setFont(biggerFont);
		txtSearchInput.setMessage("Suchbegriff eingeben");
		txtSearchInput.setTextLimit(256);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SpotlightSearchHelper.getNextPosition();
				updateLabel();
				try {
					PDFTextHighlighter.getNextMatch();
					if (pdfPreviewPartLoadHandler != null) {
						pdfPreviewPartLoadHandler.reloadPdf();
					}
					updateLabel();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SpotlightSearchHelper.getPreviousPosition();
				updateLabel();
				try {
					PDFTextHighlighter.getPreviousMatch();
					if (pdfPreviewPartLoadHandler != null) {
						pdfPreviewPartLoadHandler.reloadPdf();
					}
					updateLabel();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		txtSearchInput.addModifyListener(change -> {
			final String text = ((Text) change.widget).getText();
			setSearchText(text);
			String searchTextForSearch = text.replace("+", " ");
			if (timer != null) {
				timer.cancel();
			}
			boolean isReadyMode = StringUtils.isEmpty(text);
			switchReadyResultMode(isReadyMode);
			layeredComposite.layout(true, true);
			adjustShellSize(false);
			resultComposite.clearResults();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Display.getDefault().asyncExec(() -> {
						if (!isDisposed()) {
							spotlightService.computeResult(searchTextForSearch, spotlightContextParameters);
							SpotlightSearchHelper.clearTotalMatches();
						}
					});
				}
			}, 200);
		});
		txtSearchInput.addListener(SWT.KeyDown, event -> {
			if (event.keyCode == 13) {
				boolean success = resultComposite.handleEnterOnFirstSpotlightResultEntry();
				if (success) {
					close();
				}
			} else if (event.keyCode == SWT.ARROW_DOWN) {
				layeredComposite.setFocus();
			}

			if (event.stateMask == SWT.ALT) {
				event.doit = false;
				boolean success = resultComposite.handleAltKeyPressed(event.keyCode);
				if (success && !isDisposed()) {
					close();
				}
			}
		});

		Label lblSeparator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		parentComposite = new Composite(this, SWT.NONE);
		GridLayout parentLayout = new GridLayout(2, false);
		parentComposite.setLayout(parentLayout);
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		layeredComposite = new Composite(parentComposite, SWT.NONE);
		detailCompositeStackLayout = new StackLayout();
		layeredComposite.setLayout(detailCompositeStackLayout);
		layeredCompositeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layeredCompositeGridData.widthHint = 1200;
		layeredComposite.setLayoutData(layeredCompositeGridData);

		readyComposite = new SpotlightReadyComposite(layeredComposite, SWT.NONE, partService, spotlightReadyService);
		detailCompositeStackLayout.topControl = readyComposite;
		resultComposite = new SpotlightResultComposite(layeredComposite, SWT.NONE, spotlightService, uiUtil,
				resultEntryDetailCompositeService);

		scrolledComposite = new ScrolledComposite(parentComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		GridData scGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledComposite.setLayoutData(scGridData);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		previewComposite = new Composite(scrolledComposite, SWT.NONE);
		previewComposite.setLayout(new GridLayout(1, true));

		Label label = new Label(previewComposite, SWT.NONE);
		label.setText(Messages.PdfPreview_NoPDFSelected);
		previewComposite.layout();
		scrolledComposite.setContent(previewComposite);
		scrolledComposite.setMinSize(previewComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		this.setTabList(new Control[] { searchComposite, parentComposite });
		parentComposite.setTabList(new Control[] { layeredComposite, scrolledComposite });
		switchReadyResultMode(true);
		txtSearchInput.setFocus();
		previewComposite.setVisible(ispdfPreviewComp);
		adjustShellSize(true);
	}

	private void switchReadyResultMode(boolean setReadyMode) {
		if (setReadyMode) {
			resultComposite.setEnabled(false);
			readyComposite.setEnabled(true);
			detailCompositeStackLayout.topControl = readyComposite;
			layeredComposite.setTabList(new Control[] { readyComposite });
			resultComposite.clearResults();	
		} else {
			resultComposite.setEnabled(true);
			readyComposite.setEnabled(false);
			detailCompositeStackLayout.topControl = resultComposite;
			layeredComposite.setTabList(new Control[] { resultComposite });
		}
	}

	public static boolean ispdfPreviewComposite() {
		return ispdfPreviewComp;
	}

	public boolean setFocusAppendChar(char charachter) {
		boolean result = txtSearchInput.setFocus();
		String text = txtSearchInput.getText();
		if (SWT.BS == charachter) {
			txtSearchInput.setText(text.substring(0, text.length() - 1));
		} else {
			txtSearchInput.setText(text + charachter);
		}
		txtSearchInput.setSelection(txtSearchInput.getText().length());
		return result;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setSelectedElement(Object element) {
		this.selectedElement = element;
	}

	public boolean handleSelectedElement() {
		return uiUtil.handleEnter(selectedElement);
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText.trim();
	}

	public void setlableText(int count) {
		this.lableText = count;
		lblFound.setText("Gefundene Suchwörter: " + count);
		Display.getCurrent().syncExec(() -> this.getParent().layout(true, true));
	}

	public int getlableText() {
		return lableText;
	}

	public void updatePdfPreview(InputStream pdfIDocument2) {
		this.pdfIDocument = pdfIDocument2;
		pdfPreviewPartLoadHandler = new PdfPreviewPartLoadHandler(pdfIDocument2, Float.valueOf("0.9f"),
				previewComposite,
				scrolledComposite);
		pdfPreviewPartLoadHandler.setSearchText(searchText);
		previewComposite.setVisible(ispdfPreviewComp);
	}

	public void adjustShellSize(boolean pdfViewerVisible) {
		if (pdfViewerVisible) {
			setSize(1300, 800);
			layeredCompositeGridData.widthHint = 950;
			ispdfPreviewComp = true;
		} else {
			setSize(800, 500);
			layeredCompositeGridData.widthHint = 500;
			ispdfPreviewComp = false;
		}
	}

	public void updateLabel() {
		int currentPosition = SpotlightSearchHelper.getCurrentPosition() + 1;
		int total = SpotlightSearchHelper.getTotalMatches();
		lblFound.setText("Gefundene Suchwörter: " + currentPosition + " / " + total);
		Display.getCurrent().syncExec(() -> this.getParent().layout(true, true));
	}

	private Image getImage(String iconKey, String imagePath) {
		Image icon = JFaceResources.getImageRegistry().get(iconKey);
		if (icon == null) {
			Path path = new Path(imagePath);
			URL fileLocation = FileLocator.find(FrameworkUtil.getBundle(SpotlightShell.class), path, null);
			ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
			JFaceResources.getImageRegistry().put(iconKey, id);
			icon = JFaceResources.getImageRegistry().get(iconKey);
		}
		return icon;
	}
}