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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IOutputter;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.text.ITextPlugin.Parameter;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.text.EditLocalDocumentUtil;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
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

	private boolean addressSelection;

	public RezeptBlatt() {
		addressSelection = false;
	}

	@Override
	public void dispose() {
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
		}
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}

	private void updateTextLock() {
		if (actBrief != null) {
			// test lock and set read only before opening the Brief
			LockResponse result = LocalLockServiceHolder.get().acquireLock(actBrief);
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
	 * @param brief the Brief for the Rezept to be shown
	 */
	public void loadRezeptFromDatabase(Rezept rp, Brief brief) {
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
		}
		actBrief = brief;
		updateTextLock();
		text.open(brief);
		rp.setBrief(actBrief);
		EditLocalDocumentUtil.startEditLocalDocument(this, brief);
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	@Override
	public void setFocus() {
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
	public boolean createList(Rezept rp, String template, String replace) {
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
		}
		Kontakt adressat = addressSelection ? null : (Kontakt) ElexisEventDispatcher.getSelected(Patient.class);
		actBrief = text.createFromTemplateName(text.getAktuelleKons(), template, Brief.RP, adressat,
				template + StringUtils.SPACE + rp.getDate());
		updateTextLock();
		List<Prescription> lines = rp.getLines();
		String[][] fields = new String[lines.size()][];
		int[] wt = new int[] { 10, 70, 20 };
		if (replace.equals(Messages.RezeptBlatt_4)) {
			fields = createRezeptListFields(lines);
		}
		rp.setBrief(actBrief);
		if (insertTable(replace, fields, wt, Brief.RP)) {
			// save and open
		} else {
			replace = Messages.RezeptBlatt_4_Extended;
			fields = createExtendedTakingListFields(lines);
			wt = new int[] { 5, 45, 10, 10, 15, 15 };
			if (insertTable(replace, fields, wt, Brief.RP)) {
				// save and open
			}
		}
		text.saveBrief(actBrief, Brief.RP);
		EditLocalDocumentUtil.startEditLocalDocument(this, actBrief);
		return true;
	}

	private boolean insertTable(String replace, String[][] fields, int[] wt, String typ) {
		if (text.getPlugin().insertTable(replace, 0, fields, wt)) {
			if (text.getPlugin().isDirectOutput()) {
				text.getPlugin().print(null, null, true);
				getSite().getPage().hideView(this);
			}
			return true;
		}
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
	public boolean createList(Prescription[] prescriptions, String template, String replace) {
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
		}
		TimeTool now = new TimeTool();
		actBrief = text.createFromTemplateName(text.getAktuelleKons(), template, Brief.UNKNOWN,
				(Patient) ElexisEventDispatcher.getSelected(Patient.class),
				template + StringUtils.SPACE + now.toString(TimeTool.DATE_GER));
		updateTextLock();
		List<Prescription> lines = Arrays.asList(prescriptions);
		String[][] fields = new String[lines.size()][];
		int[] wt = new int[] { 10, 70, 20 };
		if (replace.equals(Messages.RezeptBlatt_4)) {
			fields = createRezeptListFields(lines);
		} else if (replace.equals(Messages.RezeptBlatt_6)) {
			fields = createTakingListFields(lines);
		}
		if (insertTable(replace, fields, wt, Brief.UNKNOWN)) {
			// save and open
		} else {
			if (replace.equals(Messages.RezeptBlatt_4)) {
				replace = Messages.RezeptBlatt_4_Extended;
				fields = createExtendedTakingListFields(lines);
				wt = new int[] { 5, 45, 10, 10, 15, 15 };
				if (insertTable(replace, fields, wt, Brief.RP)) {
					// save and open
				}
			} else if (replace.equals(Messages.RezeptBlatt_6)) {
				replace = Messages.RezeptBlatt_6_Extended;
				fields = createExtendedTakingListFields(lines);
				wt = new int[] { 5, 45, 10, 10, 15, 15 };
				if (insertTable(replace, fields, wt, Brief.UNKNOWN)) {
					// save and open
				}
			}
		}
		text.saveBrief(actBrief, Brief.UNKNOWN);
		EditLocalDocumentUtil.startEditLocalDocument(this, actBrief);
		return true;
	}

	public String[][] createRezeptListFields(List<Prescription> lines) {
		String[][] fields = new String[lines.size()][];

		for (int i = 0; i < fields.length; i++) {
			Prescription p = lines.get(i);
			fields[i] = new String[3];
			fields[i][0] = p.get(Messages.Core_Count); // $NON-NLS-1$
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

	public String[][] createTakingListFields(List<Prescription> lines) {
		String[][] fields = new String[lines.size()][];

		for (int i = 0; i < fields.length; i++) {
			Prescription p = lines.get(i);
			fields[i] = new String[3];
			fields[i][0] = p.get(Messages.Core_Count); // $NON-NLS-1$
			String bem = p.getBemerkung();
			String patInfo = p.getDisposalComment();
			if (StringTool.isNothing(bem)) {
				fields[i][1] = p.getSimpleLabel();
			} else {
				if (patInfo == null || patInfo.isEmpty()) {
					fields[i][1] = p.getSimpleLabel() + "\t\r" + bem; //$NON-NLS-1$
				} else {
					fields[i][1] = p.getSimpleLabel() + "\t\r" + bem + StringUtils.CR + patInfo; //$NON-NLS-1$
				}
			}
			fields[i][2] = p.getDosis();
		}
		return fields;
	}

	public String[][] createExtendedTakingListFields(List<Prescription> lines) {
		String[][] fields = new String[lines.size() + 1][];

		fields[0] = new String[6];
		fields[0][0] = StringUtils.EMPTY;
		fields[0][1] = "Medikament";
		fields[0][2] = "Einnahme";
		fields[0][3] = "Von bis und mit";
		fields[0][4] = "Anwendungsinstruktion";
		fields[0][5] = "Anwendungsgrund";

		for (int i = 1; i < fields.length; i++) {
			Prescription p = lines.get(i - 1);
			fields[i] = new String[6];
			if (p.getEntryType() != null && p.getEntryType() != EntryType.RECIPE
					&& p.getEntryType() != EntryType.UNKNOWN) {
				fields[i][0] = p.getEntryType().name().substring(0, 1);
			} else {
				fields[i][0] = StringUtils.EMPTY;
			}
			fields[i][1] = StringUtils.defaultString(p.getSimpleLabel());
			fields[i][2] = StringUtils.defaultString(p.getDosis());
			fields[i][3] = StringUtils.defaultString(p.getBeginDate());
			fields[i][4] = StringUtils.defaultString(p.getBemerkung());
			fields[i][5] = StringUtils.defaultString(p.getDisposalComment());
		}
		return fields;
	}

	public boolean createRezept(Rezept rp) {
		NoPoUtil.loadAsIdentifiable(rp, IRecipe.class).ifPresent(r -> ContextServiceHolder.get().setTyped(r));
		Optional<?> validationResult = ContextServiceHolder.get().getNamed("artikelstamm.selected.recipe.validate"); // $NON-NLS-1$
		if (validationResult.isEmpty() || Boolean.TRUE.equals(validationResult.get())) {
			boolean ret = createList(rp, TT_PRESCRIPTION, Messages.RezeptBlatt_4);
			if (ret) {
				new OutputLog(rp, this);
				return true;
			}
		}
		return false;
	}

	public boolean createEinnahmeliste(Patient pat, Prescription[] pres) {
		return createList(pres, TT_INTAKE_LIST, Messages.RezeptBlatt_6);
	}

	@Override
	public void save() {
		if (actBrief != null && text.getPlugin().storeToByteArray() != null) {
			actBrief.save(text.getPlugin().storeToByteArray(), text.getPlugin().getMimeType());
		}
	}

	@Override
	public boolean saveAs() {
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}

	@Override
	public void activation(boolean mode) {
		if (mode == false) {
			save();
		}

	}

	@Override
	public void visible(boolean mode) {

	}

	@Override
	public String getOutputterDescription() {
		return "Druckerausgabe erstellt";
	}

	@Override
	public String getOutputterID() {
		return "ch.elexis.RezeptBlatt"; //$NON-NLS-1$
	}

	@Override
	public Image getSymbol() {
		return Images.IMG_PRINTER.getImage();
	}

	public boolean isAddressSelection() {
		return addressSelection;
	}

	public void setAddressSelection(boolean value) {
		this.addressSelection = value;
	}
}
