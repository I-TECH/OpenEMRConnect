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
package ke.go.moh.oec.oecsm.sync.schema;

import java.sql.ResultSet;
import ke.go.moh.oec.oecsm.bridge.TransactionConverter;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.data.SchemaTransaction;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 13, 2010
 *
 * @author JGitahi
 */
public class SchemaSynchronizer extends DatabaseConnector {

    public void synchronize() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        try {
            List<SchemaTransaction> schemaTransactionList = new SchemaTransactionGenerator().generate();
            connectToShadow();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (SchemaTransaction schemaTransaction : schemaTransactionList) {
                System.out.println(TransactionConverter.convertToSQL(schemaTransaction));
                if (statement.executeUpdate(TransactionConverter.convertToSQL(schemaTransaction), Statement.RETURN_GENERATED_KEYS) == 1) {
                    ResultSet rs = statement.getGeneratedKeys();
                    if (rs.next()) {
                        if (schemaTransaction.getTarget().getClass() == Database.class) {
                            ((Database) schemaTransaction.getTarget()).setId(rs.getInt(1));
                        } else if (schemaTransaction.getTarget().getClass() == Table.class) {
                            ((Table) schemaTransaction.getTarget()).setId(rs.getInt(1));
                        } else if (schemaTransaction.getTarget().getClass() == Column.class) {
                            ((Column) schemaTransaction.getTarget()).setId(rs.getInt(1));
                        }
                    }
                }
            }
            connection.commit();
            statement.close();
        } finally {
            disconnect();
        }
    }
}
