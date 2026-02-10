package smartMRP.smart_mrp.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Planirani nalog sa ID-jem " + orderId + " nije pronadjen");
    }
}
