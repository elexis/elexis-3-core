package ch.elexis.core.spotlight.ui.controls;

import java.util.function.Consumer;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.internal.SpotlightShell;
import ch.elexis.core.spotlight.ui.internal.SpotlightUiUtil;

public class SpotlightResultListComposite extends Composite {

	private static Font categoryFont;

	private SpotlightResultDetailComposite resultDetailComposite;

	private TableViewer tvSpotlightResults;
	private Table tableSpotlightResults;
	private SpotlightUiUtil uiUtil;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param spotlightService
	 * @param uiUtil
	 */
	public SpotlightResultListComposite(Composite parent, int style, ISpotlightService spotlightService,
			SpotlightUiUtil uiUtil) {
		super(parent, style);

		this.uiUtil = uiUtil;
		final SpotlightShell _spotlightShell = (SpotlightShell) this.getShell();

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
		tcl_composite.setColumnData(tcLabel, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, false));

		tvSpotlightResults.setContentProvider(new SpolightResultListContentProvider());
		if (categoryFont == null) {
			FontData[] fontData = parent.getFont().getFontData();
			fontData[0].setHeight(8);
			categoryFont = new Font(this.getDisplay(), fontData[0]);
		}
		SpotlightResultLabelProvider srllp = new SpotlightResultLabelProvider(parent.getFont(), categoryFont);
		tvSpotlightResults.setLabelProvider(srllp);

		tableSpotlightResults.addListener(SWT.KeyDown, event -> {
		    if ((event.stateMask & SWT.ALT) != 0) {
		        event.doit = false;

		        boolean success = false;
		        if (resultDetailComposite != null) {
		            success = resultDetailComposite.handleAltKeyPressed(event.keyCode);
		        }

		        if (success && !_spotlightShell.isDisposed()) {
		            _spotlightShell.close();
		        }
		        return;
		    }
			// TODO prevent selection of Category objects
			int keyCode = event.keyCode;
			switch (keyCode) {
			case SWT.ARROW_UP:
			case SWT.ARROW_DOWN:
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
			default:
				break;
			}

			// user wants to modify the filter
			if (event.character != 0) {
				_spotlightShell.setFocusAppendChar(event.character);
				event.doit = false;
			}
		});

		tableSpotlightResults.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				TableItem item = tableSpotlightResults.getItem(new Point(e.x, e.y));
				if (item != null) {
					tvSpotlightResults.setSelection(new StructuredSelection(item.getData()), true);

				}
			}
		});

		tableSpotlightResults.addListener(SWT.FocusOut, event -> {
			tvSpotlightResults.setSelection(null);
		});

		tvSpotlightResults.addSelectionChangedListener(sel -> {
			Object firstElement = sel.getStructuredSelection().getFirstElement();
			if (firstElement instanceof ISpotlightResultEntry) {
				resultDetailComposite.setSelection((ISpotlightResultEntry) firstElement);
				((SpotlightShell) getShell()).setSelectedElement(firstElement);
				uiUtil.handleDocumentSelectionAndPreview(firstElement, _spotlightShell);
			}
		});

		Consumer<ISpotlightResult> resultsChangedConsumer = newInput -> {
			if (!_spotlightShell.isDisposed()) {
				_spotlightShell.getDisplay().asyncExec(() -> {
					if (!tableSpotlightResults.isDisposed()) {
						tableSpotlightResults.clearAll();
						tvSpotlightResults.setInput(newInput);
					}
				});
			}
		};
		spotlightService.setResultsChangedConsumer(resultsChangedConsumer);

	}

	@Override
	public boolean setFocus() {
		return tableSpotlightResults.setFocus();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	void setDetailComposite(SpotlightResultDetailComposite resultDetailComposite) {
		this.resultDetailComposite = resultDetailComposite;
	}

	/**
	 * Perform the default enter action on the first element in the list. Does
	 * nothing if no element.
	 *
	 * @return <code>true</code> if action was performed
	 */
	public boolean handleEnterOnFirstSpotlightResultEntry() {
		Object element = tvSpotlightResults.getElementAt(1);
		return uiUtil.handleEnter(element);
	}
}