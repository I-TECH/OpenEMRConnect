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
package ke.go.moh.oec;

/**
 * This interface is used for passing data requests and responses between the
 * OpenEMRConnect (OEC) library and software using that library.
 * <p>
 * For client requests, this interface is implemented by the OEC library for the
 * use of clients who have requests to send to a server.
 * <p>
 * For server software, this interface is implemented by the server object
 * that will receive the request. If user software is only making client
 * requests, it need not implement this interface.
 *
 * @author Jim Grace
 */
public interface IService {

    /**
     * Request data through the service interface.
     * <p>
     * A client calls the getData method of the OEC library. In the library,
     * this method will take the request, package it into a HL7 message, and
     * deliver it to the server.
     * <p>
     * On the server side, this method is also implemented by the server software
     * object that will receive the request.
     * When the OEC library receives a request destined for this server, it
     * unpacks the HL7 message, and delivers it to the object's getData method.
     * To accomplish this, the server first registers the object to receive
     * requests using {@link edu.uw.itech.oec.lib.Mediator#registerCallback}
     *
     * @param requestType type of request; constant from class {@link RequestType}
     * @param requestData request data; see {@link RequestType} Field Detail for descriptions
     * @return return data; see {@link RequestType} Field Detail for descriptions
     */
    public Object getData(int requestTypeId, Object requestData);
}
