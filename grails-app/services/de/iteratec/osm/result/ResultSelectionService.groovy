package de.iteratec.osm.result

import grails.transaction.Transactional
import org.joda.time.DateTime
import org.joda.time.Days

@Transactional
class ResultSelectionService {

     def query(ResultSelectionCommand command, ResultSelectionController.ResultSelectionType type, Closure projection) {

        boolean isStartOfDay = isStartOfDay(command.from)
        def fromFullDay = command.from.withTimeAtStartOfDay()
        if (!isStartOfDay) {
            fromFullDay = fromFullDay.plusDays(1)
        }

        boolean isEndOfDay = isEndOfDay(command.to)
        def toFullDay = command.to.withTimeAtStartOfDay()
        if (isEndOfDay) {
            toFullDay = toFullDay.plusDays(1)
        }

        boolean isFullDayBetween = Days.daysBetween(fromFullDay, toFullDay).getDays() > 0

        if (!isFullDayBetween) {
            return queryEventTable(command.from, command.to, command, type, projection, null)
        }

        def results = queryResultSelectionTable(fromFullDay, toFullDay, command, type, projection, null)
        if (!isStartOfDay) {
            results += queryEventTable(command.from, fromFullDay, command, type, projection, results)
        }
        if (!isEndOfDay) {
            results += queryEventTable(toFullDay, command.to, command, type, projection, results)
        }
        return results
    }

    private
    def queryEventTable(DateTime from, DateTime to, ResultSelectionCommand command, ResultSelectionController.ResultSelectionType type, Closure projection, Object existingResults) {
        return EventResult.createCriteria().list {
            applyResultSelectionFilters(delegate, from, to, command, type)
            projection.delegate = delegate
            projection(existingResults)
        }
    }

    private
    def queryResultSelectionTable(DateTime from, DateTime to, ResultSelectionCommand command, ResultSelectionController.ResultSelectionType type, Closure projection, Object existingResults) {
        return ResultSelectionInformation.createCriteria().list {
            applyResultSelectionFilters(delegate, from, to, command, type)
            projection.delegate = delegate
            projection(existingResults)
        }
    }

    private void applyResultSelectionFilters(Object criteriaBuilder, DateTime from, DateTime to, ResultSelectionCommand command, ResultSelectionController.ResultSelectionType resultSelectionType) {
        def filterClosure = {
            and {
                between("jobResultDate", from.toDate(), to.toDate())
                if (resultSelectionType != ResultSelectionType.JobGroups && command.jobGroupIds) {
                    'in'('jobGroup.id', command.jobGroupIds)
                }

                if (resultSelectionType != ResultSelectionType.MeasuredEvents && command.measuredEventIds) {
                    'in'("measuredEvent.id", command.measuredEventIds)
                }

                if (resultSelectionType != ResultSelectionType.MeasuredEvents && resultSelectionType != ResultSelectionType.Pages && command.pageIds) {
                    'in'("page.id", command.pageIds)
                }

                if (resultSelectionType != ResultSelectionType.Locations && command.locationIds) {
                    'in'("location.id", command.locationIds)
                }

                if (resultSelectionType != ResultSelectionType.Locations && command.browserIds) {
                    'in'("browser.id", command.browserIds)
                }

                if (resultSelectionType != ResultSelectionType.ConnectivityProfiles) {
                    or {
                        if (command.connectivityIds) {
                            'in'("connectivityProfile.id", command.connectivityIds)
                        }

                        if (command.nativeConnectivity) {
                            and {
                                isNull("connectivityProfile")
                                eq("noTrafficShapingAtAll", true)
                            }
                        }
                        if (command.customConnectivities) {
                            and {
                                isNull("connectivityProfile")
                                'in'("customConnectivityName", command.customConnectivities)
                            }
                        }
                    }
                }
            }
        }
        filterClosure.delegate = criteriaBuilder
        filterClosure()
    }

    private boolean isStartOfDay(DateTime dateTime) {
        return dateTime.getMillisOfDay() <= 1000
    }

    private boolean isEndOfDay(DateTime dateTime) {
        def millisOfDay = dateTime.getMillisOfDay()
        def maxMillis = dateTime.millisOfDay().withMaximumValue().getMillisOfDay()
        return (maxMillis - millisOfDay) <= 1000
    }
}
