package ch.elexis.core.model;


public enum OrderHistoryAction {
	CREATED("📌", Messages.OrderHistory_Created), ADDMEDI("➕", Messages.OrderHistory_AddMedi),
	EDITED("🖊️", Messages.OrderHistory_Edited), DELIVERED("📦", Messages.OrderHistory_Delivered),
	ORDERED("📤", Messages.OrderHistory_Ordered), DELETED("🗑️", Messages.OrderHistory_Deleted),
	REMOVEDMEDI("❌💊", Messages.OrderHistory_RemovedMedi), SUPPLIERADDED("🏭", Messages.OrderHistory_SupplierAdded),
	COMPLETEDELIVERY("✅", Messages.OrderHistory_CompleteDelivery), ADDED("🆕", Messages.OrderHistory_Added),
	INCREASED("🔼", Messages.OrderHistory_Increased), DECREASED("🔽", Messages.OrderHistory_Decreased),
	BILLED("💰", Messages.OrderHistory_Billed), PICKEDUP("🤝", Messages.OrderHistory_PickedUp),
	AMOUNTADJUSTED("🔢", Messages.OrderHistory_AmountAdjusted);

	private final String icon;
	private final String translation;

	OrderHistoryAction(String icon, String translation) {
		this.icon = icon;
		this.translation = translation;
	}

	public String getIcon() {
		return icon;
	}

	public String getTranslation() {
		return translation;
	}

}
