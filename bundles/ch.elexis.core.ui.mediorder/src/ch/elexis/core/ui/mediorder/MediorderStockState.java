package ch.elexis.core.ui.mediorder;

import ch.elexis.core.interfaces.ILocalizedEnum;

public enum MediorderStockState implements ILocalizedEnum {

	ENABLED_FOR_PEA, READY, PARTIALLY_READY, IN_PROGRESS, MAIL_SENT;

	@Override
	public String getLocaleText() {
		return switch (this) {
		case ENABLED_FOR_PEA -> "Für PEA freigegeben";
		case READY -> "Bereit";
		case PARTIALLY_READY -> "Teilweise bereit";
		case IN_PROGRESS -> "In Bearbeitung";
		case MAIL_SENT -> "E-Mail versendet";
		default -> throw new IllegalArgumentException("Unexpected value: " + this);
		};
	}
}