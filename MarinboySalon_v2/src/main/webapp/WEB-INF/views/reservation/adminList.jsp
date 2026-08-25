<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>예약 관리 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <h1 class="h2 mb-4">전체 예약 관리</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>

    <div class="table-responsive bg-white border rounded-3 shadow-sm">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th scope="col">고객</th>
                <th scope="col">시술</th>
                <th scope="col">예약 시간</th>
                <th scope="col">요청사항</th>
                <th scope="col">상태 변경</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="reservation" items="${reservationList}">
                <tr>
                    <td><c:out value="${reservation.userName}"/></td>
                    <td><c:out value="${reservation.serviceName}"/></td>
                    <td><c:out value="${reservation.reservationStart}"/></td>
                    <td><c:out value="${reservation.requestMemo}"/></td>
                    <td>
                        <%-- 각 예약의 id와 상태가 한 폼 안에서 함께 전송되도록 구성합니다. --%>
                        <form class="d-flex gap-2" action="${pageContext.request.contextPath}/reservation/statusUpdate" method="post">
                            <input type="hidden" name="id" value="${reservation.id}">
                            <select class="form-select form-select-sm" name="status" aria-label="예약 상태">
                                <option value="REQUESTED" ${reservation.status == 'REQUESTED' ? 'selected' : ''}>접수</option>
                                <option value="CONFIRMED" ${reservation.status == 'CONFIRMED' ? 'selected' : ''}>확정</option>
                                <option value="COMPLETED" ${reservation.status == 'COMPLETED' ? 'selected' : ''}>완료</option>
                                <option value="CANCELLED" ${reservation.status == 'CANCELLED' ? 'selected' : ''}>취소</option>
                            </select>
                            <button class="btn btn-outline-primary btn-sm" type="submit">변경</button>
                        </form>
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
