package ch.elexis.core.findings.ui.views.nattable;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class LabelDataProvider implements IDataProvider {
	
	private IDataProvider dataProvider;
	private LabelProvider labelProvider;
	
	public LabelDataProvider(IDataProvider dataProvider, LabelProvider labelProvider){
		this.dataProvider = dataProvider;
		this.labelProvider = labelProvider;
	}
	
	@Override
	public int getColumnCount(){
		return dataProvider.getColumnCount();
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		Object data = dataProvider.getDataValue(columnIndex, rowIndex);
		if (data != null) {
			return labelProvider.getText(data);
		}
		return "";
	}
	
	@Override
	public int getRowCount(){
		return dataProvider.getRowCount();
	}
	
	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue){
		dataProvider.setDataValue(columnIndex, rowIndex, newValue);
	}
}
