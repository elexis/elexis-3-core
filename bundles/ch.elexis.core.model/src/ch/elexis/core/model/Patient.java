package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;

public class Patient extends Person implements IPatient {

	public Patient(Kontakt model) {
		super(model);
	}

	@Override
	public String getLabel() {
		return super.getLabel();
	}

	@Override
	public String getDiagnosen() {
		return getEntity().getDiagnosen();
	}

	@Override
	public void setDiagnosen(String value) {
		getEntityMarkDirty().setDiagnosen(value);
	}

	@Override
	public String getRisk() {
		return getEntity().getRisk();
	}

	@Override
	public void setRisk(String value) {
		getEntityMarkDirty().setRisk(value);
	}

	@Override
	public String getFamilyAnamnese() {
		return getEntity().getFamilyAnamnese();
	}

	@Override
	public void setFamilyAnamnese(String value) {
		getEntityMarkDirty().setFamilyAnamnese(value);
	}

	@Override
	public String getPersonalAnamnese() {
		return getEntity().getPersonalAnamnese();
	}

	@Override
	public void setPersonalAnamnese(String value) {
		getEntityMarkDirty().setPersonalAnamnese(value);
	}

	@Override
	public String getAllergies() {
		return getEntity().getAllergies();
	}

	@Override
	public void setAllergies(String value) {
		getEntityMarkDirty().setAllergies(value);
	}

	@Override
	public String getPatientNr() {
		return getCode();
	}

	@Override
	public void setPatientNr(String patientNr) {
		setCode(patientNr);
	}

	@Override
	public List<ICoverage> getCoverages() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getFaelle().stream().filter(f -> !f.isDeleted())
				.map(f -> ModelUtil.getAdapter(f, ICoverage.class)).collect(Collectors.toList());
	}

	@Override
	public List<IPrescription> getMedication(List<EntryType> filterType) {
		IQuery<IPrescription> query = CoreModelServiceHolder.get().getQuery(IPrescription.class);
		query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS, this);
		query.startGroup();
		query.or(ModelPackage.Literals.IPRESCRIPTION__DATE_TO, COMPARATOR.EQUALS, null);
		query.or(ModelPackage.Literals.IPRESCRIPTION__DATE_TO, COMPARATOR.GREATER, LocalDateTime.now());
		query.andJoinGroups();
		List<IPrescription> iPrescriptions = query.execute();
		if (filterType != null && !filterType.isEmpty()) {
			// getEntryType is a special logic with rezeptId and direktvergabe cannot query
			// it from DB directly
			return iPrescriptions.stream().filter(p -> filterType.contains(p.getEntryType()))
					.collect(Collectors.toList());
		}
		return iPrescriptions;
	}

	@Override
	public List<IPrescription> getMedicationAll(List<EntryType> filterType) {
		IQuery<IPrescription> query = CoreModelServiceHolder.get().getQuery(IPrescription.class);
		query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS, this);
		query.and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.EQUALS, false);
		List<IPrescription> iPrescriptions = query.execute();
		if (filterType != null && !filterType.isEmpty()) {
			return iPrescriptions.stream().filter(p -> filterType.contains(p.getEntryType()))
					.collect(Collectors.toList());
		}
		return iPrescriptions;
	}

	@Override
	public IContact getFamilyDoctor() {
		String doctorId = (String) getExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT);
		if (doctorId != null) {
			return ch.elexis.core.model.service.holder.CoreModelServiceHolder.get().load(doctorId, IContact.class)
					.orElse(null);
		}
		return null;
	}

	@Override
	public void setFamilyDoctor(IContact value) {
		if (value != null) {
			setExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT, value.getId());
		} else {
			setExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT, null);
		}
	}

	@Override
	public void setDeceased(boolean value) {
		super.setDeceased(value);
		String configSticker = ConfigServiceHolder.get().get(Preferences.CFG_DECEASED_STICKER, StringUtils.EMPTY);
		if (configSticker == null || configSticker.isEmpty()) {
			configSticker = ch.elexis.core.model.messages.Messages.Patient_deceased;
		}
		ISticker sticker = getOrCreateSticker(configSticker, "ffffff", "000000");
		applyOrRemoveSticker(sticker, value);
	}

	private ISticker getOrCreateSticker(String stickername, String foreground, String background) {
		for (ISticker iSticker : StickerServiceHolder.get().getStickersForClass(IPatient.class)) {
			if (iSticker.getName().equalsIgnoreCase(stickername)) {
				return iSticker;
			}
		}
		ISticker ret = CoreModelServiceHolder.get().create(ISticker.class);
		ret.setName(stickername);
		ret.setForeground(foreground);
		ret.setBackground(background);
		CoreModelServiceHolder.get().save(ret);
		StickerServiceHolder.get().setStickerAddableToClass(IPatient.class, ret);
		return ret;
	}

	private void applyOrRemoveSticker(ISticker sticker, boolean apply) {
		if (apply) {
			ISticker existing = StickerServiceHolder.get().getSticker(this, sticker);
			if (existing == null) {
				StickerServiceHolder.get().addSticker(sticker, this);
			}
		} else {
			StickerServiceHolder.get().removeSticker(sticker, this);
		}
	}
}
