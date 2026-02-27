package ch.elexis.core.ui.contacts.wizard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.elexis.core.model.IBlob;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ManagedInsuranceModel {

	private static final String BLOBID = "managedInsuranceModel";

	private List<String> ignored;

	private List<String> confirmed;

	private Map<String, String> mapping;

	public static ManagedInsuranceModel load() {
		Gson gson = new GsonBuilder().create();
		IBlob namedBlob = CoreModelServiceHolder.get().load(BLOBID, IBlob.class).orElse(null);
		if (namedBlob != null) {
			return gson.fromJson(namedBlob.getStringContent(), ManagedInsuranceModel.class);
		}
		return new ManagedInsuranceModel();
	}

	public void save() {
		Gson gson = new GsonBuilder().create();
		IBlob namedBlob = CoreModelServiceHolder.get().load(BLOBID, IBlob.class).orElse(null);
		if (namedBlob == null) {
			namedBlob = CoreModelServiceHolder.get().create(IBlob.class);
			namedBlob.setDate(LocalDate.now());
			namedBlob.setId(BLOBID);
		}
		namedBlob.setStringContent(gson.toJson(this));
		CoreModelServiceHolder.get().save(namedBlob);
	}

	public List<String> getIgnored() {
		if (ignored == null) {
			ignored = new ArrayList<String>();
		}
		return ignored;
	}

	public Map<String, String> getMapping() {
		if (mapping == null) {
			mapping = new HashMap<String, String>();
		}
		return mapping;
	}

	public List<String> getConfirmed() {
		if (confirmed == null) {
			confirmed = new ArrayList<String>();
		}
		return confirmed;
	}
}
