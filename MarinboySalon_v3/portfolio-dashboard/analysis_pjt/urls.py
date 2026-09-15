"""analysis_pjt 프로젝트의 URL 라우팅 설정입니다."""

from django.contrib import admin
from django.urls import include, path
from django.views.generic import RedirectView


urlpatterns = [
    # Django 관리자 화면 경로입니다.
    path("admin/", admin.site.urls),
    # 대시보드 요청은 analytics 앱의 URL 설정으로 전달합니다.
    path("dashboard/", include("analytics.urls")),
    # 첫 접속 시 분석 대시보드로 이동합니다.
    path("", RedirectView.as_view(url="/dashboard/", permanent=False)),
]
