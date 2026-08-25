<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>영업시간 관리 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <h1 class="h2 mb-4">영업시간 관리</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>

    <div class="row g-3">
        <%-- 요일별 한 행을 독립된 수정 폼으로 만들어 잘못된 id가 섞이지 않게 합니다. --%>
        <c:forEach var="businessHour" items="${businessHourList}">
            <div class="col-12">
                <form class="card border-primary-subtle shadow-sm"
                      action="${pageContext.request.contextPath}/businessHour/update" method="post">
                    <div class="card-body">
                        <input type="hidden" name="id" value="${businessHour.id}">
                        <div class="row g-3 align-items-end">
                            <div class="col-12 col-md-2">
                                <span class="fw-bold"><c:out value="${businessHour.dayName}"/></span>
                            </div>
                            <div class="col-6 col-md-3">
                                <label class="form-label" for="openTime-${businessHour.id}">시작</label>
                                <input class="form-control" id="openTime-${businessHour.id}" name="openTime" type="time"
                                       value="${businessHour.openTime}">
                            </div>
                            <div class="col-6 col-md-3">
                                <label class="form-label" for="closeTime-${businessHour.id}">종료</label>
                                <input class="form-control" id="closeTime-${businessHour.id}" name="closeTime" type="time"
                                       value="${businessHour.closeTime}">
                            </div>
                            <div class="col-6 col-md-2">
                                <div class="form-check mb-2">
                                    <input class="form-check-input" id="closed-${businessHour.id}" name="closed" type="checkbox"
                                           value="true" ${businessHour.closed ? 'checked' : ''}>
                                    <label class="form-check-label" for="closed-${businessHour.id}">정기 휴무</label>
                                </div>
                            </div>
                            <div class="col-6 col-md-2">
                                <button class="btn btn-primary w-100" type="submit">수정</button>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </c:forEach>
    </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
