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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.codelabs.engine.FhirApplication
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.util.UUID


class MainActivity : AppCompatActivity() {

  var questionnaireJsonString: String? = null
  private val questionnaireResource: Questionnaire
    get() =
      FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaireJsonString)
              as Questionnaire

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
    val isPatientSaved = MutableLiveData<Boolean>()

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
      //  upload qusetonnaire to FHIR engine

     ////// new code
      var fhirEngine: FhirEngine = FhirEngineProvider.getInstance(this@MainActivity)
      if (QuestionnaireResponseValidator.validateQuestionnaireResponse(
          questionnaireResource,
          questionnaireResponse,
          application
        )
          .values
          .flatten()
          .any { it is Invalid }
      ) {
        isPatientSaved.value = false
        Toast.makeText(this@MainActivity, "failed to submit", Toast.LENGTH_SHORT).show()
        return@launch
      }
      val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse)

//      if (entry.type !is QuestionnaireResponse) {
//        val resourse_entry = entry.resource
//        Toast.makeText(this@MainActivity, "resource type = $resourse_entry", Toast.LENGTH_SHORT).show()
//        return@launch
//      }
      //val questionnaireresponse  = entry.resource as QuestionnaireResponse
      //questionnaireresponse.id = generateUuid()
      fhirEngine.create(entry)
      isPatientSaved.value = true
      Toast.makeText(this@MainActivity, "data submitted suuccessfully", Toast.LENGTH_SHORT).show()


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
  private fun generateUuid(): String {
    return UUID.randomUUID().toString()
}}

