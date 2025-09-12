/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.BlockSelector;
import ch.elexis.core.ui.views.codesystems.BlockTreeViewerItem;

public class AddElementToBlockDialog extends TitleAreaDialog {
	CommonViewer cv;
	ICodeElementBlock result;

	public AddElementToBlockDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		cv = new CommonViewer();
		BlockSelector bs = new BlockSelector();
		ViewerConfigurer vc = bs.createViewerConfigurer(cv);
		cv.create(vc, parent, SWT.NONE, this);
		return cv.getViewerWidget().getControl();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.AddElementToBlockDialog_blockSelection); // $NON-NLS-1$
		setMessage(Messages.AddElementToBlockDialog_selectBlock); // $NON-NLS-1$
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		getShell().setText(Messages.AddElementToBlockDialog_block); // $NON-NLS-1$
	}

	public ICodeElementBlock getResult() {
		return result;
	}

	@Override
	protected void okPressed() {
		Object[] lb = cv.getSelection();
		if ((lb != null) && (lb.length > 0) && (lb[0] instanceof BlockTreeViewerItem)) {
			result = ((BlockTreeViewerItem) lb[0]).getBlock();
		}
		super.okPressed();
	}

}
