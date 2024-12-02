package ch.elexis.core.ui.e4.dialog;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class MandantSelectorDialog extends TitleAreaDialog {
	private List<IMandator> mandatorsList;
	private ListViewer uiList;

	private List<IMandator> selMandators;
	private String idList;
	private boolean multi;
	private boolean noEmpty;
	private boolean nonActive;

	public MandantSelectorDialog(Shell parentShell, boolean multi, boolean noEmpty, boolean nonActive, String idList) {
		super(parentShell);
		this.multi = multi;
		this.noEmpty = noEmpty;
		this.nonActive = nonActive;
		this.idList = idList;
	}

	public MandantSelectorDialog(Shell parentShell, boolean multi) {
		this(parentShell, multi, true, false, null);
	}

	public MandantSelectorDialog(Shell parentShell, String idList) {
		this(parentShell, true, false, true, idList);
	}

	@Override
	public Control createDialogArea(final Composite parent) {

		Composite radioComposite = new Composite(parent, SWT.NONE);
		radioComposite.setLayout(new GridLayout(2, false));
		Button btnAlle = new Button(radioComposite, SWT.NONE);
		btnAlle.setText("alle");
		btnAlle.addListener(SWT.Selection, e -> {
			uiList.getList().selectAll();
		});
		Button btnKeine = new Button(radioComposite, SWT.NONE);
		btnKeine.setText("keine");
		btnKeine.addListener(SWT.Selection, e -> {
			uiList.getList().deselectAll();
		});

		if (multi) {
			setTitle("Mandanten Auswahl");
			StringBuilder msg = new StringBuilder("Bitte wählen Sie die Mandanten aus.");
			if (noEmpty) {
				msg.append("\nKeine Auswahl selektiert den aktiven Mandanten");
			}
			setMessage(msg.toString());
			uiList = new ListViewer(parent, SWT.BORDER | SWT.MULTI);
		} else {
			setTitle("Mandant ändern");
			setMessage("Bitte wählen Sie einen Mandanten");
			uiList = new ListViewer(parent, SWT.BORDER | SWT.SINGLE);
		}
		mandatorsList = getMandators();

		uiList.setContentProvider(ArrayContentProvider.getInstance());
		uiList.setInput(mandatorsList);
		uiList.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		uiList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IMandator) element).getLabel();
			}
		});

		setSelected(idList);

		return uiList.getList();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		selMandators = (List<IMandator>) uiList.getStructuredSelection().toList();
		super.okPressed();
	}

	public IMandator getSelectedMandator() {
		if (selMandators.isEmpty()) {
			if (noEmpty) {
				return ContextServiceHolder.getActiveMandatorOrNull();
			}
			return null;
		}
		return selMandators.get(0);
	}
	
	public List<IMandator> getSelectedMandators() {
		return selMandators;
	}

	private List<IMandator> getMandators() {
		List<IMandator> mandators = CoreModelServiceHolder.get().getQuery(IMandator.class).execute();

		List<IMandator> list = mandators.stream().filter(m -> (nonActive || isActive(m)) && isMandator(m)).map(m -> m)
				.collect(Collectors.toList());
		list.sort(Comparator.comparing(IMandator::getLabel));

		return list;
	}

	public void setSelected(String idList) {
		if (idList == null || idList.isBlank()) {
			return;
		}
		uiList.setSelection(
				new StructuredSelection(
						mandatorsList.stream().filter(m -> idList.contains(m.getId())).toList()));
	}

	private boolean isMandator(IMandator mandator) {
		return mandator != null && mandator.isMandator();
	}

	private boolean isActive(IMandator mandator) {
		return mandator != null && mandator.isActive();
	}
}
