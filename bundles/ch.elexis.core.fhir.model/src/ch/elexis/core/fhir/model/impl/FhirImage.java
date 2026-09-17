package ch.elexis.core.fhir.model.impl;

import java.time.LocalDate;

import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.MimeType;

public class FhirImage implements IImage {

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalDate getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDate(LocalDate value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPrefix(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImage(byte[] value) {
		// TODO Auto-generated method stub

	}

	@Override
	public MimeType getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMimeType(MimeType value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLastupdate() {
		// TODO Auto-generated method stub
		return null;
	}

}
