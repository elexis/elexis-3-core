package ch.elexis.core.ui.laboratory.views;

import static ch.elexis.core.ui.laboratory.LaboratoryTextTemplateRequirement.TT_LABORDERS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.Patient;

public class LabOrderView extends ViewPart implements ICallback {
	public static final String ID = "ch.elexis.core.ui.laboratory.LabOrderView"; //$NON-NLS-1$
	private static final String LABORDER_PLACEHOLDER = "[Verordnung]";
	
	private TextContainer text;
	private String[] headers;
	
	public LabOrderView(){
		headers = new String[] {
			Messages.LabOrderView_Order, Messages.LabOrderView_RefValue,
			Messages.LabOrderView_DateTime, Messages.LabOrderView_Value
		};
	}
	
	@Override
	public void createPartControl(Composite parent){
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
	}
	
	public boolean createLabOrderPrint(Patient pat, List<LabOrder> labOrders){
		Brief br = text.createFromTemplateName(Konsultation.getAktuelleKons(), TT_LABORDERS,
			Brief.LABOR, pat, null);
		// leave if template couldn't be created
		if (br == null) {
			return false;
		}
		
		if (!labOrders.isEmpty()) {
			String orderId = labOrders.get(0).get(LabOrder.FLD_ORDERID);
			text.getPlugin().insertText("[AuftragsNr]", orderId, SWT.LEFT);
		}
		
		// make textplugin insert labOrders as table
		String[][] content = populateLabOrderTable(labOrders, pat.getGeschlecht());
		boolean ret = text.getPlugin().insertTable(LABORDER_PLACEHOLDER, //$NON-NLS-1$
			ITextPlugin.FIRST_ROW_IS_HEADER, content, null);
		text.saveBrief(br, Brief.LABOR);
		return ret;
	}
	
	/**
	 * Creates an array with {@link LabOrder}s name, ref.value, date/time (empty -> to be filled
	 * out), result (empty -> to be filled out). First line is the header line.
	 * 
	 * @param labOrders
	 *            all LabOrders (only those of {@link State.ORDERED} will be considered)
	 * @param patientGender
	 *            used to load the correct reference value
	 * @return array of all relevant {@link LabOrder}s (incl. headers)
	 */
	private String[][] populateLabOrderTable(List<LabOrder> labOrders, String patientGender){
		LinkedList<String[]> usedRows = new LinkedList<String[]>();
		// add headers 
		usedRows.add(headers);
		
		// group labOrders and sort them from a-z
		Map<String, List<LabOrder>> groupMap = groupLabOrders(labOrders);
		ArrayList<String> keySet = new ArrayList<String>(groupMap.keySet());
		Collections.sort(keySet);
		
		for (String groupKey : keySet) {
			List<LabOrder> gLabOrders = groupMap.get(groupKey);
			// add group name
			usedRows.add(createRow(groupKey, "", ""));
			
			for (LabOrder labOrder : gLabOrders) {
				// only interested in those with status ORDERED
				if (labOrder.getState() == State.ORDERED) {
					LabItem labItem = labOrder.getLabItem();
					String ref = "";
					if (Patient.FEMALE.equals(patientGender)) {
						ref = labItem.getRefW();
					} else {
						ref = labItem.getRefM();
					}
					usedRows.add(createRow("\t" + labItem.getName(), ref, labItem.getEinheit()));
				}
			}
		}
		return usedRows.toArray(new String[0][]);
	}
	
	/**
	 * Create a row with 4 places inserting the passed values. Not given fields are field with ""
	 * 
	 * @param value
	 *            name [idx 0]
	 * @param ref
	 *            reference (male or female) if given [idx 1]
	 * @param unit
	 *            will only be displayed if reference is present [idx 1]
	 * @return Example: {@code new String[] "Kalium", "3.5-5.5 mmol/L", "", ""}
	 */
	private String[] createRow(String value, String ref, String unit){
		// init array filled with empty strings
		String[] row = new String[4];
		Arrays.fill(row, "");
		
		row[0] = value;
		// add ref with unit if given
		if (!ref.isEmpty()) {
			row[1] = ref + " " + unit;
		}
		return row;
	}
	
	/**
	 * groups the received LabOrders
	 * 
	 * @param labOrders
	 *            list of all orders
	 * @return a map of groups (each group stores a list of belonging labOrders)
	 */
	private Map<String, List<LabOrder>> groupLabOrders(List<LabOrder> labOrders){
		Map<String, List<LabOrder>> gloMap = new HashMap<String, List<LabOrder>>();
		for (LabOrder labOrder : labOrders) {
			String group = labOrder.get(LabOrder.FLD_GROUPNAME);
			List<LabOrder> orders = gloMap.get(group);
			if (orders == null) {
				orders = new ArrayList<LabOrder>();
			}
			orders.add(labOrder);
			gloMap.put(group, orders);
		}
		return gloMap;
	}
	
	@Override
	public void setFocus(){}
	
	@Override
	public void save(){}
	
	@Override
	public boolean saveAs(){
		return false;
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
