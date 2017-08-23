package de.iteratec.osm.result

import de.iteratec.osm.util.Constants

class SelectedMeasurand{
    Measurand measurand
    CachedView cachedView

    @Override
    String toString(){
        return this.measurand.toString() + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }
}