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

import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.AutoForm;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.artikel.Artikeldetail;
import ch.elexis.data.Artikel;

public class ArtikelDetailDialog extends TitleAreaDialog {
	protected IArticle article;
	
	public ArtikelDetailDialog(Shell shell, IPersistentObject o){
		super(shell);
		Optional<Identifiable> identifiable =
			StoreToStringServiceHolder.get().loadFromString(o.storeToString());
		if (identifiable.isPresent() && identifiable.get() instanceof IArticle) {
			article = (IArticle) identifiable.get();
		} else {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Fehler",
				"Der Artikel [" + o.getLabel() + "] konnte nicht geladen werden.");
			throw new IllegalStateException(
				"Could not load identifiable for article [" + o.getLabel() + "]");
		}
	}
	
	public ArtikelDetailDialog(Shell shell, IArticle article){
		super(shell);
		this.article = article;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		ScrolledComposite ret = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite cnt = new Composite(ret, SWT.NONE);
		ret.setContent(cnt);
		ret.setExpandHorizontal(true);
		ret.setExpandVertical(true);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cnt.setLayout(new FillLayout());
		AutoForm tblArtikel = null;
		if (article instanceof Identifiable) {
			tblArtikel = new LabeledInputField.AutoForm(cnt,
				Artikeldetail.getModelFieldDefs(parent.getShell()));
		} else {
			tblArtikel =
				new LabeledInputField.AutoForm(cnt, Artikeldetail.getFieldDefs(parent.getShell()));
		}
		tblArtikel.reload(article);
		ret.setMinSize(cnt.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return ret;
	}
	
	@Override
	protected Point getInitialSize(){
		Point orig = super.getInitialSize();
		orig.y += orig.y >> 2;
		return orig;
	}
	
	@Override
	public void create(){
		setShellStyle(getShellStyle() | SWT.RESIZE);
		super.create();
		getShell().setText(Messages.ArtikelDetailDialog_articleDetail); //$NON-NLS-1$
		setTitle(article.getLabel());
		setMessage(Messages.ArtikelDetailDialog_enterArticleDetails); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		ElexisEventDispatcher.reload(Artikel.class);
		super.okPressed();
	}
	
}