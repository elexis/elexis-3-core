package ch.elexis.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.data.interfaces.IStock;
import ch.elexis.core.data.service.CoreModelServiceHolder;

/**
 * A Stock is the definition of a storage for articles. A stock is referred to by {@link StockEntry}
 * elements, which represent the articles in store.
 * 
 * @since 3.2
 */
public class Stock extends PersistentObject implements IStock {
	
	public static final String TABLENAME = "STOCK";
	public static final String FLD_CODE = "CODE";
	public static final String FLD_PRIORITY = "PRIORITY";
	public static final String FLD_OWNER = "OWNER";
	public static final String FLD_RESPONSIBLE = "RESPONSIBLE";
	public static final String FLD_LOCATION = "LOCATION";
	public static final String FLD_DESCRIPTION = "DESCRIPTION";
	public static final String FLD_DRIVER_UUID = "DRIVER_UUID";
	public static final String FLD_DRIVER_CONFIG = "DRIVER_CONFIG";
	
	public static final String FLD_JOINT_STOCK_ENTRIES = "STOCK_ENTRIES";
	
	public static final String DEFAULT_STOCK_ID = "STD";
	
	static {
		addMapping(TABLENAME, FLD_ID, FLD_CODE, FLD_PRIORITY + "=S:N:" + FLD_PRIORITY, FLD_OWNER,
			FLD_RESPONSIBLE, FLD_LOCATION, FLD_DESCRIPTION, FLD_DRIVER_UUID, FLD_DRIVER_CONFIG,
			FLD_JOINT_STOCK_ENTRIES + "=LIST:STOCK:" + StockEntry.TABLENAME);
	}
	
	protected Stock(){}
	
	protected Stock(final String id){
		super(id);
	}
	
	public Stock(final String code, int globalPriority){
		create(null);
		set(new String[] {
			FLD_CODE, FLD_PRIORITY
		}, code, Integer.toString(globalPriority));
	}
	
	public static Stock load(final String id){
		return new Stock(id);
	}
	
	@Override
	public String getLabel(){
		String[] values = get(false, FLD_CODE, FLD_DESCRIPTION);
		if (!StringUtils.isBlank(values[1])) {
			return "[" + values[0] + "] " + values[1];
		}
		return values[0];
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public boolean delete(){
		List<String> list = getList(FLD_JOINT_STOCK_ENTRIES, false);
		for (String id : list) {
			StockEntry.load(id).delete();
		}
		return super.delete();
	}
	
	@Override
	public boolean removeFromDatabase(){
		List<String> list = getList(FLD_JOINT_STOCK_ENTRIES, false, true);
		for (String id : list) {
			StockEntry.load(id).removeFromDatabase();
		}
		return super.removeFromDatabase();
	}
	
	@Override
	public Integer getPriority(){
		String val = get(FLD_PRIORITY);
		if (StringUtils.isNotBlank(val)) {
			try {
				return Integer.valueOf(val);
			} catch (NumberFormatException nfe) {}
		}
		return null;
	}
	
	public void setPriority(Integer value){
		setInt(FLD_PRIORITY, value);
	}
	
	@Override
	public String getCode(){
		return get(FLD_CODE);
	}
	
	public void setCode(String code){
		set(FLD_CODE, code);
	}
	
	public String getDescription(){
		return get(FLD_DESCRIPTION);
	}
	
	public void setDescription(String description){
		set(FLD_DESCRIPTION, description);
	}
	
	public String getLocation(){
		return get(FLD_LOCATION);
	}
	
	public void setLocation(String location){
		set(FLD_LOCATION, location);
	}
	
	@Override
	public Mandant getOwner(){
		String mandatorId = get(FLD_OWNER);
		if (StringUtils.isNotBlank(mandatorId)) {
			return Mandant.load(mandatorId);
		}
		return null;
	}
	
	public void setOwner(Mandant owner){
		if (owner != null) {
			set(FLD_OWNER, owner.getId());
			return;
		}
		set(FLD_OWNER, null);
	}
	
	public Kontakt getResponsible(){
		String responsibleId = get(FLD_RESPONSIBLE);
		if (StringUtils.isNotBlank(responsibleId)) {
			return Kontakt.load(responsibleId);
		}
		return null;
	}
	
	public void setResponsible(Kontakt contact){
		if (contact != null) {
			set(FLD_RESPONSIBLE, contact.getId());
			return;
		}
		set(FLD_RESPONSIBLE, null);
	}
	
	@Override
	public String getDriverUuid(){
		return get(FLD_DRIVER_UUID);
	}
	
	public void setDriverUuid(String driverUuid){
		set(FLD_DRIVER_UUID, driverUuid);
	}
	
	@Override
	public String getDriverConfig(){
		return get(FLD_DRIVER_CONFIG);
	}
	
	public void setDriverConfig(String driverConfig){
		set(FLD_DRIVER_CONFIG, driverConfig);
	}
	
	/**
	 * Convenience conversion method, loads object via model service
	 * 
	 * @return
	 * @since 3.8
	 * @throws IllegalStateException if entity could not be loaded
	 */
	public ch.elexis.core.model.IStock toIStock() {
		return CoreModelServiceHolder.get().load(getId(), ch.elexis.core.model.IStock.class)
				.orElseThrow(() -> new IllegalStateException("Could not convert stock [" + getId() + "]"));
	}
}
