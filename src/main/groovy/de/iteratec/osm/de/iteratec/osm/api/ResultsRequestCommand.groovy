package de.iteratec.osm.de.iteratec.osm.api

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import grails.validation.Validateable

import javax.persistence.NoResultException

/**
 * <p>
 * A request to receive results via REST-API.
 * </p>
 *
 * @author mze
 * @since IT-81
 */
public class ResultsRequestCommand implements Validateable{

    /**
     * <p>
     * The start of the time-range for that results should be delivered;
     * this time-stamp is to be treated as inclusive. The format must
     * satisfy the format specified in ISO8601.
     * </p>
     *
     * <p>
     * Not <code>null</code>; not {@linkplain String#isEmpty() empty}.
     * </p>
     *
     * @see org.joda.time.format.ISODateTimeFormat
     */
    String timestampFrom;

    /**
     * <p>
     * The end of the time-range for that results should be delivered;
     * this time-stamp is to be treated as inclusive. The format must
     * satisfy the format specified in ISO8601.
     * </p>
     *
     * <p>
     * Not <code>null</code>; not {@linkplain String#isEmpty() empty}.
     * </p>
     *
     * @see org.joda.time.format.ISODateTimeFormat
     */
    String timestampTo;

    /**
     * <p>
     * The name of the system (CSI Group/Folder/Shop) for that results
     * should be delivered.
     * </p>
     *
     * <p>
     * Not <code>null</code>; not {@linkplain String#isEmpty() empty}.
     * </p>
     *
     * @see de.iteratec.osm.measurement.schedule.JobGroup
     * @see de.iteratec.osm.measurement.schedule.JobGroup#getName()
     */
    String system;

    /**
     * <p>
     * The page for that results should be delivered. If <code>null</code>
     * or {@linkplain String#isEmpty() empty} results for all pages will be
     * delivered.
     * </p>
     *
     * @see de.iteratec.osm.csi.Page
     * @see de.iteratec.osm.csi.Page#getName()
     */
    String page;

    /**
     * <p>
     * The id of the page for that results should be delivered. If <code>null</code>
     * or {@linkplain String#isEmpty() empty} results for all pages will be
     * delivered.
     * </p>
     *
     * @see de.iteratec.osm.csi.Page
     * @see de.iteratec.osm.csi.Page#ident()
     */
    String pageId;

    /**
     * <p>
     * The step (measured event) for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all steps will be delivered.
     * </p>
     *
     * @see de.iteratec.osm.result.MeasuredEvent
     * @see de.iteratec.osm.result.MeasuredEvent#getName()
     */
    String step;

    /**
     * <p>
     * The id of the step (measured event) for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all steps will be delivered.
     * </p>
     *
     * @see de.iteratec.osm.result.MeasuredEvent
     * @see de.iteratec.osm.result.MeasuredEvent#ident()
     */
    String stepId;

    /**
     * <p>
     * The browser for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all browser will be delivered.
     * </p>
     *
     * @see de.iteratec.osm.measurement.environment.Browser
     * @see de.iteratec.osm.measurement.environment.Browser#getName()
     */
    String browser;

    /**
     * <p>
     * The id of the browser for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all browser will be delivered.
     * </p>
     *
     * @see de.iteratec.osm.measurement.environment.Browser
     * @see de.iteratec.osm.measurement.environment.Browser#getName()
     */
    String browserId;

    /**
     * <p>
     * The location for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all locations will be delivered.
     * </p>
     *
     * @see de.iteratec.osm.measurement.environment.Location
     * @see de.iteratec.osm.measurement.environment.Location#getLocation()
     */
    String location;

    /**
     * <p>
     * The id of the location for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all locations will be delivered.
     * </p>
     *
     * @see de.iteratec.osm.measurement.environment.Location
     * @see de.iteratec.osm.measurement.environment.Location#getLocation()
     */
    String locationId;

    /**
     * Whether or not to pretty-print the json-response.
     */
    Boolean pretty

    /**
     * If this query parameter exist, only EventResults with this CachedView are returned.
     */
    CachedView cachedView

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        timestampFrom(nullable: false, blank: false)
        timestampTo(nullable: false, blank: false)
        system(nullable: false, blank: false)
        page(nullable: true, blank: true)
        pageId(nullable: true, blank: true)
        step(nullable: true, blank: true)
        stepId(nullable: true, blank: true)
        browser(nullable: true, blank: true)
        browserId(nullable: true, blank: true)
        location(nullable: true, blank: true)
        locationId(nullable: true, blank: true)
        pretty(nullable: true, blank: true)
        cachedView(nullable: true, blank: true)
    }

    static transients = ['cachedViewsToReturn']

    /**
     * <p>
     * Creates {@link de.iteratec.osm.result.MvQueryParams} based on this command. This command
     * need to be valid for this operation to be successful.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     * @throws javax.persistence.NoResultException
     *         if at least one of the specified parameters (job group, page,
     *         event, location) could not be found.
     */
    public MvQueryParams createMvQueryParams(
            BrowserService browserService
    ) throws IllegalStateException, NoResultException {

        if (!this.validate()) {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        MvQueryParams result = new MvQueryParams();

        // system
        addJobGroupQueryData(result)

        addPageQueryData(result)

        addStepQueryData(result)

        addBrowserQueryData(browserService, result)

        addLocationQueryData(result)

        return result;
    }

    private void addJobGroupQueryData(MvQueryParams result) {
        JobGroup theSystem = JobGroup.findByName(system)
        if (theSystem == null) {
            throw new NoResultException("Can not find CSI system named: " + system);
        }
        result.jobGroupIds.add(theSystem.getId());
    }

    private addPageQueryData(MvQueryParams result){
        if (pageId) {
            pageId.tokenize(",").each {singlePageId->
                if (!singlePageId.isLong()){
                    throw new NoResultException("Parameter pageId must be an Integer.");
                }
                Page thePage = Page.get(singlePageId)
                if (thePage == null) {
                    throw new NoResultException("Can not find Page with ID: " + singlePageId);
                }
                result.pageIds.add(thePage.getId());
            }
        }else if (page) {
            page.tokenize(",").each { singlePageName ->
                Page thePage = Page.findByName(singlePageName)
                if (thePage == null) {
                    throw new NoResultException("Can not find Page named: " + singlePageName);
                }
                result.pageIds.add(thePage.getId());
            }
        }

    }

    private addStepQueryData(MvQueryParams result){
        if (stepId) {
            stepId.tokenize(",").each { singleStepId ->
                if (!singleStepId.isLong()){
                    throw new NoResultException("Parameter stepId must be an Integer.");
                }
                MeasuredEvent theStep = MeasuredEvent.get(singleStepId);
                if (theStep == null) {
                    throw new NoResultException("Can not find step with ID: " + singleStepId);
                }
                result.measuredEventIds.add(theStep.getId());
            }
        }else if (step) {
            step.tokenize(",").each { singleStepName ->
                MeasuredEvent theStep = MeasuredEvent.findByName(singleStepName)
                if (theStep == null) {
                    throw new NoResultException("Can not find step named: " + singleStepName);
                }
                result.measuredEventIds.add(theStep.getId());
            }
        }
    }

    private addBrowserQueryData(BrowserService browserService, MvQueryParams result){
        if (browserId) {
            browserId.tokenize(",").each { singlebrowserId ->
                if (!singlebrowserId.isLong()){
                    throw new NoResultException("Parameter browserId must be an Integer.");
                }
                Browser theBrowser = Browser.get(singlebrowserId);
                if (theBrowser == null) {
                    throw new NoResultException("Can not find browser with ID: " + singlebrowserId);
                }
                result.browserIds.add(theBrowser.getId());
            }
        }else if (browser) {
            Browser theBrowser = browserService.findByNameOrAlias(browser);
            if (theBrowser == null) {
                throw new NoResultException("Can not find browser named: " + browser);
            }
            result.browserIds.add(theBrowser.getId());
        }
    }

    private addLocationQueryData(MvQueryParams result){
        if (locationId) {
            locationId.tokenize(",").each { singlelocationId ->
                if (!singlelocationId.isLong()){
                    throw new NoResultException("Parameter locationId must be an Integer.");
                }
                Location theLocation = Location.get(singlelocationId);
                if (theLocation == null) {
                    throw new NoResultException("Can not find location with ID: " + singlelocationId);
                }
                result.locationIds.add(theLocation.getId());
            }
        }else if (location) {
            List<Location> locations = Location.findAllByUniqueIdentifierForServer(location)
            if (locations.size() == 0) {
                throw new NoResultException("Can not find location with unique identifier \"" + location + "\"");
            }
            result.locationIds.addAll(locations*.ident())
        }
    }

    /**
     * Returns cached views to return EventResults of. If no {@link CachedView} is provided as parameter
     * EventResults are not limited by CachedView.
     * @return
     */
    public Collection<CachedView> getCachedViewsToReturn() {
        return cachedView == null ? [CachedView.UNCACHED, CachedView.CACHED] : [cachedView]
    }
}
