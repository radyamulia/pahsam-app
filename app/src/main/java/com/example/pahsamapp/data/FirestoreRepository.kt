package com.example.pahsamapp.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    fun addListData(address: String, status: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        var listRequest = hashMapOf(
            "address" to address,
            "status" to status,
        )

        db.collection("report")
            .add(listRequest)
            .addOnSuccessListener { documentReference ->
                Log.d("fire", address)
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                onFailure.invoke(e)
            }
    }

}