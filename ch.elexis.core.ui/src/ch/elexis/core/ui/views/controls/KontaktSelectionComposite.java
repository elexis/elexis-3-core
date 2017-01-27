package ch.elexis.core.ui.views.controls;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Kontakt;

public class KontaktSelectionComposite extends Composite implements ISelectionProvider {
	
	private ListenerList selectionListeners = new ListenerList();
	
	protected Button selectButton;
	protected Label selectLabel;
	
	protected Kontakt kontakt;
	
	public KontaktSelectionComposite(Composite parent, int style){
		super(parent, style);
		
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(2, false));
		
		selectLabel = new Label(this, SWT.NONE);
		selectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		selectButton = new Button(this, SWT.NONE);
		selectButton.setText("..."); //$NON-NLS-1$
		selectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktSelektor ksl = getKontaktSelector();
				if (ksl.open() == Dialog.OK) {
					kontakt = (Kontakt) ksl.getSelection();
					setKontakt(kontakt);
					callSelectionListeners();
				}
			}
		});
	}
	
	protected KontaktSelektor getKontaktSelector(){
		return new KontaktSelektor(getShell(), Kontakt.class,
			Messages.KontaktSelectionComposite_title, Messages.KontaktSelectionComposite_message,
			Kontakt.DEFAULT_SORT);
	}
	
	public void setKontakt(Kontakt kontakt){
		this.kontakt = kontakt;
		if (kontakt != null) {
			selectLabel.setText(kontakt.getLabel());
		} else {
			selectLabel.setText(""); //$NON-NLS-1$
		}
		getParent().layout();
	}
	
	public Kontakt getKontakt(){
		return kontakt;
	}
	
	private void callSelectionListeners(){
		Object[] listeners = selectionListeners.getListeners();
		if (listeners != null && listeners.length > 0) {
			for (Object object : listeners) {
				((ISelectionChangedListener) object)
					.selectionChanged(new SelectionChangedEvent(this, getSelection()));
			}
		}
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.add(listener);
	}
	
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.remove(listener);
	}
	
	@Override
	public ISelection getSelection(){
		if (kontakt != null) {
			return new StructuredSelection(kontakt);
		}
		return StructuredSelection.EMPTY;
	}
	
	@Override
	public void setSelection(ISelection selection){
		if (selection instanceof IStructuredSelection) {
			if (!selection.isEmpty()) {
				setKontakt((Kontakt) ((IStructuredSelection) selection).getFirstElement());
			} else {
				setKontakt(null);
			}
		}
	}
}
