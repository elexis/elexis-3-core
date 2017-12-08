package ch.elexis.core.ui.icons;

public enum ImageSize {
	_16x16_DefaultIconSize(16, 16), _75x66_TitleDialogIconSize(75, 66), _7x8_OverlayIconSize(7, 8),
		_12x12_TableColumnIconSize(12, 12);
	
	final public String name;
	final public int width;
	final public int height;
	
	private ImageSize(int width, int height){
		this.width = width;
		this.height = height;
		this.name = width + "x" + height;
	}
}
