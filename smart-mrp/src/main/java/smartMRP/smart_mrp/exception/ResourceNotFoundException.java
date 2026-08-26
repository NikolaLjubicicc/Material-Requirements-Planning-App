package smartMRP.smart_mrp.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " sa ID-jem " + id + " nije pronađen/a");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
