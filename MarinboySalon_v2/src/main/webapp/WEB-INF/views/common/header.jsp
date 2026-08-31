<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 모든 화면에서 같은 주소 규칙을 사용하도록 공통 내비게이션을 분리합니다. --%>
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand fw-bold text-primary-emphasis" href="${pageContext.request.contextPath}/">
            마린보이 살롱
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavigation"
                aria-controls="mainNavigation" aria-expanded="false" aria-label="메뉴 열기">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNavigation">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/serviceItem/list">시술 메뉴</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/reservation/insertForm">예약하기</a>
                </li>
                <c:if test="${not empty sessionScope.loginUser}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/reservation/list">내 예약</a>
                    </li>
                </c:if>
                <c:if test="${sessionScope.loginUser.role == 'ADMIN'}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            관리자
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/reservation/adminList">예약 관리</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/serviceItem/adminList">시술 관리</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/businessHour/list">영업시간 관리</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/holiday/list">휴무일 관리</a></li>
                        </ul>
                    </li>
                </c:if>
            </ul>
            <div class="d-flex gap-2 align-items-center">
                <c:choose>
                    <c:when test="${empty sessionScope.loginUser}">
                        <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/user/loginForm">로그인</a>
                        <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/user/insertForm">회원가입</a>
                    </c:when>
                    <c:otherwise>
                        <a class="text-decoration-none text-secondary" href="${pageContext.request.contextPath}/user/detail">
                            <c:out value="${sessionScope.loginUser.name}"/>님
                        </a>
                        <form action="${pageContext.request.contextPath}/user/logout" method="post" class="m-0">
                            <jsp:include page="/WEB-INF/views/common/csrf.jsp"/>
                            <button class="btn btn-outline-secondary btn-sm" type="submit">로그아웃</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>
