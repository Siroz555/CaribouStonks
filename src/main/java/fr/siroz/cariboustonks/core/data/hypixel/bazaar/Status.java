package fr.siroz.cariboustonks.core.data.hypixel.bazaar;

/**
 * Un résumé calculé de l'état actuel de l'item.
 *
 * @param sellPrice      la moyenne pondérée des 2% de commandes les plus importantes en volume
 * @param sellVolume     la somme des montants des items dans toutes les orders
 * @param sellMovingWeek le volume historique des transactions des 7 derniers jours + l'état en direct
 * @param sellOrders     le nombre des orders actives
 * @param buyPrice       la moyenne pondérée des 2% des orders les plus importantes en volume
 * @param buyVolume      la somme des montants des items dans toutes les orders
 * @param buyMovingWeek  le volume historique des transactions des 7 derniers jours + l'état en direct
 * @param buyOrders      le nombre des orders actives
 */
public record Status(
        double sellPrice,
        long sellVolume,
        long sellMovingWeek,
        long sellOrders,
        double buyPrice,
        long buyVolume,
        long buyMovingWeek,
        long buyOrders) {
}
