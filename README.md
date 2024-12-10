
CS643 Programming Assignment-2
This project showcases the distributed training of a machine learning model using Apache Spark on a cluster of four instances. The model evaluates wine quality and achieves an F1 score of 0.8730357142857142 on the validation dataset using an Random Forest model. The following steps detail the entire setup process, from configuring instances to running the Spark job via Docker.

Link to Docker Image: [https://hub.docker.com/r/abhiraj1625/wine-quality-eval](https://hub.docker.com/r/abhiraj1625/wine-eval)


Steps to Set Up and Execute the Project
1. SSH into the Instances
Log into each of your four instances using SSH. Replace <instance-ip> with the IP address of the specific instance:

bash

ssh -i /path/to/your/private-key.pem ubuntu@<instance-ip>
2. Generate SSH Keys
Generate SSH key pairs on each instance to enable passwordless communication between them:

bash

ssh-keygen -t rsa -N "" -f /home/ubuntu/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub
Copy the public key from each instance and add it to the authorized_keys file of all other instances.

3. Update the /etc/hosts File
Edit the /etc/hosts file on each instance to map the hostnames of all instances:

bash

sudo vim /etc/hosts
Add entries similar to the following (replace <ip-address> with actual instance IPs):

css

<ip-address> nn
<ip-address> dd1
<ip-address> dd2
<ip-address> dd3
4. Install the Required Software
Install Java, Maven, and Spark on all instances.

Install Java:

bash

sudo apt update
sudo apt install openjdk-8-jdk -y
Install Maven:

bash

sudo apt install maven -y
Install Spark: Download and extract Spark:

bash

wget https://archive.apache.org/dist/spark/spark-3.4.1/spark-3.4.1-bin-hadoop3.tgz
tar -xvzf spark-3.4.1-bin-hadoop3.tgz
Set environment variables:

bash

echo "export SPARK_HOME=/home/ubuntu/spark-3.4.1-bin-hadoop3" >> ~/.bashrc
echo "export PATH=\$SPARK_HOME/bin:\$PATH" >> ~/.bashrc
source ~/.bashrc
5. Configure Spark Workers
Copy and update the workers file to include all cluster nodes:

bash

cp $SPARK_HOME/conf/workers.template $SPARK_HOME/conf/workers
vim $SPARK_HOME/conf/workers
Add the following lines (replace with actual hostnames or IP addresses):

bash

localhost
dd1/ip-address
dd2/ip-address
dd3/ip-address
6. Set Up Directories for Training and Evaluation
On each instance, create directories for training and evaluation:

bash

mkdir ~/Training
mkdir ~/Eval
Place the respective Java code files for training and evaluation in these directories.

7. Run the Training Code
Execute the training code with Spark:

bash

spark-submit --master spark://<master-ip>:7077 --class com.example.WineQualityEval /home/ubuntu/Training/wine-quality-train-1.0-SNAPSHOT.jar
Replace <master-ip> with the IP address of the Spark master node.

8. Build a Docker Image
Create a Docker image to package the application:

Dockerfile:

dockerfile

Use the official Spark image as a base image
FROM bitnami/spark:3.4.1

Set the working directory inside the container
WORKDIR /app

Copy WineQualityEval (containing the JAR) to the container
COPY WineQualityEval /app/WineQualityEval

Copy WineQualityPredictionModel to /home/ubuntu
COPY WineQualityPredictionModel /home/ubuntu/WineQualityPredictionModel

Copy ValidationDataset.csv to /home/ubuntu
COPY ValidationDataset.csv /home/ubuntu/ValidationDataset.csv

Set the command to run your Spark job
CMD ["spark-submit", "--master", "local", "--class", "com.example.WineQualityEval", "/app/WineQualityEval/target/wine-quality-eval-1.0-SNAPSHOT.jar"]
Build and push the image:

bash

sudo docker build -t abhiraj1625/wine-quality-eval:latest .
sudo docker push abhiraj1625/wine-quality-eval:latest

9. Pull and Run the Docker Image
On each instance, pull the Docker image:
bash
sudo docker pull abhiraj1625/wine-quality-eval:latest

Run the container:
bash
sudo docker run abhiraj1625/wine-quality-eval:latest

10. Results
The final F1 score achieved on the validation dataset is:

F1 Score: 0.8730357142857142
