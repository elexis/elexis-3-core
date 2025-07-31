package ch.elexis.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.KontaktAdressJoint;
import ch.elexis.core.jpa.entities.ZusatzAdresse;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.format.PostalAddress;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.types.Country;

public class Contact extends AbstractIdDeleteModelAdapter<Kontakt> implements IdentifiableWithXid, IContact {

	public Contact(Kontakt entity) {
		super(entity);
	}

	@Override
	public boolean isMandator() {
		return getEntity().isMandator();
	}

	@Override
	public void setMandator(boolean value) {
		getEntityMarkDirty().setMandator(value);
	}

	@Override
	public boolean isUser() {
		return getEntity().isUser();
	}

	@Override
	public void setUser(boolean value) {
		getEntityMarkDirty().setUser(value);
	}

	@Override
	public boolean isPerson() {
		return getEntity().isPerson();
	}

	@Override
	public void setPerson(boolean value) {
		getEntityMarkDirty().setPerson(value);
	}

	@Override
	public boolean isPatient() {
		return getEntity().isPatient();
	}

	@Override
	public void setPatient(boolean value) {
		getEntityMarkDirty().setPatient(value);
	}

	@Override
	public boolean isLaboratory() {
		return getEntity().isLaboratory();
	}

	@Override
	public void setLaboratory(boolean value) {
		getEntityMarkDirty().setLaboratory(value);
	}

	@Override
	public boolean isOrganization() {
		return getEntity().isOrganisation();
	}

	@Override
	public void setOrganization(boolean value) {
		getEntityMarkDirty().setOrganisation(value);
	}

	@Override
	public boolean isDeceased() {
		return getEntity().isDeceased();
	}

	@Override
	public void setDeceased(boolean value) {
		getEntityMarkDirty().setDeceased(value);
	}

	@Override
	public String getDescription1() {
		return getEntity().getDescription1();
	}

	@Override
	public void setDescription1(String value) {
		getEntityMarkDirty().setDescription1(value);
	}

	@Override
	public String getDescription2() {
		return getEntity().getDescription2();
	}

	@Override
	public void setDescription2(String value) {
		getEntityMarkDirty().setDescription2(value);
	}

	@Override
	public String getDescription3() {
		return getEntity().getDescription3();
	}

	@Override
	public void setDescription3(String value) {
		getEntityMarkDirty().setDescription3(value);
	}

	@Override
	public String getCode() {
		return getEntity().getCode();
	}

	@Override
	public void setCode(String value) {
		getEntityMarkDirty().setCode(value);
	}

	@Override
	public Country getCountry() {
		return getEntity().getCountry();
	}

	@Override
	public void setCountry(Country value) {
		getEntityMarkDirty().setCountry(value);
	}

	@Override
	public String getZip() {
		return getEntity().getZip();
	}

	@Override
	public void setZip(String value) {
		getEntityMarkDirty().setZip(value);
	}

	@Override
	public String getCity() {
		return getEntity().getCity();
	}

	@Override
	public void setCity(String value) {
		getEntityMarkDirty().setCity(value);
	}

	@Override
	public String getStreet() {
		return getEntity().getStreet();
	}

	@Override
	public void setStreet(String value) {
		getEntityMarkDirty().setStreet(value);
	}

	@Override
	public String getPhone1() {
		return getEntity().getPhone1();
	}

	@Override
	public void setPhone1(String value) {
		getEntityMarkDirty().setPhone1(value);
	}

	@Override
	public String getPhone2() {
		return getEntity().getPhone2();
	}

	@Override
	public void setPhone2(String value) {
		getEntityMarkDirty().setPhone2(value);
	}

	@Override
	public String getFax() {
		return getEntity().getFax();
	}

	@Override
	public void setFax(String value) {
		getEntityMarkDirty().setFax(value);
	}

	@Override
	public String getEmail() {
		return getEntity().getEmail();
	}

	@Override
	public void setEmail(String value) {
		getEntityMarkDirty().setEmail(value);
	}

	@Override
	public String getEmail2() {
		return getEntity().getEmail2();
	}

	@Override
	public void setEmail2(String value) {
		getEntityMarkDirty().setEmail2(value);
	}

	@Override
	public String getWebsite() {
		return getEntity().getWebsite();
	}

	@Override
	public void setWebsite(String value) {
		getEntityMarkDirty().setWebsite(value);
	}

	@Override
	public String getMobile() {
		return getEntity().getMobile();
	}

	@Override
	public void setMobile(String value) {
		getEntityMarkDirty().setMobile(value);
	}

	@Override
	public String getComment() {
		return StringUtils.defaultString(getEntity().getComment());
	}

	@Override
	public void setComment(String value) {
		getEntityMarkDirty().setComment(value);
	}

	@Override
	public String getGroup() {
		return getEntity().getGruppe();
	}

	@Override
	public void setGroup(String value) {
		getEntityMarkDirty().setGruppe(value);
	}

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(getDescription1()).append(StringUtils.SPACE).append(StringUtils.defaultString(getDescription2()));
		if (!StringUtils.isBlank(getDescription3())) {
			sb.append("(").append(getDescription3()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append(", ").append(StringUtils.defaultString(getStreet())).append(", ") //$NON-NLS-1$
				.append(StringUtils.defaultString(getZip())).append(StringUtils.SPACE)
				.append(StringUtils.defaultString(getCity()));
		return sb.toString();
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}

	@Override
	public List<IAddress> getAddress() {
		CoreModelServiceHolder.get().refresh(this);
		ArrayList<ZusatzAdresse> addresses = new ArrayList<>(getEntity().getAddresses());
		return addresses.parallelStream().filter(f -> !f.isDeleted())
				.map(f -> ModelUtil.getAdapter(f, IAddress.class, true)).collect(Collectors.toList());
	}

	@Override
	public String getPostalAddress() {
		if (getEntity().getAnschrift() == null) {
			setPostalAddress(PostalAddress.of(this).getWrittenAddress(true, true));
		}
		return getEntity().getAnschrift();
	}

	@Override
	public void setPostalAddress(String value) {
		getEntityMarkDirty().setAnschrift(value);
	}

	@Override
	public IImage getImage() {
		return CoreModelServiceHolder.get().load(getId(), IImage.class).orElse(null);
	}

	@Override
	public void setImage(IImage value) {
		IImage image = CoreModelServiceHolder.get().load(getId(), IImage.class, true).orElse(null);
		if (value == null) {
			if (image != null) {
				CoreModelServiceHolder.get().delete(image);
			}
			return;
		}
		if (image == null) {
			image = CoreModelServiceHolder.get().create(IImage.class);
			image.setId(getId());
			image.setTitle("ContactImage");
		}
		image.setDate(value.getDate());
		image.setPrefix("ch.elexis.data.Kontakt");
		image.setImage(value.getImage());
		image.setMimeType(value.getMimeType());
		image.setDeleted(false);
		CoreModelServiceHolder.get().save(image);
	}

	@Override
	public List<IRelatedContact> getRelatedContacts() {
		CoreModelServiceHolder.get().refresh(this);
		ArrayList<KontaktAdressJoint> relatedContacts = new ArrayList<>(getEntity().getRelatedContacts());
		return relatedContacts.parallelStream().filter(f -> !f.isDeleted())
				.map(f -> ModelUtil.getAdapter(f, IRelatedContact.class, true)).collect(Collectors.toList());
	}

	@Override
	public IPerson asIPerson() {
		IPerson ret = CoreModelServiceHolder.get().load(getId(), IPerson.class).orElse(null);
		if (ret == null && this instanceof IPerson) {
			ret = (IPerson) this;
		}
		return ret;
	}

	@Override
	public IPatient asIPatient() {
		IPatient ret = CoreModelServiceHolder.get().load(getId(), IPatient.class).orElse(null);
		if (ret == null && this instanceof IPatient) {
			ret = (IPatient) this;
		}
		return ret;
	}

	@Override
	public IOrganization asIOrganization() {
		IOrganization ret = CoreModelServiceHolder.get().load(getId(), IOrganization.class).orElse(null);
		if (ret == null && this instanceof IOrganization) {
			ret = (IOrganization) this;
		}
		return ret;
	}
}
