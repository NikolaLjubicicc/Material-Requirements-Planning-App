package smartMRP.smart_mrp.exception;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("Artikal sa SKU '" + sku + "' već postoji");
    }
}
