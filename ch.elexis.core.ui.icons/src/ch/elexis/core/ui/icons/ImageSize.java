package ch.elexis.core.ui.icons;

public enum ImageSize {
	_16x16_DefaultIconSize("16x16"), _75x66_TitleDialogIconSize("75x66"), _7x8_OverlayIconSize(
		"7x8");
	
	public String name;
	
	private ImageSize(String name){
		this.name = name;
	}
}
