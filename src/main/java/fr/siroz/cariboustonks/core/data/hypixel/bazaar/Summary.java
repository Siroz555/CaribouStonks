package fr.siroz.cariboustonks.core.data.hypixel.bazaar;

/**
 * Représente un résumé d'une transaction.
 *
 * @param amount       la quantité totale impliquée
 * @param pricePerUnit le prix par unité individuelle
 * @param orders       le nombre total de orders
 */
public record Summary(long amount, double pricePerUnit, long orders) {
}
