package ch.elexis.core.spotlight.ui.internal;

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
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.controls.SpotlightResultComposite;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyComposite;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyService;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;

public class SpotlightShell extends Shell {
	
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
	
	private Object selectedElement;
	
	public SpotlightShell(Shell shell, EPartService partService, ISpotlightService spotlightService,
		ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService,
		SpotlightReadyService spotlightReadyService,
		Map<String, String> spotlightContextParameters){
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
					if(!isDisposed()) {
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
		
		setSize(700, 500);
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
			Path path = new Path("rsc/icons/magnifier-left-24.png");
			URL fileLocation =
				FileLocator.find(FrameworkUtil.getBundle(SpotlightShell.class), path, null);
			ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
			JFaceResources.getImageRegistry().put(SEARCH_ICON, id);
			logo = JFaceResources.getImageRegistry().get(SEARCH_ICON);
		}
		lblIcon.setImage(logo);
		lblIcon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		filterComposite = new Composite(this, SWT.NO_FOCUS);
		filterComposite.setLayout(new GridLayout(1, false));
		filterComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		filterComposite.setBackground(this.getBackground());
		
		if (spotlightContextParameters != null) {
			if (spotlightContextParameters
				.containsKey(ISpotlightService.CONTEXT_FILTER_PATIENT_ID)) {
				
				Label patientFilter = new Label(filterComposite, SWT.None);
				patientFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				patientFilter.setImage(Images.IMG_PERSON.getImage());
				patientFilter.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
				patientFilter.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
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
			
			boolean isReadyMode = StringUtils.isEmpty(text);
			switchReadyResultMode(isReadyMode);
			layeredComposite.layout(true, true);
			
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run(){
					spotlightService.computeResult(text, spotlightContextParameters);
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
		
			if(event.stateMask == SWT.ALT) {
				event.doit=false;
				boolean success = resultComposite.handleAltKeyPressed(event.keyCode);
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
			new SpotlightReadyComposite(layeredComposite, SWT.NONE, partService, spotlightReadyService);
		detailCompositeStackLayout.topControl = readyComposite;
		
		resultComposite = new SpotlightResultComposite(layeredComposite, SWT.NONE, spotlightService,
			uiUtil, resultEntryDetailCompositeService);
		
		setTabList(new Control[] {
			txtSearchInput, layeredComposite
		});
		
		switchReadyResultMode(true);
		
		txtSearchInput.setFocus();
	}
	
	private void switchReadyResultMode(boolean setReadyMode){
		if (setReadyMode) {
			resultComposite.setEnabled(false);
			readyComposite.setEnabled(true);
			detailCompositeStackLayout.topControl = readyComposite;
			layeredComposite.setTabList(new Control[] {
				readyComposite
			});
		} else {
			resultComposite.setEnabled(true);
			readyComposite.setEnabled(false);
			detailCompositeStackLayout.topControl = resultComposite;
			layeredComposite.setTabList(new Control[] {
				resultComposite
			});
		}
		
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
	
	public void setSelectedElement(Object element){
		this.selectedElement = element;
	}
	
	public boolean handleSelectedElement() {
		return uiUtil.handleEnter(selectedElement);
	}
	
}
