<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Spring Security가 만든 CSRF 토큰을 모든 POST 폼에 같은 방식으로 넣습니다. --%>
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
