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
package ke.go.moh.oec.adt.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import ke.go.moh.oec.adt.data.Column;
import ke.go.moh.oec.adt.data.RecordSource;
import ke.go.moh.oec.adt.exceptions.BadRecordSourceException;
import ke.go.moh.oec.lib.Mediator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @date Apr 29, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class ResourceManager {

    private static Map<String, Connection> connectionMap = new HashMap<String, Connection>();
    private final String DEFAULT_FILE_NAME = "record_sources.xml";

    public static Connection getDatabaseConnection(String name) throws SQLException {
        if (!connectionMap.containsKey(name)) {
            connectionMap.put(name, createDatabaseConnection(name));
        }
        return connectionMap.get(name);
    }

    public static String getSetting(String name) {
        return Mediator.getProperty(name);
    }

    public List<RecordSource> loadRecordSources() throws ParserConfigurationException, SAXException,
            IOException, BadRecordSourceException {
        return loadRecordSources(DEFAULT_FILE_NAME);
    }

    public List<RecordSource> loadRecordSources(String fileName) throws ParserConfigurationException,
            SAXException, IOException, BadRecordSourceException {
        List<RecordSource> recordSourceList = new ArrayList<RecordSource>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(fileName));
        document.getDocumentElement().normalize();
        NodeList recordSourceNodeList = document.getElementsByTagName("record_source");
        int recordSourceCount = recordSourceNodeList.getLength();
        List<Integer> orderList = new ArrayList<Integer>();
        for (int i = 0; i < recordSourceCount; i++) {
            Node recordSourceNode = recordSourceNodeList.item(i);
            if (recordSourceNode.getNodeType() == Node.ELEMENT_NODE) {
                RecordSource recordSource = createRecordSource(recordSourceNode);
                if (orderList.contains(recordSource.getOrder())) {
                    throw new BadRecordSourceException("The order " + recordSource.getOrder()
                            + " has been allocated to more than one record sources. Unique order expected.");
                }
                if (recordSourceList.contains(recordSource)) {
                    throw new BadRecordSourceException("Record source for "
                            + recordSource.getTableName() + " configured more than once in the xml "
                            + "configuration file. Only one instance is expected.");
                }
                recordSourceList.add(recordSource);
                orderList.add(recordSource.getOrder());
            }
        }
        Collections.sort(recordSourceList);
        return recordSourceList;
    }

    private static Connection createDatabaseConnection(String name) throws SQLException {
        Connection connection = null;
        try {
            if (name.equals("source")) {
                Class.forName(Mediator.getProperty("source.driver"));
                connection = DriverManager.getConnection(Mediator.getProperty("source.url"),
                        Mediator.getProperty("source.username"), Mediator.getProperty("source.password"));
            } else if (name.equals("shadow")) {
                Class.forName(Mediator.getProperty("shadow.driver"));
                connection = DriverManager.getConnection(Mediator.getProperty("shadow.url"),
                        Mediator.getProperty("shadow.username"), Mediator.getProperty("shadow.password"));
            }
        } catch (ClassNotFoundException cnfe) {
            Mediator.getLogger(ResourceManager.class.getName()).log(Level.SEVERE, null, cnfe);
        }
        return connection;
    }

    private RecordSource createRecordSource(Node recordSourceNode) throws BadRecordSourceException {
        Element recordSourceElement = (Element) recordSourceNode;
        RecordSource recordSource = new RecordSource(Integer.parseInt(readTagValue(recordSourceElement, "order")));
        recordSource.setRelationship(RecordSource.Relationship.valueOf(readTagValue(recordSourceElement, "relationship").toUpperCase().trim()));
        recordSource.setTableName(readTagValue(recordSourceElement, "table_name"));
        recordSource.setPrimaryKeyColumnMap(readKeyColumns(recordSourceElement, "primary_key_column"));
        recordSource.setForeignKeyColumnMap(readKeyColumns(recordSourceElement, "foreign_key_column"));
        recordSource.setCumulate(Boolean.parseBoolean(readTagValue(recordSourceElement, "cumulate")));
        String limitString = readTagValue(recordSourceElement, "limit");
        if (limitString != null) {
            recordSource.setLimit(Integer.parseInt(limitString));
        }
        List<Column> columnList = readOrdinaryColumns(recordSourceElement, "columns");
        if (!columnList.isEmpty()) {
            recordSource.setColumnList(columnList);
        }
        validateRecordSource(recordSource);
        return recordSource;
    }

    private Map<String, Column> readKeyColumns(Element pkListElement, String keyName) {
        Map<String, Column> primaryKeyColumnSet = new LinkedHashMap<String, Column>();
        NodeList nodeList = pkListElement.getElementsByTagName(keyName);
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Element element = (Element) nodeList.item(0);
            String name = readTagValue(element, "name");
            boolean quote = Boolean.parseBoolean(readTagValue(element, "quote"));
            primaryKeyColumnSet.put(name, new Column(name, quote));
        }
        return primaryKeyColumnSet;
    }

    private String readTagValue(Element parentElement, String tagName) {
        String tagValue = null;
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        Element element = (Element) nodeList.item(0);
        NodeList childNodeList = element.getChildNodes();
        Node childNode = (Node) childNodeList.item(0);
        if (childNode != null) {
            tagValue = childNode.getNodeValue();
        }
        return tagValue;
    }

    private List<Column> readOrdinaryColumns(Element parentElement, String tagName) {
        List<Column> columnList = new ArrayList<Column>();
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        Element element = (Element) nodeList.item(0);
        NodeList childNodeList = element.getChildNodes();
        if (childNodeList != null) {
            int length = childNodeList.getLength();
            for (int i = 0; i < length; i++) {
                Node childNode = (Node) childNodeList.item(i);
                if (childNode != null && childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    NodeList grandChildNodeList = childElement.getChildNodes();
                    Node grandChildNode = (Node) grandChildNodeList.item(0);
                    if (grandChildNode != null) {
                        columnList.add(new Column(grandChildNode.getNodeValue(), true));
                    }
                }
            }
        }
        return columnList;
    }

    private void validateRecordSource(RecordSource recordSource) throws BadRecordSourceException {
        if (recordSource.getOrder() == null) {
            throw new BadRecordSourceException("Null 'order' not allowed.");
        }
        if (recordSource.getTableName() == null) {
            throw new BadRecordSourceException("Null 'table name' not allowed.");
        }
        if (recordSource.getPrimaryKeyColumnMap() == null || recordSource.getPrimaryKeyColumnMap().isEmpty()) {
            throw new BadRecordSourceException("Null 'pk columns' not allowed.");
        }
        if (recordSource.getRelationship() == RecordSource.Relationship.SLAVE) {
            if (recordSource.getForeignKeyColumnMap() == null) {
                throw new BadRecordSourceException("Null 'fk columns' not allowed for record sources related to master.");
            }
        }
        if (recordSource.getColumnList() == null || recordSource.getColumnList().isEmpty()) {
            throw new BadRecordSourceException("Null 'columns' not allowed.");
        }
    }
}
