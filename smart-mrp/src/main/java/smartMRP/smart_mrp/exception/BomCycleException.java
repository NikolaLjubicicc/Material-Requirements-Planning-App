package smartMRP.smart_mrp.exception;

public class BomCycleException extends RuntimeException {

    public BomCycleException(Long parentId, Long componentId) {
        super("Detektovana ciklična zavisnost u sastavnici: artikal " + componentId +
              " ne može biti komponenta artikla " + parentId + " jer bi to stvorilo ciklus");
    }

    public BomCycleException(String message) {
        super(message);
    }
}
