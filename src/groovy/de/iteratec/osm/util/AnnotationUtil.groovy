package de.iteratec.osm.util

import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventService

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.joda.time.Interval

class AnnotationUtil {
    /**
     * <p>
     * Fills the annotations with values.
     * </p>
     *
     * @param modelToRender
     *         The map to be filled. Previously added entries are overridden.
     *         This map should not be <code>null</code>.
     * @param timeFrame
     *         The time-frame for that data should be calculated,
     *         not <code>null</code>.
     */
    public static void fillWithAnnotations(
            Map<String, Object> modelToRender,
            Interval timeFrame,
            Collection<Long> selectedFolder,
            EventService eventService)
    {
        Date resetFromDate = timeFrame.getStart().toDate()
        Date resetToDate = timeFrame.getEnd().toDate()

        List<Event> annotationContent = eventService.retrieveEventsByDateRangeAndVisibilityAndJobGroup(resetFromDate, resetToDate, selectedFolder)
        ArrayList<String> annotations = new ArrayList<String>()

        annotationContent.eachWithIndex { item, index ->
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
            Date date = dateFormat.parse("$item.eventDate");
            long unixTime = (long)date.getTime()/1000;
            String description = item.description.replaceAll("(\r\n|\n)", "<br />");
            annotations.add("{x: '$unixTime', text: '$item.eventDate<br><strong>$item.shortName:</strong><br/>$description'}")
        }
        modelToRender.put('annotations', annotations)

    }
}
