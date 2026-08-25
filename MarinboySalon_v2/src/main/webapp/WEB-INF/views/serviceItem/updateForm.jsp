<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>시술 수정 | 마린보이 살롱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
</head>
<body class="bg-body-tertiary">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main class="container py-5">
    <section class="form-card mx-auto bg-white border rounded-4 shadow-sm p-4">
        <h1 class="h3 mb-4">시술 수정</h1>
        <form action="${pageContext.request.contextPath}/serviceItem/update" method="post">
            <input type="hidden" name="id" value="${serviceItemDto.id}">
            <div class="mb-3">
                <label class="form-label" for="name">시술명</label>
                <input class="form-control" id="name" name="name" value="<c:out value='${serviceItemDto.name}'/>" required>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-12 col-md-6">
                    <label class="form-label" for="price">가격</label>
                    <input class="form-control" id="price" name="price" type="number" min="0" value="${serviceItemDto.price}" required>
                </div>
                <div class="col-12 col-md-6">
                    <label class="form-label" for="durationMinutes">소요 시간(분)</label>
                    <input class="form-control" id="durationMinutes" name="durationMinutes" type="number" min="1"
                           value="${serviceItemDto.durationMinutes}" required>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="description">설명</label>
                <textarea class="form-control" id="description" name="description" rows="4" maxlength="500"><c:out value="${serviceItemDto.description}"/></textarea>
            </div>
            <div class="form-check mb-4">
                <input class="form-check-input" id="active" name="active" type="checkbox" value="true" ${serviceItemDto.active ? 'checked' : ''}>
                <label class="form-check-label" for="active">고객 화면에 공개</label>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-outline-secondary w-50" href="${pageContext.request.contextPath}/serviceItem/adminList">돌아가기</a>
                <button class="btn btn-primary w-50" type="submit">수정</button>
            </div>
        </form>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
