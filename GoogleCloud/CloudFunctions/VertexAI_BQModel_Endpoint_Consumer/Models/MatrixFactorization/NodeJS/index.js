/**
 * Responds to any HTTP request.
 *
 * @param {!express:Request} req HTTP request context.
 * @param {!express:Response} res HTTP response context.
 */
// exports.getRecommendations = (req, res) => {
//   let message = req.query.message || req.body.message || 'Hello World!';
//   res.status(200).send(message);
// };



/**
 * Responds to any HTTP request.
 *
 * @param {!express:Request} req HTTP request context.
 * @param {!express:Response} res HTTP response context.
 */
// exports.helloWorld = (req, res) => {
//   let message = req.query.message || req.body.message || 'Hello World!';
//   res.status(200).send(message);
// };




// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

'use strict';

// [START functions_concepts_stateless]
// Global variable, but only shared within function instance.
let count = 0;

/**
 * HTTP Cloud Function that counts how many times
 * it is executed within a specific instance.
 *
 * @param {Object} req Cloud Function request context.
 * @param {Object} res Cloud Function response context.
 */
exports.executionCount = (req, res) => {
  count++;

  // Note: the total function invocation count across
  // all instances may not be equal to this value!
  res.send(`Instance execution count: ${count}`);
};
// [END functions_concepts_stateless]

// [START functions_concepts_after_response]
/**
 * HTTP Cloud Function that may not completely
 * execute due to early HTTP response
 *
 * @param {Object} req Cloud Function request context.
 * @param {Object} res Cloud Function response context.
 */
exports.afterResponse = (req, res) => {
  res.end();

  // This statement may not execute
  console.log('Function complete!');
};
// [END functions_concepts_after_response]

// [START functions_concepts_after_timeout]
/**
 * HTTP Cloud Function that may not completely
 * execute due to function execution timeout
 *
 * @param {Object} req Cloud Function request context.
 * @param {Object} res Cloud Function response context.
 */
exports.afterTimeout = (req, res) => {
  setTimeout(() => {
    // May not execute if function's timeout is <2 minutes
    console.log('Function running...');
    res.end();
  }, 120000); // 2 minute delay
};
// [END functions_concepts_after_timeout]

// [START functions_concepts_filesystem]
const fs = require('fs');

/**
 * HTTP Cloud Function that lists files in the function directory
 *
 * @param {Object} req Cloud Function request context.
 * @param {Object} res Cloud Function response context.
 */
exports.listFiles = (req, res) => {
  fs.readdir(__dirname, (err, files) => {
    if (err) {
      console.error(err);
      res.sendStatus(500);
    } else {
      console.log('Files', files);
      res.sendStatus(200);
    }
  });
};
// [END functions_concepts_filesystem]

// [START functions_concepts_requests]
const fetch = require('node-fetch');

/**
 * HTTP Cloud Function that makes an HTTP request
 *
 * @param {Object} req Cloud Function request context.
 * @param {Object} res Cloud Function response context.
 */
exports.getRecommendations = async (req, res) => {
  //const url = 'https://www.google.com/'; // URL to send the request to
  //const externalRes = await fetch(url);
  //res.sendStatus(externalRes.ok ? 200 : 500);

  // [START aiplatform_predict_custom_trained_model_sample]
  /**
   * TODO(developer): Uncomment these variables before running the sample.\
   * (Not necessary if passing values as arguments)
   */

  // const filename = "YOUR_PREDICTION_FILE_NAME";
   const endpointId = "4790343463801454592";
   const project = 'ecomm-analysis';
   const location = 'us-central1';
   let userId;
   ({userId} = req.body);
 const {PredictionServiceClient} = require('@google-cloud/aiplatform');

  // Specifies the location of the api endpoint
  const clientOptions = {
    apiEndpoint: 'us-central1-aiplatform.googleapis.com',
  };

  // Instantiates a client
  const predictionServiceClient = new PredictionServiceClient(clientOptions);

  async function predictCustomTrainedModel() {
    // Configure the parent resource
    const endpoint = `projects/${project}/locations/${location}/endpoints/${endpointId}`;
    const parameters = {
      structValue: {
        fields: {},
      },
    };
    //const instanceDict = await readFileAsync(filename, 'utf8');
    //const instanceValue = JSON.parse(instanceDict);
    const instance = {
      structValue: {
        fields: {
          userId: {stringValue: userId},
       
        },
      },
    };

    const instances = [instance];
    const request = {
      endpoint,
      instances,
      parameters,
    };

    // Predict request
    const [response] = await predictionServiceClient.predict(request);

    console.log('Predict custom trained model response');
    console.log(`\tDeployed model id : ${response.deployedModelId}`);
    const predictions = response.predictions;
    console.log('\tPredictions :');
    for (const prediction of predictions) {
      console.log(`\t\tPrediction : ${JSON.stringify(prediction)}`);
    }

   // let message = req.query.message || req.body.message || 'Hello World!';
    res.status(200).send(predictions);

  }
  predictCustomTrainedModel();




};
// [END functions_concepts_requests]