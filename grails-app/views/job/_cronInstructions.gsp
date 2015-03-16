<blockquote>
    <P class="con-question-sign">
        <h5><g:message code="job.cronInstructions.header" default="Cron expressions for measurement jobs"/></h5>
        <g:message code="job.cronInstructions.explainingtext"/>
        <table cellspacing="8">
            <tr>
                <th align="left">Field Name</th>
                <th align="left">&nbsp;</th>
                <th align="left">Allowed Values</th>
                <th align="left">&nbsp;</th>
                <th align="left">Allowed Special Characters</th>
            </tr>
            <tr>
                <td align="left"><code>Minutes</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>0-59</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>, - * /</code></td>
            </tr>
            <tr>
                <td align="left"><code>Hours</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>0-23</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>, - * /</code></td>
            </tr>
            <tr>
                <td align="left"><code>Day-of-month</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>1-31</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>, - * ? / L W</code></td>
            </tr>
                <td align="left"><code>Month</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>1-12 or JAN-DEC</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>, - * /</code></td>
            </tr>
            <tr>
                <td align="left"><code>Day-of-Week</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>1-7 or SUN-SAT</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>, - * ? / L #</code></td>
            </tr>
            <tr>
                <td align="left"><code>Year (Optional)</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>empty, 1970-2199</code></td>
                <td align="left">&nbsp;
                </th>
                <td align="left"><code>, - * /</code></td>
            </tr>
        </table>
        <em><g:message code="job.cronInstructions.note.header"/>:</em> <g:message code="job.cronInstructions.note.text"/><br>
        <a
            href="http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger"
            target="_blank"><g:message code="job.cronInstructions.moreInformation" />
        </a>
    </p>
</blockquote>