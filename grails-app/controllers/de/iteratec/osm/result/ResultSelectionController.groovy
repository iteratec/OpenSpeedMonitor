package de.iteratec.osm.result

import de.iteratec.osm.api.dto.JobGroupDto
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import org.springframework.http.HttpStatus

class ResultSelectionController {
    JobGroupDaoService jobGroupDaoService;

    def getJobGroupsInTimeFrame(ResultSelectionTimeFrameCommand command) {
        if (command.hasErrors()) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST,
                    "Invalid parameters: " + command.getErrors().fieldErrors.each{it.field}.join(", "))
            return
        }
        if (!command.from.before(command.to)) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST,
                    "Invalid time frame: 'from' value needs to be before 'to'")
            return
        }

        def availableJobGroups = jobGroupDaoService.findByJobResultsInTimeFrame(command.from, command.to)
        ControllerUtils.sendObjectAsJSON(response, JobGroupDto.create(availableJobGroups))
    }
}

class ResultSelectionTimeFrameCommand {
    Date from;
    Date to;
}
