package ch.elexis.core.ui.views;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central registry of deprecated views (Ticket 28206).
 * 
 * Maps deprecated views to a Stage and an optional successor view name.
 * <ul>
 * <li>{@link Stage#NOTICE}: View remains usable but warns the user once per
 * session.</li>
 * <li>{@link Stage#HIDDEN}: View is completely removed from the "Show View"
 * dialog.</li>
 * </ul>
 * Note: Also contains views from dropped features to support legacy
 * installations.
 */
public class DeprecatedViews {

	public enum Stage {
		/** View stays available and informs the user once per session. */
		NOTICE,
		/** View additionally gets removed from the show view dialog. */
		HIDDEN
	}

	public static class Entry {

		private final Stage stage;

		private final String successor;

		private Entry(Stage stage, String successor) {
			this.stage = stage;
			this.successor = successor;
		}

		public Stage getStage() {
			return stage;
		}

		/**
		 * @return the successor view as named in the UI, or <code>null</code> if there
		 *         is none
		 */
		public String getSuccessor() {
			return successor;
		}
	}

	private static final Map<String, Entry> VIEWS = createViews();

	private static Map<String, Entry> createViews() {
		Map<String, Entry> views = new LinkedHashMap<>();

		// ch.elexis.core.ui, registered in ch.elexis.core.application
		views.put("ch.elexis.dbfielddisplay", notice("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.PatientDetailView", notice("Patientendetails Neu")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.views.SearchView", notice("Spotlight")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.FallListeView", notice("Falldetail")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.LabNotSeenView", notice("Labor oder Labor Roche")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.BBSView", notice("Pendenzen")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.ODDBView", hidden("Artikelstamm")); //$NON-NLS-1$ //$NON-NLS-2$

		// elexis-3-base
		views.put("ch.elexis.arzttarife_ch.rfeView", notice(null)); //$NON-NLS-1$
		views.put("ch.elexis.archie.patientstatistik.view1", notice("Archie Statistik")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("org.iatrix.messwerte.views.MesswerteView", notice("Befunde")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.views.ExterneDokumente", notice("Omnivore oder Dokumente")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("at.medevit.elexis.impfplan.ui.ImpfplanViewPart", notice("Impfliste")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.privatrechnung.view", notice("PDF Rechnungsdruck")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.berchtold.privatrechung.view", notice("PDF Rechnungsdruck")); //$NON-NLS-1$ //$NON-NLS-2$

		// medelexis-3
		views.put("ch.elexis.TarmedTimer", notice(null)); //$NON-NLS-1$

		views.put("com.hilotec.elexis.kgview.ArchivKG", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.ArvchivKGPrintView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.DLPersAnamnese", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.DLSysAnamnese", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.DiagnoselisteView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.DiagnoseView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.medikarte.FavMedikamentListe", hidden("Medikationsliste")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.MedikarteView", hidden("Medikationsliste")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.Konsliste", hidden("Verlauf")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.VerlaufView", hidden("Verlauf")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.KonsTimerView", hidden(null)); //$NON-NLS-1$
		views.put("com.hilotec.elexis.kgview.KonsTimeView", hidden(null)); //$NON-NLS-1$
		views.put("com.hilotec.elexis.kgview.Problemliste", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.Therapieliste", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.AllgemeinStView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.LokalStView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.EKGView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.JetzLeidenView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.ProzedereView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.RoentgenView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.TherapieView", hidden("Konsultation")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.FamAnamneseView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.PersAnamneseView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.RisikoFView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.SozAnamneseView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.kgview.SysAnamneseView", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.messwerte.v2.messwerteUebersichtV21", hidden("Befunde")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("com.hilotec.elexis.stickerprefix.stickerprefixview", hidden("Patientendetails")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("at.medevit.elexis.eHealthConnectorView", hidden("Impfliste")); //$NON-NLS-1$ //$NON-NLS-2$

		// Eclipse RCP views, not ours - there is no successor to point to
		views.put("org.eclipse.ui.internal.introview", hidden(null)); //$NON-NLS-1$
		views.put("org.eclipse.ui.browser.view", hidden(null)); //$NON-NLS-1$
		views.put("org.eclipse.ui.views.PropertySheet", hidden(null)); //$NON-NLS-1$
		views.put("org.eclipse.ui.views.ContentOutline", hidden(null)); //$NON-NLS-1$
		views.put("org.eclipse.ui.views.ProgressView", hidden(null)); //$NON-NLS-1$
		views.put("ch.framsteg.elexis.finance.analytics.views.ReportingView", hidden("Archie Statistik")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.netzkonzept.elexis.medidata.receive.MedidataStatusView", hidden("Rechnungsliste")); //$NON-NLS-1$ //$NON-NLS-2$
		views.put("ch.elexis.BehandlungenVerrechnenView", hidden(null));
		views.put("ch.elexis.RechnungsListeView", hidden("Rechnungsliste"));

		return Collections.unmodifiableMap(views);
	}

	private static Entry notice(String successor) {
		return new Entry(Stage.NOTICE, successor);
	}

	private static Entry hidden(String successor) {
		return new Entry(Stage.HIDDEN, successor);
	}

	public static Entry get(String viewId) {
		if (viewId == null) {
			return null;
		}
		return VIEWS.get(normalizeId(viewId));
	}

	public static List<String> getHiddenViewIds() {
		return VIEWS.entrySet().stream().filter(e -> e.getValue().getStage() == Stage.HIDDEN).map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public static String normalizeId(String elementId) {
		if (elementId == null) {
			return null;
		}
		int idx = elementId.indexOf(':');
		return idx > -1 ? elementId.substring(0, idx) : elementId;
	}

	private DeprecatedViews() {
		// static use only
	}
}
