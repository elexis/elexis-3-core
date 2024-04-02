package ch.elexis.core.ui.views.rechnung;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Zahlung;
import ch.elexis.scripting.CSVWriter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.Tree;

//201512211341js: Info: This dialog starts the generation of output ONLY AFTER [OK] has been pressed.
class RnListeExportDialog extends TitleAreaDialog implements ICallback {
	ArrayList<Rechnung> rnn;
	private Logger log = LoggerFactory.getLogger(RnActions.class);
	private String RnListExportFileName = new SimpleDateFormat("'RnListExport-'yyyyMMddHHmmss'.csv'") //$NON-NLS-1$
			.format(new Date());

	// 201512211459js: Siehe auch RechnungsDrucker.java - nach dortigem Vorbild
	// modelliert.
	// Zur Kontrolle es Ausgabeverzeichnisses, mit permanentem Speichern.
	// ToDo: Durchgängig auf externe Konstanten umstellen, wie dort gezeigt, u.a.
	// bei Hub.LocalCfg Zugriffen.
	String RnListExportDirname = CoreHub.localCfg.get("rechnung/RnListExportDirname", null); //$NON-NLS-1$
	Text tDirName;

	public RnListeExportDialog(final Shell shell, final Object[] tree) {
		super(shell);
		rnn = new ArrayList<>(tree.length);
		for (Object o : tree) {
			if (o instanceof Tree) {
				Tree tr = (Tree) o;
				if (tr.contents instanceof Rechnung) {
					tr = tr.getParent();
				}
				if (tr.contents instanceof Fall) {
					tr = tr.getParent();
				}
				if (tr.contents instanceof Patient) {
					for (Tree tFall : (Tree[]) tr.getChildren().toArray(new Tree[0])) {
						Fall fall = (Fall) tFall.contents;
						for (Tree tRn : (Tree[]) tFall.getChildren().toArray(new Tree[0])) {
							Rechnung rn = (Rechnung) tRn.contents;
							// 201512211302js: Rechnungen sollten nicht doppelt im Verarbeitungsergebnis
							// auftreten,
							// nur weil aufgeklappt und dann bis zu 3x etwas vom gleichen
							// Patienten/Fall/Rechnung markiert war.
							if (!rnn.contains(rn)) { // deshalb prüfen, ob die rechnung schon drin ist, bevor sie
														// hinzugefügt wird.
								rnn.add(rn);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

		// 201512211432js: Siehe auch Rechnungsdrucker.java public class
		// RechnungsDrucker.createSettingsControl()
		// TODO: Auf Konstante umstellen, dann braucht's allerdings den Austausch
		// weiterer Module bei Installation!!!

		Group cSaveCopy = new Group(ret, SWT.NONE);
		cSaveCopy.setText(String.format(Messages.RnActions_exportSaveHelp, RnListExportFileName));
		cSaveCopy.setLayout(new GridLayout(2, false));
		Button bSelectFile = new Button(cSaveCopy, SWT.PUSH);
		bSelectFile.setText(Messages.RnActions_exportListDirName);
		bSelectFile.setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
		bSelectFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog ddlg = new DirectoryDialog(parent.getShell());
				RnListExportDirname = ddlg.open();
				if (RnListExportDirname == null) {
					SWTHelper.alert(Messages.RnActions_exportListDirNameMissingCaption,
							Messages.RnActions_exportListDirNameMissingText);
				} else {
					// ToDo: Umstellen auf externe Konstante!
					CoreHub.localCfg.set("rechnung/RnListExportDirname", RnListExportDirname); //$NON-NLS-1$
					tDirName.setText(RnListExportDirname);
				}
			}
		});
		tDirName = new Text(cSaveCopy, SWT.BORDER | SWT.READ_ONLY);
		tDirName.setText(CoreHub.localCfg.get("rechnung/RnListExportDirname", StringUtils.EMPTY)); //$NON-NLS-1$
		tDirName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		return ret;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.Core_Invoice_List);
		setTitle(Messages.RnActions_exportListCaption);
		setMessage(Messages.RnActions_exportListMessage);
		getShell().setSize(900, 700);
		SWTHelper.center(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), getShell());
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		if (CoreHub.localCfg.get("rechnung/RnListExportDirname_bSaveFileAs", true)) //$NON-NLS-1$
			CSVWriteTable();
	}

	@Override
	public void save() {
	}

	@Override
	public boolean saveAs() {
		return false;
	}

	public void CSVWriteTable() {
		String pathToSave = RnListExportDirname + "/" + RnListExportFileName; //$NON-NLS-1$
		CSVWriter csv = null;
		int nrLines = 0;
		try {
			csv = new CSVWriter(new FileWriter(pathToSave));
			// @formatter:off
			String[] header = new String[] {
				"Aktion?", // line 0 //$NON-NLS-1$
				"Re.Nr", // line 1 //$NON-NLS-1$
				"Re.DatumRn", // line 2 //$NON-NLS-1$
				"Re.DatumVon", // line 3 //$NON-NLS-1$
				"Re.DatumBis", // line 4 //$NON-NLS-1$
				"Re.Garant", // line 5 //$NON-NLS-1$
				"Re.Total", // line 6 //$NON-NLS-1$
				"Re.Offen", // line 7 //$NON-NLS-1$
				"Re.StatusLastUpdate", // line 8 //$NON-NLS-1$
				"Re.Status", // line 9 //$NON-NLS-1$
				"Re.StatusIsActive", // line 10 //$NON-NLS-1$
				"Re.StatusText", // line 11 //$NON-NLS-1$
				"Re.StatusChanges", // line 12 //$NON-NLS-1$
				"Re.Rejecteds", // line 13 //$NON-NLS-1$
				"Re.Outputs", // line 14 //$NON-NLS-1$
				"Re.Payments", // line 15 //$NON-NLS-1$
				"Fall.AbrSystem", // line 16 //$NON-NLS-1$
				"Fall.Bezeichnung", // line 17 //$NON-NLS-1$
				"Fall.Grund", // line 18 //$NON-NLS-1$
				"Pat.Nr", // line 10 //$NON-NLS-1$
				"Pat.Name", // line 20 //$NON-NLS-1$
				"Pat.Vorname", // line 21 //$NON-NLS-1$
				"Pat.GebDat", // line 22 //$NON-NLS-1$
				"Pat.LztKonsDat", // line 23 //$NON-NLS-1$
				"Pat.Balance", // line 24 //$NON-NLS-1$
				"Pat.GetAccountExcess", // line 25 //$NON-NLS-1$
				"Pat.BillSummary.Total.", // line 26 //$NON-NLS-1$
				"Pat.BillSummary.Paid", // line 27 //$NON-NLS-1$
				"Pat.BillSummary.Open" // line 28 //$NON-NLS-1$
			};
			// @formatter:on
			log.debug("csv export started for {} with {} fields for {} invoices", pathToSave, header.length, //$NON-NLS-1$
					rnn.size());
			csv.writeNext(header);
			nrLines++;
			int i;
			for (i = 0; i < rnn.size(); i++) {
				Rechnung rn = rnn.get(i);
				Fall fall = rn.getFall();
				Patient p = fall.getPatient();
				String[] line = new String[header.length];
				Arrays.fill(line, StringUtils.EMPTY);
				line[0] = StringUtils.EMPTY; // 201512210402js: Leere Spalte zum Eintragen der gewünschten Aktion.
				line[1] = rn.getNr();
				line[2] = rn.getDatumRn();
				line[3] = rn.getDatumVon();
				line[4] = rn.getDatumBis();
				line[5] = fall.getGarant().getLabel();
				line[6] = rn.getBetrag().toString();
				line[7] = rn.getOffenerBetrag().toString();
				long luTime = rn.getLastUpdate();
				Date date = new Date(luTime);
				// TODO: Support other date formats based upon location or configured settings
				SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
				String dateText = df2.format(date);
				line[8] = dateText.toString();
				InvoiceState st = rn.getInvoiceState();
				line[9] = Integer.toString(st.getState());
				if (st.isActive()) {
					line[10] = "True"; //$NON-NLS-1$
				} else {
					line[10] = "False"; //$NON-NLS-1$
				}
				line[11] = st.getLocaleText();
				// 201512210310js: New: produce 4 fields, each with multiline content.
				List<String> statuschgs = rn.getTrace(Rechnung.STATUS_CHANGED);
				String a = statuschgs.toString();
				if (a != null && a.length() > 1) {
					// Die Uhrzeiten rauswerfen:
					a = a.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", StringUtils.EMPTY); //$NON-NLS-1$
					// ", " durch StringUtils.LF ersetzen (Man könnte auch noch prüfen, ob danach
					// eine
					// Zahl/ein Datum kommt - die dann aber behalten werden muss.)
					a = a.replaceAll("\r\n", ", "); //$NON-NLS-1$ //$NON-NLS-2$
					a = a.replaceAll(", ", StringUtils.LF); //$NON-NLS-1$
					// Führende und Trailende [] bei der Ausgabe (!) rauswerfen
					line[12] = a.substring(1, a.length() - 1);
				}
				if (rn.getInvoiceState() == InvoiceState.DEFECTIVE) {
					List<String> rejects = rn.getTrace(Rechnung.REJECTED);
					String rnStatus = rejects.toString();
					if (rnStatus != null && rnStatus.length() > 1) {
						// Die Uhrzeiten rauswerfen:
						rnStatus = rnStatus.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", StringUtils.EMPTY); //$NON-NLS-1$
						// ", " durch StringUtils.LF ersetzen (Man könnte auch noch prüfen, ob danach
						// eine
						// Zahl/ein Datum kommt - die dann aber behalten werden muss.)
						rnStatus = rnStatus.replaceAll("\r\n", ", "); //$NON-NLS-1$ //$NON-NLS-2$
						rnStatus = rnStatus.replaceAll(", ", StringUtils.LF); //$NON-NLS-1$
						// Führende und Trailende [] bei der Ausgabe (!) rauswerfen
						line[13] = rnStatus.substring(1, rnStatus.length() - 1);
					}
				}
				List<String> outputs = rn.getTrace(Rechnung.OUTPUT);
				String rnOutput = outputs.toString();
				if (rnOutput != null && rnOutput.length() > 1) {
					// Die Uhrzeiten rauswerfen:
					rnOutput = rnOutput.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", StringUtils.EMPTY); //$NON-NLS-1$
					// ", " durch StringUtils.LF ersetzen (Man könnte auch noch prüfen, ob danach
					// eine
					// Zahl/ein Datum kommt - die dann aber behalten werden muss.)
					rnOutput = rnOutput.replaceAll("\r\n", ", "); //$NON-NLS-1$ //$NON-NLS-2$
					rnOutput = rnOutput.replaceAll(", ", StringUtils.LF); //$NON-NLS-1$
					// Führende und Trailende [] bei der Ausgabe (!) rauswerfen
					line[14] = rnOutput.substring(1, rnOutput.length() - 1);
				}
				List<String> payments = rn.getTrace(Rechnung.PAYMENT);
				String rnPayment = payments.toString();
				if (rnPayment != null && rnPayment.length() > 1) {
					// Die Uhrzeiten rauswerfen:
					rnPayment = rnPayment.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", StringUtils.EMPTY); //$NON-NLS-1$
					// ", " durch StringUtils.LF ersetzen (Man könnte auch noch prüfen, ob danach
					// eine
					// Zahl/ein Datum kommt - die dann aber behalten werden muss.)
					rnPayment = rnPayment.replaceAll("\r\n", ", "); //$NON-NLS-1$ //$NON-NLS-2$
					rnPayment = rnPayment.replaceAll(", ", StringUtils.LF); //$NON-NLS-1$
					// Führende und Trailende [] bei der Ausgabe (!) rauswerfen
					line[15] = rnPayment.substring(1, rnPayment.length() - 1);
				}
				// Jetzt alles zum betroffenen Fall:
				line[16] = fall.getAbrechnungsSystem();
				line[17] = fall.getBezeichnung();
				line[18] = fall.getGrund();
				// Jetzt alles zum betroffenen Patienten:
				line[19] = p.getKuerzel();
				line[20] = p.getName();
				line[21] = p.getVorname();
				line[22] = p.getGeburtsdatum();
				// TODO: allenfalls wieder: auf n.a. oder so setzen...
				// TODO: Ich möcht aber wissen, ob p (dürfte eigentlich nie der Fall sein) oder
				// nk schuld sind, wenn nichts rauskommt.
				// TODO: Na ja, eigentlich würd ich noch lieber wissen, WARUM da manchmal nichts
				// rauskommt, obwohl eine kons sicher vhd ist.
				String lkDatum = "p==null"; //$NON-NLS-1$
				if (p != null) {
					Konsultation lk = p.getLetzteKons(false);
					if (lk != null) {
						lkDatum = (lk.getDatum());
					} else {
						lkDatum = "lk==null"; //$NON-NLS-1$
					}
				}
				line[23] = lkDatum;
				line[24] = p.getBalance(); // returns: String
				line[25] = p.getAccountExcess().toString(); // returns: Money
				// 201512210146js: Das Folgende ist aus BillSummary - dort wird dafür keine
				// Funktion bereitgestellt,
				// TODO: Prüfen, ob das eine Redundanz DORT und HIER ist vs. obenn erwähnter
				// getKontostand(), getAccountExcess() etc.
				// maybe called from foreign thread
				String totalText = StringUtils.EMPTY;
				String paidText = StringUtils.EMPTY;
				String openText = StringUtils.EMPTY;
				// Davon, dass p != null ist, darf man eigentlich ausgehen, da ja Rechnungen zu
				// p gehören etc.
				if (p != null) {
					Money total = new Money(0);
					Money paid = new Money(0);
					List<Rechnung> rechnungen = p.getRechnungen();
					for (Rechnung rechnung : rechnungen) {
						// don't consider canceled bills
						if (rechnung.getInvoiceState() != InvoiceState.CANCELLED) {
							total.addMoney(rechnung.getBetrag());
							for (Zahlung zahlung : rechnung.getZahlungen()) {
								paid.addMoney(zahlung.getBetrag());
							}
						}
					}
					Money open = new Money(total);
					open.subtractMoney(paid);
					totalText = total.toString();
					paidText = paid.toString();
					openText = open.toString();
				}
				line[26] = totalText;
				line[27] = paidText;
				line[28] = openText;
				csv.writeNext(line);
				nrLines++;
			}
			csv.close();
			log.debug("{}: Wrote {} lines for {} invoices", pathToSave, nrLines, rnn.size());
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.error("csv exporter error", ex);
			SWTHelper.showError("Fehler", ex.getMessage());
		} finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					log.error("cannot close csv exporter", e); //$NON-NLS-1$
				}
			}
		}
	}
}