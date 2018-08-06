package ch.elexis.core.model.mixin;

import java.util.Hashtable;
import java.util.Map;

import ch.elexis.core.jpa.entities.EntityWithExtInfo;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.ModelUtil;

public class ExtInfoHandler {
	
	private AbstractIdModelAdapter<? extends EntityWithExtInfo> withExtInfo;
	private Map<Object, Object> extInfo;
	
	public ExtInfoHandler(AbstractIdModelAdapter<? extends EntityWithExtInfo> withExtInfo){
		this.withExtInfo = withExtInfo;
	}
	
	public Object getExtInfo(Object key){
		if (extInfo == null) {
			byte[] bytes = withExtInfo.getEntity().getExtInfo();
			if (bytes != null) {
				extInfo = ModelUtil.extInfoFromBytes(bytes);
			}
		}
		return extInfo != null ? extInfo.get(key) : null;
	}
	
	public void setExtInfo(Object key, Object value){
		if (extInfo == null) {
			byte[] bytes = withExtInfo.getEntity().getExtInfo();
			if (bytes != null) {
				extInfo = ModelUtil.extInfoFromBytes(bytes);
			} else {
				extInfo = new Hashtable<>();
			}
		}
		if (value == null) {
			extInfo.remove(key);
		} else {
			extInfo.put(key, value);
		}
		withExtInfo.getEntity().setExtInfo(ModelUtil.extInfoToBytes(extInfo));
	}
}
