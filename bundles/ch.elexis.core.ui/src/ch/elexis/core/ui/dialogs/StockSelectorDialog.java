package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.IStock;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.data.Mandant;
import ch.elexis.data.Stock;

public class StockSelectorDialog extends ListDialog {
	
	private IStock onlyOneStock = null;
	
	public StockSelectorDialog(Shell parent, boolean includeCommissioningSystems){
		super(parent);
		List<IStock> allStocks = StockServiceHolder.get().getAllStocks(includeCommissioningSystems);
		if (allStocks.size() == 1) {
			onlyOneStock = allStocks.get(0);
		}
		setInput(allStocks);
		setContentProvider(ArrayContentProvider.getInstance());
		setLabelProvider(new StockLabelProvider());
		setTitle("Bitte Lager auswählen");
	}
	
	private class StockLabelProvider extends LabelProvider implements IColorProvider {
		
		@Override
		public String getText(Object element){
			Stock s = (Stock) element;
			return s.getLabel();
		}
		
		@Override
		public Color getForeground(Object element){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Color getBackground(Object element){
			Stock se = (Stock) element;
			Mandant owner = se.getOwner();
			if (owner != null) {
				return UiMandant.getColorForMandator(owner);
			}
			return null;
		}
	}
	
	@Override
	public int open(){
		if (onlyOneStock != null) {
			return Dialog.OK;
		}
		return super.open();
	}
	
	@Override
	public Object[] getResult(){
		if (onlyOneStock != null) {
			return new Object[] {
				onlyOneStock
			};
		}
		return super.getResult();
	}
	
}
