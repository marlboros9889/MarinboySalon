<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>회원가입 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <section class="form-card mx-auto bg-white border rounded-4 shadow-sm p-4">
        <h1 class="h3 mb-4">회원가입</h1>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <%-- 일반 폼 전송이므로 각 input의 name은 UserDto 필드명과 일치시킵니다. --%>
        <form:form action="${pageContext.request.contextPath}/user/insert" method="post" modelAttribute="userDto">
            <div class="mb-3">
                <label class="form-label" for="email">이메일</label>
                <form:input path="email" id="email" type="email" cssClass="form-control"/>
                <form:errors path="email" cssClass="text-danger small"/>
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">비밀번호</label>
                <form:password path="password" id="password" cssClass="form-control"/>
                <form:errors path="password" cssClass="text-danger small"/>
            </div>
            <div class="mb-3">
                <label class="form-label" for="name">이름</label>
                <form:input path="name" id="name" cssClass="form-control"/>
                <form:errors path="name" cssClass="text-danger small"/>
            </div>
            <div class="mb-4">
                <label class="form-label" for="phone">연락처</label>
                <form:input path="phone" id="phone" cssClass="form-control" placeholder="010-1234-5678"/>
                <form:errors path="phone" cssClass="text-danger small"/>
            </div>
            <button class="btn btn-primary w-100" type="submit">가입하기</button>
        </form:form>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
