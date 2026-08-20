// 세션 쿠키를 사용하는 상태 변경 요청에 CSRF 헤더를 자동으로 추가합니다.
(() => {
    const originalFetch = window.fetch.bind(window);
    const safeMethods = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);
    let csrfToken;

    async function loadCsrfToken() {
        if (csrfToken) return csrfToken;
        const response = await originalFetch('/api/csrf', { credentials: 'same-origin' });
        if (!response.ok) throw new Error('CSRF 토큰을 불러오지 못했습니다.');
        csrfToken = (await response.json()).token;
        return csrfToken;
    }

    window.fetch = async (input, init = {}) => {
        const method = (init.method || 'GET').toUpperCase();
        if (safeMethods.has(method)) return originalFetch(input, init);

        const headers = new Headers(init.headers || {});
        headers.set('X-XSRF-TOKEN', await loadCsrfToken());
        return originalFetch(input, { ...init, headers, credentials: init.credentials || 'same-origin' });
    };
})();
