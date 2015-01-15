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

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class I18nService {

	boolean transactional = false

	MessageSource messageSource

	/**
	 *
	 * @param msgKey
	 * @param defaultMessage default message to use if none is defined in the message source
	 * @param objs objects for use in the message
	 * @return
	 * 		Message from i18n-file respective given key.
	 */
	String msg(String msgKey, String defaultMessage = null, List objs = null) {
		
		def msg = messageSource.getMessage(msgKey,objs?.toArray(),defaultMessage,LocaleContextHolder.locale)

		if (msg == null || msg == defaultMessage) {
			log.warn("No i18n messages specified for msgKey: ${msgKey}")
			msg = defaultMessage
		}

		return msg
	}

	/**
	 * Methode to look like g.message
	 * @param args
	 * @return
	 */
	String message(Map args) {
		return msg(args.code, args.default, args.attrs)
	}
}
