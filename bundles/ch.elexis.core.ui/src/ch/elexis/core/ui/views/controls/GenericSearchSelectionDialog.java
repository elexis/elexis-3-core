package ch.elexis.core.ui.views.controls;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.Brief;
import ch.elexis.data.PersistentObject;

public class GenericSearchSelectionDialog extends TitleAreaDialog {

	private List<?> input;
	private List<Object> selection = new LinkedList<>();

	private String shellTitle;
	private String title;
	private String message;
	private Image image;
	private int style;

	private AbstractTableViewer structuredViewer;
	private SearchDataDialog filter;

	public GenericSearchSelectionDialog(Shell parentShell, List<?> input, String shellTitle, String title,
			String message, Image image, int style) {
		super(parentShell);
		this.shellTitle = shellTitle;
		this.title = title;
		this.message = message;
		this.input = input;
		this.image = image;
		this.style = style;
	}

	public void setSelection(List<Object> selection) {
		this.selection = new LinkedList<>(selection);
	}

	public IStructuredSelection getSelection() {
		return new StructuredSelection(selection);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(title);
		setMessage(message);
		setTitleImage(image);

		Composite ret = (Composite) super.createDialogArea(parent);

		Text text = new Text(ret, SWT.BORDER);
		GridData textGridData = new GridData();
		textGridData.grabExcessVerticalSpace = false;
		textGridData.grabExcessHorizontalSpace = true;
		textGridData.horizontalAlignment = GridData.FILL;
		textGridData.verticalAlignment = GridData.BEGINNING;
		text.setLayoutData(textGridData);

		Collections.sort(input, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof Brief) {
					return ((PersistentObject) o1).getLabel().substring(11)
							.compareToIgnoreCase(((PersistentObject) o2).getLabel().substring(11));
				}
				if (o1 instanceof PersistentObject) {
					return ((PersistentObject) o1).getLabel().compareToIgnoreCase(((PersistentObject) o2).getLabel());
				}
				return 0;
			}
		});

		if (style == SWT.SINGLE) {
			structuredViewer = new TableViewer(ret, SWT.NONE);
		} else {
			structuredViewer = CheckboxTableViewer.newCheckList(ret, SWT.NONE);
		}

		GridData viewerGridData = new GridData(GridData.FILL_BOTH);
		viewerGridData.heightHint = 250;
		viewerGridData.widthHint = 300;
		((TableViewer) structuredViewer).getTable().setLayoutData(viewerGridData);
		structuredViewer.setContentProvider(ArrayContentProvider.getInstance());

		structuredViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object elements) {
				if (elements instanceof PersistentObject) {
					return ((PersistentObject) elements).getLabel();
				} else if (elements != null) {
					return elements.toString();
				}
				return null;
			}
		});
		structuredViewer.setInput(input.toArray());

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				filter.setSearchText(text.getText());
				structuredViewer.refresh();
				isLastElement(structuredViewer);
			}
		});

		filter = new SearchDataDialog();
		structuredViewer.addFilter(filter);

		structuredViewer.addDoubleClickListener(event -> okPressed());

		return ret;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(shellTitle);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		if (style == SWT.SINGLE) {
			IStructuredSelection selection = structuredViewer.getStructuredSelection();
			this.selection = selection.toList();
		} else {
			this.selection = Arrays.asList(((CheckboxTableViewer) structuredViewer).getCheckedElements());
		}
		super.okPressed();
	}

	private void isLastElement(StructuredViewer structuredViewer) {
		if (((TableViewer) structuredViewer).getTable().getItems().length == 1) {
			((TableViewer) structuredViewer).getTable().getItem(0).setChecked(true);
			((TableViewer) structuredViewer).getTable().setSelection(0);
		} else {
			if (style != SWT.SINGLE) {
				((CheckboxTableViewer) structuredViewer).setAllChecked(false);
			}
			structuredViewer.setSelection(null);
		}
	}

	protected static String getLabel(Object object) {
		if (object instanceof PersistentObject) {
			return ((PersistentObject) object).getLabel();
		} else if (object != null) {
			return object.toString();
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public static class SearchDataDialog extends ViewerFilter {

		private String searchString;

		public void setSearchText(String search) {
			this.searchString = ".*" + search + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.isEmpty()) {
				return true;
			}

			if (element instanceof PersistentObject) {
				PersistentObject pObject = (PersistentObject) element;
				if (pObject.getLabel().toLowerCase().matches(searchString.toLowerCase())) {
					return true;
				}
			} else if (element != null) {
				if (element.toString().toLowerCase().matches(searchString.toLowerCase())) {
					return true;
				}
			}
			return false;
		}
	}
}
