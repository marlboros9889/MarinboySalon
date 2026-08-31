<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>예약 수정 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<%-- 기존 예약의 시술·날짜·시간을 바꾸되 30분 단위 입력을 유지하는 화면입니다. --%>
<main class="container py-5">
    <section class="form-card mx-auto bg-white border rounded-4 shadow-sm p-4">
        <h1 class="h3 mb-4">예약 수정</h1>
        <form action="${pageContext.request.contextPath}/reservation/update" method="post">
            <jsp:include page="/WEB-INF/views/common/csrf.jsp"/>
            <input type="hidden" name="id" value="${reservationDto.id}">
            <div class="mb-3">
                <label class="form-label" for="serviceId">시술</label>
                <select class="form-select" id="serviceId" name="serviceId" required>
                    <c:forEach var="serviceItem" items="${serviceItemList}">
                        <option value="${serviceItem.id}" ${reservationDto.serviceId == serviceItem.id ? 'selected' : ''}>
                            <c:out value="${serviceItem.name}"/> / <fmt:formatNumber value="${serviceItem.price}" pattern="#,###"/>원
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-12 col-md-6">
                    <label class="form-label" for="reservationDate">예약 날짜</label>
                    <input class="form-control reservation-date" id="reservationDate" name="reservationDate"
                           type="date" value="${reservationDto.reservationDate}" required>
                </div>
                <div class="col-12 col-md-6">
                    <label class="form-label" for="reservationTime">예약 시간</label>
                    <input class="form-control" id="reservationTime" name="reservationTime"
                           type="time" value="${reservationDto.reservationTime}" step="1800" required>
                </div>
            </div>
            <div class="mb-4">
                <label class="form-label" for="requestMemo">요청사항</label>
                <textarea class="form-control" id="requestMemo" name="requestMemo" rows="3" maxlength="500"><c:out value="${reservationDto.requestMemo}"/></textarea>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-outline-secondary w-50" href="${pageContext.request.contextPath}/reservation/list">돌아가기</a>
                <button class="btn btn-primary w-50" type="submit">수정 완료</button>
            </div>
        </form>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/reservation.js"></script>
</body>
</html>
