/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public abstract class PagingComposite extends Composite {
	private int currentPage;
	private volatile boolean isLazyLoadingBusy;
	private int maxPage;
	private int fetchSize;
	private static final int DEFAULT_PAGESTEP = 1;
	private ToolItem textToolItem;
	private int elementsCount;
	
	private GridData gd;
	
	public PagingComposite(Composite parent, int style){
		super(parent, SWT.BORDER);
		createContent();
	}
	
	private void createContent(){
		setLayout(SWTHelper.createGridLayout(true, 1));
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		setLayoutData(gd);
		
		Composite main = new Composite(this, SWT.NONE);
		main.setLayout(SWTHelper.createGridLayout(true, 1));
		main.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		ToolBar toolBar = new ToolBar(main, SWT.RIGHT | SWT.FLAT);
		ToolItem prevToolItem = new ToolItem(toolBar, SWT.PUSH);
		prevToolItem.setToolTipText("");
		prevToolItem.setImage(Images.IMG_PREVIOUS.getImage());
		prevToolItem.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				mouseClicked(DEFAULT_PAGESTEP * -1);
			}
		});
		
		textToolItem = new ToolItem(toolBar, SWT.PUSH | SWT.CENTER);
		textToolItem.setToolTipText("");
		textToolItem.setText("");
		
		ToolItem nextToolItem = new ToolItem(toolBar, SWT.PUSH);
		nextToolItem.setToolTipText("");
		nextToolItem.setImage(Images.IMG_NEXT.getImage());
		nextToolItem.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				mouseClicked(DEFAULT_PAGESTEP);
			}
		});
		setVisible(false);
		gd.exclude = true;
	}
	
	public void reset(){
		setup(0, 0, 0);
	}
	
	public void setup(int currentPage, int elementsCount, int fetchSize){
		isLazyLoadingBusy = false;
		this.elementsCount = elementsCount;
		if (currentPage > 0 && elementsCount > fetchSize) {
			// activate pagination
			this.currentPage = currentPage;
			this.fetchSize = fetchSize;
			this.maxPage =
				fetchSize > 0 ? ((int) Math.ceil((double) elementsCount / fetchSize)) : 0;
		} else {
			//deactivate pagination
			this.currentPage = 0;
			this.fetchSize = 0;
			this.maxPage = 0;
		}
		
		UiDesk.getDisplay().asyncExec(new Runnable() {
			public void run(){
				refresh();
			}
		});
	}
	
	private void refresh(){
		if (!isDisposed()) {
			setVisible(currentPage > 0);
			if (textToolItem != null) {
				textToolItem.setText(currentPage + "/" + maxPage);
				textToolItem.setToolTipText("Gesamtanzahl: " + elementsCount);
			}
			
			gd.exclude = !isVisible();
			getParent().layout(true, true);
		}
	}
	
	public int getCurrentPage(){
		return currentPage;
	}
	
	/**
	 * Use this for a callback after Paging
	 * 
	 */
	public abstract void runPaging();
		
	
	private boolean doPaging(int newPage){
		if (!isLazyLoadingBusy) {
			if (newPage > 0 && newPage <= maxPage) {
				isLazyLoadingBusy = true;
				currentPage = newPage;
				return true;
			}
		}
		return false;
	}
	
	public int getFetchSize(){
		return fetchSize;
	}
	
	public void mouseClicked(int pageStep){
		UiDesk.getDisplay().asyncExec(new Runnable() {
			public void run(){
				if (doPaging(currentPage + pageStep)) {
					runPaging();
					refresh();
					isLazyLoadingBusy = false;
				}
			}
		});
	}
}
