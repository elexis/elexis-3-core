/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.eigenleistung;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.ui.dialogs.EigenLeistungDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.selectors.FieldDescriptor;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SelectorPanelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.core.ui.views.codesystems.Messages;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;

public class EigenleistungCodeSelectorFactory extends CodeSelectorFactory {
	IAction createAction;
	static SelectorPanelProvider slp;
	CommonViewer cv;
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		this.cv = cv;
		
		new EigenleistungContextMenu(
			(Eigenleistung) new PersistentObjectFactory().createTemplate(Eigenleistung.class), cv,
			null);
		
		makeActions();
		
		FieldDescriptor<?>[] lbName = new FieldDescriptor<?>[] {
			new FieldDescriptor<Eigenleistung>(Eigenleistung.CODE)
		};
		
		slp = new SelectorPanelProvider(lbName, true);
		slp.addActions(createAction);
		return new ViewerConfigurer(
			// new LazyContentProvider(cv,dataloader,null),
			new EigenleistungLoader(cv), new DefaultLabelProvider(), slp,
			new ViewerConfigurer.DefaultButtonProvider(),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null));
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenleistung.class;
	}
	
	@Override
	public void dispose(){}
	
	private void makeActions(){
		createAction = new Action("neu erstellen") {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.BlockDetailDisplay_addSelfDefinedServices);
			}
			
			@Override
			public void run(){
				Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				EigenLeistungDialog dialog = new EigenLeistungDialog(parent, null);
				dialog.open();
				cv.notify(CommonViewer.Message.update);
			}
		};
	}
	
	@Override
	public String getCodeSystemName(){
		return Eigenleistung.CODESYSTEM_NAME;
	}
	
}
