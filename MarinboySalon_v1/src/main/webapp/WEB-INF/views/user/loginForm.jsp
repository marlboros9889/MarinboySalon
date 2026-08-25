<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>로그인 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <section class="form-card mx-auto bg-white border rounded-4 shadow-sm p-4">
        <h1 class="h3 mb-4">로그인</h1>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/user/login" method="post">
            <%-- 로그인 후 원래 보려던 내부 화면으로 돌아가기 위한 값입니다. --%>
            <input type="hidden" name="returnTo" value="<c:out value='${returnTo}'/>"/>
            <div class="mb-3">
                <label class="form-label" for="email">이메일</label>
                <input class="form-control" id="email" name="email" type="email"
                       value="<c:out value='${email}'/>" required>
            </div>
            <div class="mb-4">
                <label class="form-label" for="password">비밀번호</label>
                <input class="form-control" id="password" name="password" type="password" required>
            </div>
            <button class="btn btn-primary w-100" type="submit">로그인</button>
        </form>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
