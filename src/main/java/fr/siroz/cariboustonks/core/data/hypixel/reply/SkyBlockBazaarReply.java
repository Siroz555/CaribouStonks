package fr.siroz.cariboustonks.core.data.hypixel.reply;

import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;

import java.util.Map;

public class SkyBlockBazaarReply {

    private boolean success;
    private String cause;
    private long lastUpdated;
    private Map<String, Product> products;

    public boolean isSuccess() {
        return success;
    }

    public String getCause() {
        return cause;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Map<String, Product> getProducts() {
        return products;
    }
}
