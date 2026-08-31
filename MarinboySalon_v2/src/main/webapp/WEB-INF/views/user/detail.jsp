<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>내 정보 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <section class="form-card mx-auto bg-white border rounded-4 shadow-sm p-4">
        <h1 class="h3 mb-4">내 정보</h1>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>

        <%-- id는 세션에서 읽으므로 사용자가 임의로 바꿀 수 있는 hidden input으로 보내지 않습니다. --%>
        <form action="${pageContext.request.contextPath}/user/update" method="post">
            <jsp:include page="/WEB-INF/views/common/csrf.jsp"/>
            <div class="mb-3">
                <label class="form-label" for="email">이메일</label>
                <input class="form-control" id="email" value="<c:out value='${userDto.email}'/>" readonly>
            </div>
            <div class="mb-3">
                <label class="form-label" for="name">이름</label>
                <input class="form-control" id="name" name="name" value="<c:out value='${userDto.name}'/>" required>
            </div>
            <div class="mb-4">
                <label class="form-label" for="phone">연락처</label>
                <input class="form-control" id="phone" name="phone" value="<c:out value='${userDto.phone}'/>" required>
            </div>
            <button class="btn btn-primary w-100" type="submit">정보 수정</button>
        </form>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
