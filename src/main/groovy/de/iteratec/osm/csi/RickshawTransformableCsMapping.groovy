package de.iteratec.osm.csi

/**
 * Mappings build from entities of this type can be transfomed for representation in rickshaw charts.
 * @Author Created by nkuhn on 07.09.15.
 */
interface RickshawTransformableCsMapping {
    /**
     * Name of the group, this transformable mapping is associated to. All CsMappings with the same
     * name build one mapping rule.
     */
    public String retrieveGroupingCriteria()
    /**
     * Load time of this mapping.
     */
    public Integer retrieveLoadTimeInMilliSecs()
    /**
     * Customer satisfaction of this mapping.
     */
    public Double retrieveCustomerSatisfactionInPercent()
}