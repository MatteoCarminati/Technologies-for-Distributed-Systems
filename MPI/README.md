# MPI
Some exercises to test our knowledge of MPI.

1. Simulation to estimate the value of Pi
For each process we extract a random number for x and y. Then we check how many times this equation `x^2+y^2 <= 1` is satisfied.
Then we multiply this value * 4 and divide by the number of processes and numbers of iterations for each process.
The final value should be an estimation of pi.

2. A guess game
This is a very simple game with N rounds.
One process is the leader and selects a number X between 1 and 1000.
All the other processes select a random number and they send it to the leader. The one that selects the number that is closest to X wins the round and becomes the leader for the next round. In case of a tie, no one wins and the leader does not change.
We keep track for each process the number of times it wins.

3. Traffic simulator
The street is divided in consecutive segements. Each cluster of segment is controlled by a process. The simulation evolves in descrete rounds for a given number of iterations.
At each round 
- some cars enter in the road in the segment 0
- each car either remains in the same segment or moves to the next one

