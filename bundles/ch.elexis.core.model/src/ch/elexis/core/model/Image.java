package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.DbImage;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;

public class Image extends AbstractIdDeleteModelAdapter<DbImage>
		implements IdentifiableWithXid, IImage {

	public Image(DbImage entity){
		super(entity);
	}

	@Override
	public LocalDate getDate(){
		return getEntity().getDate();
	}

	@Override
	public void setDate(LocalDate value){
		getEntity().setDate(value);
	}

	@Override
	public String getPrefix(){
		return getEntity().getPrefix();
	}

	@Override
	public void setPrefix(String value){
		getEntity().setPrefix(value);
	}

	@Override
	public String getTitle(){
		return getEntity().getTitle();
	}

	@Override
	public void setTitle(String value){
		getEntity().setTitle(value);
	}

	@Override
	public byte[] getImage(){
		return getEntity().getImage();
	}

	@Override
	public void setImage(byte[] value){
		getEntity().setImage(value);
	}

	@Override
	public void setId(String id){
		getEntity().setId(id);
	}
	
}
