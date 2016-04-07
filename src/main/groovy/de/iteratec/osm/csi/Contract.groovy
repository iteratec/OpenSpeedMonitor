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

package de.iteratec.osm.csi;


/**
 * <p>
 * A helper class for design-by-contract purposes.
 * </p>
 * 
 * <p>
 * All methods of this class are static, the class should not be inherited and
 * no instances should be created.
 * </p>
 * 
 * @author mze
 * @since IT-40
 */
public final class Contract {

	/**
	 * <p>
	 * Checks Precondition: Requires argument is not <code>null</code>.
	 * </p>
	 * 
	 * @param argumentsName
	 *            The name of the argument, not <code>null</code>, not
	 *            {@linkplain String#isEmpty() empty}.
	 * @param argument
	 *            The argument to proof to not be <code>null</code>.
	 * @throws NullPointerException
	 *             if the passed value for {@code argument} is <code>null</code>
	 *             which means the precondition is unsatisfied.
	 */
	public static void requiresArgumentNotNull(String argumentsName,
			final Object argument) throws NullPointerException {
		if (argumentsName == null || argumentsName.isEmpty()) {
			argumentsName = "(missing arguments name)";
		}

		if (argument == null) {
			throw new NullPointerException("The argument " + argumentsName
					+ " may not be null.");
		}
	}
	/**
	 * Checks whether entity-object has an not null id.
	 * @param entitiesName 
	 * @param entity
	 * @throws IllegalArgumentException
	 * 				If the ident()-method of entity-object returns null.
	 */
	public static void checkEntityHasAnId(String entitiesName, Object entity) throws IllegalArgumentException {
		if(entity.ident() == null) {
			throw new IllegalArgumentException('The entity '+entitiesName+' has no id (it was not saved before).')
		}
	}
}
