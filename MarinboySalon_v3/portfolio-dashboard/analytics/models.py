from django.db import models


class PortfolioReservation(models.Model):
    """포트폴리오 SQL의 완료 예약·후기 데이터를 분석 전용으로 보관합니다."""

    # 원본 서비스 DB와 분리된 SQLite 테이블이라 운영 예약 데이터에는 영향을 주지 않습니다.
    reservation_date = models.DateField(verbose_name="예약 일자")
    service_name = models.CharField(max_length=100, verbose_name="시술명")
    final_price = models.PositiveIntegerField(verbose_name="최종 결제 금액")
    rating = models.PositiveSmallIntegerField(verbose_name="후기 평점")
    review_summary = models.CharField(max_length=500, verbose_name="후기 요약")

    class Meta:
        ordering = ["reservation_date"]
        verbose_name = "포트폴리오 예약 분석 데이터"
        verbose_name_plural = "포트폴리오 예약 분석 데이터"

    def __str__(self):
        return f"{self.reservation_date} / {self.service_name} / {self.rating}점"


class ServiceLog(models.Model):
    # date, category, visitor_count, sales_amount 필드 정의
    # DateField : YYYY-MM-DD 날짜필드
    date=models.DateField(verbose_name="날짜")

    category=models.CharField(max_length=50, verbose_name="카테고리")

    visitor_count=models.IntegerField(default=0, verbose_name="방문자수")
    sales_amount=models.IntegerField(default=0, verbose_name="매출액")
    # Java toString 해당기능
    def __str__(self):
        return f"[{self.date}] {self.category} 로그"

    class Meta:
        # 같은 날짜와 카테고리 통계는 한 건만 저장해 새로고침 중복을 막습니다.
        constraints = [
            models.UniqueConstraint(
                fields=["date", "category"],
                name="unique_service_log_date_category",
            )
        ]
