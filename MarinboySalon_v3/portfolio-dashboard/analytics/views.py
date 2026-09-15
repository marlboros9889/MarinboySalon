import json

import pandas as pd
from django.http import JsonResponse
from django.shortcuts import render
from django.views.decorators.http import require_POST

from .models import PortfolioReservation, ServiceLog


def dashboard_view(request):
    """완료 예약·후기 데이터를 Pandas로 집계해 관리자용 화면에 전달합니다."""
    reservation_queryset = PortfolioReservation.objects.all().values(
        "reservation_date", "service_name", "final_price", "rating"
    )

    if not reservation_queryset.exists():
        return render(request, "analytics/dashboard.html", {"is_empty": True})

    # 1. Django QuerySet을 DataFrame으로 바꿔 Pandas 집계 기능을 사용합니다.
    data_frame = pd.DataFrame(list(reservation_queryset))
    data_frame["reservation_date"] = pd.to_datetime(data_frame["reservation_date"])

    # 2. 시술별 예약 건수·매출과 별점 분포를 각각 계산합니다.
    service_summary = (
        data_frame.groupby("service_name")
        .agg(reservation_count=("service_name", "size"), sales_amount=("final_price", "sum"))
        .reset_index()
        .sort_values("reservation_count", ascending=False)
    )
    rating_summary = data_frame.groupby("rating").size().reindex(range(1, 6), fill_value=0)

    # 3. 날짜별 추이를 만들어 예약 흐름을 그래프로 보여줍니다.
    daily_summary = (
        data_frame.groupby("reservation_date")
        .agg(reservation_count=("service_name", "size"), sales_amount=("final_price", "sum"))
        .reset_index()
        .sort_values("reservation_date")
    )

    total_reservations = int(len(data_frame))
    total_sales = int(data_frame["final_price"].sum())
    average_rating = round(float(data_frame["rating"].mean()), 1)
    high_rating_ratio = round(float((data_frame["rating"] >= 4).mean() * 100), 1)
    top_service = service_summary.iloc[0]["service_name"]

    context = {
        "is_empty": False,
        "stats": {
            "total_reservations": total_reservations,
            "total_sales": f"{total_sales:,}",
            "average_rating": average_rating,
            "high_rating_ratio": high_rating_ratio,
        },
        "top_service": top_service,
        "analysis_period": (
            f"{daily_summary.iloc[0]['reservation_date']:%Y.%m.%d}"
            f" ~ {daily_summary.iloc[-1]['reservation_date']:%Y.%m.%d}"
        ),
        "service_rows": service_summary.to_dict("records"),
        # json_script가 문자열을 안전하게 JavaScript 데이터로 전달합니다.
        "daily_dates": daily_summary["reservation_date"].dt.strftime("%m/%d").tolist(),
        "daily_counts": daily_summary["reservation_count"].astype(int).tolist(),
        "rating_labels": [f"{rating}점" for rating in rating_summary.index.tolist()],
        "rating_counts": rating_summary.astype(int).tolist(),
    }
    return render(request, "analytics/dashboard.html", context)


@require_POST
def api_receive_statistics(request):
    """수업에서 다룬 외부 서비스 통계 수신 예제를 유지합니다."""
    try:
        data = json.loads(request.body)
        ServiceLog.objects.update_or_create(
            date=data.get("date"),
            category=data.get("category", "커뮤니티"),
            defaults={
                "visitor_count": data.get("count", 0),
                "sales_amount": data.get("sales_amount", 0),
            },
        )
        return JsonResponse({"status": "success", "message": "통계 데이터를 저장했습니다."})
    except (TypeError, ValueError) as error:
        return JsonResponse({"status": "error", "message": str(error)}, status=400)
