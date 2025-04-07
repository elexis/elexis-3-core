package ch.elexis.core.ui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class OrderHistoryEntry {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

    @SerializedName("action")
    private String action;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("userId")
    private String userId;

    @SerializedName("details")
    private String details;

	@SerializedName("extraInfo")
	private String extraInfo;

	public OrderHistoryEntry(String action, String userId, String details, String extraInfo) {
        this.action = action;
        this.timestamp = LocalDateTime.now().format(TIME_FORMAT);
        this.userId = userId;
        this.details = details;
		this.extraInfo = extraInfo;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}
