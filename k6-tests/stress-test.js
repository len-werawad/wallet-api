import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const loginFailRate = new Rate('login_fail_rate');
const loginDuration = new Trend('login_duration');
const dashboardFailRate = new Rate('dashboard_fail_rate');
const dashboardDuration = new Trend('dashboard_duration');
const totalRequests = new Counter('total_requests');
const timeoutErrors = new Counter('timeout_errors');

export const options = {
    stages: [
        { duration: '30s', target: 10 },   // Warm up slowly
        { duration: '1m', target: 30 },    // Reduced from 50 for local
        { duration: '2m', target: 50 },    // Reduced from 100 for local
        { duration: '30s', target: 30 },   // Step down
        { duration: '30s', target: 0 },    // Cool down
    ],
    thresholds: {
        // Overall HTTP metrics - adjusted for local stress test
        http_req_failed: [
            'rate<0.15',                                    // Allow 15% failure under stress
            { threshold: 'rate<0.50', abortOnFail: true }  // Abort if >50% critical
        ],
        http_req_duration: [
            'p(95)<3000',   // 95% under 3s (relaxed for high load)
            'p(99)<6000',   // 99% under 6s
        ],

        // Login specific thresholds
        login_fail_rate: [
            'rate<0.15',                                    // 15% acceptable under stress
            { threshold: 'rate<0.40', abortOnFail: true }  // Abort if >40% fail
        ],
        login_duration: [
            'p(95)<2500',   // 95% under 2.5s
            'p(99)<5000',   // 99% under 5s
        ],

        // Dashboard specific thresholds
        dashboard_fail_rate: [
            'rate<0.15',
            { threshold: 'rate<0.40', abortOnFail: true }
        ],
        dashboard_duration: [
            'p(95)<3000',
            'p(99)<6000',
        ],

        // Checks validation - ensure data integrity
        checks: [
            'rate>0.80',                                    // 80% checks pass under stress
            { threshold: 'rate>0.50', abortOnFail: true }  // Abort if <50% (critical issue)
        ],
    },
    // HTTP/connection settings
    noConnectionReuse: false,  // Reuse connections for better performance
    userAgent: 'K6StressTest/1.0',
};

// Base URL - change this to your API URL
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test users - add more for better distribution
const TEST_USERS = [
    { userId: '000018b0e1a211ef95a30242ac180002', pin: '123456' },
    { userId: '000043b3e1a211ef95a30242ac180002', pin: '123456' },
    { userId: '00005323e1a211ef95a30242ac180002', pin: '123456' },
    { userId: '00006207e1a211ef95a30242ac180002', pin: '123456' },
    { userId: '000075a6e1a211ef95a30242ac180002', pin: '123456' },
];

export function setup() {
    console.log(`Starting stress test against ${BASE_URL}`);
    console.log(`Test will run for approximately 4.5 minutes`);
    console.log(`Max VUs: 50 (reduced for local testing)`);
    return { baseUrl: BASE_URL };
}

export default function (data) {
    // Select random user for better load distribution
    const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];

    // ====================
    // TEST 1: Login with PIN
    // ====================
    const loginStart = Date.now();
    const loginPayload = JSON.stringify({
        userId: user.userId,
        pin: user.pin,
    });

    const loginParams = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: { name: 'Login' },
        timeout: '10s',  // Add explicit timeout (was defaulting to 60s!)
    };

    const loginRes = http.post(
        `${data.baseUrl}/v1/auth/login/pin`,
        loginPayload,
        loginParams
    );

    // Track timeouts separately
    if (loginRes.status === 0 || loginRes.timings.duration > 10000) {
        timeoutErrors.add(1);
        console.warn(`⚠️ Login timeout for user ${user.userId}`);
    }

    const loginSuccess = check(loginRes, {
        'login status is 200': (r) => r.status === 200,
        'login has access token': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data && body.data.accessToken !== undefined;
            } catch (e) {
                return false;
            }
        },
        'login response time < 3s': (r) => r.timings.duration < 3000,  // More realistic
    });

    loginFailRate.add(!loginSuccess);
    loginDuration.add(Date.now() - loginStart);
    totalRequests.add(1);

    if (!loginSuccess) {
        if (loginRes.status === 0) {
            console.error(`❌ Login timeout/connection error for ${user.userId}`);
        } else {
            console.error(`❌ Login failed: ${loginRes.status} - ${loginRes.body.substring(0, 200)}`);
        }
        sleep(1);  // Back off on error
        return;
    }

    // Extract access token
    let accessToken;
    try {
        const loginBody = JSON.parse(loginRes.body);
        accessToken = loginBody.data.accessToken;
    } catch (e) {
        console.error(`❌ Failed to parse login response: ${e}`);
        sleep(1);
        return;
    }

    // Small pause between requests (simulate user behavior)
    sleep(1);

    // ====================
    // TEST 2: Get Dashboard
    // ====================
    const dashboardStart = Date.now();
    const dashboardParams = {
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
        tags: { name: 'Dashboard' },
        timeout: '10s',  // Add explicit timeout
    };

    const dashboardRes = http.get(
        `${data.baseUrl}/v1/dashboards`,
        dashboardParams
    );

    // Track timeouts
    if (dashboardRes.status === 0 || dashboardRes.timings.duration > 10000) {
        timeoutErrors.add(1);
        console.warn(`⚠️ Dashboard timeout for user ${user.userId}`);
    }

    const dashboardSuccess = check(dashboardRes, {
        'dashboard status is 200': (r) => r.status === 200,
        'dashboard has greeting': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data.greeting !== undefined;
            } catch (e) {
                return false;
            }
        },
        'dashboard has accounts': (r) => {
            try {
                const body = JSON.parse(r.body);
                return Array.isArray(body.data.accounts);
            } catch (e) {
                return false;
            }
        },
        'dashboard response time < 3s': (r) => r.timings.duration < 3000,
    });

    dashboardFailRate.add(!dashboardSuccess);
    dashboardDuration.add(Date.now() - dashboardStart);
    totalRequests.add(1);

    if (!dashboardSuccess) {
        if (dashboardRes.status === 0) {
            console.error(`❌ Dashboard timeout/connection error for ${user.userId}`);
        } else {
            console.error(`❌ Dashboard failed: ${dashboardRes.status}`);
        }
        sleep(1);  // Back off on error
        return;
    }

    // Think time - simulate user reading dashboard
    sleep(Math.random() * 1 + 0.5); // Random sleep between 0.5-1.5 seconds
}

export function teardown(data) {
    console.log('\n=================================');
    console.log('✅ Stress test completed!');
    console.log(`📍 Base URL: ${data.baseUrl}`);
    console.log('=================================');
}
