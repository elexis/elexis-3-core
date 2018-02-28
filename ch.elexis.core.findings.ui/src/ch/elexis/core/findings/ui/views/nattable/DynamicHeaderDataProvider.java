package ch.elexis.core.findings.ui.views.nattable;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ch.elexis.core.findings.ICoding;

public class DynamicHeaderDataProvider
		implements IDataProvider {
	
	private List<ICoding> shownCodings;
	
	@Override
	public int getColumnCount(){
		return shownCodings.size();
	}
	
	public void setShownCodings(List<ICoding> showCodings){
		this.shownCodings = showCodings;
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		if (columnIndex >= 0 && columnIndex <= shownCodings.size()) {
			return shownCodings.get(columnIndex).getDisplay();
		}
		return null;
	}
	
	@Override
	public int getRowCount(){
		return 1;
	}
	
	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue){
		throw new UnsupportedOperationException();
	};
}
