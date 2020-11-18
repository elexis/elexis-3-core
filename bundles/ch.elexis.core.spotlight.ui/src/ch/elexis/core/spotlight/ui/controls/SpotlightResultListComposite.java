package ch.elexis.core.spotlight.ui.controls;

import java.util.function.Consumer;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.SpotlightShell;
import ch.elexis.core.spotlight.ui.SpotlightUiUtil;

public class SpotlightResultListComposite extends Composite {
	
	private static Font categoryFont;
	
	private SpotlightResultDetailComposite resultDetailComposite;
	
	private TableViewer tvSpotlightResults;
	private Table tableSpotlightResults;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param spotlightService
	 * @param uiUtil
	 */
	public SpotlightResultListComposite(Composite parent, int style,
		ISpotlightService spotlightService, SpotlightUiUtil uiUtil){
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite = new Composite(this, SWT.NONE);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		tvSpotlightResults = new TableViewer(composite, SWT.FULL_SELECTION);
		tableSpotlightResults = tvSpotlightResults.getTable();
		tableSpotlightResults.setBackground(parent.getBackground());
		
		TableViewerColumn tvcIcon = new TableViewerColumn(tvSpotlightResults, SWT.NONE);
		TableColumn tcIcon = tvcIcon.getColumn();
		tcl_composite.setColumnData(tcIcon, new ColumnPixelData(20, false, false));
		
		TableViewerColumn tvcLabel = new TableViewerColumn(tvSpotlightResults, SWT.NONE);
		TableColumn tcLabel = tvcLabel.getColumn();
		tcl_composite.setColumnData(tcLabel,
			new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, false));
		
		tvSpotlightResults.setContentProvider(new SpolightResultListContentProvider());
		if (categoryFont == null) {
			FontData[] fontData = parent.getFont().getFontData();
			fontData[0].setHeight(8);
			categoryFont = new Font(this.getDisplay(), fontData[0]);
		}
		SpotlightResultLabelProvider srllp =
			new SpotlightResultLabelProvider(parent.getFont(), categoryFont);
		tvSpotlightResults.setLabelProvider(srllp);
		
		tableSpotlightResults.addListener(SWT.KeyDown, event -> {
			// TODO prevent selection of Category objects
			int keyCode = event.keyCode;
			switch (keyCode) {
			case SWT.ARROW_UP:
			case SWT.ARROW_DOWN:
				System.out.println("event");
				return;
			case SWT.ARROW_RIGHT:
				// try to focus detail composite
				boolean result = resultDetailComposite.setFocus();
				if (!result) {
					event.doit = false;
				}
				return;
			case SWT.ARROW_LEFT:
				// ignore
				return;
			case 13:
				// enter, handle and close
				ISpotlightResultEntry selected = (ISpotlightResultEntry) tvSpotlightResults
					.getStructuredSelection().getFirstElement();
				uiUtil.handleEnter(selected);
				((Shell) getParent().getParent()).close();
				return;
			default:
				break;
			}
			// user wants to modify the filter
			((SpotlightShell) parent.getParent()).setFocusAppendChar(event.character);
		});
		
		tvSpotlightResults.addSelectionChangedListener(sel -> {
			Object firstElement = sel.getStructuredSelection().getFirstElement();
			if (firstElement instanceof ISpotlightResultEntry) {
				resultDetailComposite.setSelection((ISpotlightResultEntry) firstElement);
			} else {
				resultDetailComposite.setSelection(null);
			}
		});
		
		final SpotlightShell _spotlightShell = (SpotlightShell) this.getShell();
		Consumer<ISpotlightResult> resultsChangedConsumer = newInput -> {
			_spotlightShell.getDisplay().asyncExec(() -> {
				tvSpotlightResults.setInput(newInput);
				_spotlightShell.refresh();
			});
		};
		spotlightService.setResultsChangedConsumer(resultsChangedConsumer);
		
	}
	
	@Override
	public boolean setFocus(){
		boolean result = tableSpotlightResults.setFocus();
		// first non category element
		int itemCount = tableSpotlightResults.getItemCount();
		if (itemCount >= 1) {
			Object item = tableSpotlightResults.getItem(1).getData();
			tvSpotlightResults.setSelection(new StructuredSelection(item));
		}
		return result;
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	void setDetailComposite(SpotlightResultDetailComposite resultDetailComposite){
		this.resultDetailComposite = resultDetailComposite;
	}
}
