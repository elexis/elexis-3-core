package ch.elexis.core.model;

public enum OrderEntryState {
	OPEN(0), ORDERED(1), PARTIAL_DELIVER(2), DONE(3), MARKED(4);

	private final Integer value;

	private OrderEntryState(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public static OrderEntryState ofValue(int value) {
		for (OrderEntryState state : values()) {
			if (state.getValue() == value) {
				return state;
			}
		}
		return null;
	}
}
