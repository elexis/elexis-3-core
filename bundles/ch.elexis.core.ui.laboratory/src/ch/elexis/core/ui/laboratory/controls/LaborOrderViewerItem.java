package ch.elexis.core.ui.laboratory.controls;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.types.LabItemTyp;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class LaborOrderViewerItem {
	
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	
	private StructuredViewer viewer;
	private LabOrder order;
	
	private String labItemLabel;
	private String labResultString;
	
	private String itemPrio;
	
	public volatile boolean resolved;
	
	public volatile boolean resolving;
	
	public LaborOrderViewerItem(StructuredViewer viewer, LabOrder order){
		this.viewer = viewer;
		this.order = order;
	}
	
	public LabResult getLabResult(){
		return (LabResult) order.getLabResult();
	}
	
	public Optional<String> getLabResultString(){
		if (hasLabResult()) {
			if (labResultString == null) {
				if (!resolved && !resolving) {
					resolving = true;
					executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
				}
			}
			return labResultString != null ? Optional.of(labResultString) : Optional.of("...");
		}
		return Optional.empty();
	}
	
	public void refreshResultString(){
		labResultString = null;
		resolved = false;
		viewer.refresh(this);
	}
	
	private boolean hasLabResult(){
		return getLabResult() != null;
	}
	
	@SuppressWarnings("unchecked")
	public void deleteOrder(){
		order.delete();
		((List<LaborOrderViewerItem>) viewer.getInput()).remove(this);
		viewer.refresh();
	}
	
	public Optional<String> getLabItemPrio(){
		if (hasLabItem()) {
			if (itemPrio == null) {
				itemPrio = order.getLabItem().getPrio();
			}
			return Optional.of(itemPrio);
		}
		return Optional.empty();
	}
	
	public TimeTool getObservationTime(){
		return order.getObservationTime();
	}
	
	public void setObservationTimeWithResults(TimeTool date){
		order.setObservationTimeWithResults(date);
		viewer.refresh(this);
	}
	
	public LabResult createResult(){
		LabResult ret = order.createResult();
		viewer.refresh(this);
		return ret;
	}
	
	public LabResult createResult(Kontakt origin){
		LabResult ret = order.createResult(origin);
		viewer.refresh(this);
		return ret;
	}
	
	public void setState(State state){
		order.setState(state);
		viewer.refresh(this);
	}
	
	public boolean hasLabItem(){
		return order.getLabItem() != null;
	}
	
	public LabItemTyp getLabItemTyp(){
		return order.getLabItem().getTyp();
	}
	
	public State getState(){
		return order.getState();
	}
	
	public TimeTool getTime(){
		return order.getTime();
	}
	
	public Optional<String> getOrderId(){
		return Optional.ofNullable(order.get(LabOrder.FLD_ORDERID));
	}
	
	public Optional<String> getOrderGroupName(){
		return Optional.ofNullable(order.get(LabOrder.FLD_GROUPNAME));
	}
	
	public Optional<String> getLabItemLabel(){
		if (hasLabItem()) {
			if (labItemLabel == null) {
				if (!resolved && !resolving) {
					resolving = true;
					executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
				}
			}
			return labItemLabel != null ? Optional.of(labItemLabel) : Optional.of("...");
		}
		return Optional.empty();
	}
	
	private static class ResolveLazyFieldsRunnable implements Runnable {
		private LaborOrderViewerItem item;
		private StructuredViewer viewer;
		
		public ResolveLazyFieldsRunnable(StructuredViewer viewer, LaborOrderViewerItem item){
			this.item = item;
			this.viewer = viewer;
		}
		
		@Override
		public void run(){
			resolveLabItemLabel();
			resolveLabResultString();
			item.resolved = true;
			item.resolving = false;
			updateViewer();
		}
		
		private void resolveLabItemLabel(){
			LabItem labItem = item.order.getLabItem();
			if (labItem != null) {
				item.labItemLabel = labItem.getLabel();
			}
		}
		
		private void resolveLabResultString(){
			LabResult labResult = (LabResult) item.order.getLabResult();
			if (labResult != null) {
				item.labResultString = getNonEmptyResultString(labResult);
			}
		}
		
		private String getNonEmptyResultString(LabResult labResult){
			String result = labResult.getResult();
			if (result != null && result.isEmpty()) {
				return "?"; //$NON-NLS-1$
			}
			if (labResult.getItem().getTyp() == LabItemTyp.TEXT) {
				if (labResult.isLongText()) {
					result = labResult.getComment();
					if (result.length() > 20) {
						result = result.substring(0, 20);
					}
				}
			}
			return result;
		}
		
		private void updateViewer(){
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				viewer.getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run(){
						if (control.isVisible()) {
							viewer.refresh(item, true);
						}
					}
				});
			}
		}
	}
}
