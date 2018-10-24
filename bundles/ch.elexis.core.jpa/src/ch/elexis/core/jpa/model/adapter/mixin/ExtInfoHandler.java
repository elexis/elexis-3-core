package ch.elexis.core.jpa.model.adapter.mixin;

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
	
	private void doLoadExtInfo(){
		if (extInfo == null) {
			byte[] bytes = withExtInfo.getEntity().getExtInfo();
			if (bytes != null) {
				extInfo = JpaModelUtil.extInfoFromBytes(bytes);
			} else {
				extInfo = new Hashtable<>();
			}
		}
	}
	
	public Object getExtInfo(Object key){
		doLoadExtInfo();
		
		return extInfo.get(key);
	}
	
	public void setExtInfo(Object key, Object value){
		doLoadExtInfo();
		
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
		doLoadExtInfo();
		
		return new HashMap<>(extInfo);
	}
}
