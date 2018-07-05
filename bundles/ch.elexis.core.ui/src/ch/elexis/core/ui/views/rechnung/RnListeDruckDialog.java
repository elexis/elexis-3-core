package ch.elexis.core.ui.views.rechnung;

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_LIST;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.Tree;

public class RnListeDruckDialog extends TitleAreaDialog implements ICallback {
	ArrayList<Rechnung> rnn = new ArrayList<Rechnung>();
	private TextContainer text;
	
	public RnListeDruckDialog(final Shell shell, final Object[] tree){
		super(shell);
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
							rnn.add(rn);
							// Rechnungen sollten nicht doppelt im Verarbeitungsergebnis auftreten,
							// nur weil aufgeklappt und dann bis zu 3x etwas vom gleichen Patienten/Fall/Rechnung markiert war.
							// deshalb prüfen, ob die rechnung schon drin ist, bevor sie hinzugefügt wird.
							if (!rnn.contains(rn)) {
								rnn.add(rn);
							}
						}
					}
				}
			}
		}
	}
	
	public RnListeDruckDialog(final Shell shell, List<Rechnung> invoiceSelections){
		super(shell);
		rnn.addAll(invoiceSelections);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		text = new TextContainer(getShell());
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		text.getPlugin().createContainer(ret, this);
		text.getPlugin().showMenu(false);
		text.getPlugin().showToolbar(false);
		text.createFromTemplateName(null, TT_LIST, Brief.UNKNOWN, CoreHub.actUser,
			Messages.RnActions_bills); //$NON-NLS-1$ //$NON-NLS-2$
		text.getPlugin().insertText("[Titel]", //$NON-NLS-1$
			Messages.RnActions_billsListPrintetAt + new TimeTool().toString(TimeTool.DATE_GER)
				+ "\n", //$NON-NLS-1$ //$NON-NLS-2$
			SWT.CENTER);
		String[][] table = new String[rnn.size() + 1][];
		Money sum = new Money();
		int i;
		for (i = 0; i < rnn.size(); i++) {
			Rechnung rn = rnn.get(i);
			table[i] = new String[3];
			StringBuilder sb = new StringBuilder();
			Fall fall = rn.getFall();
			Patient p = fall.getPatient();
			table[i][0] = rn.getNr();
			sb.append(p.getLabel()).append(" - ").append(fall.getLabel()); //$NON-NLS-1$
			table[i][1] = sb.toString();
			Money betrag = rn.getBetrag();
			sum.addMoney(betrag);
			table[i][2] = betrag.getAmountAsString();
		}
		table[i] = new String[3];
		table[i][0] = ""; //$NON-NLS-1$
		table[i][1] = Messages.RnActions_sum; //$NON-NLS-1$
		table[i][2] = sum.getAmountAsString();
		text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9); //$NON-NLS-1$
		text.getPlugin().insertTable("[Liste]", 0, table, new int[] { //$NON-NLS-1$
			10, 80, 10
		});
		text.getPlugin().showMenu(true);
		text.getPlugin().showToolbar(true);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(Messages.RnActions_billsList); //$NON-NLS-1$
		setTitle(Messages.RnActions_printListCaption); //$NON-NLS-1$
		setMessage(Messages.RnActions_printListMessage); //$NON-NLS-1$
		getShell().setSize(900, 700);
		SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(),
			getShell());
	}
	
	@Override
	protected void okPressed(){
		super.okPressed();
	}
	
	public void save(){
		// TODO Auto-generated method stub
		
	}
	
	public boolean saveAs(){
		// TODO Auto-generated method stub
		return false;
	}
}
