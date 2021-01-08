package ch.elexis.core.spotlight.ui.internal;

import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.controls.SpotlightResultComposite;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyComposite;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyService;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class SpotlightShell extends Shell {
	
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
	
	public SpotlightShell(Shell shell, ISpotlightService spotlightService,
		ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService,
		SpotlightReadyService spotlightReadyService,
		Map<String, String> spotlightContextParameters){
		super(shell, SWT.NO_TRIM | SWT.TOOL);
		this.spotlightService = spotlightService;
		this.resultEntryDetailCompositeService = resultEntryDetailCompositeService;
		this.spotlightReadyService = spotlightReadyService;
		this.spotlightContextParameters = spotlightContextParameters;
		
		// ESC closes the shell
		addListener(SWT.Traverse, event -> {
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
				close();
				event.detail = SWT.TRAVERSE_NONE;
				event.doit = false;
				break;
			case SWT.TRAVERSE_RETURN:
				// TODO globally handle enter here?
				System.out.println("ENTER");
				break;
			}
		});
		
		// clicking outside closes shell
		addListener(SWT.Deactivate, event -> close());
		
		uiUtil = new SpotlightUiUtil();
		CoreUiUtil.injectServicesWithContext(uiUtil);
		
		setSize(700, 400);
		createContents();
	}
	
	private final String SEARCH_ICON = "spotlight-search-icon";
	private final String SEARCHTEXT_FONT = "spotlight-searchtext-font";
	
	/**
	 * Create contents of the shell.
	 * 
	 * @param spotlightService
	 */
	protected void createContents(){
		GridLayout gridLayout = new GridLayout(3, false);
		setLayout(gridLayout);
		
		Label lblIcon = new Label(this, SWT.NONE);
		Image logo = JFaceResources.getImageRegistry().get(SEARCH_ICON);
		if (logo == null) {
			Path path = new Path("rsc/icons/magnifier-left.png");
			URL fileLocation =
				FileLocator.find(FrameworkUtil.getBundle(SpotlightShell.class), path, null);
			ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
			JFaceResources.getImageRegistry().put(SEARCH_ICON, id);
			logo = JFaceResources.getImageRegistry().get(SEARCH_ICON);
		}
		lblIcon.setImage(logo);
		lblIcon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		filterComposite = new Composite(this, SWT.None);
		filterComposite.setLayout(new GridLayout(1, false));
		filterComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		filterComposite.setBackground(this.getBackground());
		
		if (spotlightContextParameters != null) {
			if (spotlightContextParameters
				.containsKey(ISpotlightService.CONTEXT_FILTER_PATIENT_ID)) {
				
				Label patientFilter = new Label(filterComposite, SWT.None);
				patientFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				patientFilter.setText("PF");
				patientFilter.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
			}
		}
		
		txtSearchInput = new Text(this, SWT.None);
		txtSearchInput.setBackground(this.getBackground());
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
		txtSearchInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearchInput.setTextLimit(256);
		txtSearchInput.addModifyListener(change -> {
			final String text = ((Text) change.widget).getText();
			if (timer != null) {
				timer.cancel();
			}
			if (StringUtils.isEmpty(text)) {
				detailCompositeStackLayout.topControl = readyComposite;
			} else {
				detailCompositeStackLayout.topControl = resultComposite;
			}
			layeredComposite.layout(true, true);
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run(){
					spotlightService.computeResult(text, spotlightContextParameters);
				}
			}, 200);
		});
		txtSearchInput.addListener(SWT.Traverse, event -> {
			if (event.keyCode == SWT.ARROW_DOWN) {
				event.detail = SWT.TRAVERSE_NONE;
				event.doit = false;
				detailCompositeStackLayout.topControl.setFocus();
			}
		});
		txtSearchInput.addListener(SWT.KeyDown, event -> {
			if (event.keyCode == 13) {
				boolean success = resultComposite.handleEnterOnFirstSpotlightResultEntry();
				if (success) {
					close();
				}
			}
		});
		
		Label lblSeparator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		layeredComposite = new Composite(this, SWT.NONE);
		detailCompositeStackLayout = new StackLayout();
		layeredComposite.setLayout(detailCompositeStackLayout);
		layeredComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		readyComposite =
			new SpotlightReadyComposite(layeredComposite, SWT.NONE, spotlightReadyService, uiUtil);
		detailCompositeStackLayout.topControl = readyComposite;
		resultComposite = new SpotlightResultComposite(layeredComposite, SWT.NONE, spotlightService,
			uiUtil, resultEntryDetailCompositeService);
		
		txtSearchInput.setFocus();
	}
	
	public void refresh(){
		setSize(700, 400);
	}
	
	public boolean setFocusAppendChar(char charachter){
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
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
}
