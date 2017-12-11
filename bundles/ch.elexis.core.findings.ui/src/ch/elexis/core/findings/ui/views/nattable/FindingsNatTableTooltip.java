package ch.elexis.core.findings.ui.views.nattable;

import java.util.List;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

import ch.elexis.core.findings.IFinding;

public class FindingsNatTableTooltip extends DefaultToolTip {
	
	private NatTable natTable;
	private DynamicDataProvider dataProvider;
	
	public FindingsNatTableTooltip(NatTable natTable, DynamicDataProvider dataProvider){
		super(natTable, ToolTip.NO_RECREATE, false);
		this.natTable = natTable;
		this.dataProvider = dataProvider;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.ToolTip#getToolTipArea(org.eclipse.swt.widgets.Event)
	 * 
	 * Implementation here means the tooltip is not redrawn unless mouse hover moves outside of the
	 * current cell (the combination of ToolTip.NO_RECREATE style and override of this method).
	 */
	protected Object getToolTipArea(Event event){
		int col = natTable.getColumnPositionByX(event.x);
		int row = natTable.getRowPositionByY(event.y);
		
		return new Point(col, row);
	}
	
	@SuppressWarnings("unchecked")
	protected String getText(Event event){
		int col = natTable.getColumnPositionByX(event.x);
		int row = natTable.getRowPositionByY(event.y);
		
		Object value = dataProvider.getDataValue(natTable.getColumnIndexByPosition(col),
			natTable.getRowIndexByPosition(row));
		if (value instanceof List) {
			List<IFinding> findings = (List<IFinding>) value;
			StringBuilder sb = new StringBuilder();
			for (IFinding iFinding : findings) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(iFinding.getText().orElse(""));
			}
			return sb.toString();
		}
		return null;
	}
	
	@Override
	protected boolean shouldCreateToolTip(Event event){
		int col = natTable.getColumnPositionByX(event.x);
		int row = natTable.getRowPositionByY(event.y);
		
		Object value = dataProvider.getDataValue(natTable.getColumnIndexByPosition(col),
			natTable.getRowIndexByPosition(row));
		return value instanceof List;
	}
}
