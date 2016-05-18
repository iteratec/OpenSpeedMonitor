<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<tr class=${evenOdd} id=batchActivity_${batchActivityInstance.id} status=${batchActivityInstance.status}>
    <td><g:link action="show"
                id="${batchActivityInstance.id}">${fieldValue(bean: batchActivityInstance, field: "name")}</g:link></td>

    <td>${message(code: batchActivityInstance.activity.getI18nCode(), default: batchActivityInstance.activity)}</td>

    <td>${message(code: batchActivityInstance.status.getI18nCode(), default: batchActivityInstance.status)}</td>

    <td>${batchActivityInstance.actualStage}/${batchActivityInstance.maximumStages}</td>

    <td>${batchActivityInstance.stepInStage}/${batchActivityInstance.maximumStepsInStage}</td>

    <td>${fieldValue(bean: batchActivityInstance, field: "lastFailureMessage")}</td>

    <td><g:formatDate type="datetime" date="${batchActivityInstance.startDate}" style="LONG"
                      timeStyle="SHORT"/></td>

    <td><g:formatDate type="datetime" date="${batchActivityInstance.lastUpdate}" style="LONG"
                      timeStyle="SHORT"/></td>

    <td><g:formatDate type="datetime" date="${batchActivityInstance.endDate}" style="LONG"
                      timeStyle="SHORT"/></td>
</tr>