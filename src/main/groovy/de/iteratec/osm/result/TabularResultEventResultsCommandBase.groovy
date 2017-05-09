package de.iteratec.osm.result

import de.iteratec.osm.util.ParameterBindingUtility
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Interval
/**
 * <p>
 * Command of {@link TabularResultPresentationController#listResults(TabularResultListResultsCommand)
 *}.
 * </p>
 * Created by Marko Schnecke on 04.04.2016.
 */
class TabularResultEventResultsCommandBase implements Validateable {
    /**
     * The selected start date.
     *
     * Please use {@link #receiveSelectedTimeFrame()}.
     */
    @BindUsing({
        obj, source -> ParameterBindingUtility.parseDateTimeParameter(source["from"], false)
    })
    DateTime from

    /**
     * The selected end date.
     *
     * Please use {@link #receiveSelectedTimeFrame()}.
     */
    @BindUsing({
        obj, source -> ParameterBindingUtility.parseDateTimeParameter(source["to"], true)
    })
    DateTime to

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

    Integer max = 50

    Integer offset = 0

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: { DateTime currentFrom, TabularResultEventResultsCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { DateTime currentTo, TabularResultEventResultsCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && !currentTo.isAfter(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })
        max(nullable: true)
        offset(nullable: true)
    }

    static transients = ['selectedTimeFrame']

    /**
     * <p>
     * Returns the selected time frame as {@link org.joda.time.Interval} while respecting selectedTimeFrameInterval.
     * </p>
     *
     * @return not <code>null</code>; end is intended to be inclusive
     */
    Interval receiveSelectedTimeFrame() {
        if (this.selectedTimeFrameInterval != 0) {
            DateTime end = new DateTime()
            return new Interval(end.minusSeconds(this.selectedTimeFrameInterval), end)
        }
        return new Interval(from, to)
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
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {
        viewModelToCopyTo.put('from', this.from)
        viewModelToCopyTo.put('to', this.to)

        viewModelToCopyTo.put('max', this.max)
        viewModelToCopyTo.put('offset', this.offset)
    }
}
