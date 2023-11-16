# Akka Scenario
In order to test our knowledge of Kafka, we decided to implement this simple scenario with 3 different components
- Client actor
- Server actor
- Supervisor actor

The server in encharge of saving inside a map tuples formed with
- random `email`,
- random `name`

The client can interact with the server actor with two possible types of messages
- `GetMsg` in order to retrieve the email associated to a certain name
- `PutMsg` to add to the datastructure of the server a new tuple

We also added the possibility of failure, such that in a random way, the server when receives a put message it can go on failure and throw a new Exception. In this case the supervisor is encharge of stopping the Server actor and of trying to restart it. Of course by simply changing the policy we can also test the resume operation, in order to retrieve the state of the server before its failure.
