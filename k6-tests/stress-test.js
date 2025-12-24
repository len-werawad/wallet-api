import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const loginFailRate = new Rate('login_fail_rate');
const loginDuration = new Trend('login_duration');
const dashboardFailRate = new Rate('dashboard_fail_rate');
const dashboardDuration = new Trend('dashboard_duration');
const totalRequests = new Counter('total_requests');

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '2m', target: 100 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<1500', 'p(99)<3000'],
        login_fail_rate: ['rate<0.10'],
        dashboard_fail_rate: ['rate<0.10'],
        login_duration: ['p(95)<1200'],
        dashboard_duration: ['p(95)<1500'],
    },
};

// Base URL - change this to your API URL
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test users
const TEST_USERS = [
    { userId: '000018b0e1a211ef95a30242ac180002', pin: '123456' },
    { userId: '000043b3e1a211ef95a30242ac180002', pin: '123456' },
    { userId: '00005323e1a211ef95a30242ac180002', pin: '123456' },
];

export function setup() {
    console.log(`Starting stress test against ${BASE_URL}`);
    console.log(`Test will run for approximately 5 minutes`);
    return { baseUrl: BASE_URL };
}

export default function (data) {
    // Select random user
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
    };

    const loginRes = http.post(
        `${data.baseUrl}/v1/auth/login/pin`,
        loginPayload,
        loginParams
    );

    const loginSuccess = check(loginRes, {
        'login status is 200': (r) => r.status === 200,
        'login has access token': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data.accessToken !== undefined;
            } catch (e) {
                return false;
            }
        },
        'login response time < 500ms': (r) => r.timings.duration < 500,
    });

    loginFailRate.add(!loginSuccess);
    loginDuration.add(Date.now() - loginStart);
    totalRequests.add(1);

    if (!loginSuccess) {
        console.error(`Login failed: ${loginRes.status} - ${loginRes.body}`);
        sleep(1);
        return;
    }

    // Extract access token
    let accessToken;
    try {
        const loginBody = JSON.parse(loginRes.body);
        accessToken = loginBody.accessToken;
    } catch (e) {
        console.error(`Failed to parse login response: ${e}`);
        sleep(1);
        return;
    }

    // Small pause between requests
    // sleep(0.5);

    // ====================
    // TEST 2: Get Dashboard
    // ====================
    // const dashboardStart = Date.now();
    // const dashboardParams = {
    //   headers: {
    //     'Authorization': `Bearer ${accessToken}`,
    //     'Content-Type': 'application/json',
    //   },
    //   tags: { name: 'Dashboard' },
    // };
    //
    // const dashboardRes = http.get(
    //   `${data.baseUrl}/v1/dashboards`,
    //   dashboardParams
    // );
    //
    // const dashboardSuccess = check(dashboardRes, {
    //   'dashboard status is 200': (r) => r.status === 200,
    //   'dashboard has greeting': (r) => {
    //     try {
    //       const body = JSON.parse(r.body);
    //       return body.greeting !== undefined;
    //     } catch (e) {
    //       return false;
    //     }
    //   },
    //   'dashboard has accounts': (r) => {
    //     try {
    //       const body = JSON.parse(r.body);
    //       return Array.isArray(body.accounts);
    //     } catch (e) {
    //       return false;
    //     }
    //   },
    //   'dashboard response time < 1000ms': (r) => r.timings.duration < 1000,
    // });
    //
    // dashboardFailRate.add(!dashboardSuccess);
    // dashboardDuration.add(Date.now() - dashboardStart);
    // totalRequests.add(1);
    //
    // if (!dashboardSuccess) {
    //   console.error(`Dashboard failed: ${dashboardRes.status} - ${dashboardRes.body}`);
    // }
    //
    // // Think time - simulate user reading dashboard
    // sleep(Math.random() * 2 + 1); // Random sleep between 1-3 seconds
}

export function teardown(data) {
    console.log('Stress test completed!');
    console.log(`Base URL: ${data.baseUrl}`);
}

