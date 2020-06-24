package ch.elexis.core.findings.ui.views.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DynamicCodingHeaderDataProvider
		implements IDataProvider {
	
	private DynamicDataProvider dataProvider;
	
	public DynamicCodingHeaderDataProvider(DynamicDataProvider dataProvider){
		this.dataProvider = dataProvider;
	}
	
	@Override
	public int getColumnCount(){
		return dataProvider.getShownCodings().size();
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		if (columnIndex >= 0 && columnIndex <= dataProvider.getShownCodings().size()) {
			return dataProvider.getShownCodings().get(columnIndex).getDisplay();
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
