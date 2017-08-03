package de.iteratec.osm.result

import de.iteratec.osm.util.Constants


class Selected {
    String name
    CachedView cachedView
    SelectedType selectedType

    @Override
    String toString(){
        return this.name + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }

    Double getNormalizedValueFrom(EventResult eventResult){
        return eventResult.getNormalizedValueFor(selectedType.getEventResultParam.call(name))
    }

    MeasurandGroup getMeasurandGroup(){
        return selectedType.getMeasurandGroup.call(name)
    }
}
