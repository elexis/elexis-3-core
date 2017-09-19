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
package ch.elexis.core.ui.contacts.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.types.LocalizeUtil;
import ch.elexis.core.types.RelationshipType;
import ch.elexis.core.ui.contacts.views.Patientenblatt2;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.BezugsKontaktRelation;
import ch.rgw.tools.StringTool;

public class BezugsKontaktAuswahl extends Dialog {
	Combo cbBezugSrc;
	Combo cbTypeSrc;
	Combo cbTypeDest;
	Map<String, BezugsKontaktRelation> mapBezugKonktatRelation = new HashMap<>();
	BezugsKontaktRelation selectedBezugKontaktRelation = new BezugsKontaktRelation();
	
	String srcLabel;
	String destLabel;
	
	Composite dynComposite;
	
	BezugsKontakt bezugsKontakt;
	
	private boolean locked = false;
	
	public BezugsKontaktAuswahl(String destLabel, String srcLabel){
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.srcLabel = srcLabel;
		this.destLabel = destLabel;
	}
	
	public BezugsKontaktAuswahl(String destLabel, String srcLabel, BezugsKontakt bezugsKontakt,
		boolean locked){
		this(destLabel, srcLabel);
		this.bezugsKontakt = bezugsKontakt;
		this.locked = locked;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(Messages.Patientenblatt2_kindOfRelation); //$NON-NLS-1$
		if (locked) {
			Button btnOk = getButton(IDialogConstants.OK_ID);
			if (btnOk != null) {
				btnOk.setEnabled(false);
			}
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		new Label(ret, SWT.NONE)
			.setText(Messages.Patientenblatt2_pleaseEnterKindOfRelationship); //$NON-NLS-1$
		
		new Label(ret, SWT.NONE).setText(srcLabel + " " + Messages.Bezugskontakt_Is); //$NON-NLS-1$
		cbBezugSrc = new Combo(ret, SWT.NONE);
		cbBezugSrc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		String bez = CoreHub.globalCfg.get(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN, ""); //$NON-NLS-1$
		
		String[] items = getBezugKonkaktTypes(bez);
		
		cbBezugSrc.setItems(items);
		cbBezugSrc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				reCalc(true);
			}
		});
		
		cbBezugSrc.setEnabled(!locked);
		if (bezugsKontakt != null && bezugsKontakt.exists()) {
			cbBezugSrc.setText(bezugsKontakt.getBezug());
			mapBezugKonktatRelation.put(bezugsKontakt.getBezug(),
				new BezugsKontaktRelation(bezugsKontakt));
		}
		
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		
		Button btn = new Button(ret, SWT.TOGGLE);
		btn.setText("+ " + Messages.BezugsKonktat_FormalerReference); //$NON-NLS-1$
		btn.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true, 1, 1));
		btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				data.exclude = !data.exclude;
				dynComposite.setVisible(!data.exclude);
				
				dynComposite.getShell().pack();
				if (dynComposite.isVisible()) {
					btn.setText("- " + Messages.BezugsKonktat_FormalerReference);
					reCalc(true);
				}
				else {
					btn.setText("+ " + Messages.BezugsKonktat_FormalerReference);
				}
			}
		});
		
		dynComposite = new Composite(ret, SWT.BORDER);
		dynComposite.setLayout(new GridLayout());
		dynComposite.setLayoutData(data);
		dynComposite.setVisible(false);
		data.exclude = true;

		String[] bezugKontaktTypes = getBezugKontaktTypes();
		
		new Label(dynComposite, SWT.NONE).setText(Messages.Bezugskontakt_RelationFrom + " "); //$NON-NLS-1$
		cbTypeDest = new Combo(dynComposite, SWT.READ_ONLY);
		cbTypeDest.setEnabled(!locked);
		cbTypeDest.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbTypeDest.setItems(bezugKontaktTypes);
		
		new Label(dynComposite, SWT.NONE).setText(Messages.Bezugskontakt_RelationTo + " "); //$NON-NLS-1$
		cbTypeSrc = new Combo(dynComposite, SWT.READ_ONLY);
		cbTypeSrc.setEnabled(!locked);
		cbTypeSrc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbTypeSrc.setItems(bezugKontaktTypes);
		reCalc(true);
		return ret;
	}
	
	public static String[] getBezugKontaktTypes(){
		RelationshipType[] allRelationshipTypes = RelationshipType.values();
		String[] displayValues = new String[allRelationshipTypes.length];
		int idx = 0;
		for (RelationshipType relationshipType : allRelationshipTypes) {
			displayValues[idx++] = LocalizeUtil.getLocaleText(relationshipType);
		}
		return displayValues;
	}
	
	private String[] getBezugKonkaktTypes(String cfgBezugKonktaks){
		for (String cfgPart : cfgBezugKonktaks.split(Patientenblatt2.SPLITTER)) {
			BezugsKontaktRelation bezugsKontaktRelation = new BezugsKontaktRelation();
			bezugsKontaktRelation.loadValuesByCfg(cfgPart);
			mapBezugKonktatRelation.put(bezugsKontaktRelation.getName(), bezugsKontaktRelation);
		}
		
		List<String> keys = new ArrayList<>(mapBezugKonktatRelation.keySet());
		Collections.sort(keys);
		return keys.toArray(new String[0]);
	}
	
	public void reCalc(boolean autoSelect){
		BezugsKontaktRelation bezugsKontaktType = mapBezugKonktatRelation.get(cbBezugSrc.getText());
		selectedBezugKontaktRelation = bezugsKontaktType;
		if (selectedBezugKontaktRelation == null) {
			selectedBezugKontaktRelation = new BezugsKontaktRelation(cbBezugSrc.getText(),
				RelationshipType.AGENERIC, RelationshipType.AGENERIC);
		}
		
		if (selectedBezugKontaktRelation.getDestRelationType() == null) {
			selectedBezugKontaktRelation.setDestRelationType(RelationshipType.AGENERIC);
		}
		
		if (selectedBezugKontaktRelation.getSrcRelationType() == null) {
			selectedBezugKontaktRelation.setSrcRelationType(RelationshipType.AGENERIC);
		}
		if (autoSelect) {
			cbTypeSrc.setText(LocalizeUtil.getLocaleText(selectedBezugKontaktRelation.getSrcRelationType()));
			cbTypeDest.setText(
				LocalizeUtil.getLocaleText(selectedBezugKontaktRelation.getDestRelationType()));
		}
	}
	
	@Override
	protected void okPressed(){
		reCalc(false);
		int selIdx = cbTypeDest.getSelectionIndex();
		if (selIdx != -1)
		{
			selectedBezugKontaktRelation.setDestRelationType(RelationshipType.values()[selIdx]);
		}
		selIdx = cbTypeSrc.getSelectionIndex();
		if (selIdx != -1) {
			selectedBezugKontaktRelation.setSrcRelationType(RelationshipType.values()[selIdx]);
		}
		
		String[] items = cbBezugSrc.getItems();
		String nitem = selectedBezugKontaktRelation.getName();
		if (StringTool.getIndex(items, nitem) == -1) {
			String res = CoreHub.globalCfg.get(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN, "")
				+ Patientenblatt2.SPLITTER + selectedBezugKontaktRelation.getCfgString();
			CoreHub.globalCfg.set(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN, res);
		}
		
		super.okPressed();
	}
	public BezugsKontaktRelation getBezugKonkaktRelation(){
		return selectedBezugKontaktRelation;
	}
}
