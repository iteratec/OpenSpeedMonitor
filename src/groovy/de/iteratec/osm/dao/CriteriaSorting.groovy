package de.iteratec.osm.dao

/**
 * This class defines the sorting in a hibernate criteria.
 *
 * @author Created by nkuhn on 15.10.15.
 */
class CriteriaSorting {
    enum SortOrder {

        ASC('asc'),
        DESC('desc');

        private final hibernateCriteriaRepresentation
        SortOrder(hibernateCriteriaRepresentation){
            this.hibernateCriteriaRepresentation = hibernateCriteriaRepresentation
        }
        public String getHibernateCriteriaRepresentation(){
            return this.hibernateCriteriaRepresentation
        }

    }
    /**
     * Whether or not to sort criteria.
     */
    Boolean sortingActive = true
    /**
     * Order, criteria should be sorted.
     */
    SortOrder sortOrder
    /**
     * Name of the attribute, criteria should be sorted by.
     */
    String sortAttribute
}
