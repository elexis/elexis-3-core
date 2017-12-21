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

package ch.elexis.core.ui.views;

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_INTAKE_LIST;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_PRESCRIPTION;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IOutputter;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.text.EditLocalDocumentUtil;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.ITextPlugin.Parameter;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.OutputLog;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rezept;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class RezeptBlatt extends ViewPart implements ICallback, IActivationListener, IOutputter {
	public final static String ID = "ch.elexis.RezeptBlatt"; //$NON-NLS-1$
	TextContainer text;
	Brief actBrief;
	
	public RezeptBlatt(){
	}
	
	@Override
	public void dispose(){
		if (actBrief != null) {
			CoreHub.getLocalLockService().releaseLock(actBrief);
		}
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	private void updateTextLock(){
		if (actBrief != null) {
			// test lock and set read only before opening the Brief
			LockResponse result = CoreHub.getLocalLockService().acquireLock(actBrief);
			if (result.isOk()) {
				text.getPlugin().setParameter(null);
			} else {
				LockResponseHelper.showInfo(result, actBrief, null);
				text.getPlugin().setParameter(Parameter.READ_ONLY);
			}
		}
	}
	
	/**
	 * load a Rezept from the database
	 * 
	 * @param brief
	 *            the Brief for the Rezept to be shown
	 */
	public void loadRezeptFromDatabase(Rezept rp, Brief brief){
		if (actBrief != null) {
			CoreHub.getLocalLockService().releaseLock(actBrief);
		}
		actBrief = brief;
		updateTextLock();
		text.open(brief);
		rp.setBrief(actBrief);
		EditLocalDocumentUtil.startEditLocalDocument(this, brief);
	}
	
	@Override
	public void createPartControl(Composite parent){
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	@Override
	public void setFocus(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	/**
	 * Create a recipe document, with a list of prescriptions from Rezept parameter.
	 * 
	 * @param rp
	 * @param template
	 * @param replace
	 * @return
	 */
	public boolean createList(Rezept rp, String template, String replace){
		if (actBrief != null) {
			CoreHub.getLocalLockService().releaseLock(actBrief);
		}
		actBrief =
			text.createFromTemplateName(Konsultation.getAktuelleKons(), template, Brief.RP,
				(Patient) ElexisEventDispatcher.getSelected(Patient.class),
				template + " " + rp.getDate());
		updateTextLock();
		List<Prescription> lines = rp.getLines();
		String[][] fields = new String[lines.size()][];
		if (replace.equals(Messages.RezeptBlatt_4)) {
			fields = createRezeptListFields(lines);
		} else {
			fields = createTakingListFields(lines);
		}
		int[] wt = new int[] {
			10, 70, 20
		};
		rp.setBrief(actBrief);
		if (text.getPlugin().insertTable(replace, 0, fields, wt)) {
			if (text.getPlugin().isDirectOutput()) {
				text.getPlugin().print(null, null, true);
				getSite().getPage().hideView(this);
			}
			text.saveBrief(actBrief, Brief.RP);
			EditLocalDocumentUtil.startEditLocalDocument(this, actBrief);
			return true;
		}
		text.saveBrief(actBrief, Brief.RP);
		return false;
	}
	
	/**
	 * Create a document with a list of prescriptions, not a recipe.
	 * 
	 * @param prescriptions
	 * @param template
	 * @param replace
	 * @return
	 */
	public boolean createList(Prescription[] prescriptions, String template, String replace){
		if (actBrief != null) {
			CoreHub.getLocalLockService().releaseLock(actBrief);
		}
		TimeTool now = new TimeTool();
		actBrief = text.createFromTemplateName(Konsultation.getAktuelleKons(), template,
			Brief.UNKNOWN, (Patient) ElexisEventDispatcher.getSelected(Patient.class),
			template + " " + now.toString(TimeTool.DATE_GER));
		updateTextLock();
		List<Prescription> lines = Arrays.asList(prescriptions);
		String[][] fields = new String[lines.size()][];
		if (replace.equals(Messages.RezeptBlatt_4)) {
			fields = createRezeptListFields(lines);
		} else {
			fields = createTakingListFields(lines);
		}
		int[] wt = new int[] {
			10, 70, 20
		};
		if (text.getPlugin().insertTable(replace, 0, fields, wt)) {
			if (text.getPlugin().isDirectOutput()) {
				text.getPlugin().print(null, null, true);
				getSite().getPage().hideView(this);
			}
			text.saveBrief(actBrief, Brief.UNKNOWN);
			EditLocalDocumentUtil.startEditLocalDocument(this, actBrief);
			return true;
		}
		text.saveBrief(actBrief, Brief.UNKNOWN);
		return false;
	}
	
	public String[][] createRezeptListFields(List<Prescription> lines){
		String[][] fields = new String[lines.size()][];
		
		for (int i = 0; i < fields.length; i++) {
			Prescription p = lines.get(i);
			fields[i] = new String[3];
			fields[i][0] = p.get(Messages.RezeptBlatt_number); //$NON-NLS-1$
			String bem = p.getBemerkung();
			if (StringTool.isNothing(bem)) {
				fields[i][1] = p.getSimpleLabel();
			} else {
				fields[i][1] = p.getSimpleLabel() + "\t\r" + bem; //$NON-NLS-1$
			}
			fields[i][2] = p.getDosis();
			
		}
		return fields;
	}
	
	public String[][] createTakingListFields(List<Prescription> lines){
		String[][] fields = new String[lines.size()][];
		
		for (int i = 0; i < fields.length; i++) {
			Prescription p = lines.get(i);
			fields[i] = new String[3];
			fields[i][0] = p.get(Messages.RezeptBlatt_number); //$NON-NLS-1$
			String bem = p.getBemerkung();
			String patInfo = p.getDisposalComment();
			if (StringTool.isNothing(bem)) {
				fields[i][1] = p.getSimpleLabel();
			} else {
				if (patInfo == null || patInfo.isEmpty()) {
					fields[i][1] = p.getSimpleLabel() + "\t\r" + bem; //$NON-NLS-1$
				} else {
					fields[i][1] = p.getSimpleLabel() + "\t\r" + bem + "\r" + patInfo; //$NON-NLS-1$
				}
			}
			fields[i][2] = p.getDosis();
		}
		return fields;
	}
	
	public boolean createRezept(Rezept rp){
		if (createList(rp, TT_PRESCRIPTION, Messages.RezeptBlatt_4)) { //$NON-NLS-1$ //$NON-NLS-2$
			new OutputLog(rp, this);
			return true;
		}
		return false;
	}
	
	public boolean createEinnahmeliste(Patient pat, Prescription[] pres){
		return createList(pres, TT_INTAKE_LIST, Messages.RezeptBlatt_6); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void save(){
		if (actBrief != null) {
			actBrief.save(text.getPlugin().storeToByteArray(), text.getPlugin().getMimeType());
		}
	}
	
	public boolean saveAs(){
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}
	
	public void activation(boolean mode){
		if (mode == false) {
			save();
		}
		
	}
	
	public void visible(boolean mode){
		
	}
	
	public String getOutputterDescription(){
		return "Druckerausgabe erstellt";
	}
	
	public String getOutputterID(){
		return "ch.elexis.RezeptBlatt";
	}
	
	public Image getSymbol(){
		return Images.IMG_PRINTER.getImage();
	}
}
