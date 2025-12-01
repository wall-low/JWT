// Общий модуль для управления JWT токенами

// Проверяет и обновляет токен если он истёк или скоро истечёт
async function checkAndRefreshToken() {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    if (!accessToken || !refreshToken) {
        return false;
    }

    // Декодируем JWT чтобы проверить срок действия
    const tokenData = parseJwt(accessToken);
    const currentTime = Math.floor(Date.now() / 1000);

    // Если токен истёк или истечёт в течение 10 секунд
    if (tokenData.exp <= currentTime + 10) {
        console.log('🔄 Access token истёк или скоро истечёт. Обновляем...');

        try {
            const response = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    refreshToken: refreshToken
                })
            });

            if (response.ok) {
                const data = await response.json();

                // Сохраняем новый access token
                const oldToken = localStorage.getItem('accessToken');
                localStorage.setItem('accessToken', data.accessToken);

                // Статистика
                const count = parseInt(localStorage.getItem('refreshCount') || '0');
                localStorage.setItem('refreshCount', (count + 1).toString());
                localStorage.setItem('lastRefreshTime', Date.now().toString());

                console.log('✅ Access token обновлён!');
                console.log('🔑 Старый:', oldToken.slice(-20));
                console.log('🔑 Новый:', data.accessToken.slice(-20));
                console.log('♻️  Refresh token не изменился:', refreshToken === data.refreshToken);

                return true; // Токен был обновлён
            } else {
                console.error('❌ Ошибка обновления токена');
                // Редирект на логин если refresh токен невалиден
                localStorage.clear();
                window.location.href = '/login';
            }
        } catch (error) {
            console.error('❌ Ошибка:', error);
        }
    } else {
        const timeLeft = tokenData.exp - currentTime;
        console.log(`✅ Access token ещё валиден (осталось ${timeLeft} сек)`);
    }

    return false; // Токен не обновлялся
}

// Декодирование JWT токена
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Ошибка декодирования JWT:', e);
        return { exp: 0 };
    }
}

// Показать информацию о токене в консоли
function showTokenInfo() {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    if (accessToken) {
        const data = parseJwt(accessToken);
        const currentTime = Math.floor(Date.now() / 1000);
        const timeLeft = data.exp - currentTime;

        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        console.log('📊 Информация о токенах:');
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        console.log('👤 Username:', data.sub);
        console.log('⏰ Выдан:', new Date(data.iat * 1000).toLocaleTimeString());
        console.log('⏳ Истекает:', new Date(data.exp * 1000).toLocaleTimeString());
        console.log('⌛ Осталось:', timeLeft, 'секунд');
        console.log('🔑 Access (конец):', accessToken.slice(-20));
        console.log('♻️  Refresh (конец):', refreshToken.slice(-20));
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    }
}
