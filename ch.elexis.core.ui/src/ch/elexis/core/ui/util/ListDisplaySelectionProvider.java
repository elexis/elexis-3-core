package ch.elexis.core.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionListener;

import ch.elexis.data.Prescription;

public class ListDisplaySelectionProvider implements ISelectionProvider {
	private ListDisplay<Prescription> listDisplay;
	
	public ListDisplaySelectionProvider(ListDisplay<Prescription> listDisplay){
		this.listDisplay = listDisplay;
	}
	
	public void addListener(final SelectionListener l){
		listDisplay.addListener(l);
	}
	
	@Override
	public ISelection getSelection(){
		if (listDisplay.getSelection() == null) {
			return new StructuredSelection();
		}
		return new StructuredSelection(listDisplay.getSelection());
	}
	
	public void removeListener(final SelectionListener l){
		listDisplay.removeListener(l);
	}
	
	@Override
	public void setSelection(ISelection selection){
		IStructuredSelection structSel = (IStructuredSelection) selection;
		Prescription prescription = (Prescription) structSel.getFirstElement();
		listDisplay.setSelection(prescription);
	}
	
	/**
	 * use addListener instead
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){}
	
	/**
	 * use removeListener instead
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){}
	
}
