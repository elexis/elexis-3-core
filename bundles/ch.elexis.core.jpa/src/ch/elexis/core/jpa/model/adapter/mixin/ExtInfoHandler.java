package ch.elexis.core.jpa.model.adapter.mixin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import ch.elexis.core.jpa.entities.EntityWithExtInfo;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.util.JpaModelUtil;

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
				extInfo = JpaModelUtil.extInfoFromBytes(bytes);
			}
		}
		return extInfo != null ? extInfo.get(key) : null;
	}
	
	public void setExtInfo(Object key, Object value){
		if (extInfo == null) {
			byte[] bytes = withExtInfo.getEntity().getExtInfo();
			if (bytes != null) {
				extInfo = JpaModelUtil.extInfoFromBytes(bytes);
			} else {
				extInfo = new Hashtable<>();
			}
		}
		if (value == null) {
			extInfo.remove(key);
		} else {
			extInfo.put(key, value);
		}
		withExtInfo.getEntity().setExtInfo(JpaModelUtil.extInfoToBytes(extInfo));
	}
	
	/**
	 * @return modifications to this map are not persisted. Use {@link #setExtInfo(Object, Object)}
	 *         to handle persistent sets
	 */
	public Map<Object, Object> getMap(){
		return (extInfo != null) ? new HashMap<>(extInfo) : Collections.emptyMap();
	}
}
