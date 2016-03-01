package de.iteratec.osm.csi

/**
 * This enum is used to decide on which value the csi data should be based
 */
enum CsiType {
        VISUALLY_COMPLETE, DOC_COMPLETE

        public static List<CsiType> getCsiTypes(CsiDashboardShowAllCommand command) {
           List<CsiType> csiTypes = []
            if(command.csiTypeVisuallyComplete) csiTypes<< VISUALLY_COMPLETE
            if(command.csiTypeDocComplete) csiTypes<< DOC_COMPLETE
            return csiTypes
        }

    @Override
    String toString() {
        return super.toString().toLowerCase().replaceAll("_"," ")
    }

}