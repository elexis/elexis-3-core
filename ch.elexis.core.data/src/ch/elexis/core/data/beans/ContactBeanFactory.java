package ch.elexis.core.data.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.elexis.data.Kontakt;

public class ContactBeanFactory {

	public static List<ContactBean> createContactBeans(List<Kontakt> kontakte){
		if (kontakte == null)
			return Collections.emptyList();
		ArrayList<ContactBean> ret = new ArrayList<>(kontakte.size());
		for (Kontakt k : kontakte) {		
			ret.add(new ContactBean(k));
		}

		return ret;
	}
	
}
