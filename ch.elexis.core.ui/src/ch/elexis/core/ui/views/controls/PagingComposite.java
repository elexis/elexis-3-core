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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public abstract class PagingComposite extends Composite implements MouseListener {
	
	private Label pageInfo;
	private int currentPage;
	private volatile boolean isLazyLoadingBusy;
	private int maxPage;
	private int fetchSize;
	
	public PagingComposite(Composite parent, int style){
		super(parent, style);
		createContent();
	}
	
	private void createContent(){
		setLayout(SWTHelper.createGridLayout(true, 1));
		
		Composite pageControl = new Composite(this, SWT.NONE);
		pageControl.setLayout(SWTHelper.createGridLayout(true, 3));
		
		Label buttonPrev = new Label(pageControl, SWT.CURSOR_HAND | SWT.RIGHT);
		buttonPrev.setImage(Images.IMG_PREVIOUS.getImage());
		buttonPrev.setData("pageStep", -1);
		buttonPrev.addMouseListener(this);
		
		pageInfo = new Label(pageControl, SWT.NONE);
		pageInfo.setText("");
		
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, true, false);
		gd.horizontalIndent = 5;
		pageInfo.setLayoutData(gd);
		
		Label buttonNext = new Label(pageControl, SWT.CURSOR_HAND | SWT.LEFT);
		GridData gd2 = new GridData(SWT.LEFT, SWT.TOP, true, false);
		gd2.horizontalIndent = 5;
		buttonNext.setImage(Images.IMG_NEXT.getImage());
		buttonNext.addMouseListener(this);
		buttonNext.setData("pageStep", 1);
		setVisible(false);
	}
	
	public void reset(){
		setup(0, 0, 0);
	}
	
	public void setup(int currentPage, int elementsCount, int fetchSize){
		isLazyLoadingBusy = false;
		
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
		refresh();
	}
	
	private void refresh(){
		setVisible(currentPage > 0);
		pageInfo.setText(currentPage + "/" + maxPage);
		
		layout(true, true);
	}
	
	public int getCurrentPage(){
		return currentPage;
	}
	
	/**
	 * Use this for a callback after Paging
	 * 
	 * @param isNext
	 */
	public abstract void run(boolean isNext);
		
	
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
	
	@Override
	public void mouseDoubleClick(MouseEvent e){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseDown(MouseEvent e){
		Object source = e.getSource();
		if (source instanceof Label) {
			Object pageStep = ((Label) source).getData("pageStep");
			if (pageStep instanceof Integer) {
				if (doPaging(currentPage + (1 * ((int) pageStep)))) {
					run(false);
					refresh();
					isLazyLoadingBusy = false;
				}
			}
		}
	}
	
	@Override
	public void mouseUp(MouseEvent e){
		// TODO Auto-generated method stub
		
	}
}
