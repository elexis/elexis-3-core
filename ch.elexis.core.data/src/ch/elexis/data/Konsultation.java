/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IOptifier;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;

/**
 * Eine Konsultation ist ein einzelner Mandant/Patient-Kontakt. Eine Konsultation gehört immer zu
 * einem Fall und zu einem Mandanten, und hat ein bestimmtes Datum. Eine Konsultation kann eine oder
 * mehrere der Fall-Diagnosen betreffen. Eine Konsultation enthält ausserdem auch einen
 * Behandlungstext, und nicht zuletzt auch einen Verrechnungs-Set. Eine Konsultation kann nicht mehr
 * geändert werden, wenn sie geschlossen ist
 * 
 * @author gerry
 */
public class Konsultation extends PersistentObject implements Comparable<Konsultation> {
	public static final String FLD_ENTRY = "Eintrag";
	public static final String DATE = "Datum";
	public static final String FLD_BILL_ID = "RechnungsID";
	public static final String FLD_CASE_ID = "FallID";
	public static final String FLD_MANDATOR_ID = "MandantID";
	public static final String FLD_JOINT_DIAGNOSEN = "Diagnosen";
	
	private static final String TABLENAME = "BEHANDLUNGEN";
	volatile int actEntry;
	
	protected String getTableName(){
		return TABLENAME;
	}
	
	static {
		addMapping(TABLENAME, FLD_MANDATOR_ID, PersistentObject.DATE_COMPOUND, FLD_CASE_ID,
			FLD_BILL_ID, "Eintrag=S:V:Eintrag",
			FLD_JOINT_DIAGNOSEN + "=JOINT:BehandlungsID:DiagnoseID:BEHDL_DG_JOINT");
	}
	
	protected Konsultation(String id){
		super(id);
		
	}
	
	/**
	 * Prüfen, ob diese Konsultation gültig ist. Dies ist dann der Fall, wenn sie in der Datenbank
	 * existiert und wenn sie einen zugeordneten Mandanten und einen zugeordeten Fall hat.
	 */
	public boolean isValid(){
		if (!super.isValid()) {
			return false;
		}
		Mandant m = getMandant();
		if ((m == null) || (!m.isValid())) {
			return false;
		}
		Fall fall = getFall();
		if ((fall == null) || (!fall.isValid())) {
			return false;
		}
		
		return true;
	}
	
	/** Den zugehörigen Fall holen */
	public Fall getFall(){
		return Fall.load(get(FLD_CASE_ID));
	}
	
	/** Die Konsultation einem Fall zuordnen */
	@Deprecated
	public void setFall(Fall f){
		transferToFall(f, false, true);
	}
	
	/**
	 * Transfers a {@link Konsultation} to a {@link Fall}
	 * 
	 * @param f
	 *            transfered fall
	 * @param ignoreEditable
	 *            ignores konsultations editable
	 * @param setToStandartPreis
	 *            sets to the standard price
	 * @since 3.3
	 */
	public void transferToFall(Fall f, boolean ignoreEditable, boolean setToStandartPreis){
		if (ignoreEditable || isEditable(true)) {
			Fall alt = getFall();
			set(FLD_CASE_ID, f.getId());
			if (alt != null) {
				ICodeElementService codeElementService = CodeElementServiceHolder.getService();
				HashMap<Object, Object> context = getCodeElementServiceContext();
				List<Verrechnet> vv = getLeistungen();
				for (Verrechnet verrechnet : vv) {
					if (setToStandartPreis) {
						verrechnet.setStandardPreis();
					} else {
						IVerrechenbar v = verrechnet.getVerrechenbar();
						// tarmed needs to be recharged
						if (isTarmed(verrechnet)) {
							// make sure verrechenbar is matching for the kons
							Optional<ICodeElement> matchingVerrechenbar = codeElementService
								.createFromString(v.getCodeSystemName(), v.getCode(), context);
							if (matchingVerrechenbar.isPresent()) {
								int amount = verrechnet.getZahl();
								removeLeistung(verrechnet);
								for (int i = 0; i < amount; i++) {
									addLeistung((IVerrechenbar) matchingVerrechenbar.get());
								}
							} else {
								MessageEvent.fireInformation("Info",
									"Achtung: durch den Fall wechsel wurde die Position "
										+ v.getCode()
										+ " automatisch entfernt, da diese im neuen Fall nicht vorhanden ist.");
								removeLeistung(verrechnet);
							}
						} else {
							TimeTool date = new TimeTool(verrechnet.getKons().getDatum());
							double factor = v.getFactor(date, f);
							verrechnet.set(Verrechnet.SCALE_SELLING, Double.toString(factor));
						}
					}
				}
			}
			refreshLastUpdateAndSendUpdateEvent(FLD_CASE_ID);
		}
	}
	
	private HashMap<Object, Object> getCodeElementServiceContext(){
		HashMap<Object, Object> ret = new HashMap<>();
		ret.put(ContextKeys.CONSULTATION, this);
		IPersistentObject coverage = getFall();
		if (coverage != null) {
			ret.put(ContextKeys.COVERAGE, coverage);
		}
		return ret;
	}
	
	private boolean isTarmed(Verrechnet verrechnet){
		String fullname = verrechnet.get(Verrechnet.CLASS);
		return fullname.contains("TarmedLeistung");
	}
	
	/** Eine neue Konsultation zu einem Fall erstellen */
	public Konsultation(Fall fall){
		if (fall == null) {
			fall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		}
		
		if (fall == null) {
			MessageEvent.fireError("Kein Fall ausgewählt",
				"Bitte zunächst einen Fall auswählen, dem die neue Konsultation zugeordnet werden soll");
		} else if (fall.isOpen() == false) {
			MessageEvent.fireError("Fall geschlossen",
				"Zu einem abgeschlossenen Fall kann keine neue Konsultation erstellt werden");
		} else {
			create(null);
			set(new String[] {
				DATE, FLD_CASE_ID, FLD_MANDATOR_ID
			}, new TimeTool().toString(TimeTool.DATE_GER), fall.getId(),
				CoreHub.actMandant.getId());
			fall.getPatient().setInfoElement("LetzteBehandlung", getId());
		}
		if (getDefaultDiagnose() != null)
			addDiagnose(getDefaultDiagnose());
	}
	
	/** Eine Konsultation anhand ihrer ID von der Datenbank einlesen */
	public static Konsultation load(String id){
		Konsultation ret = new Konsultation(id);
		return ret;
	}
	
	/**
	 * get the number of the last (highest) Version
	 * 
	 * @return
	 */
	public int getHeadVersion(){
		VersionedResource vr = getVersionedResource(FLD_ENTRY, false);
		return vr.getHeadVersion();
	}
	
	/**
	 * get the text entry od this Konsultation
	 * 
	 * @return
	 */
	public VersionedResource getEintrag(){
		VersionedResource vr = getVersionedResource(FLD_ENTRY, true);
		return vr;
	}
	
	/**
	 * Insert an XREF to the EMR text
	 * 
	 * @param provider
	 *            unique String identifying the provider
	 * @param id
	 *            String identifying the item
	 * @param pos
	 *            position of the item as offset relative to the contents
	 * @param text
	 *            text to insert
	 */
	public void addXRef(String provider, String id, int pos, String text){
		// fire prerelease triggers save
		ElexisEventDispatcher.getInstance().fire(new ElexisEvent(this, Konsultation.class,
			ElexisEvent.EVENT_LOCK_PRERELEASE, ElexisEvent.PRIORITY_SYNC));
		
		VersionedResource vr = getEintrag();
		String ntext = vr.getHead();
		Samdas samdas = new Samdas(ntext);
		Samdas.Record record = samdas.getRecord();
		String recText = record.getText();
		if ((pos == -1) || pos > recText.length()) {
			pos = recText.length();
			recText += "\n" + text;
		} else {
			recText = recText.substring(0, pos) + "\n" + text + recText.substring(pos);
		}
		record.setText(recText);
		// ++pos because \n has been added
		Samdas.XRef xref = new Samdas.XRef(provider, id, ++pos, text.length());
		record.add(xref);
		updateEintrag(samdas.toString(), true); // XRefs may always be added
		// update with the added content
		ElexisEventDispatcher.getInstance().fire(new ElexisEvent(this, Konsultation.class,
			ElexisEvent.EVENT_UPDATE, ElexisEvent.PRIORITY_NORMAL));
	}
	
	private Samdas getEntryRaw(){
		VersionedResource vr = getEintrag();
		String ntext = vr.getHead();
		Samdas samdas = new Samdas(ntext);
		return samdas;
	}
	
	private void updateEntryRaw(Samdas samdas){
		updateEintrag(samdas.toString(), false);
	}
	
	/**
	 * Remove an XREF from the EMR text. Will remove all XREFS of the given provider with the given
	 * ID from this EMR. Warning: The IKonsExtension's removeXRef method will not be called.
	 * 
	 * @param provider
	 *            unique provider id
	 * @param id
	 *            item ID
	 */
	public void removeXRef(String provider, String id){
		VersionedResource vr = getEintrag();
		String ntext = vr.getHead();
		Samdas samdas = new Samdas(ntext);
		Samdas.Record record = samdas.getRecord();
		String recText = record.getText();
		List<Samdas.XRef> xrefs = record.getXrefs();
		boolean changed = false;
		for (Samdas.XRef xref : xrefs) {
			if ((xref.getProvider().equals(provider)) && (xref.getID().equals(id))) {
				if (recText.length() > xref.getPos() + xref.getLength()) {
					recText = recText.substring(0, xref.getPos())
						+ recText.substring(xref.getPos() + xref.getLength());
					record.setText(recText);
				}
				record.remove(xref);
				changed = true;
			}
			
		}
		if (changed) {
			updateEintrag(samdas.toString(), true);
		}
		
	}
	
	/**
	 * Normally, the thext of a Konsultation may only be changed, if the Konsultation has not yet
	 * been billed. Due to customer demand, this was weakended: A User can have the right
	 * ADMIN_KONS_EDIT_IF_BILLED and then can edit all Konsultations, even billed ones.
	 * 
	 * @return
	 */
	private boolean isEintragEditable(){
		boolean editable = false;
		boolean hasRight = CoreHub.acl.request(AccessControlDefaults.ADMIN_KONS_EDIT_IF_BILLED);
		if (hasRight) {
			// user has right to change Konsultation. in this case, the user
			// may change the text even if the Konsultation has already been
			// billed, so don't check if it is billed
			editable = isEditable(true, false, true);
		} else {
			// normal case, check all
			editable = isEditable(true, true, true);
		}
		
		return editable;
	}
	
	/**
	 * Den Eintrag eintragen. Da es sich um eine VersionedResource handelt, wird nicht der alte
	 * Eintrag gelöscht, sondern der neue wird angehängt.
	 * 
	 * @param force
	 *            bei true wird der Eintrag auch dann geändert, wenn die Konsultation eigentlich
	 *            nicht änderbar ist.
	 */
	public void setEintrag(VersionedResource eintrag, boolean force){
		if (force || isEintragEditable()) {
			setVersionedResource(FLD_ENTRY, eintrag.getHead());
		}
	}
	
	/**
	 * Eine Änderung des Eintrags hinzufügen (der alte Eintrag wird nicht überschrieben)
	 * 
	 * @param force
	 *            bei true wird der Eintrag auch dann geändert, wenn die Konsultation eigentlich
	 *            nicht änderbar ist.
	 */
	public void updateEintrag(String eintrag, boolean force){
		if (force || isEintragEditable()) {
			setVersionedResource(FLD_ENTRY, eintrag);
			// ElexisEventDispatcher.update(this);
		}
	}
	
	/**
	 * remove all but the newest version of the entry
	 */
	public void purgeEintrag(){
		VersionedResource vr = getEintrag();
		vr.purge();
		setBinary(FLD_ENTRY, vr.serialize());
	}
	
	/** Den zugeordneten Mandanten holen */
	public Mandant getMandant(){
		return Mandant.load(get(FLD_MANDATOR_ID));
	}
	
	/** Die Konsultation einem Mandanten zuordnen */
	public void setMandant(Mandant m){
		if (m != null) {
			set(FLD_MANDATOR_ID, m.getId());
		}
	}
	
	/**
	 * Das Behandlungsdatum setzen
	 * 
	 * @param force
	 *            auch setzen, wenn Kons nicht änderbar
	 */
	public void setDatum(String dat, boolean force){
		if (dat != null) {
			if (force || isEditable(true)) {
				set(DATE, dat);
			}
		}
	}
	
	/** das Behandlungsdatum auslesen */
	public String getDatum(){
		String ret = get(DATE);
		return ret;
	}
	
	public Rechnung getRechnung(){
		String invoiceId = get(FLD_BILL_ID);
		if (invoiceId == null) {
			return null;
		}
		return Rechnung.load(invoiceId);
	}
	
	/**
	 * Lookup {@link Rechnung} including canceled ones for this {@link Konsultation}. Only works
	 * with {@link Konsultation} created with Elexis version 3.0.0 or newer.
	 * 
	 * @since 3.0.0
	 * @return
	 */
	public List<Rechnung> getRechnungen(){
		List<VerrechnetCopy> konsVerrechnet = VerrechnetCopy.getVerrechnetCopyByConsultation(this);
		List<Rechnung> ret = new ArrayList<Rechnung>();
		HashSet<String> rechnungsIds = new HashSet<String>();
		for (VerrechnetCopy verrechnetCopy : konsVerrechnet) {
			String rechnungsId = verrechnetCopy.get(VerrechnetCopy.RECHNUNGID);
			rechnungsIds.add(rechnungsId);
		}
		for (String rechnungsId : rechnungsIds) {
			Rechnung rechnung = Rechnung.load(rechnungsId);
			if (rechnung != null) {
				ret.add(rechnung);
			}
		}
		return ret;
	}
	
	public void setRechnung(Rechnung r){
		if (r != null) {
			set(FLD_BILL_ID, r.getId());
		}
	}
	
	/**
	 * Checks if the Konsultation can be altered. This method is internally used.
	 * 
	 * @param checkMandant
	 *            checks whether the current mandant is the owner of this Konsultation
	 * @param checkBill
	 *            checks whether the Konsultation has already been billed
	 * @param showError
	 *            if true, show error messages
	 * @return true if the Konsultation can be altered in repsect to the given checks, else
	 *         otherwise.
	 */
	private boolean isEditable(boolean checkMandant, boolean checkBill, boolean showError){
		Mandant m = getMandant();
		checkMandant = !CoreHub.acl.request(AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
		boolean mandantOK = true;
		boolean billOK = true;
		Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
		boolean bMandantLoggedIn = (mandator != null);
		
		// if m is null, ignore checks (return true)
		if (m != null && mandator != null) {
			if (checkMandant && !(m.getId().equals(mandator.getId()))) {
				mandantOK = false;
			}
			
			if (checkBill) {
				Rechnung rn = getRechnung();
				if (rn == null || (!rn.exists())) {
					billOK = true;
				} else {
					int stat = rn.getStatus();
					if (stat == RnStatus.STORNIERT) {
						billOK = true;
					} else {
						billOK = false;
					}
				}
			}
		}
		
		boolean ok = billOK && mandantOK && bMandantLoggedIn;
		if (ok) {
			return true;
		}
		
		// something is not ok
		if (showError) {
			String msg = "";
			if (!bMandantLoggedIn) {
				msg = "Es ist kein Mandant eingeloggt";
			} else {
				if (!billOK) {
					msg = "Für diese Behandlung wurde bereits eine Rechnung erstellt.";
				} else {
					msg = "Diese Behandlung ist nicht von Ihnen";
				}
			}
			
			MessageEvent.fireError("Konsultation kann nicht geändert werden", msg);
		}
		
		return false;
	}
	
	/**
	 * Checks if the Konsultation can be altered. A user that has the right LSTG_CHARGE_FOR_ALL can
	 * charge for all mandators. Others can only charge a Konsultation that belongs to their own
	 * logged in mandator.
	 * 
	 * @param showError
	 *            if true, show error messages
	 * @return true if the Konsultation can be altered, else otherwise.
	 */
	public boolean isEditable(boolean showError){
		Fall fall = getFall();
		if (fall != null) {
			if ((!fall.isOpen()) && showError) {
				MessageEvent.fireError("Fall geschlossen",
					"Diese Konsultation gehört zu einem abgeschlossenen Fall");
				return false;
			}
		}
		
		// check mandant and bill
		return isEditable(true, true, showError);
	}
	
	public int getStatus(){
		return getStatus(getRechnung());
	}
	
	private int getStatus(Rechnung invoice){
		if (invoice != null) {
			return invoice.getStatus();
		}
		Mandant rm = getMandant();
		if ((rm != null) && (rm.equals(ElexisEventDispatcher.getSelected(Mandant.class)))) {
			if (getDatum().equals(new TimeTool().toString(TimeTool.DATE_GER))) {
				return RnStatus.VON_HEUTE;
			} else {
				return RnStatus.NICHT_VON_HEUTE;
			}
		} else {
			return RnStatus.NICHT_VON_IHNEN;
		}
	}
	
	public String getStatusText(){
		String statusText = "";
		
		Rechnung rechnung = getRechnung();
		if (rechnung != null) {
			statusText += "RG " + rechnung.getNr() + ": ";
		}
		
		statusText += RnStatus.getStatusText(getStatus(rechnung));
		
		return statusText;
	}
	
	/** Eine einzeilige Beschreibung dieser Konsultation holen */
	public String getLabel(){
		StringBuffer ret = new StringBuffer();
		Mandant m = getMandant();
		ret.append(getDatum()).append(" (").append(getStatusText()).append(") - ")
			.append((m == null) ? "?" : m.getLabel());
		return ret.toString();
	}
	
	public String getVerboseLabel(){
		StringBuilder ret = new StringBuilder();
		ret.append(getFall().getPatient().getName()).append(" ")
			.append(getFall().getPatient().getVorname()).append(", ")
			.append(getFall().getPatient().getGeburtsdatum()).append(" - ").append(getDatum());
		return ret.toString();
	}
	
	/** Eine Liste der Diagnosen zu dieser Konsultation holen */
	public ArrayList<IDiagnose> getDiagnosen(){
		ArrayList<IDiagnose> ret = new ArrayList<IDiagnose>();
		Stm stm = getDBConnection().getStatement();
		ResultSet rs1 = stm.query(
			"SELECT DIAGNOSEID FROM BEHDL_DG_JOINT INNER JOIN BEHANDLUNGEN on BehandlungsID=BEHANDLUNGEN.id where BEHDL_DG_JOINT.deleted='0' and BEHANDLUNGEN.deleted='0' AND BEHANDLUNGSID="
				+ JdbcLink.wrap(getId()));
		StringBuilder sb = new StringBuilder();
		try {
			while (rs1.next() == true) {
				String dgID = rs1.getString(1);
				
				Stm stm2 = getDBConnection().getStatement();
				ResultSet rs2 = stm2
					.query("SELECT DG_CODE,KLASSE FROM DIAGNOSEN WHERE ID=" + JdbcLink.wrap(dgID));
				if (rs2.next()) {
					sb.setLength(0);
					sb.append(rs2.getString(2)).append("::");
					sb.append(rs2.getString(1));
					try {
						PersistentObject dg = CoreHub.poFactory.createFromString(sb.toString());
						if (dg != null) {
							ret.add((IDiagnose) dg);
						}
					} catch (Exception ex) {
						log.error("Fehlerhafter Diagnosecode " + sb.toString());
					}
				}
				rs2.close();
				getDBConnection().releaseStatement(stm2);
			}
			rs1.close();
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Persistence error: " + ex.getMessage(), ex, ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		} finally {
			getDBConnection().releaseStatement(stm);
		}
		return ret;
	}
	
	/** Eine weitere Diagnose dieser Konsultation zufügen */
	public void addDiagnose(IDiagnose dg){
		if (!isEditable(true)) {
			return;
		}
		
		String dgid = prepareDiagnoseSelectWithCodeAndClass(dg.getCode(), dg.getClass().getName());
		if (dgid != null) {
			return;
		}
		
		String diagnosisEntryExists = getDBConnection().queryString(
			"SELECT ID FROM DIAGNOSEN WHERE KLASSE=" + JdbcLink.wrap(dg.getClass().getName())
				+ " AND DG_CODE=" + JdbcLink.wrap(dg.getCode()));
		StringBuilder sql = new StringBuilder(200);
		if (StringTool.isNothing(diagnosisEntryExists)) {
			diagnosisEntryExists = StringTool.unique("bhdl");
			sql.append("INSERT INTO DIAGNOSEN (ID, LASTUPDATE, DG_CODE, DG_TXT, KLASSE) VALUES (")
				.append(JdbcLink.wrap(diagnosisEntryExists)).append(",")
				.append(Long.toString(System.currentTimeMillis())).append(",")
				.append(JdbcLink.wrap(dg.getCode())).append(",").append(JdbcLink.wrap(dg.getText()))
				.append(",").append(JdbcLink.wrap(dg.getClass().getName())).append(")");
			getDBConnection().exec(sql.toString());
			sql.setLength(0);
		}
		/**
		 * @deprecated remove ID,lastupdate,deleted in 3.3
		 * @see https://redmine.medelexis.ch/issues/5629
		 */
		sql.append("INSERT INTO BEHDL_DG_JOINT (ID,BEHANDLUNGSID,DIAGNOSEID) VALUES (")
			.append(JdbcLink.wrap(StringTool.unique("bhdx"))).append(",").append(getWrappedId())
			.append(",").append(JdbcLink.wrap(diagnosisEntryExists)).append(")");
		getDBConnection().exec(sql.toString());
		
		// Statistik nachführen
		getFall().getPatient().countItem(dg);
		CoreHub.actUser.countItem(dg);
		
		refreshLastUpdateAndSendUpdateEvent(FLD_JOINT_DIAGNOSEN);
	}
	
	/** Eine Diagnose aus der Diagnoseliste entfernen */
	public void removeDiagnose(IDiagnose dg){
		if (isEditable(true)) {
			String dgid =
				prepareDiagnoseSelectWithCodeAndClass(dg.getCode(), dg.getClass().getName());
			
			if (dgid == null) {
				String code = dg.getCode();
				// chapter of a TI-Code
				if (code != null && code.length() == 2 && code.charAt(1) == '0') {
					code = code.substring(0, 1);
					dgid = prepareDiagnoseSelectWithCodeAndClass(code, dg.getClass().getName());
				}
			}
			
			if (dgid == null) {
				log.warn(
					"Requested delete of diagnosis which could not be resolved [{}] in consultation [{}]",
					dg.getCode() + "/" + dg.getClass().getName(), getId());
			} else {
				StringBuilder sql = new StringBuilder();
				sql.append("DELETE FROM BEHDL_DG_JOINT WHERE BehandlungsID=").append(getWrappedId())
					.append(" AND DiagnoseId=" + JdbcLink.wrap(dgid));
				log.debug(sql.toString());
				getDBConnection().exec(sql.toString());
				refreshLastUpdateAndSendUpdateEvent(FLD_JOINT_DIAGNOSEN);
			}
		}
	}
	
	private final String STM_S_BDJ = "SELECT BDJ.DiagnoseId FROM BEHDL_DG_JOINT BDJ, DIAGNOSEN D"
		+ " WHERE BDJ.BehandlungsID=? AND D.ID = BDJ.DiagnoseID AND D.DG_CODE=? AND D.KLASSE=?;";
	
	private String prepareDiagnoseSelectWithCodeAndClass(String code, String classname){
		PreparedStatement pst = getDBConnection().getPreparedStatement(STM_S_BDJ);
		try {
			pst.setString(1, getId());
			pst.setString(2, code);
			pst.setString(3, classname);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			MessageEvent.fireError("Fehler beim Löschen", e.getMessage(), e);
			log.error("Error deleting diagnosis", e);
		} finally {
			getDBConnection().releasePreparedStatement(pst);
		}
		return null;
	}
	
	/** Die zu dieser Konsultation gehörenden Leistungen holen */
	public List<Verrechnet> getLeistungen(){
		Query<Verrechnet> qbe = new Query<Verrechnet>(Verrechnet.class);
		qbe.add(Verrechnet.KONSULTATION, Query.EQUALS, getId());
		qbe.orderBy(false, Verrechnet.CLASS, Verrechnet.LEISTG_CODE);
		return qbe.execute();
	}
	
	/** Die zu dieser Konsultation gehörenden Leistungen holen */
	public List<Verrechnet> getLeistungen(String[] prefetch){
		Query<Verrechnet> qbe =
			new Query<Verrechnet>(Verrechnet.class, Verrechnet.KONSULTATION, getId(),
				Verrechnet.TABLENAME, prefetch);
		qbe.orderBy(false, Verrechnet.CLASS, Verrechnet.LEISTG_CODE);
		return qbe.execute();
	}
	
	/**
	 * Liefert eine Verrechnete Leistung anhand verrechnbar id
	 * 
	 * @param code
	 * @return
	 */
	public Verrechnet getVerrechnet(IVerrechenbar iVerrechenbar){
		if (iVerrechenbar != null && iVerrechenbar.getId() != null) {
			Query<Verrechnet> qbe = new Query<Verrechnet>(Verrechnet.class);
			qbe.add(Verrechnet.KONSULTATION, Query.EQUALS, getId());
			qbe.add(Verrechnet.LEISTG_CODE, Query.EQUALS, iVerrechenbar.getId());
			
			List<Verrechnet> verrechnets = qbe.execute();
			if (verrechnets.size() == 1) {
				return verrechnets.get(0);
			}
		}
		return null;
	}
	
	/**
	 * Eine Verrechenbar aus der Konsultation entfernen
	 * 
	 * @param ls
	 *            die Verrechenbar
	 * @return Ein Optifier- Resultat
	 */
	
	public Result<Verrechnet> removeLeistung(Verrechnet ls){
		if (isEditable(true)) {
			IVerrechenbar v = ls.getVerrechenbar();
			int z = ls.getZahl();
			Result<Verrechnet> result = v.getOptifier().remove(ls, this);
			if (result.isOK()) {
				if (v instanceof Artikel) {
					Artikel art = (Artikel) v;
					CoreHub.getStockService().performSingleReturn(art, z);
					
					Object prescId = ls.getDetail(Verrechnet.FLD_EXT_PRESC_ID);
					if (prescId instanceof String) {
						Prescription prescription = Prescription.load((String) prescId);
						if (prescription.getEntryType() == EntryType.SELF_DISPENSED) {
							prescription.remove();
							ElexisEventDispatcher.reload(Prescription.class);
						}
					}
				}
			}
			return result;
		}
		return new Result<Verrechnet>(Result.SEVERITY.WARNING, 3,
			"Behandlung geschlossen oder nicht von Ihnen", null, false);
	}
	
	/**
	 * Eine verrechenbare Leistung zu dieser Konsultation zufügen
	 * 
	 * @return ein Verifier-Resultat.
	 */
	public Result<IVerrechenbar> addLeistung(IVerrechenbar l){
		if (isEditable(true)) {
			// TODO: ch.elexis.data.Konsultation.java: Weitere Leistungestypen
			// ausser Medikamente_BAG und arzttarif_ch=Tarmed,
			// TODO: ch.elexis.data.Konsultation.java: beim/nach dem Hinzufügen
			// auf <>0.00 prüfen, entweder verteilt in den Optifiern,
			// TODO: oder an dieser Stelle zentral, dann ggf. auch die schon
			// existierenden Prüfungen durch eine zentrale hier mitersetzen.
			IOptifier optifier = l.getOptifier();
			Result<IVerrechenbar> result = optifier.add(l, this);
			if (!result.isOK() && result.getCode() == 11) {
				String initialResult = result.toString();
				// code 11 is tarmed exclusion due to side see TarmedOptifier#EXKLUSIONSIDE
				// set a context variable to specify the side see TarmedLeistung#SIDE, TarmedLeistung#SIDE_L, TarmedLeistung#SIDE_R
				optifier.putContext("Seite", "r");
				result = optifier.add(l, this);
				if (!result.isOK() && result.getCode() == 11) {
					optifier.putContext("Seite", "l");
					result = optifier.add(l, this);
				}
				if (result.isOK()) {
					MessageEvent.fireInformation("Info",
						"Achtung: " + initialResult + "\n\n Es wurde bei der Position "
							+ l.getCode()
							+ " automatisch die Seite gewechselt."
							+ " Bitte korrigieren Sie die Leistung falls dies nicht korrekt ist.");
				}
				optifier.clearContext();
			}
			if (result.isOK()) {
				ElexisEventDispatcher.update(this);
				// Statistik nachführen
				getFall().getPatient().countItem(l);
				CoreHub.actUser.countItem(l);
				CoreHub.actUser.statForString("LeistungenMFU", l.getCodeSystemName());
			}
			return result;
		}
		return new Result<IVerrechenbar>(Result.SEVERITY.WARNING, 2,
			"Behandlung geschlossen oder nicht von Ihnen", null, false);
	}
	
	/**
	 * Returns the author of the latest version of a consultation entry. Each consultation always
	 * only has one author, and that's the one saved in the last version of a consultation entry.
	 * 
	 * @return Username of the author or an empty string.
	 */
	public String getAuthor(){
		String author = "";
		VersionedResource resource = this.getEintrag();
		if (resource != null) {
			ResourceItem item = resource.getVersion(resource.getHeadVersion());
			if (item != null) {
				return item.remark;
			}
			
		}
		return author;
	}
	
	/** Wieviel hat uns diese Konsultation gekostet? */
	public int getKosten(){
		int sum = 0;
		/*
		 * TimeTool mine=new TimeTool(getDatum()); List<Verrechenbar> l=getLeistungen();
		 * for(Verrechenbar v:l){ sum+=(v.getZahl()v.getKosten(mine)); }
		 */
		Stm stm = getDBConnection().getStatement();
		try {
			ResultSet res =
				stm.query("SELECT EK_KOSTEN FROM LEISTUNGEN WHERE deleted='0' AND BEHANDLUNG="
					+ getWrappedId());
			while ((res != null) && res.next()) {
				sum += res.getInt(1);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return 0;
		} finally {
			getDBConnection().releaseStatement(stm);
		}
		return sum;
		
	}
	
	/** Wieviel Zeit können wir für diese Konsultation anrechnen? */
	public int getMinutes(){
		int sum = 0;
		List<Verrechnet> l = getLeistungen();
		for (Verrechnet v : l) {
			IVerrechenbar iv = v.getVerrechenbar();
			if (iv != null) {
				sum += (v.getZahl() * iv.getMinutes());
			}
		}
		return sum;
		
	}
	
	/**
	 * Wieviel Umsatz (in Rappen) bringt uns diese Konsultation ein?
	 * 
	 * @deprecated not accurate. use getLeistungen()
	 */
	@Deprecated
	public double getUmsatz(){
		double sum = 0.0;
		Stm stm = getDBConnection().getStatement();
		try {
			ResultSet res = stm.query(
				"SELECT VK_PREIS,ZAHL,SCALE FROM LEISTUNGEN WHERE deleted='0' AND BEHANDLUNG="
					+ getWrappedId());
			while ((res != null) && res.next()) {
				double scale = res.getDouble(3) / 100.0;
				sum += (res.getDouble(1) * res.getDouble(2)) * scale;
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return 0;
		} finally {
			getDBConnection().releaseStatement(stm);
		}
		return sum;
		
	}
	
	/**
	 * Wieviel vom Umsatz bleibt uns von dieser Konsultation?
	 * 
	 */
	@Deprecated
	public double getGewinn(){
		return getUmsatz() - getKosten();
	}
	
	public void changeScale(IVerrechenbar v, int scale){
		if (isEditable(true)) {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE LEISTUNGEN SET SCALE='").append(scale).append("' WHERE BEHANDLUNG=")
				.append(getWrappedId()) /*
										 * .append ( " AND " ) .append ( "KLASSE=" ) .append (
										 * JdbcLink . wrap (v .getClass ( ).getName ()))
										 */
				.append(" AND LEISTG_CODE=").append(JdbcLink.wrap(v.getId()));
			
			getDBConnection().exec(sb.toString());
		}
	}
	
	/** Zahl einer Leistung ändern */
	public void changeZahl(IVerrechenbar v, int nz){
		if (isEditable(true)) {
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE LEISTUNGEN SET ZAHL=").append(nz)
				/*
				 * .append(" WHERE KLASSE=").append(JdbcLink.wrap(v.getClass().getName ()))
				 */
				.append(" WHERE LEISTG_CODE=").append(JdbcLink.wrap(v.getId()))
				.append(" AND BEHANDLUNG=").append(getWrappedId());
			getDBConnection().exec(sql.toString());
		}
	}
	
	@Override
	public boolean delete(){
		return delete(true);
	}
	
	public boolean delete(boolean forced){
		if (forced || isEditable(true)) {
			List<Verrechnet> vv = getLeistungen();
			// VersionedResource vr=getEintrag();
			if ((vv.size() == 0) || (forced == true)
				&& (CoreHub.acl.request(AccessControlDefaults.DELETE_FORCED) == true)) {
				delete_dependent();
				return super.delete();
			}
		}
		return false;
	}
	
	private boolean delete_dependent(){
		for (Verrechnet vv : new Query<Verrechnet>(Verrechnet.class, Verrechnet.KONSULTATION,
			getId()).execute()) {
			vv.delete();
		}
		getDBConnection().exec("DELETE FROM BEHDL_DG_JOINT WHERE BEHANDLUNGSID=" + getWrappedId());
		return true;
	}
	
	/** Interface Comparable, um die Behandlungen nach Datum sortieren zu können */
	public int compareTo(Konsultation b){
		TimeTool me = new TimeTool(getDatum());
		TimeTool other = new TimeTool(b.getDatum());
		return me.compareTo(other);
	}
	
	/**
	 * Helper: Get the "active" cons. Normally, it is the actually selected cons. if the actually
	 * selected cons does not match the actually selected patient, then it is rather the latest cons
	 * of the actually selected patient.
	 * 
	 * @return the active Kons
	 * @author gerry new concept due to some obscure selection problems
	 */
	public static Konsultation getAktuelleKons(){
		Konsultation ret = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		Patient pat = ElexisEventDispatcher.getSelectedPatient();
		if ((ret != null)
			&& ((pat == null) || (ret.getFall().getPatient().getId().equals(pat.getId())))) {
			return ret;
		}
		if (pat != null) {
			ret = pat.getLetzteKons(true);
			return ret;
		}
		MessageEvent.fireError("Kein Patient ausgewählt",
			"Bitte wählen Sie zuerst einen Patienten aus");
		return null;
	}
	
	protected Konsultation(){}
	
	static class BehandlungsComparator implements Comparator<Konsultation> {
		boolean rev;
		
		BehandlungsComparator(boolean reverse){
			rev = reverse;
		}
		
		public int compare(Konsultation b1, Konsultation b2){
			TimeTool t1 = new TimeTool(b1.getDatum());
			TimeTool t2 = new TimeTool(b2.getDatum());
			if (rev == true) {
				return t2.compareTo(t1);
			} else {
				return t1.compareTo(t2);
			}
		}
		
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	/*
	 * public interface Listener { public boolean creatingKons(Konsultation k); }
	 */
	
	/**
	 * Creates a new Konsultation object, with an optional initial text.
	 * 
	 * @param initialText
	 *            the initial text to be set, or null if no initial text should be set.
	 */
	public static void neueKons(final String initialText){
		Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
		Fall actFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		if (actFall == null) {
			if (actPatient == null) {
				MessageEvent.fireError(Messages.GlobalActions_CantCreateKons,
					Messages.GlobalActions_DoSelectPatient);
				return;
			}
			if (actFall == null) {
				Konsultation k = actPatient.getLetzteKons(false);
				if (k != null) {
					actFall = k.getFall();
					if (actFall == null) {
						MessageEvent.fireError(Messages.GlobalActions_CantCreateKons,
							Messages.GlobalActions_DoSelectCase);
						return;
					}
				} else {
					Fall[] faelle = actPatient.getFaelle();
					if ((faelle == null) || (faelle.length == 0)) {
						actFall = actPatient.neuerFall(Fall.getDefaultCaseLabel(),
							Fall.getDefaultCaseReason(), Fall.getDefaultCaseLaw());
					} else {
						actFall = faelle[0];
					}
				}
			}
		} else {
			if (!actFall.getPatient().equals(actPatient)) {
				if (actPatient != null) {
					Konsultation lk = actPatient.getLetzteKons(false);
					if (lk != null) {
						actFall = lk.getFall();
					}
				} else {
					MessageEvent.fireError(Messages.GlobalActions_CantCreateKons,
						Messages.GlobalActions_DoSelectCase);
					return;
				}
			}
		}
		if (!actFall.isOpen()) {
			MessageEvent.fireError(Messages.GlobalActions_casclosed,
				Messages.GlobalActions_caseclosedexplanation);
			return;
		}
		Konsultation actLetzte = actFall.getLetzteBehandlung();
		if ((actLetzte != null)
			&& actLetzte.getDatum().equals(new TimeTool().toString(TimeTool.DATE_GER))) {
			
			if (cod.openQuestion(Messages.GlobalActions_SecondForToday,
				Messages.GlobalActions_SecondForTodayQuestion) == false) {
				return;
			}
		}
		Konsultation n = actFall.neueKonsultation();
		n.setMandant(ElexisEventDispatcher.getSelectedMandator());
		if (initialText != null) {
			n.updateEintrag(initialText, false);
		}
		
		ElexisEventDispatcher.fireSelectionEvent(actFall);
		ElexisEventDispatcher.fireSelectionEvent(n);
	}
	
	public static IDiagnose getDefaultDiagnose(){
		IDiagnose ret = null;
		String diagnoseId = CoreHub.userCfg.get(Preferences.USR_DEFDIAGNOSE, "");
		if (diagnoseId.length() > 1) {
			ret = (IDiagnose) CoreHub.poFactory.createFromString(diagnoseId);
		}
		return ret;
	}
	
	/**
	 * Makes a simple copy for a {@link Konsultation} of some attributes. This method should only be
	 * used for {@link Rechnung} proposes.
	 * 
	 * @param fall
	 * @param invoiceSrc
	 * @return
	 */
	public Konsultation createCopy(Fall fall, Rechnung invoiceSrc){
		if (fall != null && invoiceSrc != null) {
			Konsultation clone = fall.neueKonsultation();
			Mandant m = getMandant();
			if (m != null) {
				clone.setMandant(m);
			}
			clone.setDatum(getDatum(), true);
			for (IDiagnose diagnose : getDiagnosen()) {
				clone.addDiagnose(diagnose);
			}
			VersionedResource vr = clone.getEintrag();
			vr.update(
				"Diese Konsultation wurde durch die Korrektur der Rechnung "
					+ invoiceSrc.getNr() + " erstellt.",
				"Rechnungskorrektur");
			clone.setEintrag(vr, true);
			return clone;
		}
		return null;
	}
}
