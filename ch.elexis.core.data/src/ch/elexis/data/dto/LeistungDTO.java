package ch.elexis.data.dto;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class LeistungDTO {
	private final String id;
	private String code;
	private String text;
	private String priceText;
	private int count;
	private IVerrechenbar iVerrechenbar;
	private long lastUpdate;
	private Verrechnet verrechnet;
	
	private int tp = 0;
	private double tpw = 1.0;
	private double scale1 = 1.0;
	private double scale2 = 1.0;
	
	public LeistungDTO(Verrechnet verrechnet) throws ElexisException{
		
		if (!verrechnet.exists()) {
			throw new ElexisException(
				"Verrechnete Leistung wird ignoriert - Keine Leistung vorhanden [ID: "
					+ verrechnet.getId() + "].",
				new Exception());
		}
		try {
			if (verrechnet.getLastUpdate() < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new ElexisException(
				"Die verrechnete Leistung wird ignoriert - Datum der letzten Aktualisierung ist fehlerhaft [ID: "
					+ verrechnet.getId() + "].",
				e);
		}
		this.verrechnet = verrechnet;
		this.lastUpdate = verrechnet.getLastUpdate();
		this.id = verrechnet.getId();
		this.code = verrechnet.getCode();
		this.text = verrechnet.getText();
		this.tp = Verrechnet.checkZero(verrechnet.get(Verrechnet.SCALE_TP_SELLING));
		this.tpw = verrechnet.getTPW();
		this.count = verrechnet.getZahl();
		this.iVerrechenbar = verrechnet.getVerrechenbar();
	}
	
	public LeistungDTO(IVerrechenbar iVerrechenbar, IFall fall){
		this.lastUpdate = System.currentTimeMillis();
		this.id = iVerrechenbar.getId();
		this.code = iVerrechenbar.getCode();
		this.text = iVerrechenbar.getText();
		this.tp = iVerrechenbar.getTP(new TimeTool(), fall);
		this.tpw = 1.0;
		this.scale1 = 1.0;
		this.scale2 = 1.0;
		this.count = 1;
		this.iVerrechenbar = iVerrechenbar;
	}
	
	public void calcPrice(KonsultationDTO konsultationDTO, FallDTO fallDTO){
		if (getVerrechnet() != null) {
			tpw = getVerrechnet().getVerrechenbar()
				.getFactor(new TimeTool(konsultationDTO.getDate()), fallDTO);
		}
		else if (iVerrechenbar != null) {
			tpw = iVerrechenbar.getFactor(new TimeTool(konsultationDTO.getDate()), fallDTO);
		}
		if (verrechnet != null) {
			scale1 = verrechnet.getPrimaryScaleFactor();
			scale2 = verrechnet.getSecondaryScaleFactor();
		}
	}
	
	public void setTp(int tp){
		this.tp = tp;
	}
	
	public void setScale2(double scale2){
		this.scale2 = scale2;
	}
	
	public double getScale2(){
		return scale2;
	}
	
	public void setPriceText(String priceText){
		this.priceText = priceText;
	}
	
	public String getPriceText(){
		return priceText;
	}
	
	public Verrechnet getVerrechnet(){
		return verrechnet;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getText(){
		return text;
	}
	
	public void setVerrechnet(Verrechnet verrechnet){
		this.verrechnet = verrechnet;
	}
	
	public String getId(){
		return id;
	}
	
	public Money getPrice(){
		return new Money((int) (Math.round(tp * tpw) * scale1 * scale2 * count));
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setiVerrechenbar(IVerrechenbar iVerrechenbar){
		this.iVerrechenbar = iVerrechenbar;
	}
	
	public IVerrechenbar getIVerrechenbar(){
		return iVerrechenbar;
	}
	
	public long getLastUpdate(){
		return lastUpdate;
	}
	
	public int getTp(){
		return tp;
	}
	
	public double getTpw(){
		return tpw;
	}
}
