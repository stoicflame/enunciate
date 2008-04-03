<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>
<%@ taglib prefix="authz" uri="http://acegisecurity.org/authz" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
  <title>Sparklr</title>
  <link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>"/>
</head>

<body>
<div id="container">
  <div id="header">
    <div id="headertitle">Sparklr</div>
  </div>
  <div id="mainbody">

    <c:if test="${!empty sessionScope.ACEGI_SECURITY_LAST_EXCEPTION}">
      <div class="errorHeader">Woops!</div>

      <p class="bodytext"><font color="red">Access could not be granted. (<%= ((AuthenticationException) session.getAttribute(AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>)</font></p>
    </c:if>
    <c:remove scope="session" var="ACEGI_SECURITY_LAST_EXCEPTION"/>

    <authz:authorize ifAllGranted="ROLE_USER">
      <div class="header1">Please Confirm</div>

      <p class="bodytext">You hereby authorize "<c:out value="${consumer.displayName}"/>" to access the following resource:</p>

      <div class="bodytext"><b><c:out value="${consumer.resourceName}"/></b></div>

      <p class="bodytext"><c:out value="${consumer.resourceDescription}"/></p>

      <form action="<c:url value="/oauth/authorize"/>" method="POST">
        <input name="oauth_token" value="<c:out value="${oauth_token}"/>" type="hidden"/>
        <c:if test="${!empty oauth_callback}">
        <input name="oauth_callback" value="<c:out value="${oauth_callback}"/>" type="hidden"/>
        </c:if>
        <p class="formtext"><input name="authorize" value="authorize" type="submit"></p>
      </form>
    </authz:authorize>
  </div>

  <div id="footer">Design by <a href="http://www.pyserwebdesigns.com" target="_blank">Pyser Web Designs</a></div>

</div>
</body>
</html>
