package com.sillylife.knocknock.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AsyncTaskAlternative {

    fun <P, R> CoroutineScope.executeAsyncTask(onPreExecute: () -> Unit,
                                               doInBackground: suspend (suspend (P) -> Unit) -> R,
                                               onPostExecute: (R) -> Unit,
                                               onProgressUpdate: (P) -> Unit) = launch {
        onPreExecute()

        val result = withContext(Dispatchers.IO) {
            doInBackground {
                withContext(Dispatchers.Main) { onProgressUpdate(it) }
            }
        }
        onPostExecute(result)
    }
}

//CoroutineScope(Dispatchers.IO).executeAsyncTask(onPreExecute = {
//    // ... runs in Main Thread
//}, doInBackground = { publishProgress: suspend (progress: Int) -> Unit ->
//
//    // ... runs in Background Thread
//
//    // simulate progress update
//    publishProgress(50) // call `publishProgress` to update progress, `onProgressUpdate` will be called
//    delay(1000)
//    publishProgress(100)
//
//
//    "Result" // send data to "onPostExecute"
//}, onPostExecute = { it ->
//    // runs in Main Thread
//    // ... here "it" is a data returned from "doInBackground"
//}, onProgressUpdate = {
//    // runs in Main Thread
//    // ... here "it" contains progress
//})