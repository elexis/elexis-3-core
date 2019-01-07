package ch.elexis.core.ui.dbcheck.contributions;

import ch.rgw.tools.TimeTool;

public class ReChargeTarmedOpenConsLastYear extends ReChargeTarmedOpenCons {
	
	@Override
	protected TimeTool getBeginOfYear(){
		TimeTool ret = super.getBeginOfYear();
		ret.set(TimeTool.YEAR, ret.get(TimeTool.YEAR) - 1);
		return ret;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Tarmed Leistungen aller offenen Konsutlationen des letzten Jahres neu verrechnen.";
	}
}
