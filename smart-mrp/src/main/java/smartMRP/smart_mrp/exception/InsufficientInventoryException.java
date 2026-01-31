package smartMRP.smart_mrp.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long itemId, Double required, Double available) {
        super("Nedovoljna zaliha za artikal ID " + itemId +
              ": potrebno " + required + ", dostupno " + available);
    }

    public InsufficientInventoryException(String sku, Double required, Double available) {
        super("Nedovoljna zaliha za artikal '" + sku +
              "': potrebno " + required + ", dostupno " + available);
    }
}
