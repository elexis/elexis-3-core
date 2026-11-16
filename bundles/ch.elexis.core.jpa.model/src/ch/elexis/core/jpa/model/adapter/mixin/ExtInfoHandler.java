package ch.elexis.core.jpa.model.adapter.mixin;

import java.util.Hashtable;
import java.util.Map;

import ch.elexis.core.jpa.entities.EntityWithExtInfo;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.util.JpaModelUtil;

public class ExtInfoHandler {

	private AbstractIdModelAdapter<? extends EntityWithExtInfo> withExtInfo;

	public ExtInfoHandler(AbstractIdModelAdapter<? extends EntityWithExtInfo> withExtInfo) {
		this.withExtInfo = withExtInfo;
	}

	private Map<Object, Object> getExtInfoMap() {
		synchronized (withExtInfo) {
			byte[] bytes = withExtInfo.getEntity().getExtInfo();
			if (bytes != null) {
				return JpaModelUtil.extInfoFromBytes(bytes);
			} else {
				return new Hashtable<>();
			}
		}
	}

	public Object getExtInfo(Object key) {
		return getExtInfoMap().get(key);
	}

	public void setExtInfo(Object key, Object value) {
		synchronized (withExtInfo) {
			Map<Object, Object> extInfo = getExtInfoMap();

//			debugLog(key, value, extInfo);

			if (value == null) {
				extInfo.remove(key);
			} else {
				extInfo.put(key, value);
			}
			withExtInfo.getEntityMarkDirty().setExtInfo(JpaModelUtil.extInfoToBytes(extInfo));
		}
	}

//	private void debugLog(Object key, Object value, Map<Object, Object> extInfo) {
//		Logger logger = LoggerFactory.getLogger(getClass());
//		logger.info("Set key [" + key + "] value [" + value + "] of [" + withExtInfo + "]");
//		logger.info("Current ExtInfo " + extInfo.keySet().stream().map(k -> k + "=" + extInfo.get(k))
//				.collect(Collectors.joining(", ", "{", "}")));
//	}

	/**
	 * @return modifications to this map are not persisted. Use
	 *         {@link #setExtInfo(Object, Object)} to handle persistent sets
	 */
	public Map<Object, Object> getMap() {
		return getExtInfoMap();
	}
}
