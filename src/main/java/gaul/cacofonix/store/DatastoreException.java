package gaul.cacofonix.store;

/**
 *
 * @author ashish
 */
public class DatastoreException extends Exception {

    public DatastoreException(String message) {
        super(message);
    }

    public DatastoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
