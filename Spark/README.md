# Apache Spark 
In the folder `files/cities` there are two csv files used for the purpose of the exercise.
We have three sources of input:
Three input datasets
1. citiesRegion - Type: static, csv file
Fields: city, region
2. citiesPopulation – Type: static, csv file
Fields: id (of the city), city, population
3.bookings – Type: dynamic, stream
Fields: timestamp, value
Each entry with value x indicates that someone booked a hotel in the city with id = x

We used Spark SQL to perform two queries on the static datasets 1 & 2:
- we computed the total population for each region
- we computed the number of cities and the population of the most populated city for each region

Then we tested a possible iterative computation, by printing the evolution of the population in Italy year by year until the total population in Italy overcomes 100M people
We asssumed that the population evolves as follows:
- In cities with more than 1000 inhabitants, it increases by 1% every year
- In cities with less than 1000 inhabitants, it decreased by 1% every year

Then we combined some static analysis with dynamic one, using the 3rd input dataset.
We computed the total number of bookings for each region, in a window of 30 seconds, sliding every 5 seconds
