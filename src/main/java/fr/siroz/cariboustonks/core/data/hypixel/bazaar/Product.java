package fr.siroz.cariboustonks.core.data.hypixel.bazaar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Représente les informations sur un Item dans le Bazaar.
 *
 * @param productId   ID de l'item SkyBlock
 * @param sellSummary les 30 premières orders actuelles pour le type de transaction SELL
 * @param buySummary  les 30 premières orders actuelles pour le type de transaction BUY
 * @param quickStatus résumé calculé de l'état actuel de l'item
 */
public record Product(
        @SerializedName("product_id") String productId,
        @SerializedName("sell_summary") List<Summary> sellSummary,
        @SerializedName("buy_summary") List<Summary> buySummary,
        @SerializedName("quick_status") Status quickStatus) {
}
