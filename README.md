## WebSocket Sync Application

The goal of this service is to showcase data synchronization design using WebSockets.

WebSocket Notifications:
Enables clients to subscribe to notifications for a specific tenant.
Broadcasts change notifications to subscribed clients.
Client is responsible for storing point in time where last message was successfully read.

After connecting, server replays all the events since the last time client had disconnected.

Reservation REST API:
Allows the creation of new reservation entities.
Triggers notifications through the WebSocket channel upon reservation changes.

### Technologies

1. Micronaut
2. WebSocket
3. PostgreSQL PubSub

### Example usage

Start the server
```bash
./gradlew run
```
Start the client
```bash
node client/client.js 1
```
Create Reservation
```bash
curl -X POST -H "Content-Type: application/json" -d '{"startAt": "2024-03-12T15:00:00", "endAt": "2024-03-12T16:30:00", "customer": "Alice"}' http://localhost:8080/1/reservations 
```
