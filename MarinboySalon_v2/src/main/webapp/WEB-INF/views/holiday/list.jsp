<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>휴무일 관리 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<%-- 임시 휴무일을 등록·삭제하여 해당 날짜의 예약 접수를 막는 관리자 화면입니다. --%>
<main class="container py-5">
    <h1 class="h2 mb-4">휴무일 관리</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>

    <section class="bg-white border rounded-3 shadow-sm p-4 mb-4">
        <h2 class="h5 mb-3">새 휴무일 등록</h2>
        <form class="row g-3 align-items-end" action="${pageContext.request.contextPath}/holiday/insert" method="post">
            <jsp:include page="/WEB-INF/views/common/csrf.jsp"/>
            <div class="col-12 col-md-4">
                <label class="form-label" for="holidayDate">날짜</label>
                <input class="form-control reservation-date" id="holidayDate" name="holidayDate" type="date" required>
            </div>
            <div class="col-12 col-md-6">
                <label class="form-label" for="reason">휴무 사유</label>
                <input class="form-control" id="reason" name="reason" maxlength="200">
            </div>
            <div class="col-12 col-md-2">
                <button class="btn btn-primary w-100" type="submit">등록</button>
            </div>
        </form>
    </section>

    <div class="table-responsive bg-white border rounded-3 shadow-sm">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th scope="col">휴무일</th>
                <th scope="col">사유</th>
                <th scope="col">관리</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="holiday" items="${holidayList}">
                <tr>
                    <td><c:out value="${holiday.holidayDate}"/></td>
                    <td><c:out value="${holiday.reason}"/></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/holiday/delete" method="post">
                            <jsp:include page="/WEB-INF/views/common/csrf.jsp"/>
                            <input type="hidden" name="id" value="${holiday.id}">
                            <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/reservation.js"></script>
</body>
</html>
