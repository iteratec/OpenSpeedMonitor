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
                if (from && to) {
                    between("jobResultDate", from.toDate(), to.toDate())
                }
                if (resultSelectionType != ResultSelectionController.ResultSelectionType.JobGroups && command.jobGroupIds) {
                    jobGroup {
                        'in'("id", command.jobGroupIds)
                    }
                }

                if (resultSelectionType != ResultSelectionController.ResultSelectionType.MeasuredEvents && command.measuredEventIds) {
                    measuredEvent {
                        'in'("id", command.measuredEventIds)
                    }
                }

                if (resultSelectionType != ResultSelectionController.ResultSelectionType.MeasuredEvents && resultSelectionType != ResultSelectionController.ResultSelectionType.Pages && command.pageIds) {
                    page {
                        'in'("id", command.pageIds)
                    }
                }

                if (resultSelectionType != ResultSelectionController.ResultSelectionType.Locations && command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                }

                if (resultSelectionType != ResultSelectionController.ResultSelectionType.Locations && command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }

                if (resultSelectionType != ResultSelectionController.ResultSelectionType.ConnectivityProfiles) {
                    or {
                        if (command.connectivityIds) {
                            connectivityProfile {
                                'in'("id", command.connectivityIds)
                            }
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
