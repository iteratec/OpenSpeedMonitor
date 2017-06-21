/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.util

import grails.converters.JSON
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

/**
 * <p>
 * A collection of Utilities for controller purposes.
 * </p>
 *
 * <p>
 * Unless otherwise noted passing <code>null</code> as argument to any method
 * will cause a {@link NullPointerException} to be thrown.
 * </p>
 *
 * <p>
 * All methods of this class are pure functional (results depending on arguments
 * only).
 * </p>
 *
 * <p>
 * This class is not intended to be instantiated.
 * </p>
 *
 * @author mze
 * @since IT-106
 */
class ControllerUtils {
    static Logger log = LoggerFactory.getLogger(ControllerUtils.class)

    /**
     * <p>
     * Checks weather the current request is to be treated as an empty request.
     * Even request that contain some "grails magic keys" or language selector
     * but no context specific parameters should be treated as empty. Empty
     * means that the request is somewhat like the first request to the page
     * before the user made any selections on it.
     * </p>
     *
     * @param params
     *            The request parameters.
     * @return <code>true</code> if and only if the request should be treagted
     *         as empty, <code>false</code> else.
     */
    static boolean isEmptyRequest(Map<String, Object> params) {
        if (params.isEmpty()) {
            return true
        }

        Set<String> keys = new HashSet<String>(params.keySet())

        keys.remove("lang")
        keys.remove("action")
        keys.remove("controller")

        // We found the param named "format" in Grails 2.3.2 first, it is
        // initially set to null and has no effect. We use this name to, so we
        // only remove it, if the value is null
        // We do not check if it is really there, remove will handle it anyway.
        if (params.get("format") == null) {
            keys.remove("format")
        }

        return keys.isEmpty()
    }

    /**
     * Sends message with given httpStatus as http response and breaks action (no subsequent action code is executed).
     * @param response The servlet response
     * @param httpStatus Status code of the response
     * @param message The message to respond
     */
    static void sendSimpleResponseAsStream(HttpServletResponse response, HttpStatus httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status = httpStatus.value()

        try {
            response.getOutputStream().withWriter {
                it.write(message)
            }
        } catch (IOException e) {
            log.info("Error writing HTTP response stream", e)
        }
    }

    /**
     * <p>
     * Sends the object rendered as JSON. All public getters are used to
     * render the result. This call should be placed as last statement, the
     * return statement, of an action.
     * </p>
     *
     * @param response
     *         The servlet response
     *
     * @param objectToSend
     *         The object to render end to be sent to the client,
     *         not <code>null</code>.
     * @param prettyPrint
     *         Set to <code>true</code> if the JSON should be "pretty
     *         formated" (easy to read but larger file).
     * @throws NullPointerException
     *         if {@code objectToSend} is <code>null</code>.
     */
    static void sendObjectAsJSON(HttpServletResponse response, Object objectToSend, boolean prettyPrint = false) {
        JSON jsonObject = objectToSend as JSON
        jsonObject.setPrettyPrint(prettyPrint)
        sendSimpleResponseAsStream(response, HttpStatus.OK, jsonObject.toString())
    }

    /**
     * Avoid instances.
     */
    private ControllerUtils() {
    }
}
