/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.equo.chromium.swt.Browser;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class DisplayTextDialog extends TitleAreaDialog {
	String t, m, cnt;
	Boolean hS = true;
	Font f = null;

	public DisplayTextDialog(Shell parentShell, String title, String message, String content) {
		super(parentShell);
		t = title;
		m = message;
		cnt = content;
	}

	public void setWhitespaceNormalized(Boolean hideSpaces) {
		hS = hideSpaces;
	}

	public void setFont(Font font) {
		f = font;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		ScrolledForm form = UiDesk.getToolkit().createScrolledForm(parent);
		form.getBody().setLayout(new ColumnLayout());
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Control ret = null;
		if (cnt.startsWith("<html>")) { //$NON-NLS-1$
			ret = new Browser(form.getBody(), SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			((Browser) ret).setText(cnt);
			if (cnt.length() > 300) {
				getShell().setSize(800, 600);
			}

		} else {
			cnt = cnt.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			cnt = cnt.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			cnt = cnt.replaceAll(StringUtils.LF, "<br />"); //$NON-NLS-1$
			cnt = cnt.replaceAll("\\*\\.(.{1,30})\\.\\*", "<b>$1</b>"); //$NON-NLS-1$ //$NON-NLS-2$
			cnt = cnt.replaceAll("\\\\\\.br\\\\", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
			cnt = cnt.replaceAll("\\\\\\.BR\\\\", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
			cnt = cnt.replaceAll("\\n\\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$

			ret = UiDesk.getToolkit().createFormText(form.getBody(), false);
			((FormText) ret).setWhitespaceNormalized(hS);
			if (f != null)
				try {
					((FormText) ret).setFont(f);
				} catch (Exception ex) {
					// Do nothing -> Use System Default font
				} finally {
					// Do nothing
				}
			// ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			((FormText) ret).setText("<form><p>" + cnt + "</p></form>", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		}
		SWTHelper.center(UiDesk.getTopShell(), getShell());
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(t);
		IPatient patient = ContextServiceHolder.get().getActivePatient().orElse(null);
		setTitle((patient != null) ? patient.getLabel() : "missing patient name"); //$NON-NLS-1$
		setMessage(m);
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		Rectangle screen = UiDesk.getDisplay().getBounds();
		int w = screen.width - screen.width / 4;
		int h = screen.width - screen.width / 4;
		getShell().setBounds(0, 0, w, h);
		SWTHelper.center(getShell());
	}

}
