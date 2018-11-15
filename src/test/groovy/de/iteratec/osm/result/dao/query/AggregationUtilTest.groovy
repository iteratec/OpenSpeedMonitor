package de.iteratec.osm.result.dao.query

import spock.lang.Specification
import spock.lang.Unroll

class AggregationUtilTest extends Specification {

    @Unroll("GetPercentileInt repeated #i time")
    def "GetPercentileInt"() {
        given:
        int size = 1000000
        def list = new Random().ints(size,0, 9999).toArray().toList()
        int index = new Random().nextInt(size)

        when:
        long time = System.currentTimeMillis()
        int result = AggregationUtil.getPercentile( list, index )
        println("QuickSelect with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        time = System.currentTimeMillis()
        int actual = list.sort()[index]
        println("Sort with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        then:
        result == actual

        where:
        i << (0..4)
    }

    @Unroll("GetPercentileFloat repeated #i time")
    def "GetPercentileFloat"() {
        given:
        int size = 1000000
        def list = new ArrayList()
        def rand = new Random()
        for(i in 0..(size-1)){list.add(rand.nextFloat() * 10)}
        int index = new Random().nextInt(size)

        when:
        long time = System.currentTimeMillis()
        int result = AggregationUtil.getPercentile( list, index )
        println("QuickSelect with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        time = System.currentTimeMillis()
        int actual = list.sort()[index]
        println("Sort with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        then:
        result == actual

        where:
        i << (0..4)
    }

    @Unroll("GetPercentileMultipleDuplicates repeated #i time")
    def "GetPercentileMultipleDuplicates"() {
        given:
        int size = 100000
        def list = new Random().ints(size,0, 10).toArray().toList()
        int index = new Random().nextInt(size)

        when:
        long time = System.currentTimeMillis()
        int result = AggregationUtil.getPercentile( list, index )
        println("QuickSelect with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        time = System.currentTimeMillis()
        int actual = list.sort()[index]
        println("Sort with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        then:
        result == actual

        where:
        i << (0..4)
    }

    def "GetPercentileDuplicatesOnly"() {
        given:
        int size = 1000
        def list = [1] * size
        int index = new Random().nextInt(size)

        when:
        long time = System.currentTimeMillis()
        int result = AggregationUtil.getPercentile( list, index )
        println("QuickSelect with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        time = System.currentTimeMillis()
        int actual = list.sort()[index]
        println("Sort with ${list.size()} elements: ${(System.currentTimeMillis() - time)}ms")

        then:
        result == actual
    }
}
