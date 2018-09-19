package ch.elexis.core.test.initializer;

import ch.elexis.core.constants.Preferences;
import info.elexis.server.core.connector.elexis.services.ConfigService;

public class ConfigInitializer {

	public static final String VVG_NAME = "VVG";
	public static final String PRIVATE_NAME = "privat";
	public static final String MV_NAME = "MV";
	public static final String IV_NAME = "IV";
	public static final String KVG_REQUIREMENTS = "Kostenträger\\:K;Versicherungsnummer\\:T";
	public static final String KVG_NAME = "KVG";
	public static final String UVG_NAME = "UVG";
	public static final String CONST_TARMED_LEISTUNG = "Tarmedleistung";
	public static final String CONST_TARMED_DRUCKER = "Tarmed-Drucker";
	public static final String UVG_REQUIREMENTS = "Kostenträger\\:K;Unfallnummer\\:T;Unfalldatum\\:D";

	public static void initializeConfiguration() {
		initializeFallConfiguration();
	}

	public static void initializeFallConfiguration() {
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/name", KVG_NAME);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/gesetz", "KVG");
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/leistungscodes", CONST_TARMED_LEISTUNG);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/standardausgabe", CONST_TARMED_DRUCKER);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/bedingungen", KVG_REQUIREMENTS);

		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/name", UVG_NAME);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/leistungscodes", CONST_TARMED_LEISTUNG);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/standardausgabe", CONST_TARMED_DRUCKER);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/bedingungen", UVG_REQUIREMENTS);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/gesetz", "UVG");

		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/name", IV_NAME);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/leistungscodes", CONST_TARMED_LEISTUNG);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/standardausgabe", CONST_TARMED_DRUCKER);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/bedingungen",
				"Kostenträger:K;Fallnummer:T");
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/gesetz", "IVG");

		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/name", MV_NAME);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/leistungscodes", CONST_TARMED_LEISTUNG);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/standardausgabe", CONST_TARMED_DRUCKER);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/bedingungen", "Kostenträger:K");
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/gesetz", "MVG");

		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/name", PRIVATE_NAME);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/leistungscodes",
				CONST_TARMED_LEISTUNG);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/standardausgabe",
				CONST_TARMED_DRUCKER);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/gesetz", "VVG");

		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/name", VVG_NAME);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/leistungscodes", CONST_TARMED_LEISTUNG);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/standardausgabe", CONST_TARMED_DRUCKER);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/bedingungen", KVG_REQUIREMENTS);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/gesetz", "VVG");
	}

}
