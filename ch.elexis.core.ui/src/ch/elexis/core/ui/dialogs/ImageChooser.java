/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.AbstractElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.core.data.DBImage;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.UiDBImage;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class ImageChooser extends AbstractElementListSelectionDialog {
	
	private Object[] fElements;
	private Hyperlink hl;
	private Text tTitle;
	private static String NOFILESELECTED = Messages.ImageChooser_PleaseChooseFile; //$NON-NLS-1$
	private Button bDB, bFile;
	private UiDBImage result;
	
	public UiDBImage getSelection(){
		return result;
	}
	
	public ImageChooser(Shell shell){
		super(shell, new LabelProvider() {
			@Override
			public Image getImage(Object element){
				if (element instanceof UiDBImage) {
					return ((UiDBImage) element).getImage();
				}
				return null;
			}
			
			@Override
			public String getText(Object element){
				if (element instanceof DBImage) {
					return ((DBImage) element).getName();
				}
				return "?"; //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Sets the elements of the list.
	 * 
	 * @param elements
	 *            the elements of the list.
	 */
	public void setElements(Object[] elements){
		fElements = elements;
	}
	
	/*
	 * @see SelectionStatusDialog#computeResult()
	 */
	protected void computeResult(){
		if (bDB.getSelection()) {
			setResult(Arrays.asList(getSelectedElements()));
			Object[] sel = getResult();
			if (sel != null && sel.length > 0) {
				result = new UiDBImage((DBImage) sel[0]);
			} else {
				result = null;
			}
		}
	}
	
	private Menu createMenu(Control parent){
		Menu ret = new Menu(parent);
		MenuItem item = new MenuItem(ret, SWT.NONE);
		item.setText(Messages.ImageChooser_delete); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Object[] oo = getSelectedElements();
				if (oo != null && oo.length > 0) {
					if (SWTHelper.askYesNo(Messages.ImageChooser_reallyDeleteHeading, //$NON-NLS-1$
						Messages.ImageChooser_reallyDeleteText)) { //$NON-NLS-1$
						for (Object o : oo) {
							((DBImage) o).delete();
						}
					}
				}
			}
		});
		return ret;
	}
	
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		bDB = new Button(ret, SWT.RADIO);
		bDB.setText(Messages.ImageChooser_chooseImagefromDB); //$NON-NLS-1$
		createMessageArea(ret);
		createFilterText(ret);
		FilteredList list = createFilteredList(ret);
		list.setMenu(createMenu(list));
		list.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				bFile.setSelection(false);
				bDB.setSelection(true);
			}
			
		});
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(1,
			true, 1, false));
		bFile = new Button(ret, SWT.RADIO);
		bFile.setText(Messages.ImageChooser_importImage); //$NON-NLS-1$
		Composite cBottom = new Composite(ret, SWT.BORDER);
		cBottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		cBottom.setLayout(new GridLayout(2, false));
		new Label(cBottom, SWT.NONE).setText(Messages.ImageChooser_imageFile); //$NON-NLS-1$
		new Label(cBottom, SWT.NONE).setText(Messages.ImageChooser_imageTitle); //$NON-NLS-1$
		hl = new Hyperlink(cBottom, SWT.NONE);
		tTitle = new Text(cBottom, SWT.BORDER);
		hl.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				bFile.setSelection(true);
				bDB.setSelection(false);
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setFilterExtensions(new String[] {
					"*.png", "*.gif", //$NON-NLS-1$ //$NON-NLS-2$
					"*.jpg", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterNames(new String[] {
					"Portable Network Graphics", //$NON-NLS-1$
					"Grafics Interchange Format", "JPEG", Messages.ImageChooser_allFilesDesc}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String filename = fd.open();
				if (filename != null) {
					hl.setText(filename);
					getOkButton().setEnabled(true);
				}
			}
			
		});
		hl.setText(NOFILESELECTED);
		hl.setForeground(UiDesk.getColor(UiDesk.COL_BLUE));
		hl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tTitle.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tTitle.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e){
				bFile.setSelection(true);
				bDB.setSelection(false);
			}
			
		});
		bDB.setSelection(true);
		
		Query<DBImage> qbe = new Query<DBImage>(DBImage.class);
		List<DBImage> imgs = qbe.execute();
		if (imgs != null) {
			fElements = imgs.toArray();
		} else {
			fElements = new Object[0];
		}
		setListElements(fElements);
		setSelection(getInitialElementSelections().toArray());
		return ret;
	}
	
	@Override
	public void okPressed(){
		if (bFile.getSelection()) {
			String fname = hl.getText();
			if (!fname.equals(NOFILESELECTED)) {
				try {
					File file = new File(fname);
					result = new UiDBImage("ch.elexis.images", tTitle.getText() + ":" //$NON-NLS-1$
						+ file.getName(), new FileInputStream(file));
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		}
		super.okPressed();
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(Messages.ImageChooser_choseFileFromDBHeading); //$NON-NLS-1$
		setMessage(Messages.ImageChooser_choseFileFromDBText); //$NON-NLS-1$
		setTitle(Messages.ImageChooser_imageSelection); //$NON-NLS-1$
	}
	
}
