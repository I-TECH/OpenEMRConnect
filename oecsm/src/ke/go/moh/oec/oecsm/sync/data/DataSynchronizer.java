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
package ke.go.moh.oec.oecsm.sync.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.oecsm.bridge.TransactionConverter;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.bridge.Transaction;
import ke.go.moh.oec.oecsm.data.LoggableTransaction;
import ke.go.moh.oec.oecsm.data.LoggableTransactionDatum;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class DataSynchronizer extends DatabaseConnector {

    public void synchronize() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        try {
            List<Transaction> dataTransactionList = new DataTransactionGenerator().generate();
            connectToShadow();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (Transaction dataTransaction : dataTransactionList) {
                System.out.println(TransactionConverter.convertToSQL(dataTransaction));
                if (statement.executeUpdate(TransactionConverter.convertToSQL(dataTransaction), Statement.RETURN_GENERATED_KEYS) == 1) {
                    if (dataTransaction.getClass() == LoggableTransaction.class) {
                        ResultSet rs = statement.getGeneratedKeys();
                        LoggableTransaction loggableTransaction = (LoggableTransaction) dataTransaction;
                        if (rs.next()) {
                            loggableTransaction.setId(rs.getInt(1));
                        }
                        for (LoggableTransactionDatum loggableTransactionDatum : loggableTransaction.getLoggableTransactionDatumList()) {
                            System.out.println(TransactionConverter.convertToSQL(loggableTransactionDatum));
                            statement.executeUpdate(TransactionConverter.convertToSQL(loggableTransactionDatum));
                        }
                    }
                }
            }
            connection.commit();
            statement.close();
        } catch (Exception ex) {
            Logger.getLogger(DataSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            disconnect();
        }
    }
}
