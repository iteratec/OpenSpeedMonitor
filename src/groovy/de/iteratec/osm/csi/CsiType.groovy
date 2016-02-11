package de.iteratec.osm.csi

/**
 * This enum is used to decide on which value the csi data should be based
 */
enum CsiType {
        visually_complete, doc_complete, both

        public CsiType getCsiType(boolean vc, boolean dc) {
            if (vc) {
                if (dc) {
                    return both
                } else {
                    return visually_complete
                }
            } else if (dc) {
                return doc_complete
            } else {
                throw new UndefinedCsiTypeException(vc, dc)
            }
        }
}