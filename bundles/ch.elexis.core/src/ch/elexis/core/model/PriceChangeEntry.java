package ch.elexis.core.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PriceChangeEntry extends OrderHistoryEntry {

	@SerializedName("updatedEncounters")
	private List<EncounterInfo> updatedEncounters;

	public PriceChangeEntry() {
		super(null, null, null, null); // für Gson
	}

	// 💡 Komfort-Konstruktor für deine Logger-Aufrufe:
	public PriceChangeEntry(OrderHistoryAction action, String userId, String details, String extraInfo,
			List<EncounterInfo> updatedEncounters) {
		super(action, userId, details, extraInfo); // Timestamp setzt die Basisklasse
		this.updatedEncounters = updatedEncounters;
	}

	public List<EncounterInfo> getUpdatedEncounters() {
		return updatedEncounters;
	}

	public void setUpdatedEncounters(List<EncounterInfo> updatedEncounters) {
		this.updatedEncounters = updatedEncounters;
	}

	public static class EncounterInfo {
		@SerializedName("encounterId")
		private String encounterId;
		@SerializedName("encounterDate")
		private String encounterDate;
		@SerializedName("patientName")
		private String patientName;
		@SerializedName("mandatorName")
		private String mandatorName;

		public EncounterInfo() {
		}

		public EncounterInfo(String encounterId, String encounterDate, String patientName, String mandatorName) {
			this.encounterId = encounterId;
			this.encounterDate = encounterDate;
			this.patientName = patientName;
			this.mandatorName = mandatorName;
		}

		public String getEncounterId() {
			return encounterId;
		}

		public String getEncounterDate() {
			return encounterDate;
		}

		public String getPatientName() {
			return patientName;
		}

		public String getMandatorName() {
			return mandatorName;
		}
	}
}
