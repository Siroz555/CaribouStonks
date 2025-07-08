package fr.siroz.cariboustonks.core.data.hypixel.reply;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class SkyBlockItemsReply {

    private final boolean success;
    private final String cause;
    private final long lastUpdated;
    private final JsonObject response;

    public SkyBlockItemsReply(@NotNull JsonObject response) {
        this.response = response;
        this.success = response.has("success") && response.get("success").getAsBoolean();
        this.cause = response.has("cause") ? response.get("cause").getAsString() : null;
        this.lastUpdated = response.has("lastUpdated") ? response.get("lastUpdated").getAsLong() : -1;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCause() {
        return cause;
    }

    public JsonObject getResponse() {
        if (response == null || response.isJsonNull()) {
            return null;
        } else {
            return response.getAsJsonObject();
        }
    }

    /**
     * Gets unix time when the resource was updated.
     * Will return -1 if last updated was not included in response
     *
     * @return long unix time
     */
    public long getLastUpdated() {
        return lastUpdated;
    }
}
