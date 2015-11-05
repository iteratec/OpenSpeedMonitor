package de.iteratec.osm.dao

import grails.orm.HibernateCriteriaBuilder

/**
 * Aggregates query criterias for a Domain Class.
 * From: http://blog.serindu.com/2014/02/05/criteria-aggregator-dynamic-criteria-queries-in-grails/
 */
class CriteriaAggregator {

    private Class forClass
    private List<Closure> criteriaClosures

    /**
     * @param forClass Class of the grails domain criterias should be aggregated for.
     */
    public CriteriaAggregator(Class forClass) {
        this.forClass = forClass;
        criteriaClosures = new ArrayList<Closure>(10)
    }

    /**
     * Adds another criteria to the list
     *
     * @param criteriaClosure The exact same type of closure you'd pass to DomainClass.withCriteria(criteriaClosure).
     */
    public void addCriteria(Closure criteriaClosure) {
        criteriaClosures << criteriaClosure
    }

    public long count() {
        return runQuery('get', [:]) {projections {rowCount()}}
    }
    public def get(Closure additionalCriteria=null) {
        return runQuery('get', [:], additionalCriteria)
    }
    public def list(Closure additionalCriteria=null) {
        return runQuery('list', [:], additionalCriteria)
    }

    private def runQuery(String method, Map criteriaRestrictions, Closure additionalCriteria=null) {
        HibernateCriteriaBuilder criteriaBuilder = forClass.createCriteria()
        def critClosures = criteriaClosures // Bizarre that criteriaClosures won't evaluate properly inside the "$method" closure, but it won't so this works around that issue
        criteriaBuilder."$method" (criteriaRestrictions){
            critClosures.each{closure ->
                closure.delegate = criteriaBuilder
                closure()
            }
            if (additionalCriteria) {
                additionalCriteria.delegate = criteriaBuilder
                additionalCriteria()
            }
        }
    }

}
