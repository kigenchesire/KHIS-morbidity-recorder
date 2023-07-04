/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codelab.sdclibrary

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse


class MainActivity : AppCompatActivity() {

  var questionnaireJsonString: String? = null

  override fun onCreate(savedInstanceState: android.os.Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // 4.2 Replace with code from the codelab to add a questionnaire fragment.
    // Step 2: Configure a QuestionnaireFragment
    questionnaireJsonString = getStringFromAssets("records-final.R4.json")

    if (savedInstanceState == null) {
      supportFragmentManager.commit {
        setReorderingAllowed(true)
        add(
          R.id.fragment_container_view,
          QuestionnaireFragment.builder().setQuestionnaire(questionnaireJsonString!!).build()
        )
      }
    }
  }

  private fun submitQuestionnaire() {

    // 5 Replace with code from the codelab to get a questionnaire response.
    // Get a questionnaire response
    val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            as QuestionnaireFragment
    val questionnaireResponse = fragment.getQuestionnaireResponse()

// Print the response to the log
    val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val questionnaireResponseString =
      jsonParser.encodeResourceToString(questionnaireResponse)
    Log.d("response", questionnaireResponseString)

    // 6 Replace with code from the codelab to extract FHIR resources from QuestionnaireResponse.
    lifecycleScope.launch {
      val questionnaire =
        jsonParser.parseResource(questionnaireJsonString) as Questionnaire
      val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse)
      Log.d("extraction result", jsonParser.encodeResourceToString(bundle))
      val fhirServerUrl = "http://hapi.fhir.org/baseR4/" // Replace with your FHIR server URL
      FHIRUploader.uploadQuestionnaireResponse(fhirServerUrl, questionnaireResponseString)
    }

  }

  object FHIRUploader {
    fun uploadQuestionnaireResponse(fhirServerUrl: String, questionnaireResponseString: String) {
      // Create a FHIR context
      val ctx = FhirContext.forR4()

      // Create a FHIR client
      val client = ctx.newRestfulGenericClient(fhirServerUrl)

      // Parse the QuestionnaireResponse from the JSON string
      val questionnaireResponse = ctx.newJsonParser()
        .parseResource(QuestionnaireResponse::class.java, questionnaireResponseString)

      // Create a Bundle to contain the QuestionnaireResponse
      val bundle = org.hl7.fhir.r4.model.Bundle()
      bundle.type = org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION
      bundle.addEntry().setResource(questionnaireResponse).request.method = org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT

      // Upload the Bundle to the FHIR server
      val responseBundle = client.transaction().withBundle(bundle).execute()

      // Process the server response
      for (entry in responseBundle.entry) {
        if (entry.response.status.startsWith("20")) {
          val resourceId = entry.response.location
          println("Successfully uploaded QuestionnaireResponse with ID: ${resourceId}")
        } else {
          println("Failed to upload QuestionnaireResponse. Response status: ${entry.response.status}")
        }
      }
    }


  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.submit_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.submit) {
      submitQuestionnaire()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun getStringFromAssets(fileName: String): String {
    return assets.open(fileName).bufferedReader().use { it.readText() }
  }
}
