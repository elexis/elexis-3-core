package ch.elexis.core.findings.util.fhir.accessor;

public class EnumMapping {

	@SuppressWarnings("rawtypes")
	private Class<? extends Enum> fhirEnum;
	@SuppressWarnings("rawtypes")
	private Class<? extends Enum> localEnum;

	public EnumMapping(Class<? extends Enum<?>> fhirEnum, Class<? extends Enum<?>> localEnum) {
		this.fhirEnum = fhirEnum;
		this.localEnum = localEnum;
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getLocalEnumValueByEnum(Enum<?> fhirEnumValue) {
		return Enum.valueOf(localEnum, fhirEnumValue.name());
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getLocalEnumValueByCode(String code) {
		return Enum.valueOf(localEnum, code);
	}
	
	@SuppressWarnings("unchecked")
	public Enum<?> getFhirEnumValueByEnum(Enum<?> localEnumValue) {
		return Enum.valueOf(fhirEnum, localEnumValue.name());
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getFhirEnumValueByCode(String code) {
		return Enum.valueOf(fhirEnum, code);
	}
}
