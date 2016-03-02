package de.iteratec.osm.csi

/**
 * Created by bwo on 11.02.16.
 */
class UndefinedCsiTypeException extends RuntimeException {


   public UndefinedCsiTypeException (boolean vc, boolean dc) {
        super("Undefined csi type: visually complete:$vc, doc complete:$dc")
    }
}
