/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.mpi;

/**
 * Holds a single value translation between an Enum and a database string value.
 * This is used as the basic element to create a ValueList.
 * 
 * @author Jim Grace
 */
public class Value {

    /** Corresponding enumerated value in the Java object. */
    private Enum val;
    /** Corresponding string value in the database. */
    private String db;

    /**
     * Constructor a Value object given how the value is represented
     * in the Java object and the database.
     * 
     * @param val enumerated value in the Java object.
     * @param db string value in the database. 
     */
    public Value(Enum val, String db) {
        this.val = val;
        this.db = db;
    }

    //
    // Note that we need only get methods for these values.
    // They are always set in the constructor.
    //
    public String getDb() {
        return db;
    }

    public Enum getVal() {
        return val;
    }
}
