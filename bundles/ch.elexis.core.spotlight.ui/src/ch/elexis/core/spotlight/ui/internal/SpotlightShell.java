package ch.elexis.core.spotlight.ui.internal;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.controls.SpotlightResultComposite;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class SpotlightShell extends Shell {
	
	private ISpotlightService spotlightService;
	private ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService;
	
	private Timer timer;
	private Text txtSearchInput;
	private SpotlightResultComposite resultComposite;
	private GridData resultCompositeGridData;
	
	private SpotlightUiUtil uiUtil;
	
	public SpotlightShell(Shell shell, ISpotlightService spotlightService,
		ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService){
		super(shell, SWT.NO_TRIM | SWT.TOOL);
		this.spotlightService = spotlightService;
		this.resultEntryDetailCompositeService = resultEntryDetailCompositeService;
		
		// ESC closes the shell
		addListener(SWT.Traverse, event -> {
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
				close();
				event.detail = SWT.TRAVERSE_NONE;
				event.doit = false;
				break;
			}
		});
		
		uiUtil = new SpotlightUiUtil();
		CoreUiUtil.injectServicesWithContext(uiUtil);
		
		setSize(700, 40);
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
		GridLayout gridLayout = new GridLayout(2, false);
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
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run(){
					spotlightService.computeResult(text, null);
				}
			}, 200);
			
		});
		txtSearchInput.addListener(SWT.Traverse, event -> {
			if (event.keyCode == SWT.ARROW_DOWN) {
				event.detail = SWT.TRAVERSE_NONE;
				event.doit = false;
				resultComposite.setFocus();
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
		
		resultComposite = new SpotlightResultComposite(this, SWT.NONE, spotlightService, uiUtil,
			resultEntryDetailCompositeService);
		resultCompositeGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		resultCompositeGridData.exclude = true;
		resultComposite.setLayoutData(resultCompositeGridData);
		
		txtSearchInput.setFocus();
	}
	
	public void refresh(){
		resultCompositeGridData.exclude = false;
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
