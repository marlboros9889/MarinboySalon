# 포트폴리오 분석 전용 원본 데이터를 Django SQLite에 적재하기 위한 테이블입니다.
from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ("analytics", "0002_servicelog_unique_date_category"),
    ]

    operations = [
        migrations.CreateModel(
            name="PortfolioReservation",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("reservation_date", models.DateField(verbose_name="예약 일자")),
                ("service_name", models.CharField(max_length=100, verbose_name="시술명")),
                ("final_price", models.PositiveIntegerField(verbose_name="최종 결제 금액")),
                ("rating", models.PositiveSmallIntegerField(verbose_name="후기 평점")),
                ("review_summary", models.CharField(max_length=500, verbose_name="후기 요약")),
            ],
            options={
                "ordering": ["reservation_date"],
                "verbose_name": "포트폴리오 예약 분석 데이터",
                "verbose_name_plural": "포트폴리오 예약 분석 데이터",
            },
        ),
    ]
