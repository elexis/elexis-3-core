package ch.elexis.core.ui.util.viewers;

import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

public abstract class AbstractCommonViewerContentProvider implements ICommonViewerContentProvider {
	
	private CommonViewer commonViewer;
	
	protected Map<String, String> fieldFilterValues;
	protected String fieldOrderBy;
	protected ORDER fieldOrder = ORDER.DESC;
	protected String[] orderFields;
	
	public AbstractCommonViewerContentProvider(CommonViewer commonViewer){
		this.commonViewer = commonViewer;
	}
	
	@Override
	public void changed(HashMap<String, String> values){
		if (commonViewer.getConfigurer().getControlFieldProvider().isEmpty()) {
			commonViewer.notify(CommonViewer.Message.empty);
		} else {
			commonViewer.notify(CommonViewer.Message.notempty);
		}
		fieldFilterValues = values;
		commonViewer.notify(CommonViewer.Message.update);
	}
	
	@Override
	public void reorder(String field){
		if (fieldOrderBy != null && fieldOrderBy.equals(field)) {
			fieldOrder = fieldOrder == ORDER.DESC ? ORDER.ASC : ORDER.DESC;
		} else {
			fieldOrder = ORDER.DESC;
		}
		fieldOrderBy = field;
		commonViewer.notify(CommonViewer.Message.update);
	}
	
	public void setOrderFields(String... name){
		orderFields = name;
	}
	
	@Override
	public void selected(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startListening(){
		commonViewer.getConfigurer().controlFieldProvider.addChangeListener(this);
	}
	
	@Override
	public void stopListening(){
		commonViewer.getConfigurer().controlFieldProvider.removeChangeListener(this);
	}
}
