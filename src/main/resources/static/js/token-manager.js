// Работа с JWT на стороне браузера: хранение, обновление, запросы с токеном.

const TokenManager = (() => {

    const listeners = [];

    function notify(message) {
        const time = new Date().toLocaleTimeString('ru-RU');
        listeners.forEach(fn => fn(time, message));
    }

    function onEvent(fn) {
        listeners.push(fn);
    }

    function parseJwt(token) {
        try {
            const payload = token.split('.')[1];
            const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
            const json = decodeURIComponent(
                atob(normalized)
                    .split('')
                    .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                    .join('')
            );
            return JSON.parse(json);
        } catch (e) {
            return null;
        }
    }

    function secondsLeft(token) {
        const data = parseJwt(token);
        if (!data || !data.exp) {
            return 0;
        }
        return Math.max(0, data.exp - Math.floor(Date.now() / 1000));
    }

    function getAccessToken() {
        return localStorage.getItem('accessToken');
    }

    function getUsername() {
        return localStorage.getItem('username');
    }

    function getRefreshCount() {
        return parseInt(localStorage.getItem('refreshCount') || '0', 10);
    }

    function clear() {
        localStorage.clear();
    }

    function requireAuth() {
        if (!getAccessToken() || !localStorage.getItem('refreshToken')) {
            window.location.href = '/login';
            return false;
        }
        return true;
    }

    // Обновляет access токен, если до истечения осталось меньше порога.
    // Возвращает true, если обновление действительно произошло.
    async function ensureFresh(thresholdSeconds = 10) {
        const accessToken = getAccessToken();
        const refreshToken = localStorage.getItem('refreshToken');

        if (!accessToken || !refreshToken) {
            return false;
        }

        if (secondsLeft(accessToken) > thresholdSeconds) {
            return false;
        }

        notify('Access токен истекает, запрашиваю обновление');

        const response = await fetch('/api/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (!response.ok) {
            notify('Refresh токен отклонён, требуется повторный вход');
            clear();
            window.location.href = '/login';
            return false;
        }

        const data = await response.json();
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshCount', String(getRefreshCount() + 1));

        notify('Получен новый access токен, refresh не менялся');
        return true;
    }

    // Запрос к защищённому API: сначала при необходимости обновляет токен.
    async function authFetch(url, options = {}) {
        await ensureFresh();

        const headers = Object.assign({}, options.headers, {
            'Authorization': 'Bearer ' + getAccessToken()
        });

        return fetch(url, Object.assign({}, options, { headers }));
    }

    async function logout() {
        const token = getAccessToken();

        if (token) {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            }).catch(() => {});
        }

        clear();
        window.location.href = '/login';
    }

    return {
        parseJwt, secondsLeft, getAccessToken, getUsername, getRefreshCount,
        requireAuth, ensureFresh, authFetch, logout, onEvent
    };
})();
