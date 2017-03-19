package de.iteratec.osm.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by jwi on 08/02/17.
 */

// Tag methods to destinct between REST call and normal requests
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface RestAction {

}