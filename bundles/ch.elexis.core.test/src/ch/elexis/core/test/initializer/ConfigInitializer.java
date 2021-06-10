package ch.elexis.core.test.initializer;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.IConfigService;

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

	public void initializeConfiguration(IConfigService configService) {
		// test_initSampleContacts initializes patient 1 and 2
		configService.set("PatientNummer", "2");
		initializeBillingSystems(configService);
	}

	public void initializeBillingSystems(IConfigService configService) {
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/name", KVG_NAME);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/gesetz", "KVG");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/leistungscodes", CONST_TARMED_LEISTUNG);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/standardausgabe", CONST_TARMED_DRUCKER);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/bedingungen", KVG_REQUIREMENTS);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/defaultBillingLaw", "KVG");

		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/name", UVG_NAME);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/leistungscodes", CONST_TARMED_LEISTUNG);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/standardausgabe", CONST_TARMED_DRUCKER);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/bedingungen", UVG_REQUIREMENTS);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/gesetz", "UVG");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/defaultBillingLaw", "UVG");

		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/name", IV_NAME);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/leistungscodes", CONST_TARMED_LEISTUNG);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/standardausgabe", CONST_TARMED_DRUCKER);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/bedingungen",
				"Kostenträger:K;Fallnummer:T");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/gesetz", "IVG");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/defaultBillingLaw", "IVG");

		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/name", MV_NAME);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/leistungscodes", CONST_TARMED_LEISTUNG);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/standardausgabe", CONST_TARMED_DRUCKER);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/bedingungen", "Kostenträger:K");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/gesetz", "MVG");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/defaultBillingLaw", "MVG");

		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/name", PRIVATE_NAME);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/leistungscodes",
				CONST_TARMED_LEISTUNG);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/standardausgabe",
				CONST_TARMED_DRUCKER);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/gesetz", "VVG");

		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/name", VVG_NAME);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/leistungscodes", CONST_TARMED_LEISTUNG);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/standardausgabe", CONST_TARMED_DRUCKER);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/bedingungen", KVG_REQUIREMENTS);
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/gesetz", "VVG");
		configService.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/defaultBillingLaw", "VVG");
	}

}
