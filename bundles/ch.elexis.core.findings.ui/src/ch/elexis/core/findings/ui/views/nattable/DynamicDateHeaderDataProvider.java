package ch.elexis.core.findings.ui.views.nattable;

import java.time.format.DateTimeFormatter;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DynamicDateHeaderDataProvider
		implements IDataProvider {
	
	private DynamicDataProvider dataProvider;
	
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	public DynamicDateHeaderDataProvider(DynamicDataProvider dataProvider){
		this.dataProvider = dataProvider;
	}
	
	@Override
	public int getColumnCount(){
		return dataProvider.getShownDates().size();
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		if (columnIndex >= 0 && columnIndex <= dataProvider.getShownDates().size()) {
			return dataProvider.getShownDates().get(columnIndex).format(formatter);
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
