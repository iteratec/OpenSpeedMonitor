package de.iteratec.osm.csi

/**
 * This enum is used to decide on which value the csi data should be based
 */
enum CsiType {
        visually_complete, doc_complete

        public static List<CsiType> getCsiTypes(CsiDashboardShowAllCommand command) {
           def csiTypes = []
            if(command.csiTypeVisuallyComplete) csiTypes<< visually_complete
            if(command.csiTypeDocComplete) csiTypes<< doc_complete
            return csiTypes
        }

    @Override
    String toString() {
        return super.toString().replaceAll("_"," ")
    }
}