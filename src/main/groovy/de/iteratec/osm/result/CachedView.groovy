package de.iteratec.osm.result
/**
 * The cache-setting a {@link EventResult result} is based on.
 *
 * @author nkuhn
 */
public enum CachedView {
    /**
     * A WPT repeated view result; could have used some data out of the
     * browsers cache.
     */
    CACHED("rv-"),

    /**
     * A WPT first view result; the browsers cache have been cleared before,
     * all date was loaded from the web-server.
     */
    UNCACHED("fv-")

    static transients = ['cached']
    private String graphiteLabelPrefix

    private CachedView(String label){
        graphiteLabelPrefix = label
    }

    String getGraphiteLabelPrefix(){
        return graphiteLabelPrefix
    }

    /**
     * <p>
     * Determines weather this cached-view-state is a cached state (repeated
     * view) or an un-cached state (first view).
     * </p>
     *
     * @return <code>true</code> if this is a cached state,
     *         <code>false</code> else.
     */
    boolean isCached() {
        return this == CACHED;
    }
}