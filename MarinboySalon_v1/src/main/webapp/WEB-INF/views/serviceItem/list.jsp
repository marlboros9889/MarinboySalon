<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>시술 메뉴 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="container py-5">
    <div class="d-flex justify-content-between align-items-end mb-4">
        <div>
            <span class="badge rounded-pill text-bg-primary mb-2">SERVICE</span>
            <h1 class="h2 mb-1">시술 메뉴</h1>
            <p class="text-secondary mb-0">가격과 예상 소요 시간을 확인하고 예약할 수 있습니다.</p>
        </div>
    </div>

    <%-- 목록이 비어 있을 때 빈 화면 대신 안내 문구를 보여줍니다. --%>
    <c:choose>
        <c:when test="${empty serviceItemList}">
            <div class="alert alert-secondary">현재 예약 가능한 시술이 없습니다.</div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="serviceItem" items="${serviceItemList}">
                    <div class="col-12 col-md-6 col-lg-4">
                        <article class="card h-100 border-primary-subtle shadow-sm">
                            <div class="card-body">
                                <h2 class="h5 card-title"><c:out value="${serviceItem.name}"/></h2>
                                <p class="card-text text-secondary"><c:out value="${serviceItem.description}"/></p>
                                <p class="fw-bold text-primary-emphasis mb-1">
                                    <fmt:formatNumber value="${serviceItem.price}" pattern="#,###"/>원
                                </p>
                                <p class="small text-secondary mb-3">예상 <c:out value="${serviceItem.durationMinutes}"/>분</p>
                                <a class="btn btn-primary w-100"
                                   href="${pageContext.request.contextPath}/reservation/insertForm?serviceId=${serviceItem.id}">
                                    이 시술 예약하기
                                </a>
                            </div>
                        </article>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
