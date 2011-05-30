/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.mpi;

/**
 * Represents a name string value for matching.
 * <p>
 * Name matching extends string matching as follows: string matching can look
 * for approximate matches such as edit distance that can detect typographic errors.
 * Name matching does this too. But name matching also assumes that the item
 * to be matched is a spoken word that may be misspelled or alternately spelled
 * depending on how it sounds. So name matching may use approximate matching
 * based on word sounds.
 * 
 * @author Jim Grace
 */
public class NameMatch extends StringMatch {

    /** (modified) Soundex */
    private String soundex = null;
    /** Metaphone result 1 */
    private String metaphone1 = null;
    /** Metaphone result 2 */
    private String metaphone2 = null;

    /**
     * Construct a NameMatch from a name string.
     * <p>
     * Information about the name is extracted and stored ahead of time for quick matching.
     * For names coming from the database, this information is extracted when all the
     * database values are loaded into memory. Then a database value can be compared
     * more quickly with multiple searches. For names coming from the search terms,
     * this information is extracted before comparing the search terms with all
     * the database values. Then a search term can be compared more quickly with
     * multiple database values.
     *
     * @param original the name string to use in matching.
     */
    public NameMatch(String original) {
        super(original);
        //TODO: Set soundex, metaphone1, metaphone2.
    }

    /**
     * Find the score for a match between two names.
     * @param s The Scorecard in which to add the score.
     * @param m The other name to match against.
     */
    public void score(Scorecard s, NameMatch m) {
        super.score(s, m);
        //TODO: Test soundex, metaphone1, metaphone2.
    }
}
