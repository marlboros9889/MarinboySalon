import csv
from pathlib import Path

from django.core.management.base import BaseCommand
from django.db import transaction

from analytics.models import PortfolioReservation


class Command(BaseCommand):
    help = "포트폴리오 후기 시드 SQL을 재현한 익명 분석 CSV를 SQLite에 적재합니다."

    def handle(self, *args, **options):
        # Django 앱 내부 CSV를 읽어 실행 위치가 달라도 같은 자료를 사용합니다.
        csv_path = Path(__file__).resolve().parents[2] / "data" / "portfolio_reservations.csv"

        with csv_path.open("r", encoding="utf-8-sig", newline="") as csv_file:
            rows = list(csv.DictReader(csv_file))

        with transaction.atomic():
            # 분석 전용 테이블만 초기화해 이전 수업 예제 데이터와 섞이지 않게 합니다.
            PortfolioReservation.objects.all().delete()
            reservation_list = []

            for row in rows:
                reservation_list.append(
                    PortfolioReservation(
                        reservation_date=row["reservation_date"],
                        service_name=row["service_name"],
                        final_price=int(row["final_price"]),
                        rating=int(row["rating"]),
                        review_summary=row["review_summary"],
                    )
                )

            PortfolioReservation.objects.bulk_create(reservation_list)

        self.stdout.write(self.style.SUCCESS(f"포트폴리오 분석 데이터 {len(rows)}건을 적재했습니다."))
