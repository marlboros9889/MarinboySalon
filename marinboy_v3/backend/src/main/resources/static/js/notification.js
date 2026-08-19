// 관리자 화면의 알림 개수, 목록, SSE 실시간 갱신을 담당합니다.
(() => {
  const badge = document.querySelector('#notificationBadge');
  const list = document.querySelector('#notificationList');
  const detailModalElement = document.querySelector('#reservationNotificationModal');
  if (!badge || !list || !detailModalElement) return;

  const statusLabels = {
    REQUESTED: '예약 대기', CONFIRMED: '예약 승인', REJECTED: '예약 거절',
    CANCELED: '예약 취소', COMPLETED: '시술 완료', NO_SHOW: '노쇼'
  };

  function setDetail(id, value) {
    // 고객 입력값은 textContent로 표시해 상세 창에서 HTML이 실행되지 않게 합니다.
    document.querySelector(id).textContent = value || '-';
  }

  //1. 예약 상세 조회 후 새 모달 창 표시  GET: /api/admin/reservations/{id}
  async function showReservationDetail(reservationId) {
    const response = await fetch(`/api/admin/reservations/${reservationId}`);
    if (!response.ok) return;
    const reservation = await response.json();
    setDetail('#notificationReservationId', `#${reservation.id}`);
    setDetail('#notificationReservationDateTime', String(reservation.reservationDateTime || '').replace('T', ' '));
    setDetail('#notificationCustomerName', reservation.customerName);
    setDetail('#notificationCustomerPhone', reservation.customerPhone);
    setDetail('#notificationCustomerEmail', reservation.customerEmail);
    setDetail('#notificationServiceName', reservation.serviceName);
    setDetail('#notificationServiceDetail', `${reservation.serviceCategory || '-'} · ${reservation.durationMinutes || '-'}분`);
    setDetail('#notificationMemo', reservation.memo || '요청 사항 없음');
    setDetail('#notificationStatus', statusLabels[reservation.status] || reservation.status);
    // 모달을 body 바로 아래에 두어 다른 화면 요소의 쌓임 맥락보다 항상 앞에 표시합니다.
    if (detailModalElement.parentElement !== document.body) document.body.append(detailModalElement);
    bootstrap.Modal.getOrCreateInstance(detailModalElement).show();
  }

  function renderBadge(count) {
    badge.textContent = count;
    badge.classList.toggle('d-none', count < 1);
  }

  async function loadCount() {
    const response = await fetch('/api/admin/notifications/count');
    if (response.ok) renderBadge(await response.json());
  }

  function appendTextRow(item) {
    const row = document.createElement('li');
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'dropdown-item border-bottom py-2 text-wrap';
    const message = document.createElement('strong');
    message.className = 'd-block';
    message.textContent = item.message;
    const time = document.createElement('small');
    time.className = 'text-muted';
    time.textContent = String(item.createdAt || '').replace('T', ' ');
    button.append(message, time);
    button.addEventListener('click', () => showReservationDetail(item.reservationId));
    row.append(button);
    list.append(row);
  }

  async function openNotifications() {
    const response = await fetch('/api/admin/notifications');
    if (!response.ok) return;
    const notifications = await response.json();
    list.replaceChildren();
    if (notifications.length === 0) {
      const empty = document.createElement('li');
      empty.className = 'dropdown-item text-muted';
      empty.textContent = '알림이 없습니다.';
      list.append(empty);
    } else {
      notifications.forEach(appendTextRow);
    }
    const readResponse = await fetch('/api/admin/notifications/read-all', { method: 'POST' });
    if (readResponse.ok) renderBadge(0);
  }

  function connect() {
    const source = new EventSource('/api/admin/notifications/subscribe');
    source.addEventListener('newReservation', event => {
      const data = JSON.parse(event.data);
      renderBadge(data.unreadCount);
      // 작은 토스트 대신 상세 정보가 담긴 큰 말풍선 창을 화면 중앙에 표시합니다.
      showReservationDetail(data.reservationId);
      if (typeof window.load === 'function') window.load();
    });
    source.onerror = () => {
      source.close();
      window.setTimeout(connect, 5000);
    };
  }

  document.querySelector('#notificationButton').addEventListener('click', openNotifications);
  loadCount();
  connect();
})();
