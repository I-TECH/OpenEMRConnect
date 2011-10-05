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
package ke.go.moh.oec.oecsm.logger;

import ke.go.moh.oec.oecsm.data.LoggableTransaction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ke.go.moh.oec.oecsm.data.LoggableTransactionDatum;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author JGitahi
 *
 * @date Sep 8, 2010
 */
public class XMLTransactionGenerator {

    public void generate(List<LoggableTransaction> transactionList)
            throws FileNotFoundException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        if (!transactionList.isEmpty()) {
            File transactionFile = new File("OECSM_Transactions");
            transactionFile.mkdir();
            PrintWriter out = new PrintWriter(new File("OECSM_Transactions/" + "OECSM Transaction No. " + transactionList.get(0).getId() + " - " + transactionList.get(transactionList.size() - 1).getId() + ".xml"));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            DOMImplementation dom = builder.getDOMImplementation();
            Document document = dom.createDocument(null, "transaction_log", null);
            Element root = document.getDocumentElement();

            for (LoggableTransaction transaction : transactionList) {
                Element logEntryElement = document.createElementNS(null, "log_entry");

                Element idElement = document.createElementNS(null, "id");
                Node idNode = document.createTextNode(String.valueOf(transaction.getId()));
                idElement.appendChild(idNode);
                logEntryElement.appendChild(idElement);

                Element typeElement = document.createElementNS(null, "type");
                Node typeNode = document.createTextNode(transaction.getType().toString());
                typeElement.appendChild(typeNode);
                logEntryElement.appendChild(typeElement);

                Element tableElement = document.createElementNS(null, "table");
                Node tableNode = document.createTextNode(transaction.getTable().getName());
                tableElement.appendChild(tableNode);
                logEntryElement.appendChild(tableElement);

                Element valuesElement = document.createElementNS(null, "values");
                for (LoggableTransactionDatum ltv : transaction.getLoggableTransactionDatumList()) {
                    Element columnElement = document.createElementNS(null, ltv.getCell().getColumn().getName());
                    Node valueNode = document.createTextNode(ltv.getCell().getData());
                    columnElement.appendChild(valueNode);
                    valuesElement.appendChild(columnElement);
                }
                logEntryElement.appendChild(valuesElement);

                root.appendChild(logEntryElement);
            }
            document.setXmlStandalone(true);
            document.getXmlEncoding();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            serializer.transform(domSource, streamResult);
        }
    }
}
