from django.contrib import admin
from .models import PortfolioReservation, ServiceLog

# ServiceLogAdmin 클래스 정의 및 등록
# @admin.register(ServiceLog)  admin 사이트에 노출
@admin.register(ServiceLog)
class ServiceLogAdmin(admin.ModelAdmin):
    list_display = ('date', 'category', 'visitor_count', 'sales_amount')


@admin.register(PortfolioReservation)
class PortfolioReservationAdmin(admin.ModelAdmin):
    # 관리자 화면에서도 분석용 예약 데이터의 핵심 열을 한 번에 확인합니다.
    list_display = ('reservation_date', 'service_name', 'final_price', 'rating')
    list_filter = ('service_name', 'rating')
    search_fields = ('review_summary',)
