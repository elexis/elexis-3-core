package ch.elexis.core.ui.views.rechnung;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Zahlung;
import ch.elexis.scripting.CSVWriter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.Tree;

//201512211341js: Info: This dialog starts the generation of output ONLY AFTER [OK] has been pressed.
class RnListeExportDialog extends TitleAreaDialog implements ICallback {
	ArrayList<Rechnung> rnn;
	private Logger log = LoggerFactory.getLogger(RnActions.class);
	private String RnListExportFileName =
			new SimpleDateFormat("'RnListExport-'yyyyMMddHHmmss'.csv'").format(new Date());
	
	//201512211459js: Siehe auch RechnungsDrucker.java - nach dortigem Vorbild modelliert.
	//Zur Kontrolle es Ausgabeverzeichnisses, mit permanentem Speichern.
	//ToDo: Durchgängig auf externe Konstanten umstellen, wie dort gezeigt, u.a. bei Hub.LocalCfg Zugriffen.
	String RnListExportDirname = CoreHub.localCfg.get("rechnung/RnListExportDirname", null);
	Text tDirName;
	
	public RnListeExportDialog(final Shell shell, final Object[] tree){
		super(shell);
		rnn = new ArrayList<Rechnung>(tree.length);
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
							//201512211302js: Rechnungen sollten nicht doppelt im Verarbeitungsergebnis auftreten,
							//nur weil aufgeklappt und dann bis zu 3x etwas vom gleichen Patienten/Fall/Rechnung markiert war.
							if (!rnn.contains(rn)) { //deshalb prüfen, ob die rechnung schon drin ist, bevor sie hinzugefügt wird.
								rnn.add(rn);
							}
						}
					}
				}
			}
		}	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		//201512211432js: Siehe auch Rechnungsdrucker.java public class RechnungsDrucker.createSettingsControl()
		//TODO: Auf Konstante umstellen, dann braucht's allerdings den Austausch weiterer Module bei Installation!!!
		
		Group cSaveCopy = new Group(ret, SWT.NONE);
		cSaveCopy.setText(String.format(Messages.RnActions_exportSaveHelp, RnListExportFileName));
		cSaveCopy.setLayout(new GridLayout(2, false));
		Button bSelectFile = new Button(cSaveCopy, SWT.PUSH);
		bSelectFile.setText(Messages.RnActions_exportListDirName);
		bSelectFile.setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
		bSelectFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DirectoryDialog ddlg = new DirectoryDialog(parent.getShell());
				RnListExportDirname = ddlg.open();
				if (RnListExportDirname == null) {
					SWTHelper.alert(Messages.RnActions_exportListDirNameMissingCaption,
						Messages.RnActions_exportListDirNameMissingText);
				} else {
					//ToDo: Umstellen auf externe Konstante!
					CoreHub.localCfg.set("rechnung/RnListExportDirname", RnListExportDirname);
					tDirName.setText(RnListExportDirname);
				}
			}
		});
		tDirName = new Text(cSaveCopy, SWT.BORDER | SWT.READ_ONLY);
		tDirName.setText(CoreHub.localCfg.get("rechnung/RnListExportDirname", "")); //$NON-NLS-1$
		tDirName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(Messages.RnActions_billsList);
		setTitle(Messages.RnActions_exportListCaption);
		setMessage(Messages.RnActions_exportListMessage);
		getShell().setSize(900, 700);
		SWTHelper.center(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			getShell());
	}
	
	@Override
	protected void okPressed(){
		super.okPressed();
		if (CoreHub.localCfg.get("rechnung/RnListExportDirname_bSaveFileAs", true))
			CSVWriteTable();
	}
	
	public void save(){}
	
	public boolean saveAs(){
		return false;
	}
	
	public void CSVWriteTable(){
		String pathToSave = RnListExportDirname + "/" + RnListExportFileName;
		CSVWriter csv = null;
		int nrLines = 0;
		try {
			csv = new CSVWriter(new FileWriter(pathToSave));
			// @formatter:off
			String[] header = new String[] {
				"Aktion?", // line 0 
				"Re.Nr", // line 1
				"Re.DatumRn", // line 2
				"Re.DatumVon", // line 3
				"Re.DatumBis", // line 4
				"Re.Garant", // line 5
				"Re.Total", // line 6
				"Re.Offen", // line 7
				"Re.StatusLastUpdate", // line 8
				"Re.Status", // line 9
				"Re.StatusIsActive", // line 10
				"Re.StatusText", // line 11
				"Re.StatusChanges", // line 12
				"Re.Rejecteds", // line 13
				"Re.Outputs", // line 14
				"Re.Payments", // line 15
				"Fall.AbrSystem", // line 16
				"Fall.Bezeichnung", // line 17
				"Fall.Grund", // line 18
				"Pat.Nr", // line 10
				"Pat.Name", // line 20
				"Pat.Vorname", // line 21
				"Pat.GebDat", // line 22
				"Pat.LztKonsDat", // line 23
				"Pat.Balance", // line 24
				"Pat.GetAccountExcess", // line 25
				"Pat.BillSummary.Total.", // line 26
				"Pat.BillSummary.Paid", // line 27
				"Pat.BillSummary.Open" // line 28
			};
			// @formatter:on
			log.debug("csv export started for {} with {} fields for {} invoices", pathToSave, header.length, rnn.size());
			csv.writeNext(header);
			nrLines++;
			int i;
			for (i = 0; i < rnn.size(); i++) {
				Rechnung rn = rnn.get(i);
				Fall fall = rn.getFall();
				Patient p = fall.getPatient();
				String[] line = new String[header.length];
				line[0] = ""; //201512210402js: Leere Spalte zum Eintragen der gewünschten Aktion.
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
				SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yyyy");
				String dateText = df2.format(date);
				line[8] = dateText.toString();
				int st = rn.getStatus();
				line[9] = Integer.toString(st);
				if (RnStatus.isActive(st)) {
					line[10] = "True";
				} else {
					line[10] = "False";
				}
				line[11] = RnStatus.getStatusText(st);
				// 201512210310js: New: produce 4 fields, each with multiline content.
				List<String> statuschgs = rn.getTrace(Rechnung.STATUS_CHANGED);
				String a = statuschgs.toString();
				if (a != null && a.length() > 1) {
					//Die Uhrzeiten rauswerfen:
					a = a.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
					//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
					a = a.replaceAll(", ", "\n");
					//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
					line[12] = a.substring(1, a.length() - 1);
				}
				if (rn.getStatus() == RnStatus.FEHLERHAFT) {
					List<String> rejects = rn.getTrace(Rechnung.REJECTED);
					String rnStatus = rejects.toString();
					if (rnStatus != null && rnStatus.length() > 1) {
						//Die Uhrzeiten rauswerfen:
						rnStatus =
							rnStatus.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
						//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
						rnStatus = rnStatus.replaceAll(", ", "\n");
						//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
						line[13] = rnStatus.substring(1, rnStatus.length() - 1);
					}
				}
				List<String> outputs = rn.getTrace(Rechnung.OUTPUT);
				String rnOutput = outputs.toString();
				if (rnOutput != null && rnOutput.length() > 1) {
					//Die Uhrzeiten rauswerfen:
					rnOutput = rnOutput.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
					//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
					rnOutput = rnOutput.replaceAll(", ", "\n");
					//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
					line[14] = rnOutput.substring(1, rnOutput.length() - 1);
				}
				List<String> payments = rn.getTrace(Rechnung.PAYMENT);
				String rnPayment = payments.toString();
				if (rnPayment != null && rnPayment.length() > 1) {
					//Die Uhrzeiten rauswerfen:
					rnPayment = rnPayment.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
					//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
					rnPayment = rnPayment.replaceAll(", ", "\n");
					//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
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
				// TODO: Ich möcht aber wissen, ob p (dürfte eigentlich nie der Fall sein) oder nk schuld sind, wenn nichts rauskommt.
				// TODO: Na ja, eigentlich würd ich noch lieber wissen, WARUM da manchmal nichts rauskommt, obwohl eine kons sicher vhd ist.
				String lkDatum = "p==null";
				if (p != null) {
					Konsultation lk = p.getLetzteKons(false);
					if (lk != null) {
						lkDatum = (lk.getDatum());
					} else {
						lkDatum = "lk==null";
					}
				}
				line[23] = lkDatum;
				line[24] = p.getBalance(); //returns: String
				line[25] = p.getAccountExcess().toString(); //returns: Money
				//201512210146js: Das Folgende ist aus BillSummary - dort wird dafür keine Funktion bereitgestellt,
				// TODO: Prüfen, ob das eine Redundanz DORT und HIER ist vs. obenn erwähnter getKontostand(), getAccountExcess() etc.
				// maybe called from foreign thread
				String totalText = ""; //$NON-NLS-1$
				String paidText = ""; //$NON-NLS-1$
				String openText = ""; //$NON-NLS-1$
				// Davon, dass p != null ist, darf man eigentlich ausgehen, da ja Rechnungen zu p gehören etc.
				if (p != null) {
					Money total = new Money(0);
					Money paid = new Money(0);
					List<Rechnung> rechnungen = p.getRechnungen();
					for (Rechnung rechnung : rechnungen) {
						// don't consider canceled bills
						if (rechnung.getStatus() != RnStatus.STORNIERT) {
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
					log.error("cannot close csv exporter", e);
				}
			}
		}
	}
}