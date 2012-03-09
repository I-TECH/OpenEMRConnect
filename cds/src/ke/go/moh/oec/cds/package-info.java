/* ***** BEGIN LICENSE BLOCK *****
 * Version: cds 1.1
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

/**
 * This application stores clinical documents and makes them available on request. 
 * It uses a MySQL database that consists of one table :the cds_store,  this table consists of six fields to 
 * store the destination of the notification, an XML blob of the data in the notification, a field to record 
 * information on whether or not the notification has been processed and fields to record when it was received 
 * and/ or processed. These proactive notifications, updates or alerts (information on migration, death, 
 * start of pregnancy,  end of pregnancy) that are sent to the facility where the client was last seen from 
 * the DSS Server and are stored in the standard HL7 format - XML data type. The MPI database which has been 
 * described below contains fields for information on migration, death, start of pregnancy,  end of pregnancy 
 * and these are updated by the oecsm also described below, when these fields have updated or new information, 
 * this data is sent by the MPI as a notification to the cds database of the facility where the client was last 
 * seen,  If that reception is not currently running, it will pick its list of alerts from the MPI, this 
 * information movement is automated. 
 */
package ke.go.moh.oec.cds;
