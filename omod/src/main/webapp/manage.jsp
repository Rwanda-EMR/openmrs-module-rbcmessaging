<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>
<script type="text/javascript">
	jQuery(document).ready(function() {
		jQuery(".dependencies").toggle();
		jQuery("a#toggleLink").click(function () {
			jQuery(".dependencies").toggle();
			});
		
		jQuery("a#customExporter").click(function() {
			var selectedChk = '';
			 jQuery("input:checkbox:checked").each(function() {
		         selectedChk += jQuery(this).val()+"-";
		     });
			 if(selectedChk == ''){
				 alert("Must select modules to export");
			 }else{
			 window.location.href = "${pageContext.request.contextPath}/module/rbcmessaging/exportFile.form?exportType=CUSTOM&moduleId="+selectedChk;
			  }
			
		});
	});
</script>

<div class="boxHeader">
	<b><spring:message code="" /></b>
</div>
<div class="box">
	<table cellpadding="2" cellspacing="0" width="100%">
		<tr>
			<th><spring:message code="general.destination" />
			</th>
			<th><spring:message code="general.recipient" />
			</th>
			<th><spring:message code="general.content" /></th>
			<th><spring:message code="general.date" /></th>
			<th><spring:message code="general.status" /></th>
			<th><spring:message code="general.action" /></th>
		</tr>
		<c:forEach var="message" items="${messages}" varStatus="status">
			<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"}'>
				<td><input type="checkbox" name=""
					value="${message.messageId}" />${message.destination}</td>
				<td>${message.recipient.givenName} ${message.recipient.familyName}</td>
				<td>${message.content}</td>
				<td>
				${message.date}
				</td>
				<td>
				${message.status}
				</td>
				<td>
				<a href="${pageContext.request.contextPath}/module/rbcmessaging/resend.form?messageId=${message.messageId}&resendType=SINGLE"><spring:message code="rbcmessaging.resend.single" /></a>
                &nbsp;&nbsp;<a href="${pageContext.request.contextPath}/module/rbcmessaging/delete.form?messageId=${message.messageId}&deleteType=SINGLE"><spring:message code="rbcmessaging.delete.single" /></a>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>