package ch.elexis.core.spotlight.ui.internal.ready;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.e4.core.di.annotations.Optional;

import ch.elexis.core.model.IPatient;
import jakarta.inject.Inject;

public class PatientSelectionHistorySupplier implements Supplier<List<Object[]>> {

	private LinkedHashMap<String, Object[]> selectionsMap;
	private DateTimeFormatter TIME_ONLY;

	@SuppressWarnings("serial")
	public PatientSelectionHistorySupplier() {
		TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm:ss");
		selectionsMap = new LinkedHashMap<>() {
			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<String, Object[]> eldest) {
				return size() > 5;
			}
		};
	}

	@Override
	public List<Object[]> get() {
		return selectionsMap.entrySet().stream()
				.sorted((o1, o2) -> Long.compare((Long) o2.getValue()[2], (Long) o1.getValue()[2]))
				.map(entry -> new Object[] { entry.getKey(), entry.getValue()[0], entry.getValue()[1] })
				.collect(Collectors.toList());
	}

	@Inject
	@Optional
	public void selectPatient(IPatient patient) {
		if (patient != null) {
			selectionsMap.put(patient.getId(), new Object[] { TIME_ONLY.format(LocalDateTime.now()), patient.getLabel(),
					System.currentTimeMillis() });
		}
	}

}