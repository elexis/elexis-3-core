package ch.elexis.core.ui.e4.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class MandantSelectorDialog extends TitleAreaDialog {
	private List<IMandator> mandatorsList;
	private ListViewer uiList;
	private IMandator selMandator;

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
		selMandator = ContextServiceHolder.getActiveMandatorOrNull();
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

	@Override
	protected void okPressed() {
		Object[] selectedMandators = uiList.getStructuredSelection().toArray();
		selMandators = new ArrayList<IMandator>();

		if (selectedMandators.length > 0) {
			for (Object element : selectedMandators) {
				if (element instanceof IMandator) {
					selMandators.add((IMandator) element);
				}
			}
		}

		if (!multi && !selMandators.isEmpty()) {
			selMandator = selMandators.get(0);
		}

		super.okPressed();
	}

	public IMandator getSelectedMandator() {
		return selMandator;
	}
	
	public List<IMandator> getSelectedMandators() {
		return selMandators;
	}

	private List<IMandator> getMandators() {
		IQuery<IMandator> query = CoreModelServiceHolder.get().getQuery(IMandator.class);
		List<IMandator> mandators = query.execute();

		List<IMandator> list = mandators.stream().filter(m -> (nonActive || isActive(m)) && isMandator(m)).map(m -> m)
				.collect(Collectors.toList());
		list.sort(Comparator.comparing(IMandator::getLabel));

		return list;
	}

	public void setSelected(String idList) {
		if (idList.isBlank()) {
			return;
		}

		Map<String, IMandator> mandatorMap = mandatorsList.stream().collect(Collectors.toMap(IMandator::getId, m -> m));

		Arrays.stream(idList.split(",")) //$NON-NLS-1$
				.map(String::trim).filter(mandatorMap::containsKey).forEach(id -> {
					IMandator mandator = mandatorMap.get(id);
					int index = mandatorsList.indexOf(mandator);
					if (index != -1)
						uiList.getList().select(index);
				});
	}

	private boolean isMandator(IMandator mandator) {
		return mandator != null && mandator.isMandator();
	}

	private boolean isActive(IMandator mandator) {
		return mandator != null && mandator.isActive();
	}
}
