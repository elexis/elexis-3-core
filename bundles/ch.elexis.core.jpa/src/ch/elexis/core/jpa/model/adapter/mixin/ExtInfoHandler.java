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

	public ExtInfoHandler(AbstractIdModelAdapter<? extends EntityWithExtInfo> withExtInfo) {
		this.withExtInfo = withExtInfo;
	}

	private void doLoadExtInfo(boolean reload) {
		if (extInfo == null || reload) {
			byte[] bytes = withExtInfo.getEntity().getExtInfo();
			if (bytes != null) {
				extInfo = JpaModelUtil.extInfoFromBytes(bytes);
			} else {
				extInfo = new Hashtable<>();
			}
		}
	}

	public Object getExtInfo(Object key) {
		doLoadExtInfo(false);

		return extInfo.get(key);
	}

	public void setExtInfo(Object key, Object value) {
		doLoadExtInfo(!withExtInfo.isDirty());

		if (value == null) {
			extInfo.remove(key);
		} else {
			extInfo.put(key, value);
		}
		withExtInfo.getEntityMarkDirty().setExtInfo(JpaModelUtil.extInfoToBytes(extInfo));
	}

	public void resetExtInfo() {
		extInfo = null;
	}

	/**
	 * @return modifications to this map are not persisted. Use
	 *         {@link #setExtInfo(Object, Object)} to handle persistent sets
	 */
	public Map<Object, Object> getMap() {
		doLoadExtInfo(false);

		return new HashMap<>(extInfo);
	}
}
