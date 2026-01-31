package smartMRP.smart_mrp.exception;

/**
 * Generički exception za sve resurse koji nisu pronađeni.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " sa ID-jem " + id + " nije pronađen/a");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
