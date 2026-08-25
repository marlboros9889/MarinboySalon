<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>마린보이 살롱</title>
    <%-- Bootstrap을 이용해 모바일에서도 읽기 쉬운 화면을 만듭니다. --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <%-- 첫 버전의 시작 상태와 앞으로 구현할 핵심 기능을 보여줍니다. --%>
    <section class="salon-hero mx-auto rounded-4 border border-primary-subtle bg-white p-4 p-md-5 shadow-sm">
        <span class="badge rounded-pill text-bg-primary mb-3">MarinboySalon_v1</span>
        <h1 class="display-6 fw-bold text-primary-emphasis">1인 헤어샵 예약 서비스</h1>
        <p class="lead text-secondary mb-4">
            서비스 메뉴 확인부터 예약, 내 예약 관리, 관리자 일정 관리까지 단계별로 구현합니다.
        </p>
        <div class="alert alert-primary mb-0" role="alert">
            새 프로젝트 기본 골격이 정상적으로 실행되었습니다.
        </div>
    </section>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/home.js"></script>
</body>
</html>
