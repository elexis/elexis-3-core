/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.dynamic;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.model.IContact;

public class KontaktStickerSwitcher extends ContributionItem {
	
	private IContact k;
	
	public KontaktStickerSwitcher(){}
	
	public KontaktStickerSwitcher(String id){
		super(id);
	}
	
	@Override
	public void fill(Menu menu, int index){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getActivePage().getSelection();
		IStructuredSelection strucSelection = (IStructuredSelection) selection;
		k = (IContact) strucSelection.getFirstElement();
		
//		if (k != null) {
//			List<ISticker> stickersForClass =
//				IStickerFactory.eINSTANCE.findStickersForClass(k.getClass());
//			List<ISticker> definedStickers = IStickerFactory.eINSTANCE.findStickersForObject(k);
//			
//			for (ISticker st : stickersForClass) {
//				MenuItem item = new MenuItem(menu, SWT.CHECK, index);
//				item.setText(st.getName());
//				item.setData("ID", st.getId());
//				for (ISticker dst : definedStickers) {
//					if (dst.getId().equalsIgnoreCase(st.getId()))
//						item.setSelection(true);
//				}
//				item.addSelectionListener(new SelectionAdapter() {
//					@Override
//					public void widgetSelected(SelectionEvent e){
//						MenuItem i = (MenuItem) e.widget;
//						ISticker is = IStickerFactory.eINSTANCE.findById((String) i.getData("ID"));
//						if (i.getSelection()) {
//							// sticker is not set -> set
//							IStickerFactory.eINSTANCE.setStickerOnObject(is, k);
//						} else {
//							// sticker is set -> remove
//							IStickerFactory.eINSTANCE.removeStickerFromObject(is, k);
//						}
//					}
//				});
//			}
//		}
		
	}
	
	@Override
	public boolean isDynamic(){
		return true;
	}
}
