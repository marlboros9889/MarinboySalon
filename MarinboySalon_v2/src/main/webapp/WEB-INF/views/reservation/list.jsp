<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>내 예약 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="h2 mb-0">내 예약</h1>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/reservation/insertForm">새 예약</a>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty reservationList}">
            <div class="alert alert-secondary">등록된 예약이 없습니다.</div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive bg-white border rounded-3 shadow-sm">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">시술</th>
                        <th scope="col">예약 시간</th>
                        <th scope="col">금액</th>
                        <th scope="col">상태</th>
                        <th scope="col">관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    <%-- 예약 행은 c:forEach로 한 건씩 반복하여 출력합니다. --%>
                    <c:forEach var="reservation" items="${reservationList}">
                        <tr>
                            <td><c:out value="${reservation.serviceName}"/></td>
                            <td><c:out value="${reservation.reservationStart}"/></td>
                            <td><fmt:formatNumber value="${reservation.servicePrice}" pattern="#,###"/>원</td>
                            <td><span class="badge text-bg-secondary"><c:out value="${reservation.status}"/></span></td>
                            <td>
                                <c:if test="${reservation.status == 'REQUESTED'}">
                                    <div class="d-flex gap-2">
                                        <a class="btn btn-outline-primary btn-sm"
                                           href="${pageContext.request.contextPath}/reservation/updateForm?id=${reservation.id}">수정</a>
                                        <form action="${pageContext.request.contextPath}/reservation/delete" method="post">
                                            <input type="hidden" name="id" value="${reservation.id}">
                                            <button class="btn btn-outline-danger btn-sm" type="submit">취소</button>
                                        </form>
                                    </div>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
