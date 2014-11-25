<%-- 
    Template to paginate EventResults
    
    Required properties:
      model: An instance of PaginationListing, not null. 
--%>
<g:if test="${model != null && !model.isEmpty()}">
	<div class="pagination pagination-centered">      
		 <ul>
		 	<g:if test="${model.isNotFirst()}">
		 		<li><a href="${model.getPrevLink()}"><g:message code="de.iteratec.isr.pagination.prev" /></a></li>
		 	</g:if>
		 	<g:each var="eachResult" in="${model.rows}">
				<li><a href="${eachResult.pageLink}">${eachResult.pageNumber}</a></li>
			</g:each>
			<g:if test="${model.isNotLast()}">
				<li><a href="${model.getNextLink()}"><g:message code="de.iteratec.isr.pagination.next" /></a></li>
			</g:if>	
		</ul>
	</div>   
</g:if>