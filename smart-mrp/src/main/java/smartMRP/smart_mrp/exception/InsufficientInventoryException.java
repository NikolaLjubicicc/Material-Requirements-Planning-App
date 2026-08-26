package smartMRP.smart_mrp.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long itemId, Double required, Double available) {
        super("Nedovoljna zaliha za artikal ID " + itemId +
              ": potrebno " + formatNumber(required) + ", dostupno " + formatNumber(available));
    }

    public InsufficientInventoryException(String itemName, Double required, Double available) {
        super("Nedovoljna zaliha za artikal '" + itemName +
              "': potrebno " + formatNumber(required) + ", dostupno " + formatNumber(available));
    }

    private static String formatNumber(Double value) {
        if (value == null) return "0";
        if (value == Math.floor(value)) {
            return String.valueOf(value.intValue());
        }
        return String.format("%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
