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
package ke.go.moh.oec.oecsm.data;

import java.util.List;
import ke.go.moh.oec.oecsm.bridge.Transaction;

/**
 * @author Gitahi Ng'ang'a
 *
 * @date Sep 10, 2010
 */
public class LoggableTransaction extends Transaction {

    private int id;
    private Table table;
    private List<LoggableTransactionDatum> loggableTransactionDatumList;

    public LoggableTransaction() {
    }

    public LoggableTransaction(TransactionType type) {
        this.type = type;
    }

    public LoggableTransaction(Table table, TransactionType type) {
        this.table = table;
        this.type = type;
    }

    public LoggableTransaction(int id, Table table, TransactionType type) {
        this.id = id;
        this.table = table;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<LoggableTransactionDatum> getLoggableTransactionDatumList() {
        return loggableTransactionDatumList;
    }

    public void setLoggableTransactionDatumList(List<LoggableTransactionDatum> loggableTransactionDatumList) {
        this.loggableTransactionDatumList = loggableTransactionDatumList;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
