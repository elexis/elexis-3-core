package ch.elexis.core.findings.ui.dialogs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;

public class VisibleCodingsSelectionDialog extends TitleAreaDialog {

	private List<ICoding> available;

	private CheckboxTableViewer viewer;

	private List<ICoding> selected;

	private Font boldFont;

	public VisibleCodingsSelectionDialog(Shell parentShell, List<ICoding> available) {
		super(parentShell);
		this.available = available;

	}

	@Override
	public void create() {
		super.create();
		setTitle("Sichtbare Befunde Codes auswählen.");
		setMessage(
				"Definiert welche Codes bei der Ansicht der Werte zur Auswahl stehen.\nWenn kein Code ausgewählt ist, stehen alle zu Auswahl.");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new GridLayout());

		ToolBarManager mgr = new ToolBarManager();
		mgr.add(new Action("Gruppen selektieren") {
			@Override
			public void run() {
				List<ICoding> groups = available.stream().filter(c -> FindingsUiUtil.isCodingForGroup(c))
						.collect(Collectors.toList());
				viewer.setAllChecked(false);
				for (ICoding iCoding : groups) {
					viewer.setChecked(iCoding, true);
				}
				selected = groups;
			}
		});
		ToolBar toolbar = mgr.createControl(ret);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Table table = new Table(ret, SWT.MULTI | SWT.CHECK);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		boldFont = createBoldFont(table.getFont());

		viewer = new CheckboxTableViewer(table);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider());

		viewer.setInput(available);
		for (ICoding iCoding : selected) {
			viewer.setChecked(iCoding, true);
		}

		return ret;
	}

	@Override
	protected void okPressed() {
		this.selected = Arrays.asList(viewer.getCheckedElements()).stream().map(c -> (ICoding) c)
				.collect(Collectors.toList());
		super.okPressed();
	}

	public void setSelected(List<ICoding> selected) {
		this.selected = selected;
	}

	public List<ICoding> getSelected() {
		return selected;
	}

	private Font createBoldFont(Font baseFont) {
		FontData fd = baseFont.getFontData()[0];
		Font font = new Font(baseFont.getDevice(), fd.getName(), fd.getHeight(), fd.getStyle() | SWT.BOLD);
		return font;
	}

	private class LabelProvider extends org.eclipse.jface.viewers.LabelProvider implements ITableFontProvider {

		@Override
		public String getText(Object element) {
			ICoding iCoding = (ICoding) element;
			return iCoding != null ? iCoding.getDisplay() + " (" + iCoding.getCode() + ")" : "";
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			if (FindingsUiUtil.isCodingForGroup((ICoding) element)) {
				return boldFont;
			}
			return null;
		}

	}
}
