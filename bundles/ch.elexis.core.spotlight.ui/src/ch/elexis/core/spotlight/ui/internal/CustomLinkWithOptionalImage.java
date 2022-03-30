package ch.elexis.core.spotlight.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/**
 * To represent a selectable read-only widget that contains an element. On
 * selection it shows the selection state and propagates the selection to the
 * spotlight shell.
 *
 */
public class CustomLinkWithOptionalImage extends Composite {

	private Label imageLabel;
	private Link link;

	public CustomLinkWithOptionalImage(Composite parent, int style, Image image) {
		super(parent, style);

		int numColumns = (image != null) ? 2 : 1;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		if (image != null) {
			imageLabel = new Label(this, SWT.NO_FOCUS);
			imageLabel.setEnabled(false);
			imageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			imageLabel.setImage(image);
		}

		link = new Link(this, style);
		link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		link.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.keyCode >= 97 && e.keyCode <= 122) || (e.keyCode >= 48 && e.keyCode <= 57)) {
					((SpotlightShell) getShell()).setFocusAppendChar(e.character);
					return;
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					link.traverse(SWT.TRAVERSE_TAB_NEXT);
					return;
				} else if (e.keyCode == SWT.ARROW_UP) {
					link.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
					return;
				}
				super.keyPressed(e);
			}
		});

		link.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				link.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
				if (imageLabel != null) {
					imageLabel.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
				}
				link.setForeground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
				((SpotlightShell) parent.getShell()).setSelectedElement(getData());
			}

			@Override
			public void focusLost(FocusEvent e) {
				link.setBackground(parent.getBackground());
				if (imageLabel != null) {
					imageLabel.setBackground(parent.getBackground());
				}
				link.setForeground(parent.getForeground());
				((SpotlightShell) parent.getShell()).setSelectedElement(null);
			}
		});

		setTabList(new Control[]{link});
	}

	@Override
	protected void checkSubclass() {
	}

	public Link getLink() {
		return link;
	}

}
