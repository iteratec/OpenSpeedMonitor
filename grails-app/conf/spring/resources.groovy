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

package spring

import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import de.iteratec.osm.result.JobResultInsertListener
import io.swagger.models.Contact
import io.swagger.models.Info
import io.swagger.models.License
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import org.apache.commons.lang.StringUtils

// Place your Spring DSL code here

beans = {
    jobResultInsertListener(JobResultInsertListener)
    jobGroupDaoService(DefaultJobGroupDaoService)
    graphiteSocketProvider(DefaultGraphiteSocketProvider)

    swagger(Swagger) {
        Map swaggerConfig = (application.config.swagger as Map) ?: [:]
        Map infoConfig = swaggerConfig.info ?: [:]
        Info swaggerInfo = new Info(
                description: infoConfig.description ?: StringUtils.EMPTY,
                version: infoConfig.version ?: StringUtils.EMPTY,
                title: infoConfig.title ?: StringUtils.EMPTY,
                termsOfService: infoConfig.termsOfServices ?: StringUtils.EMPTY
        )
        Map contactConfig = infoConfig.contact ?: [:]
        swaggerInfo.setContact(new Contact(
                name: contactConfig.name ?: StringUtils.EMPTY,
                url: contactConfig.url ?: StringUtils.EMPTY,
                email: contactConfig.email ?: StringUtils.EMPTY)
        )
        Map licenseConfig = infoConfig.license ?: [:]
        swaggerInfo.license(new License(
                name: licenseConfig.name ?: StringUtils.EMPTY,
                url: licenseConfig.url ?: StringUtils.EMPTY)
        )
        info = swaggerInfo
        schemes = swaggerConfig.schemes ?: [Scheme.HTTP]
        consumes = swaggerConfig.consumes ?: ["application/json"]
    }
}
