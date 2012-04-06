package ke.go.moh.oec.pisinterfaces.util;

/**
 * Exception used to carry problems to top for user notification.
 */
public class SiteException extends Exception {

    /**
     * Add message to exception thrown, generally used in a catch block for a 
     * specific error situation, to provide a more user friendly message.
     * 
     * @param message to display to user
     * @param cause the caught exception being amended
     */
    public SiteException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
