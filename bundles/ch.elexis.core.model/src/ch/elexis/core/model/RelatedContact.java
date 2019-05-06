package ch.elexis.core.model;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.KontaktAdressJoint;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.types.RelationshipType;

public class RelatedContact extends AbstractIdDeleteModelAdapter<KontaktAdressJoint>
		implements IdentifiableWithXid, IRelatedContact {

	public RelatedContact(KontaktAdressJoint entity) {
		super(entity);
	}

	@Override
	public IContact getMyContact() {
		if (getEntity().getMyKontakt() != null) {
			return ModelUtil.getAdapter(getEntity().getMyKontakt(), IContact.class);
		}
		return null;
	}

	@Override
	public void setMyContact(IContact value) {
		if (value != null) {
			if (value instanceof AbstractIdModelAdapter) {
				getEntity().setMyKontakt((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
			}
		} else {
			getEntity().setMyKontakt(null);
		}
	}

	@Override
	public IContact getOtherContact() {
		if (getEntity().getOtherKontakt() != null) {
			return ModelUtil.getAdapter(getEntity().getOtherKontakt(), IContact.class);
		}
		return null;
	}

	@Override
	public void setOtherContact(IContact value) {
		if (value != null) {
			if (value instanceof AbstractIdModelAdapter) {
				getEntity().setOtherKontakt((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
			}
		} else {
			getEntity().setOtherKontakt(null);
		}
	}

	@Override
	public String getRelationshipDescription() {
		return getEntity().getBezug();
	}

	@Override
	public void setRelationshipDescription(String value) {
		getEntity().setBezug(value);
	}

	@Override
	public RelationshipType getMyType() {
		if (getEntity().getMyRType() != null) {
			try {
				return RelationshipType.get(getEntity().getMyRType());
			} catch (NumberFormatException e) {
				LoggerFactory.getLogger(getClass()).warn("[{}] Unparseable MyRType value [{}], returning UNKNOWN",
						getId(), getEntity().getMyRType());
				return RelationshipType.AUNKNOWN;
			}
		}
		return null;
	}

	@Override
	public void setMyType(RelationshipType value) {
		getEntity().setMyRType((value != null) ? value.getValue() : null);
	}

	@Override
	public RelationshipType getOtherType() {
		if (getEntity().getOtherRType() != null) {
			try {
				return RelationshipType.get(getEntity().getOtherRType());
			} catch (NumberFormatException e) {
				LoggerFactory.getLogger(getClass()).warn("[{}] Unparseable OtherRType value [{}], returning UNKNOWN",
						getId(), getEntity().getOtherRType());
				return RelationshipType.AUNKNOWN;
			}
		}
		return null;
	}

	@Override
	public void setOtherType(RelationshipType value) {
		getEntity().setOtherRType((value != null) ? value.getValue() : null);
	}

}
