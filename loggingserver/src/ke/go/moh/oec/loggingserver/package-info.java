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
/**
 * This application records at a central location the transactions performed by the other components. Events that 
 * occur at other packages such as the cds,esb/fsb,Kisumuhdss,reception,oecsm are recorded by the loggingserver 
 * for future reference, audit and debugging, this can be thought of as a centralized audit trail of what has been 
 * happening throughout the entire OEC system.
 * <p>
 * It also acts as a logging mechanism to collect and store messages that describe any event that may have 
 * occurred and caused an error, this is very helpful in maintenance of the system. System usage and errors 
 * can be known centrally and in detail, without having to be reported from the field. For the loggingserver 
 * to record an event, the application experiencing the event calls up the messaging module and passes 
 * information such as source of the message, severity, message, date/time. This information is stored in a 
 * central log file by the loggingserver.
 */
package ke.go.moh.oec.loggingserver;
