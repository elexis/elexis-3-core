package ch.elexis.core.findings.ui.views.nattable;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ch.elexis.core.findings.IFinding;

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
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		Object data = dataProvider.getDataValue(columnIndex, rowIndex);
		if (data instanceof List) {
			StringBuilder sb = new StringBuilder();
			List<IFinding> findings = (List<IFinding>) data;
			for (IFinding iFinding : findings) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(labelProvider.getText(iFinding));
			}
			return sb.toString();
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
