package ch.elexis.core.ui.views.controls;

import java.util.ArrayList;
import java.util.List;

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
	
	protected List<Kontakt> kontakt;
	protected boolean multi;
	
	public KontaktSelectionComposite(Composite parent, int style){
		super(parent, style);
		multi = (style & SWT.MULTI) > 0;
		kontakt = new ArrayList<>();
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
					Kontakt selected = (Kontakt) ksl.getSelection();
					setKontakt(selected);
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
	
	/**
	 * Set the current selected {@link Kontakt}. If style is SWT.MULTI, the {@link Kontakt} is added
	 * to the List of selected {@link Kontakt}.
	 * 
	 * @param kontakt
	 */
	public void setKontakt(Kontakt kontakt){
		if (multi) {
			this.kontakt.add(kontakt);
		} else {
			this.kontakt.clear();
			if (kontakt != null) {
				this.kontakt.add(kontakt);
			}
		}
		if (this.kontakt != null && !this.kontakt.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			this.kontakt.stream().forEach(k -> {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(k.getLabel());
			});
			selectLabel.setText(sb.toString());
		} else {
			selectLabel.setText(""); //$NON-NLS-1$
		}
		getParent().layout();
	}
	
	/**
	 * Get the selected {@link Kontakt}. Is SWT style is SWT.MULTI, the first {@link Kontakt} is
	 * returned. Use {@link KontaktSelectionComposite#getSelection()} to access the {@link List} of
	 * selected {@link Kontakt}.
	 * 
	 * @return
	 */
	public Kontakt getKontakt(){
		if (!kontakt.isEmpty()) {
			return kontakt.get(0);
		}
		return null;
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
