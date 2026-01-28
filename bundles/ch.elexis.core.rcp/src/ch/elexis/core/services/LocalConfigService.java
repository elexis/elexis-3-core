package ch.elexis.core.services;

import ch.elexis.core.utils.OsgiServiceUtil;

public class LocalConfigService {

	public static boolean get(String cfgPrintGlobaloutputdirs, boolean b) {
		return LocalConfigServiceImpl.localConfig.get(cfgPrintGlobaloutputdirs, b);
	}

	public static String get(String cfgPrintGlobaloutputdirs, String empty) {
		return LocalConfigServiceImpl.localConfig.get(cfgPrintGlobaloutputdirs, empty);
	}

	public static void set(String rnnExportdir, String outputDir) {
		ILocalConfigService iLocalConfigService = OsgiServiceUtil.getService(ILocalConfigService.class).get();
		iLocalConfigService.set(rnnExportdir, outputDir);
		OsgiServiceUtil.ungetService(iLocalConfigService);
	}

	public static void flush() {
		// TODO Auto-generated method stub

	}

	public static void clear() {
		// TODO Auto-generated method stub

	}

	public static void set(String rnnDefaultexportmode, int idx) {
		// TODO Auto-generated method stub

	}

}
