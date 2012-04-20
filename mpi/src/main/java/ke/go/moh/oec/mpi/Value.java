/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OpenEMRConnect.
 *
 * The Initial Developer of the Original Code is International Training &
 * Education Center for Health (I-TECH) <http://www.go2itech.org/>
 *
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
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
