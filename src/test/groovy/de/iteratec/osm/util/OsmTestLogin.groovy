package de.iteratec.osm.util

import grails.util.Holders

trait OsmTestLogin {

    static String getConfiguredUsername(){
        def login = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.test?.geb?.login
        return login?:"admin"
    }
    static String getConfiguredPassword(){
        def password = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.test?.geb?.password
        return password?:"password"
    }
}
