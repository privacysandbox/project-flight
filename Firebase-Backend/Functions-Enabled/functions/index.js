/**
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

const admin = require("firebase-admin");

// TODO: Replace the following with your app's Firebase project configuration
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  // ...
  // The value of `databaseURL` depends on the location of the database
  databaseURL: "https://privacy-sandbox-flight-test.firebaseio.com",
};



exports.receiveReport = onRequest((req,res) => {
	const data = req.body;
	const url = req.url;
	logger.info(`Report: ${JSON.stringify(url)} ${JSON.stringify(data)}`, {structuredData: true});
	res.sendStatus(204);
});
exports.reportWin = onRequest((req,res) => {
	const data = req.body;
	const url = req.url;
	logger.info(`PA Report Win: ${JSON.stringify(url)} ${JSON.stringify(data)}`, {structuredData: true});
	res.sendStatus(204);
});
exports.attributionSource = onRequest((req,res) => {
	const data = req.body;
	const url = req.url;
	const result = {
		ca: req.query.attribution_id,
		source_event_id: "123001",
		destination: "android-app://com.example.advertiserapp",
		expiry: "172800",
		aggregation_keys: {campaignCounts: "0x159", geoValue: "0x5"}
	};
	logger.info(
		`Attribution Source: ${JSON.stringify(url)} ${JSON.stringify(data)} ${JSON.stringify(result)}`,
		{structuredData: true}
	);
    res.setHeader("Content-Type", "application/json")
    res.setHeader("Attribution-Reporting-Register-Source", JSON.stringify(result));
	res.sendStatus(204);
});
exports.databaseWrite = onRequest((req,res) => {
  	logger.info("Database Write Begin");
	// Initialize Firebase
	const app = admin.initializeApp(firebaseConfig);
	// Write the data to the database
	admin.database().ref('test/').set(testData="TestData");
  	logger.info("Database Write End");
});

exports.databaseRead = onRequest((req,res) => {
  	logger.info("Database Read Begin");
	// Initialize Firebase
	const app = admin.initializeApp(firebaseConfig);
	// Write the data to the database
	const dbRef = admin.database().ref();
	dbRef.child('test/').get().then((snapshot) =>{
		if (snapshot.exists()) {
			logger.info(snapshot.val());
	  	} else {
	    console.log("No data available");
	  }
	}).catch((error) => {
	  console.error(error);
	})
  logger.info("Database Read End");
  
  //send debug response
  res.status(200).send("debugValue : 99999");
});

