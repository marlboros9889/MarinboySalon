# Marinboy Salon 예약·후기 분석 대시보드

`Django + Pandas + Chart.js` 수업 예제를 Marinboy Salon 포트폴리오 데이터로 확장한 결과물입니다.

## 실행

```powershell
cd MarinboySalon_v3\portfolio-dashboard
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
python manage.py migrate
python manage.py load_portfolio_data
python manage.py runserver
```

브라우저에서 `http://127.0.0.1:8000/dashboard/`를 열면 됩니다.

## 자료 출처와 보호 방식

- 원본: `C:\marinboySalon\MarinboySalon_v3\database\migrations\20260909_portfolio_review_seed.sql`
- 대시보드 입력: `analytics\data\portfolio_reservations.csv`
- 원본 운영 DB에는 접속하거나 수정하지 않습니다. 분석 전용 SQLite 테이블에 익명 데이터만 적재합니다.

## 핵심 파일

- `analytics/models.py`: 분석 전용 `PortfolioReservation` 모델
- `analytics/management/commands/load_portfolio_data.py`: CSV 적재 명령
- `analytics/views.py`: Pandas 집계 로직
- `analytics/templates/analytics/dashboard.html`: Chart.js 화면
- `report/ANALYSIS_REPORT.md`: 결과보고서와 분석
