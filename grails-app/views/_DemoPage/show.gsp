
<%@ page import="kickstartwithbootstrapgrailsplugin._DemoPage" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: '_DemoPage.label', default: '_DemoPage')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>
</head>

<body>

<section id="show-_DemoPage" class="first">

	<table class="table">
		<tbody>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.name.label" default="Name" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "name")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myDate.label" default="My Date" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${_DemoPageInstance?.myDate}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myDate2.label" default="My Date 2" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${_DemoPageInstance?.myDate2}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myBoolean.label" default="My Boolean" /></td>
				
				<td valign="top" class="value"><g:formatBoolean boolean="${_DemoPageInstance?.myBoolean}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myInt.label" default="My Int" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myInt")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myShort.label" default="My Short" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myShort")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myLong.label" default="My Long" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myLong")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myFloat.label" default="My Float" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myFloat")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myDouble.label" default="My Double" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myDouble")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myByte.label" default="My Byte" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myByte")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myChar.label" default="My Char" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myChar")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myByteArray.label" default="My Byte Array" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myInteger.label" default="My Integer" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myInteger")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myTimeZone.label" default="My Time Zone" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myTimeZone")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myLocale.label" default="My Locale" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myLocale")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myCurrency.label" default="My Currency" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: _DemoPageInstance, field: "myCurrency")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.dateCreated.label" default="Date Created" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${_DemoPageInstance?.dateCreated}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.lastUpdated.label" default="Last Updated" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${_DemoPageInstance?.lastUpdated}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="_DemoPage.myEnum.label" default="My Enum" /></td>
				
				<td valign="top" class="value">${_DemoPageInstance?.myEnum?.encodeAsHTML()}</td>
				
			</tr>
		
		</tbody>
	</table>
</section>

</body>

</html>
