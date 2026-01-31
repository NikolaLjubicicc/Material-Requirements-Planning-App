package smartMRP.smart_mrp.entity;

/**
 * Status plana proizvodnje.
 */
public enum PlanStatus {
    PENDING,      // Čeka obradu
    PROCESSING,   // U obradi (MRP kalkulacija u toku)
    COMPLETED,    // Završeno (nalozi generisani)
    CANCELLED     // Otkazano
}
