import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const loginFailRate = new Rate('login_fail_rate');
const loginDuration = new Trend('login_duration');
const dashboardFailRate = new Rate('dashboard_fail_rate');
const dashboardDuration = new Trend('dashboard_duration');
const timeoutErrors = new Counter('timeout_errors');
const totalRequests = new Counter('total_requests');

const BASE_URL = 'http://localhost:8080';
const REQUEST_TIMEOUT = '10s';

const USERS = [
    { userId: 'u1', pin: '123456' },
];

const THINK_TIME_MIN_SECONDS = 0.1;
const THINK_TIME_MAX_SECONDS = 0.4;

export const options = {
    scenarios: {
        steady_ramp: {
            executor: 'ramping-vus',
            gracefulStop: '30s',
            stages: [
                { duration: '1m', target: 25 },
                { duration: '2m', target: 75 },
                { duration: '2m', target: 75 },
                { duration: '2m', target: 150 },
                { duration: '2m', target: 150 },
                { duration: '2m', target: 75 },
                { duration: '1m', target: 0 },
            ],
        },
        spike: {
            executor: 'ramping-vus',
            startTime: '7m',
            gracefulStop: '30s',
            stages: [
                { duration: '5s', target: 250 },
                { duration: '30s', target: 250 },
                { duration: '10s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05', { threshold: 'rate<0.25', abortOnFail: true }],
        http_req_duration: ['p(90)<1000', 'p(95)<2000', 'p(99)<5000'],

        login_fail_rate: ['rate<0.05', { threshold: 'rate<0.20', abortOnFail: true }],
        login_duration: ['p(95)<1500', 'p(99)<3000'],

        dashboard_fail_rate: ['rate<0.05', { threshold: 'rate<0.20', abortOnFail: true }],
        dashboard_duration: ['p(90)<1200', 'p(95)<2000', 'p(99)<5000'],

        checks: ['rate>0.95', { threshold: 'rate>0.80', abortOnFail: true }],
        timeout_errors: ['count<1'],
    },
    noConnectionReuse: false,
    userAgent: 'K6StressTest/1.0',
};

function safeJson(text) {
    try {
        return JSON.parse(text);
    } catch (_) {
        return null;
    }
}

function pickUser() {
    return USERS[Math.floor(Math.random() * USERS.length)];
}

function thinkTime() {
    const t = THINK_TIME_MIN_SECONDS + Math.random() * (THINK_TIME_MAX_SECONDS - THINK_TIME_MIN_SECONDS);
    sleep(t);
}

function login(userId, pin) {
    const start = Date.now();

    const res = http.post(
        `${BASE_URL}/v1/auth/login/pin`,
        JSON.stringify({ userId, pin }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'Login' },
            timeout: REQUEST_TIMEOUT,
        }
    );

    if (res.status === 0) {
        timeoutErrors.add(1);
    }

    const ok = check(res, {
        'login status is 200': (r) => r.status === 200,
        'login has access token': (r) => {
            const body = safeJson(r.body);
            return !!(body && body.data && body.data.accessToken);
        },
    });

    loginFailRate.add(!ok);
    loginDuration.add(Date.now() - start);
    totalRequests.add(1);

    const body = safeJson(res.body);
    return body && body.data && body.data.accessToken ? body.data.accessToken : null;
}

export function setup() {
    console.log(`Starting k6 stress test against ${BASE_URL}`);

    const tokensByUserId = {};
    for (const u of USERS) {
        const token = login(u.userId, u.pin);
        if (token) {
            tokensByUserId[u.userId] = token;
        }
    }

    if (Object.keys(tokensByUserId).length === 0) {
        throw new Error('No tokens acquired in setup. Check BASE_URL / credentials.');
    }

    return { tokensByUserId };
}

export default function (data) {
    const user = pickUser();

    let token = data.tokensByUserId[user.userId];
    if (!token) {
        token = login(user.userId, user.pin);
        if (!token) {
            sleep(1);
            return;
        }
        data.tokensByUserId[user.userId] = token;
    }

    thinkTime();

    const start = Date.now();
    const res = http.get(`${BASE_URL}/v1/dashboards`, {
        headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
        tags: { name: 'Dashboard' },
        timeout: REQUEST_TIMEOUT,
    });

    if (res.status === 0) {
        timeoutErrors.add(1);
    }

    const ok = check(res, {
        'dashboard status is 200': (r) => r.status === 200,
        'dashboard has greeting': (r) => {
            const body = safeJson(r.body);
            return !!(body && body.data && body.data.greeting !== undefined);
        },
        'dashboard has accounts': (r) => {
            const body = safeJson(r.body);
            return !!(body && body.data && Array.isArray(body.data.accounts));
        },
    });

    dashboardFailRate.add(!ok);
    dashboardDuration.add(Date.now() - start);
    totalRequests.add(1);

    if (!ok && res.status === 401) {
        const refreshed = login(user.userId, user.pin);
        if (refreshed) {
            data.tokensByUserId[user.userId] = refreshed;
        }
    }

    thinkTime();
}

export function teardown() {
    console.log('Stress test completed');
}
