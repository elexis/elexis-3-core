package ch.elexis.data.dto;

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class LeistungDTO {
	private final String id;
	private String code;
	private String text;
	private Money price;
	private String priceText;
	private Double priceSecondaryScaleFactor;
	private int count;
	private IVerrechenbar iVerrechenbar;
	private long lastUpdate;
	private Verrechnet verrechnet;
	
	private Money customPrice;
	
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
		this.lastUpdate = verrechnet.getLastUpdate();
		this.id = verrechnet.getId();
		this.code = verrechnet.getCode();
		this.text = verrechnet.getText();
		this.price = verrechnet.getNettoPreis();
		this.count = verrechnet.getZahl();
		this.iVerrechenbar = verrechnet.getVerrechenbar();
		this.verrechnet = verrechnet;
		this.customPrice = null;
	}
	
	public LeistungDTO(IVerrechenbar iVerrechenbar){
		this.lastUpdate = System.currentTimeMillis();
		this.id = iVerrechenbar.getId();
		this.code = iVerrechenbar.getCode();
		this.text = iVerrechenbar.getText();
		
		this.price = new Money();
		this.count = 1;
		this.iVerrechenbar = iVerrechenbar;
		this.customPrice = null;
		
	}
	
	public void calcPrice(KonsultationDTO konsultationDTO, FallDTO fallDTO){
		int tp = 0;
		double factor = 1.0;
		double scale1 = 1.0;
		double scale2 = 1.0;
		if (iVerrechenbar != null) {
			tp = iVerrechenbar.getTP(new TimeTool(konsultationDTO.getDate()), fallDTO);
			factor = iVerrechenbar.getFactor(new TimeTool(konsultationDTO.getDate()), fallDTO);
		} else if (getVerrechnet() != null) {
			tp = Verrechnet.checkZero(getVerrechnet().get(Verrechnet.SCALE_TP_SELLING));
		}
		
		if (verrechnet != null) {
			scale1 = verrechnet.getPrimaryScaleFactor();
			scale2 = verrechnet.getSecondaryScaleFactor();
		}
		if (priceSecondaryScaleFactor != null) {
			scale2 = priceSecondaryScaleFactor.doubleValue();
		}
		this.price = new Money((int) (Math.round(tp * factor) * scale1 * scale2 * count));
	}
	
	public void setPriceSecondaryScaleFactor(Double priceSecondaryScaleFactor){
		this.priceSecondaryScaleFactor = priceSecondaryScaleFactor;
	}
	
	public Double getPriceSecondaryScaleFactor(){
		return priceSecondaryScaleFactor;
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
		if (getCustomPrice() == null) {
			return price;
		}
		return getCustomPrice();
	}
	
	public void setPrice(Money price){
		this.price = price;
	}
	
	public void setCustomPrice(Money customPrice){
		this.customPrice = customPrice;
	}
	
	private Money getCustomPrice(){
		return customPrice;
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
}
