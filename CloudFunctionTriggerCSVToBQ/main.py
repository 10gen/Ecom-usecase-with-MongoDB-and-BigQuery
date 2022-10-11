def startDataflowProcess(event, context):
    """Triggered by a change to a Cloud Storage bucket.

    Args:



         event (dict): Event payload.



         context (google.cloud.functions.Context): Metadata for the event.



    """

    from googleapiclient.discovery import build
    from datetime import datetime
    
    project = "ecomm-analysis"
    
    now = datetime.now()
    dt_string = now.isoformat()

    job = "csvtobigquery"+dt_string

    template = "gs://dataflow-templates-us-central1/latest/GCS_Text_to_BigQuery"

    inputFile = "gs://clickstreamdatamongobq/eventsUnwinded.csv"

    javascriptTextTransformGcsPath = "gs://clickstreamcsv/udfjs/csvtransform.js"

    jsonPath = "gs://clickstreamcsv/bqschema/eventsBatchLoadSchema.json"

    javascriptTextTransformFunctionName = "transform"

    outputTable = "ecomm-analysis:ecommerce_prod.events"

    bigQueryLoadingTemporaryDirectory = "gs://clickstreamcsv/temp/"

    # user defined parameters to pass to the dataflow pipeline job
    print("Before setting parameters")
    parameters = {



        'inputFilePattern': inputFile,



        'javascriptTextTransformGcsPath': javascriptTextTransformGcsPath,



        'JSONPath': jsonPath,



        'javascriptTextTransformFunctionName': javascriptTextTransformFunctionName,



        'outputTable': outputTable,



        'bigQueryLoadingTemporaryDirectory': bigQueryLoadingTemporaryDirectory



    }

    # tempLocation is the path on GCS to store temp files generated during the dataflow job

    environment = {'tempLocation': 'gs://clickstreamcsv/tempLoc/'}

    service = build('dataflow', 'v1b3', cache_discovery=False)

    # below API is used when we want to pass the location of the dataflow job

    request = service.projects().locations().templates().launch(



        projectId=project,



        gcsPath=template,



        location='us-central1',



        body={



            'jobName': job,



            'parameters': parameters,



            'environment': environment



        },



    )

    response = request.execute()

    print(str(response))
