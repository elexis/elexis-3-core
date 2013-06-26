/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.preferences.inputs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.DecoratedString;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.Settings;

/**
 * Ein Preference-Element zum EInstellen eines DecoratedStrings (Text mit Farbe und Icon)
 * 
 * @author gerry
 * 
 */
public class DecoratedStringChooser extends Composite {
	
	public DecoratedStringChooser(Composite parent, final Settings cfg,
		final DecoratedString[] strings){
		super(parent, SWT.BORDER);
		
		int num = strings.length;
		int typRows = ((int) Math.sqrt(num));
		int typCols = typRows + (num - (typRows * typRows));
		if (typCols < 4) {
			typCols = 4;
		}
		setLayout(new GridLayout(typCols, true));
		Label expl = new Label(this, SWT.WRAP);
		expl.setText(Messages.getString("DecoratedStringChooser.howToChange")); //$NON-NLS-1$
		expl.setLayoutData(SWTHelper.getFillGridData(typCols, false, 1, false));
		for (int i = 0; i < num; i++) {
			Label lab = new Label(this, SWT.NONE);
			lab.setText(strings[i].getText());
			String coldesc = cfg.get(strings[i].getText(), "FFFFFF"); //$NON-NLS-1$
			Color background = UiDesk.getColorFromRGB(coldesc);
			lab.setBackground(background);
			GridData gd = new GridData(GridData.FILL_BOTH);
			lab.setLayoutData(gd);
			lab.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseDoubleClick(MouseEvent e){
					ColorDialog cd = new ColorDialog(getShell());
					Label l = (Label) e.getSource();
					RGB selected = cd.open();
					if (selected != null) {
						String symbolic = UiDesk.createColor(selected);
						l.setBackground(UiDesk.getColorFromRGB(symbolic));
						cfg.set(l.getText(), symbolic);
					}
				}
				
			});
		}
	}
}
