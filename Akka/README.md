# Kafka Scenario
In order to test our knowledge of Kafka, we decided to implement this simple scenario with 3 different components
- Producer component, which creates some tuples of random <`key`,`value`> related to the `topicA`
- C2 component, the first consumer that reads the tuples related to the `topicA` and it produces some `other tuples related to the ones already read, and publishes them under the `topicB`
- C1 component, which is subscribed to the `topicB` and reads all the tuples written by C2

The producer continues to write tuples with
- random `key`, which is a random integer
- random `value`, which is a random sequence of characters all lowercase
Then it publishes those tuples on the `topicA`.

C2 is subscribed to the `topicA` and it gets all the tuples produced by the Producer. C2 has a Map structure to keep track of each key published by the Producer, with the number of times a tuple with that specific key is produced. So C2 is a stateful component, because it has to update constantly the value of the Map, based on the new tuple read from the `topicA`.  Then for each update done, it publishes the new tuple of value <`key`,`counter`> on the `topicB`.

C1 compopnent is subscriebd to `topicB`, so in the end it gets the values of the Map of the C2 component.
