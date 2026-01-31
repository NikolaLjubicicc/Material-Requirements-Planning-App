package smartMRP.smart_mrp.exception;

public class ItemInUseException extends RuntimeException {

    public ItemInUseException(Long itemId) {
        super("Artikal sa ID-jem " + itemId + " se koristi u sastavnici i ne može biti obrisan");
    }

    public ItemInUseException(String sku) {
        super("Artikal '" + sku + "' se koristi u sastavnici i ne može biti obrisan");
    }
}
