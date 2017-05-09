package de.iteratec.osm.result
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
     */
    Collection<Long> selectedMeasuredEventIds = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     * <code>false</code>.
     */
    Collection<Long> selectedBrowsers = []


    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     */
    Collection<Long> selectedLocations = []

    /**
     * The selected connectivities. Could include connectivityProfile ids, customConnectivityNames or 'native'
     */
    Collection<String> selectedConnectivities = []


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
        selectedMeasuredEventIds(nullable: true)

        // selectedBrowsers is only allowed to be empty if selectedAllBrowsers is true
        selectedBrowsers(nullable:true)
        // selectedLocations is only allowed to be empty if selectedAllLocations is true
        selectedLocations(nullable:true)
    }

    /**
     * returns the selected connectivityProfiles by filtering all selected connectivities.
     */
    Collection<Long> getSelectedConnectivityProfiles() {
        return selectedConnectivities.findAll { it.isLong() }.collect {
            Long.parseLong(it)
        }
    }

    /**
     * returns the selected customConnectivityNames by filtering all selected connectivities.
     */
    Collection<String> getSelectedCustomConnectivityNames() {
        return selectedConnectivities.findAll {
            !it.isLong() && it != ResultSelectionController.MetaConnectivityProfileId.Native.value
        }
    }

    /**
     * Whether or not EventResults measured with native connectivity should get included.
     */
    boolean getIncludeNativeConnectivity() {
        return !selectedConnectivities || selectedConnectivities.contains(ResultSelectionController.MetaConnectivityProfileId.Native.value)
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
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
    {
        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)
        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)
        viewModelToCopyTo.put('selectedConnectivities', this.selectedConnectivities)

        super.copyRequestDataToViewModelMap(viewModelToCopyTo)
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
    private ErQueryParams createErQueryParams() throws IllegalStateException
    {

        if( !this.validate() )
        {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        ErQueryParams result = new ErQueryParams()

        result.jobGroupIds.addAll(this.selectedFolder)
        result.measuredEventIds.addAll(this.selectedMeasuredEventIds)
        result.pageIds.addAll(this.selectedPages);
        result.browserIds.addAll(this.selectedBrowsers)
        result.locationIds.addAll(this.selectedLocations)

        result.includeNativeConnectivity = this.getIncludeNativeConnectivity()
        result.customConnectivityNames.addAll(this.selectedCustomConnectivityNames)

        result.includeAllConnectivities = !this.selectedConnectivities
        result.connectivityProfileIds.addAll(this.selectedConnectivityProfiles)

        return result
    }
}
