package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.ConnectivityProfile

import java.util.regex.Pattern

/**
 * <p>
 * Command of {@link TabularResultPresentationController#listResults(de.iteratec.osm.result.TabularResultListResultsCommand)} (ListResultsCommand)}.
 * </p>
 *
 * <p>
 * None of the properties will be <code>null</code> for a valid instance.
 * </p>
 *
 * @author mze
 * @since IT-74
 */

public class TabularResultListResultsCommand extends TabularResultEventResultsCommandBase{
    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     */
    Collection<Long> selectedFolder = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.csi.Page pages}
     * which results to be shown.
     */
    Collection<Long> selectedPages = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.result.MeasuredEvent
     * measured events} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllMeasuredEvents} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedMeasuredEventIds = []

    /**
     * User enforced the selection of all measured events.
     * This selection <em>is not</em> reflected in
     * {@link #selectedMeasuredEventIds} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedMeasuredEventIds} should be ignored.
     */
    Boolean selectedAllMeasuredEvents = true

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllBrowsers} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedBrowsers = []

    /**
     * User enforced the selection of all browsers.
     * This selection <em>is not</em> reflected in
     * {@link #selectedBrowsers} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedBrowsers} should be ignored.
     */
    Boolean selectedAllBrowsers = true

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllLocations} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedLocations = []

    /**
     * User enforced the selection of all locations.
     * This selection <em>is not</em> reflected in
     * {@link #selectedLocations} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedLocations} should be ignored.
     */
    Boolean selectedAllLocations = true

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.ConnectivityProfile}s which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllConnectivityProfiles} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedConnectivityProfiles = []

    /**
     * User enforced the selection of all ConnectivityProfiles.
     * This selection <em>is not</em> reflected in
     * {@link #selectedConnectivityProfiles} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedConnectivityProfiles} should be ignored.
     */
    Boolean selectedAllConnectivityProfiles = true

    /**
     * Whether or not EventResults measured with native connectivity should get included.
     */
    Boolean includeNativeConnectivity

    /**
     * Wheter or not EventResult measured with custom connectivity should get included.
     */
    Boolean includeCustomConnectivity;

    /**
     * If set, this is handled as a regular expression to select results measured with custom connectivity and whos custom
     * connectivity name matches this regex.
     */
    String customConnectivityName

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        selectedFolder(nullable: false, validator: { Collection currentCollection, TabularResultListResultsCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder']
        })
        // selectedPages is not allowed to be empty
        selectedPages(nullable: false, validator: { Collection currentCollection, TabularResultListResultsCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedPage.error.validator.error.selectedPage']
        })
        // selectedMeasuredEventIds is only allowed to be empty if selectedAllMeasuredEvents is true
        selectedMeasuredEventIds(nullable:false, validator: { Collection currentCollection, TabularResultListResultsCommand cmd ->
             if(!(cmd.selectedAllMeasuredEvents || (!currentCollection.isEmpty()))) return ['de.iteratec.osm.gui.selectMeasurings.error.selectedMeasuredEvents.validator.error.selectedMeasuredEvents']
        })

        // selectedBrowsers is only allowed to be empty if selectedAllBrowsers is true
        selectedBrowsers(nullable:false, validator: { Collection currentCollection, TabularResultListResultsCommand cmd ->
            if (!cmd.selectedAllBrowsers && currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedBrowsers.error.validator.error.selectedBrowsers']
        })

        // selectedLocations is only allowed to be empty if selectedAllLocations is true
        selectedLocations(nullable:false, validator: { Collection currentCollection, TabularResultListResultsCommand cmd ->
            if (!cmd.selectedAllLocations && currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedLocations.error.validator.error.selectedLocations']
        })

        selectedAllConnectivityProfiles(nullable: true)

        includeNativeConnectivity(nullable: false)

        includeCustomConnectivity(nullable: false)

        customConnectivityName(nullable: true)
    }

    /**
     * <p>
     * Copies all request data to the specified map. This operation does
     * not care about the validation status of this instance.
     * For missing values the defaults are inserted.
     * </p>
     *
     * @param viewModelToCopyTo
     *         The {@link Map} the request data contained in this command
     *         object should be copied to. The map must be modifiable.
     *         Previously contained data will be overwritten.
     *         The argument might not be <code>null</code>.
     */
    @Override
    public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
    {
        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('selectedAllMeasuredEvents', this.selectedAllMeasuredEvents )
//            viewModelToCopyTo.put('selectedAllMeasuredEvents', (this.selectedAllMeasuredEvents as boolean ? 'on' : ''))
        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

        viewModelToCopyTo.put('selectedAllBrowsers', this.selectedAllBrowsers)
//            viewModelToCopyTo.put('selectedAllBrowsers', (this.selectedAllBrowsers as boolean ? 'on' : ''))
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

        viewModelToCopyTo.put('selectedAllLocations', this.selectedAllLocations)
//            viewModelToCopyTo.put('selectedAllLocations', (this.selectedAllLocations as boolean ? 'on' : ''))
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

        viewModelToCopyTo.put('selectedAllConnectivityProfiles', this.selectedAllConnectivityProfiles)
        viewModelToCopyTo.put('selectedConnectivityProfiles', this.selectedConnectivityProfiles)
        viewModelToCopyTo.put('includeNativeConnectivity', this.includeNativeConnectivity)
        viewModelToCopyTo.put('includeCustomConnectivity', this.includeCustomConnectivity)
        viewModelToCopyTo.put('customConnectivityName', this.customConnectivityName)

        super.copyRequestDataToViewModelMap(viewModelToCopyTo)
    }

    /**
     * <p>
     * Creates a query tag to find results matching the selections made
     * with this command.
     * </p>
     *
     * @param csiAggregationTagService
     *         The {@link CsiAggregationTagService} to create the tag with.
     * @return Never <code>null</code>.
     */
    public Pattern createResultsQueryPattern(CsiAggregationTagService csiAggregationTagService) {
        return csiAggregationTagService.getTagPatternForHourlyCsiAggregations(createMvQueryParams());
    }

    /**
     * <p>
     * Returns a boolean to include custom connectivity in selected connetivities
     * </p>
     */
    public boolean includeCustomConnectivity() {
        return includeCustomConnectivity;
    }

    /**
     * <p>
     * Creates {@link MvQueryParams} based on this command. This command
     * need to be valid for this operation to be successful.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     */
    private ErQueryParams createMvQueryParams() throws IllegalStateException
    {

        if( !this.validate() )
        {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        ErQueryParams result = new ErQueryParams();

        result.jobGroupIds.addAll(this.selectedFolder);

        if( !this.selectedAllMeasuredEvents )
        {
            result.measuredEventIds.addAll(this.selectedMeasuredEventIds);
        }

        result.pageIds.addAll(this.selectedPages);

        if( !this.selectedAllBrowsers )
        {
            result.browserIds.addAll(this.selectedBrowsers);
        }

        if( !this.selectedAllLocations )
        {
            result.locationIds.addAll(this.selectedLocations);
        }

        result.includeNativeConnectivity = this.includeNativeConnectivity
        result.includeCustomConnectivity = this.includeCustomConnectivity
        if (this.includeCustomConnectivity){
            result.customConnectivityNameRegex = this.customConnectivityName ?: '.*'
        }
        if (this.selectedAllConnectivityProfiles){
            result.connectivityProfileIds.addAll(ConnectivityProfile.list()*.ident())
        }else if (this.selectedConnectivityProfiles.size() > 0){
            result.connectivityProfileIds.addAll(this.selectedConnectivityProfiles)
        }

        return result;
    }
}