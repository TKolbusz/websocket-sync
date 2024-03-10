let WebSocket = require('ws'); // Include the 'ws' library for WebSockets

const tenantId = process.argv[2];

// Ensure a URL is provided
if (!tenantId) {
    console.error("Please provide a Tenant ID as a command-line argument.");
    process.exit(1);
}

let lastSyncAt = 0

function connect() {
    console.log("Connecting with lastSyncAt=" + lastSyncAt);
    let webSocket = new WebSocket(`ws://localhost:8080/${tenantId}/notifications?since=${lastSyncAt}`);
    webSocket.onmessage = function (event) {
        console.log(event.data)
        lastSyncAt = Math.floor(Date.now() / 1000);
    };

    webSocket.onerror = function (error) {
        console.error("WebSocket Error:", error);
    };

    webSocket.onclose = function () {
        console.log("WebSocket disconnected. Attempting to reconnect...");
        setTimeout(connect, 2000);
    };
    webSocket.onopen = function () {
        console.log("Connected to WebSocket");
    };
}

connect();
