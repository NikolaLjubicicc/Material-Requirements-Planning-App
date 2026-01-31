package smartMRP.smart_mrp.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Artikal sa ID-jem " + id + " nije pronađen");
    }

    public ItemNotFoundException(String sku) {
        super("Artikal sa SKU '" + sku + "' nije pronađen");
    }
}
