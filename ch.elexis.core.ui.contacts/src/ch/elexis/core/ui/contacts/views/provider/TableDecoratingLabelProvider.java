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
package ch.elexis.core.ui.contacts.views.provider;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.model.IContact;

/**
 * @author Annamalai Chockalingam
 * 
 */
public class TableDecoratingLabelProvider extends DecoratingLabelProvider implements
		ITableLabelProvider {
	
	ITableLabelProvider provider;
	ILabelDecorator decorator;
	
	/**
	 * @param provider
	 * @param decorator
	 */
	public TableDecoratingLabelProvider(ILabelProvider provider, ILabelDecorator decorator){
		super(provider, decorator);
		this.provider = (ITableLabelProvider) provider;
		this.decorator = decorator;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex){
		Image image = provider.getColumnImage(element, columnIndex);
		if (decorator != null) {
			Image decorated = decorator.decorateImage(image, element);
			if (decorated != null) {
				return decorated;
			}
		}
		return image;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex){
		String text = provider.getColumnText(element, columnIndex);
		if (decorator != null) {
			String decorated = decorator.decorateText(text, element);
			if (decorated != null) {
				return decorated;
			}
		}
		return text;
	}
	
	@Override
	public Color getForeground(Object element){
		IContact c = (IContact) element;
		if(c.isDeleted()) return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		return null;
	}
	
}
