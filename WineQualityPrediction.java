package com.example;

import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import static org.apache.spark.sql.functions.col;

public class WineQualityPrediction {
    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("Wine Quality Prediction")
                .master("local[*]") // Use your cluster's master URL if deploying
                .getOrCreate();

        // Read and process dataset
        Dataset<Row> trainData = spark.read()
                .option("header", "true") // Automatically uses the first row as header
                .option("delimiter", ";")
                .csv("/home/ubuntu/TrainingDataset.csv");

        // Clean the column names by removing extra quotes and spaces
        String[] cleanedColumns = trainData.columns();
        for (int i = 0; i < cleanedColumns.length; i++) {
            cleanedColumns[i] = cleanedColumns[i].replaceAll("\"", "").trim(); // Remove quotes and trim spaces
        }
        trainData = trainData.toDF(cleanedColumns);

        // Cast "quality" to integer and create binary labels
        trainData = trainData.withColumn("quality", col("quality").cast("int"));
        trainData = trainData.withColumn("quality", functions.when(col("quality").geq(7), 1).otherwise(0));

        // Cast feature columns to DoubleType for proper processing
        String[] featureColumns = {"fixed acidity", "volatile acidity", "sulphates", "alcohol", "density"};
        for (String column : featureColumns) {
            trainData = trainData.withColumn(column, col(column).cast("double"));
        }

        // Create feature vector using VectorAssembler
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(featureColumns)
                .setOutputCol("features");

        Dataset<Row> featureData = assembler.transform(trainData);

        // Split into training and testing sets
        Dataset<Row>[] splits = featureData.randomSplit(new double[]{0.8, 0.2}, 1000);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testingData = splits[1];

        // Apply feature scaling
        StandardScaler scaler = new StandardScaler()
                .setInputCol("features")
                .setOutputCol("scaledFeatures")
                .setWithMean(true)
                .setWithStd(true);

        trainingData = scaler.fit(trainingData).transform(trainingData);
        testingData = scaler.fit(testingData).transform(testingData);

        // Logistic Regression Model
        LogisticRegression logisticRegression = new LogisticRegression()
                .setMaxIter(1000)
                .setFeaturesCol("scaledFeatures")
                .setLabelCol("quality");

        LogisticRegressionModel logisticModel = logisticRegression.fit(trainingData);

        // Predict on the test set
        Dataset<Row> predictions = logisticModel.transform(testingData);

        // Evaluate the model
        predictions.select("quality", "prediction")
                .show();

        spark.stop();
    }
}
