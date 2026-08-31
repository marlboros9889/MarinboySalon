<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>시술 관리 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2 mb-0">시술 메뉴 관리</h1>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/serviceItem/insertForm">시술 등록</a>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>

    <div class="table-responsive bg-white border rounded-3 shadow-sm">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th scope="col">시술명</th>
                <th scope="col">가격</th>
                <th scope="col">소요 시간</th>
                <th scope="col">공개 상태</th>
                <th scope="col">관리</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="serviceItem" items="${serviceItemList}">
                <tr>
                    <td><c:out value="${serviceItem.name}"/></td>
                    <td><fmt:formatNumber value="${serviceItem.price}" pattern="#,###"/>원</td>
                    <td>${serviceItem.durationMinutes}분</td>
                    <td>
                        <span class="badge ${serviceItem.active ? 'text-bg-success' : 'text-bg-secondary'}">
                            ${serviceItem.active ? '공개' : '비공개'}
                        </span>
                    </td>
                    <td>
                        <div class="d-flex gap-2">
                            <a class="btn btn-outline-primary btn-sm"
                               href="${pageContext.request.contextPath}/serviceItem/updateForm?id=${serviceItem.id}">수정</a>
                            <c:if test="${serviceItem.active}">
                                <form action="${pageContext.request.contextPath}/serviceItem/delete" method="post">
                                    <jsp:include page="/WEB-INF/views/common/csrf.jsp"/>
                                    <input type="hidden" name="id" value="${serviceItem.id}">
                                    <button class="btn btn-outline-danger btn-sm" type="submit">비공개</button>
                                </form>
                            </c:if>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
