package de.iteratec.osm.measurement.environment.wptserverproxy

/**
 * Created by nkuhn on 29.05.15.
 */
enum Protocol{
    HTTP(80, 'http://'),
    HTTPS(443, 'https://');

    private final int defaultPort
    private final String scheme

    Protocol(int defaultPort, String scheme){
        this.defaultPort = defaultPort
        this.scheme = scheme
    }

    public int defaultPort(){
        return this.defaultPort
    }
    public String scheme(){
        return this.scheme
    }
}