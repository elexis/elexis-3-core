package ch.elexis.core.model.format;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IContact;
import ch.elexis.core.types.Country;
import ch.rgw.tools.StringTool;

public class PostalAddress {
	
	private IContact contact;
	
	public static PostalAddress of(IContact contact){
		PostalAddress ret = new PostalAddress();
		ret.setContact(contact);
		return ret;
	}
	
	private void setContact(IContact contact){
		this.contact = contact;
	}
	
	public String getWrittenAddress(boolean withName, boolean multiline){
		String sep = StringTool.lf;
		if (multiline == false) {
			sep = ", "; //$NON-NLS-1$
		}
		StringBuilder ret = new StringBuilder(100);
		if (withName == true) {
			ret.append(contact.getLabel()).append(sep);
		}
		if (StringUtils.isNotEmpty(contact.getStreet())) {
			ret.append(contact.getStreet()).append(sep);
		}
		if (contact.getCountry() != null && contact.getCountry() != Country.NDF) {
			ret.append(contact.getCountry().toString()).append(" - "); //$NON-NLS-1$
		}
		if (StringUtils.isNotEmpty(contact.getZip()) && StringUtils.isNotEmpty(contact.getCity())) {
			ret.append(contact.getZip()).append(StringTool.space).append(contact.getCity());
		}
		if (multiline) {
			// append trailing newline
			ret.append(StringTool.lf);
		}
		return ret.toString();
	}
	
	public String getLabel(){
		return getWrittenAddress(true, false);
	}
}
