package ch.elexis.core.ui.e4.dialog;

import org.eclipse.jface.dialogs.DialogTray;

public abstract class DialogTrayWithSelectionListener extends DialogTray {

	public abstract void selectionChanged(Object selection);

}
