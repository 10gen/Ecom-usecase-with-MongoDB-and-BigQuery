# Dataflow - MongoDB-to-BigQuery
## Batch Job -Template

**Purpose**

The Batch dataflow jobs will be responsible to move data from mongodb collection to BigQuery tables.
The BigQuery table will be created automatically if already not exist.
Mongodb URI and Collection details are input to the program.

**Setup Details:**

1.Export below variables into your local environmental variable(the machine used to build image). 
```
export PROJECT=***${your project-id}*** \
export IMAGE_NAME=***"mongodb-to-bigquery***" \
export BUCKET_NAME=gs://***bucket-name*** \
export TARGET_GCR_IMAGE=gcr.io/${PROJECT}/${IMAGE_NAME} \
export BASE_CONTAINER_IMAGE=gcr.io/dataflow-templates-base/java8-template-launcher-base \
export BASE_CONTAINER_IMAGE_VERSION=latest \
export TEMPLATE_MODULE=mongodb-to-googlecloud \
export APP_ROOT=/template/${TEMPLATE_MODULE} \
export COMMAND_SPEC=***${APP_ROOT}/resources/mongodb-to-bigquery-command-spec.json*** \
export TEMPLATE_IMAGE_SPEC=${BUCKET_NAME}/images/mongodb-to-bigquery-image-spec.json \
export MONGODB_HOSTNAME="mongodb+srv://username:password@server-connection-string" \
export MONGODB_DATABASE_NAME=database name \
export MONGODB_COLLECTION_NAME=Collection name \
export OUTPUT_TABLE_SPEC=output tabel spec \
export USER_OPTION = user-option \
export INPUT_TOPIC=input-topic 
```
Note: 
    **Depending on collection whose data are to be processed by  dataflow job, MONGODB_COLLECTION_NAME, OUTPUT_TABLE_SPEC  needs to be set.**  

2.Build and push image to Google Container Repository 

```mvn clean package -Dcheckstyle.skip=true -Dmaven.test.skip=true -Dimage=${TARGET_GCR_IMAGE} -Dbase-container-image=${BASE_CONTAINER_IMAGE} -Dbase-container-image.version=${BASE_CONTAINER_IMAGE_VERSION} -Dapp-root=${APP_ROOT} -Dcommand-spec=${COMMAND_SPEC} -am -pl ${TEMPLATE_MODULE}``` 

3.Create spec file in Cloud Storage under the path ${TEMPLATE_IMAGE_SPEC} describing container image location and metadata. 
```
{ 
 
 "image": "gcr.io/***your project_id***/mongodb-to-bigquery", \
  "metadata": { \
    "name": "MongoDb To BigQuery", \
    "description": "A pipeline reads from MongoDB and writes to BigQuery.", \
    "parameters": [ \
      { \
        "name": "mongoDbUri", \
        "label": "MongoDB Connection URI", \
        "helpText": "URI to connect to MongoDb Atlas", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "database", \
        "label": "mongo database", \
        "helpText": "Database in MongoDB to store the collection. ex: my-db.", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "collection", \
        "label": "mongo collection", \
        "helpText": "Name of the collection inside MongoDB database. ex: my-collection.", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "outputTableSpec", \
        "label": "outputTableSpec", \
        "helpText": "BigQuery destination table spec. e.g bigquery-project:dataset.output_table", \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "userOption", \
        "label": "User option", \
        "helpText": " ", \
        "is_optional": true, \
        "paramType": "TEXT" \
      }
    ] \
  }, \
  "sdk_info": { \
    "language": "JAVA" \
  } \
} 
```
4.Template can be executed using the following gcloud command. 
```
export JOB_NAME="${TEMPLATE_MODULE}-`date +%Y%m%d-%H%M%S-%N`" 

gcloud beta dataflow flex-template run ${JOB_NAME} --project=${PROJECT} --region=${REGION} --template-file-gcs-location=${TEMPLATE_IMAGE_SPEC} 
--parameters mongoDbUri=${MONGODB_HOSTNAME},database=${MONGODB_DATABASE_NAME},collection=${MONGODB_COLLECTION_NAME},outputTableSpec=${OUTPUT_TABLE_SPEC},userOption=${USER_OPTION} 
```
5.**The job can process one collection .We need to run multiple job with collection name for multiple collections**.

## Stream Job -CDC Template(Linux/ Mac)

**Purpose**

Delta changes in mongo collections orders, users and products are pushed to pubsub via mongopubsub application. These streaming messages need to be consumed and updates needs to be made in respective big query tables. These CDC dataflow jobs will be responsible for listening to respective pubsub topics and updating respective big query tables. 

**Details:**

1.Set below variables in classpath. 

```
export PROJECT=project-id \
export IMAGE_NAME="mongodb-to-bigquery-cdc" \
export BUCKET_NAME=gs://bucket-name \
export TARGET_GCR_IMAGE=gcr.io/${PROJECT}/${IMAGE_NAME} \
export BASE_CONTAINER_IMAGE=gcr.io/dataflow-templates-base/java8-template-launcher-base \
export BASE_CONTAINER_IMAGE_VERSION=latest \
export TEMPLATE_MODULE=mongodb-to-googlecloud \
export APP_ROOT=/template/${TEMPLATE_MODULE} \
export COMMAND_SPEC=${APP_ROOT}/resources/mongodb-to-bigquery-cdc-command-spec.json \
export TEMPLATE_IMAGE_SPEC=${BUCKET_NAME}/images/mongodb-to-bigquery-image-spec.json \
export MONGODB_HOSTNAME="mongodb+srv://username:password@server-connection-string" \
export MONGODB_DATABASE_NAME=database name \
export MONGODB_COLLECTION_NAME=Collection name \
export OUTPUT_TABLE_SPEC=output tabel spec \
export USER_OPTION = user-option \
export INPUT_TOPIC=input-topic 
```

Note: Depending on collection whose changes are to be processed by CDC dataflow job, MONGODB_COLLECTION_NAME, OUTPUT_TABLE_SPEC and INPUT_TOPIC needs to be set.  

2.Build and push image to Google Container Repository 

mvn clean package -Dcheckstyle.skip=true -Dmaven.test.skip=true -Dimage=${TARGET_GCR_IMAGE} -Dbase-container-image=${BASE_CONTAINER_IMAGE} -Dbase-container-image.version=${BASE_CONTAINER_IMAGE_VERSION} -Dapp-root=${APP_ROOT} -Dcommand-spec=${COMMAND_SPEC} -am -pl ${TEMPLATE_MODULE} 

3.Create spec file in Cloud Storage under the path ${TEMPLATE_IMAGE_SPEC} describing container image location and metadata. 
```
{ 
  "image": "gcr.io/ecomm-analysis/mongodb-to-bigquery-cdc", \
  "metadata": { \
    "name": "MongoDb To BigQuery", \
    "description": "A pipeline reads from MongoDB and writes to BigQuery.", \
    "parameters": [ \
      { \
        "name": "mongoDbUri", \
        "label": "MongoDB Connection URI", \
        "helpText": "URI to connect to MongoDb Atlas", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "database", \
        "label": "mongo database", \
        "helpText": "Database in MongoDB to store the collection. ex: my-db.", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "collection", \
        "label": "mongo collection", \
        "helpText": "Name of the collection inside MongoDB database. ex: my-collection.", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "outputTableSpec", \
        "label": "outputTableSpec", \
        "helpText": "BigQuery destination table spec. e.g bigquery-project:dataset.output_table", \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "userOption", \
        "label": "User option", \
        "helpText": " ", \
        "is_optional": true, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "inputTopic", \
        "label": "input Pubsub Topic name", \
        "helpText": "Topic Name to read from e.g. projects/project-name/topics/topic-name", \
        "is_optional": false, \
        "paramType": "TEXT" \
      } \
    ] \
  }, \
  "sdk_info": { \
    "language": "JAVA" \
  } \
} 
```
4.Template can be executed using the following gcloud command. 

export JOB_NAME="${TEMPLATE_MODULE}-`date +%Y%m%d-%H%M%S-%N`" 
```
gcloud beta dataflow flex-template run ${JOB_NAME} --project=${PROJECT} --region=us-central1 --template-file-gcs-location=${TEMPLATE_IMAGE_SPEC} 
--parameters mongoDbUri=${MONGODB_HOSTNAME},database=${MONGODB_DATABASE_NAME},collection=${MONGODB_COLLECTION_NAME},outputTableSpec=${OUTPUT_TABLE_SPEC},inputTopic=${INPUT_TOPIC},userOption=${USER_OPTION} 
```

5.Depending on for which collection, CDC needs to be captured and processed, above steps needs to be repeated for each job pertaining to each collection. 

## Stream Job -CDC Template(Windows)

**Details:**

1.Set below variables in classpath using powershell. 

```
Set-Variable -Name "TEMPLATE_MODULE" -Value "mongodb-to-googlecloud"
Set-Variable -Name "PROJECT" -Value "ecomm-analysis"
```

Note: Depending on collection whose changes are to be processed by CDC dataflow job, MONGODB_COLLECTION_NAME, OUTPUT_TABLE_SPEC and INPUT_TOPIC needs to be set.  

2.Build and push image to Google Container Repository 

mvn clean package '-Dcheckstyle.skip=true' '-Dmaven.test.skip=true' '-Dimage=gcr.io/ecomm-analysis/mongodb-to-bigquery-cdc' '-Dbase-container-image=gcr.io/dataflow-templates-base/java8-template-launcher-base' '-Dbase-container-image.version=latest' '-Dapp-root=/template/mongodb-to-googlecloud' '-Dcommand-spec=/template/mongodb-to-googlecloud/resources/mongodb-to-bigquery-cdc-command-spec.json' -am -pl ${TEMPLATE_MODULE}


3.Create spec file in Cloud Storage under the path ${TEMPLATE_IMAGE_SPEC} describing container image location and metadata. 
```
{ 
  "image": "gcr.io/ecomm-analysis/mongodb-to-bigquery-cdc", \
  "metadata": { \
    "name": "MongoDb To BigQuery", \
    "description": "A pipeline reads from MongoDB and writes to BigQuery.", \
    "parameters": [ \
      { \
        "name": "mongoDbUri", \
        "label": "MongoDB Connection URI", \
        "helpText": "URI to connect to MongoDb Atlas", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "database", \
        "label": "mongo database", \
        "helpText": "Database in MongoDB to store the collection. ex: my-db.", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "collection", \
        "label": "mongo collection", \
        "helpText": "Name of the collection inside MongoDB database. ex: my-collection.", \
        "is_optional": false, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "outputTableSpec", \
        "label": "outputTableSpec", \
        "helpText": "BigQuery destination table spec. e.g bigquery-project:dataset.output_table", \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "userOption", \
        "label": "User option", \
        "helpText": " ", \
        "is_optional": true, \
        "paramType": "TEXT" \
      }, \
      { \
        "name": "inputTopic", \
        "label": "input Pubsub Topic name", \
        "helpText": "Topic Name to read from e.g. projects/project-name/topics/topic-name", \
        "is_optional": false, \
        "paramType": "TEXT" \
      } \
    ] \
  }, \
  "sdk_info": { \
    "language": "JAVA" \
  } \
} 
```
4.Template can be executed using the following gcloud command. 

export JOB_NAME="${TEMPLATE_MODULE}-`date +%Y%m%d-%H%M%S-%N`" 
```
gcloud beta dataflow flex-template run mongodb-to-googlecloud-order --project=${PROJECT} --region=us-central1 --template-file-gcs-location=gs://bucketname/images/mongodb-to-bigquery-cdc-image-spec-new.json --parameters mongoDbUri=mongodb+srv://username:password@hostname.net --parameters database=databasename --parameters collection=collection-name --parameters outputTableSpec=ecomm-analysis.datasetname.tablename  --parameters inputTopic=projects/ecomm-analysis/topics/topicname --parameters userOption=  
```

5.Depending on for which collection, CDC needs to be captured and processed, above steps needs to be repeated for each job pertaining to each collection. 


