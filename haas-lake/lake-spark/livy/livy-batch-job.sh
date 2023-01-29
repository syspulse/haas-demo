JOB_FILE=${1:-spark-examples_2.12-3.2.0-amzn-0.jar}

NAME=${2:-HAAS-JOB}
SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

# Copy Job to S3 (for compute nodes)
cp $JOB_FILE /mnt/s3/data/dev/job/

DATA="{ \"name\": \"${NAME}\", \"file\": \"s3://haas-data-dev/data/dev/job/${JOB_FILE}\", \"className\": \"org.apache.spark.examples.SparkPi\" }"

curl -k -X POST --data "${DATA}" \
   -H "Content-Type: application/json" \
   ${SERVICE_URI}/batches

