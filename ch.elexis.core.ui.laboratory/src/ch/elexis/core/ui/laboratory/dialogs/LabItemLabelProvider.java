package ch.elexis.core.ui.laboratory.dialogs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.PersistentObject;

public class LabItemLabelProvider extends ColumnLabelProvider implements ILabelProvider {
	public enum ItemLabelFields {
			REFERENCES, KUERZEL, NAME, GROUP, UNIT
	}
	
	private HashMap<LabItem, String> cache = new HashMap<LabItem, String>();
	
	private static List<ItemLabelFields> defaultFields =
		Arrays.asList(ItemLabelFields.KUERZEL, ItemLabelFields.NAME, ItemLabelFields.GROUP,
			ItemLabelFields.UNIT, ItemLabelFields.REFERENCES);
	
	private List<ItemLabelFields> fields;
	private boolean createToolTip = false;
	
	public LabItemLabelProvider(boolean createToolTip){
		this(null, createToolTip);
	}
	
	public LabItemLabelProvider(List<ItemLabelFields> fields, boolean createToolTip){
		if (fields == null) {
			this.fields = defaultFields;
		} else {
			this.fields = fields;
		}
		this.createToolTip = createToolTip;
	}
	
	private StringBuilder sb = new StringBuilder();
	
	@Override
	public String getText(Object element){
		String ret = cache.get(element);
		if (ret == null) {
			sb.setLength(0);
			if (element instanceof LabItem) {
				((LabItem) element).get(true, LabItem.SHORTNAME, LabItem.TITLE, LabItem.GROUP,
					LabItem.UNIT);
					
				for (ItemLabelFields itemLabelField : fields) {
					if (sb.length() == 0) {
						sb.append(getItemLabelField(itemLabelField, (LabItem) element));
					} else {
						sb.append(", ")
							.append(getItemLabelField(itemLabelField, (LabItem) element));
					}
				}
			}
			ret = sb.toString();
			cache.put((LabItem) element, ret);
		}
		return ret;
	}
	
	private String getItemLabelField(ItemLabelFields itemLabelField, LabItem element){
		String ret = "";
		String[] values = ((LabItem) element).get(true, LabItem.SHORTNAME, LabItem.TITLE,
			LabItem.GROUP, LabItem.UNIT);
			
		switch (itemLabelField) {
		case KUERZEL:
			return values[0];
		case REFERENCES:
			String refW = shortenString((element).getRefW());
			String refM = shortenString((element).getRefM());
			return refW + " / " + refM;
		case GROUP:
			return values[2];
		case NAME:
			return values[1];
		case UNIT:
			return "[" + values[3] + "]";
		default:
			break;
		}
		return ret;
	}
	
	private String shortenString(String string){
		if (string.length() > 15) {
			return string.substring(0, 14) + "..."; //$NON-NLS-1$
		}
		return string;
	}
	
	@Override
	public String getToolTipText(Object element){
		if (createToolTip && element instanceof LabItem) {
			int results = 0;
			PreparedStatement ps = PersistentObject.getConnection().getPreparedStatement(
				"SELECT COUNT(*) AS results FROM LABORWERTE WHERE " + LabResult.ITEM_ID + "=?"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				ps.setString(1, ((LabItem) element).getId());
				if (ps.execute()) {
					ResultSet resultSet = ps.getResultSet();
					while (resultSet.next()) {
						results = resultSet.getInt("results"); //$NON-NLS-1$
					}
					resultSet.close();
				}
			} catch (SQLException e) {
				StatusManager.getManager().handle(
					new ElexisStatus(ElexisStatus.WARNING,
						"ch.elexis", //$NON-NLS-1$
						ElexisStatus.CODE_NOFEEDBACK,
						"Could not determine count of LabResult.", e)); //$NON-NLS-1$
			} finally {
				PersistentObject.getConnection().releasePreparedStatement(ps);
			}
			return String.format(Messages.MergeLabItemDialog_toolTipResultsCount, results);
		}
		return null;
	}
}