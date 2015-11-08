<%=packageName%>
<g:render template="./editOrCreate" model="['mode': 'edit', 'entityDisplayName': message(code: 'de.iteratec.iss.script', default: 'Skript'), 'entity': script]" />