<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>예약하기 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<%-- 고객이 시술과 희망 시간을 선택해 예약 요청을 보내는 화면입니다. --%>
<main class="container py-5">
    <section class="form-card mx-auto bg-white border rounded-4 shadow-sm p-4">
        <h1 class="h3 mb-4">예약하기</h1>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/reservation/insert" method="post">
            <div class="mb-3">
                <label class="form-label" for="serviceId">시술</label>
                <select class="form-select" id="serviceId" name="serviceId" required>
                    <option value="">시술을 선택해 주세요.</option>
                    <c:forEach var="serviceItem" items="${serviceItemList}">
                        <option value="${serviceItem.id}" ${selectedServiceId == serviceItem.id ? 'selected' : ''}>
                            <c:out value="${serviceItem.name}"/> / <fmt:formatNumber value="${serviceItem.price}" pattern="#,###"/>원 / ${serviceItem.durationMinutes}분
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-12 col-md-6">
                    <label class="form-label" for="reservationDate">예약 날짜</label>
                    <input class="form-control reservation-date" id="reservationDate" name="reservationDate"
                           type="date" value="${reservationDate}" required>
                </div>
                <div class="col-12 col-md-6">
                    <label class="form-label" for="reservationTime">예약 시간</label>
                    <input class="form-control" id="reservationTime" name="reservationTime"
                           type="time" value="${reservationTime}" step="1800" required>
                </div>
            </div>
            <div class="mb-4">
                <label class="form-label" for="requestMemo">요청사항</label>
                <textarea class="form-control" id="requestMemo" name="requestMemo" rows="3" maxlength="500"><c:out value="${requestMemo}"/></textarea>
            </div>
            <button class="btn btn-primary w-100" type="submit">예약 접수</button>
        </form>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/reservation.js"></script>
</body>
</html>
