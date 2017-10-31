package ch.elexis.core.findings.ui.views.nattable;

import java.time.format.DateTimeFormatter;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DynamicRowDataProvider
		implements IDataProvider {
	
	private DynamicDataProvider dataProvider;
	
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	public DynamicRowDataProvider(DynamicDataProvider dataProvider){
		this.dataProvider = dataProvider;
	}
	
	@Override
	public int getColumnCount(){
		return 1;
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		if (rowIndex >= 0 && rowIndex <= dataProvider.getShownDates().size()) {
			return dataProvider.getShownDates().get(rowIndex).format(formatter);
		}
		return null;
	}
	
	@Override
	public int getRowCount(){
		return dataProvider.getShownDates().size();
	}
	
	@Override
	public void setDataValue(int arg0, int arg1, Object arg2){
		// TODO Auto-generated method stub
		
	}
}
