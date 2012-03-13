/* ***** BEGIN LICENSE BLOCK *****
* Version: kisumuhdss 1.1
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
* 
* This application basically coordinates activities between oecsm and the MPI.
* <p>
* (1)    Obtains the data from the oecsm which has been described below and sends it to the MPI database.
* <p>
* (2)    Obtains a list of household members on demand.
* <p>
* Basically this component coordinates activities between the Oecsm (OpenEMRConnect Source Module) and the MPI (Master Person Index) by ensuring that the MPI is updated after the Oecsm has mined new data and updated the shadow database with new information. The kisumuhdss properties file is configured with an interval at which to poll the shadow database enabling it to keep track of changes. 
*
*/
package ke.go.moh.oec.kisumuhdss;
