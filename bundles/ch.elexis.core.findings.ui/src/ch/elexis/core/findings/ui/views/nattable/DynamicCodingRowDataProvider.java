package ch.elexis.core.findings.ui.views.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DynamicCodingRowDataProvider implements IDataProvider {

	private DynamicDataProvider dataProvider;

	public DynamicCodingRowDataProvider(DynamicDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (rowIndex >= 0 && rowIndex <= dataProvider.getShownCodings().size()) {
			return dataProvider.getShownCodings().get(rowIndex).getDisplay();
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return dataProvider.getShownCodings().size();
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub

	}
}
