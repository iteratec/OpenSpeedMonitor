package de.iteratec.osm.measurement.environment.wptserverproxy

/**
 * Created by nkuhn on 29.05.15.
 */
enum Protocol{
    HTTP(80, 'http://'),
    HTTPS(443, 'https://')

    Protocol(int defaultPort, String scheme){
        this.defaultPort = defaultPort
        this.scheme = scheme
    }
    private int defaultPort
    private String scheme

    public int getDefaultPort(){
        return this.defaultPort
    }
    public String getScheme(){
        return this.scheme
    }
}