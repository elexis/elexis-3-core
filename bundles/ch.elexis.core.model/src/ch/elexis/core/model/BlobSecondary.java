package ch.elexis.core.model;

import java.time.LocalDate;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Heap2;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.util.JpaModelUtil;

public class BlobSecondary extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Heap2>
		implements IdentifiableWithXid, IBlobSecondary {
	
	public BlobSecondary(Heap2 entity){
		super(entity);
	}
	
	@Override
	public byte[] getContent(){
		return JpaModelUtil.getExpanded(getEntity().getContents());
	}
	
	@Override
	public void setContent(byte[] value){
		getEntity().setContents(JpaModelUtil.getCompressed(value));
	}
	
	@Override
	public String getStringContent(){
		byte[] comp = getContent();
		if ((comp == null) || (comp.length == 0)) {
			return "";
		}
		byte[] exp = JpaModelUtil.getExpanded(comp);
		try {
			return new String(exp, "utf-8");
		} catch (Exception ex) {
			LoggerFactory.getLogger(getClass()).error("Error getting String content", ex);
			// should really not happen
			return null;
		}
	}
	
	@Override
	public void setStringContent(String value){
		byte[] comp = JpaModelUtil.getCompressed(value);
		setContent(comp);
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getDatum();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntity().setDatum(value);
	}
	
	@Override
	public void setId(String id){
		getEntity().setId(id);
	}
}
