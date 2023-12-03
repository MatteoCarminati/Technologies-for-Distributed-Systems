package it.polimi.nsds.spark.lab.cities;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "/Users/andreapirrotta/Documents/Uni/Magistrale/Secondo_Anno/Primo_Semestre/NSDS/spark/lab/lab_ex/";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        final Dataset<Row> joinedTable = citiesRegions
            .join(citiesPopulation,citiesRegions.col("city").equalTo(citiesPopulation.col("city")));
        joinedTable.cache();

        final Dataset<Row> q1 = joinedTable
            .groupBy(col("region"))
            .sum("population")
            .select("region","sum(population)");

        q1.show();

        final Dataset<Row> q2 = joinedTable
            .groupBy(col("region"))
            .count()
            .join(
                joinedTable
                    .groupBy("region")
                    .max("population")
                    .withColumnRenamed("region","region2")
                , col("region").equalTo(col("region2"))
            )
            .select("region","count","max(population)");

        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        population.cache();
        int i=1;
        int tot = population.reduce((a,b)->a+b);
        while(tot<100000000){
            population = population.map(r -> r<1000?r*99/100:r*101/100);
            population.cache();
            tot = population.reduce((a,b)->a+b);
            System.out.println("Year: " +i+ ", total population: " + tot);
            i++;
        }

        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        final StreamingQuery q4 = bookings
                .join(joinedTable,col("value").equalTo(col("id")))
                .groupBy(
                        col("region"),
                        window(
                                col("timestamp"),"30 seconds","5 seconds")
                        )
                .count()
                .select("count","region")
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}