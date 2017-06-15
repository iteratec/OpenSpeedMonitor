package de.iteratec.osm.result

import de.iteratec.osm.util.ParameterBindingUtility
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Interval

import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * <p>
 * Command of DetailAnalysisDashboardController
 * </p>
 */
class DetailAnalysisDashboardShowCommand implements Validateable {
    /**
     * The selected start date (inclusive).
     */
    @BindUsing({ obj, source ->
        ParameterBindingUtility.parseDateTimeParameter(source["from"], false)
    })
    DateTime from

    /**
     * The selected end date (inclusive).
     */
    @BindUsing({ obj, source ->
        ParameterBindingUtility.parseDateTimeParameter(source["to"], true)
    })
    DateTime to

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = Duration.of(3, ChronoUnit.DAYS).getSeconds()

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     *
     */
    Collection<Long> selectedFolder = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.csi.Page pages}
     * which results to be shown.
     */
    Collection<Long> selectedPages = []


    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: { DateTime currentFrom, DetailAnalysisDashboardShowCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { DateTime currentTo, DetailAnalysisDashboardShowCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && !currentTo.isAfter(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })

        selectedFolder(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardShowCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder']
        })
        selectedPages(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardShowCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedPage']
        })
    }

    /**
     * <p>
     * Returns the selected time frame as {@link org.joda.time.Interval}.
     * That is the interval from {@link #from} to {@link #to} if {@link #selectedTimeFrameInterval} is 0 (that means manual).
     * If {@link #selectedTimeFrameInterval} is greater 0 the returned time frame is now minus {@link #selectedTimeFrameInterval} seconds to now.
     * </p>
     *
     * @return not <code>null</code>.
     */
    Interval createTimeFrameInterval() {
        if (this.selectedTimeFrameInterval == 0) {
            return new Interval(this.from, this.to)
        } else {
            DateTime now = DateTime.now()
            return new Interval(now.minusSeconds(this.selectedTimeFrameInterval), now)
        }
    }

    /**
     * <p>
     * Copies all request data to the specified map. This operation does
     * not care about the validation status of this instance.
     * For missing values the defaults are inserted.
     * </p>
     *
     * @param viewModelToCopyTo The {@link Map} the request data contained in this command object should be copied to.
     */
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {
        viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)
        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)
        viewModelToCopyTo.put('from', ParameterBindingUtility.formatDateTimeParameter(this.from))
        viewModelToCopyTo.put('to', ParameterBindingUtility.formatDateTimeParameter(this.to))
    }
}
