/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.mpi;

import com.griaule.grfingerjava.GrFingerJava;
import com.griaule.grfingerjava.GrFingerJavaException;
import com.griaule.grfingerjava.MatchingContext;
import com.griaule.grfingerjava.Template;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Fingerprint;

/**
 * Represents a fingerprint for matching.
 * <p>
 * This class presently implements fingerprint matching using the Griaule Java SDK.
 * If other technologies are introduced in the future, this class could be
 * refactored to use subclasses for the various technologies.
 * <p>
 * If the Griaule SDK cannot be properly initialized because the current system
 * does not have a valid license, then fingerprint matching is done based on
 * whether the fingerprint template byte array exactly matches. This is not
 * useful for real fingerprint matching, but may be used for testing, where
 * the fingerprint searched for is an exact copy of one of the fingerprints
 * from the database.
 * 
 * @author Jim Grace
 */
public class FingerprintMatch implements Cloneable {

    private Fingerprint fingerprint;
    private Template grTemplate = null;
    private MatchingContext grMatchingContext = null;
    private boolean nonSdkMatch;
    static boolean grInitialized = false;
    static boolean useFingerprintSdk = false;

    private synchronized static void init() {
        if (!grInitialized) {
            useFingerprintSdk = true;
            File directory = new File(".");
            String dirName = directory.getAbsolutePath();
            GrFingerJava.setNativeLibrariesDirectory(directory);
            long startTime, elapsedTime;
            try {
                GrFingerJava.setLicenseDirectory(directory);
            } catch (GrFingerJavaException ex) {
                Logger.getLogger(FingerprintMatch.class.getName()).log(Level.WARNING,
                        "Griaule license not found or not valid -- fingerprinting will not be used.", ex);
                useFingerprintSdk = false;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(FingerprintMatch.class.getName()).log(Level.WARNING,
                        "Griaule license not found or not valid -- fingerprinting will not be used.", ex);
                useFingerprintSdk = false;
            }
            grInitialized = true;
        }
    }

    /**
     * Returns the maximum number of threads that can be used for fingerprint matching.
     * 
     * @return the maximum number of threads that can be used for fingerprint matching.
     */
    public static int maxThreadCount() {
        int returnCount = 0; // Unlimited unless we find out otherwise.
        if (!grInitialized) {
            init();
        }
        if (useFingerprintSdk) {
            returnCount = MatchingContext.getMaximumNumberOfContexts();
        }
        return returnCount;
    }

    /**
     * Construct a FingerprintMatch from a Fingerprint.
     * <p>
     * Information about the fingerprint is extracted and stored ahead of time for quick matching.
     * For fingerprints coming from the database, this information is extracted when all the
     * database values are loaded into memory. Then a database value can be compared
     * more quickly with multiple searches. For fingerprints coming from the search terms,
     * this information is extracted before comparing the search terms with all
     * the database values. Then a search term can be compared more quickly with
     * multiple database values.
     *
     * @param f The fingerprint that will be matched with.
     */
    public FingerprintMatch(Fingerprint f) {
        if (!grInitialized) {
            init();
        }
        fingerprint = f;
        if (useFingerprintSdk) {
            grTemplate = new Template(f.getTemplate());
        }
    }

    /**
     * Does a shallow clone of a FingerprintMatch object. This is
     * useful to copy the fingerprint template for use in multiple,
     * concurrent threads for matching. Each thread must have its own
     * prepared FingerprintMatch object.
     *
     * @return the cloned FingerprintMatch object.
     */
    @Override
    public FingerprintMatch clone() {
        try {
            return (FingerprintMatch) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    /**
     * Prepares a fingerprit to be matched as a search term. Note that
     * the comparison between a search term fingerprint and a database
     * fingerprint is not symmetric. The search term fingerprint requires
     * extra preparation. This method does that extra preparation.
     * Also, the search term fingerprint must be used by only one thread
     * (at a time). So for concurrent threads, each thread must have its own
     * prepared FingerprintMatch object.
     */
    public void prepare() {
        if (useFingerprintSdk) {
            try {
                grMatchingContext = new MatchingContext();
            } catch (GrFingerJavaException ex) {
                Logger.getLogger(FingerprintMatch.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                grMatchingContext.prepareForIdentification(grTemplate);
            } catch (GrFingerJavaException ex) {
                Logger.getLogger(FingerprintMatch.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(FingerprintMatch.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Match the (prepared) search fingerprint against a database fingerprint.
     *
     * @param fDatabase fingerprint from the database to match against.
     * @return True if the fingerprint matched, otherwise false.
     */
    public boolean match(FingerprintMatch fDatabase) {
        boolean match = false;
        if (useFingerprintSdk) {
            try {
                match = grMatchingContext.identify(fDatabase.grTemplate);
            } catch (GrFingerJavaException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            nonSdkMatch = Arrays.equals(fingerprint.getTemplate(), fDatabase.fingerprint.getTemplate());
            match = nonSdkMatch;
        }
        return match;
    }

    /**
     * Returns the score from the most recent match operation.
     * 
     * @return the score from the most recent match operation.
     */
    public int score() {
        int score = 0;
        if (useFingerprintSdk) {
            try {
                score = grMatchingContext.getScore();
            } catch (GrFingerJavaException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (score > 100) {
                score = 100;
            }
        } else if (nonSdkMatch) {
            score = 100;
        }
        return score;
    }
}
