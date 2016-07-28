<%=packageName%>
<g:render template="editOrCreate" model="['mode': 'create', 'entityDisplayName': message(code: 'de.iteratec.iss.script', default: 'Skript'), 'entity': script]" />