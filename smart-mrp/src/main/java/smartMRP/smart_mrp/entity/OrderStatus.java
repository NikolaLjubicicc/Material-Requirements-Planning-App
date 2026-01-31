package smartMRP.smart_mrp.entity;

/**
 * Status planiranog naloga.
 */
public enum OrderStatus {
    PLANNED,      // Planiran (još nije pokrenut)
    RELEASED,     // Pušten u realizaciju
    IN_PROGRESS,  // U toku
    COMPLETED,    // Završen
    CANCELLED     // Otkazan
}
