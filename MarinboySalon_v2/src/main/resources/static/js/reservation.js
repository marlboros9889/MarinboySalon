// 과거 날짜를 선택하지 못하도록 오늘 날짜를 예약 입력의 최솟값으로 지정합니다.
document.addEventListener("DOMContentLoaded", function () {
    const reservationDateInputs = document.querySelectorAll(".reservation-date");
    const today = new Date();
    const timezoneOffset = today.getTimezoneOffset() * 60000;
    const localToday = new Date(today.getTime() - timezoneOffset);
    const minimumDate = localToday.toISOString().slice(0, 10);

    reservationDateInputs.forEach(function (reservationDateInput) {
        reservationDateInput.min = minimumDate;
    });

    console.log("예약 날짜의 최소 선택일을 설정했습니다.");
});
