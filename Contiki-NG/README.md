# Contiki-NG
In order to test our knowledge of Kafka, we decided to implement this simple scenario with 2 different components
- UDP client that sends fake temperature readings to a server every 10 seconds
- UDP server that collects the readings coming from the clients

Assumptions:
- clients may appear at any time, but they never disappear

The server has to handle two things:
- for each temperature it has to compute the average of the most recent readings and then if the average is above ALERT_THRESHOLD, it immediately notifies back-to-back all existing clients of a “high temperature alert”
- the server can handle a number of clients equal to MAX_RECEIVERS, so every reading from a new client is discarded

