/*******************************************************************************
 * Copyright (c) 2008-2011, G. Weirich and Elexis
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

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Sticker;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.data.UiSticker;
import ch.elexis.core.ui.util.SWTHelper;

public class AssignStickerDialog extends TitleAreaDialog {
	PersistentObject mine;
	Table table;
	List<Sticker> alleEtiketten;
	List<ISticker> mineEtiketten;

	public AssignStickerDialog(Shell shell, PersistentObject obj) {
		super(shell);
		mine = obj;
		mineEtiketten = mine.getStickers();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		Label lbl = new Label(ret, SWT.WRAP);
		lbl.setText(Messages.AssignStickerDialog_PleaseConfirm); //$NON-NLS-1$
		table = new Table(ret, SWT.CHECK | SWT.SINGLE);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		// Query<Sticker> qbe = new Query<Sticker>(Sticker.class);
		// alleEtiketten = qbe.execute();
		alleEtiketten = Sticker.getStickersForClass(mine.getClass());
		for (Sticker et : alleEtiketten) {
			TableItem it = new TableItem(table, SWT.NONE);
			if (mineEtiketten.contains(et)) {
				it.setChecked(true);
			}
			it.setText(et.getLabel() + "(" + et.getWert() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			
			UiSticker uiet = new UiSticker(et);
			it.setImage(uiet.getImage());
			it.setForeground(uiet.getForeground());
			it.setBackground(uiet.getBackground());

			it.setData(et);
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Sticker"); //$NON-NLS-1$
		setMessage(MessageFormat
				.format(Messages.AssignStickerDialog_enterStickers, mine.getLabel())); //$NON-NLS-1$
		getShell().setText("Elexis Sticker"); //$NON-NLS-1$
	}

	@Override
	protected void okPressed() {

		for (TableItem it : table.getItems()) {
			Sticker et = (Sticker) it.getData();
			if (it.getChecked()) {
				if (!mineEtiketten.contains(et)) {
					mine.addSticker(et);
				}
			} else {
				if (mineEtiketten.contains(et)) {
					mine.removeSticker(et);
				}
			}
		}
		super.okPressed();
	}

}
