package com.example.ticititacititoe.auth

import android.content.Context
import android.widget.Toast
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.ticititacititoe.profile.User
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlin.Result
import com.google.firebase.Firebase

import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions


//import kotlin.Int

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(
        username: String,
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit
    ){
        // =========== Check if username already exists ============
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    onResult(Result.failure(Exception("Username already taken")))
                    return@addOnSuccessListener
                }

                // =========== Create user in Firebase Auth ============
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        if (!task.isSuccessful) {
                            onResult(
                                Result.failure(
                                    task.exception ?: Exception("Registration was failed")
                                )
                            )
                            return@addOnCompleteListener
                        }

                        val uid = task.result.user?.uid
                        if (uid == null) {
                            onResult(Result.failure(Exception("Incorrect")))
                            return@addOnCompleteListener
                        }

                        // =========== Create user object for Firestore ============
                        val user = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "email" to email,
                            "rating" to 1300,
                            "totalGames" to 0,
                            "lostGames" to 0,
                            "wonGames" to 0,
                            "totalMovesMade" to 0,
                            "currentStreak" to 0,
                            "maxStreak" to 0
                        )

                        // =========== Save user in Firestore ============
                        Firebase.firestore
                            .collection("users")
                            .document(uid)
                            .set(user)
                            .addOnCompleteListener { setTask ->
                                if (setTask.isSuccessful) {
                                    onResult(Result.success(Unit))
                                } else {
                                    onResult(
                                        Result.failure(
                                            setTask.exception
                                                ?: Exception("Failed to save user")
                                        )
                                    )
                                }
                            }
                    }
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess()}
            .addOnFailureListener { onFailure(it)}
    }

    fun logout(){
        auth.signOut()
    }

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        val result : AuthResult = auth.signInWithCredential(credential)
            .await()
        return result
    }

    suspend fun handleSignIn(result: GetCredentialResponse): Result<FirebaseUser> {
        return try {
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val authResult = loginWithGoogle(googleIdTokenCredential.idToken)

                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    saveGoogleUserToFirestore(firebaseUser)
                    Result.success(firebaseUser)

                } else {
                    Result.failure(Exception("No user found"))
                }
            } else {
                Result.failure(Exception("Wrong type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun handleFailure(exception: androidx.credentials.exceptions.GetCredentialException, context: Context) {
        when (exception) {
            is GetCredentialCancellationException -> {
                // Update error
            }
            is NoCredentialException -> {
//                AlertDialog.Builder(context)
//                    .setTitle("Add Google Account")
//                    .setMessage("Please add a Google Account in Settings")
//                    .setPositiveButton("Yes go to settings") { dialog, _ ->
//                        val intent = Intent(Settings.ACTION_SETTINGS)
//                        startActivity(context, intent)
//                        dialog.dismiss()
//                    }
//                    .setNegativeButton(getString(R.string.cancel_alert_btn_text)) { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    .show()
            }
            else -> {
                //
                Toast.makeText( context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()

            }
        }
    }

    suspend fun saveGoogleUserToFirestore(firebaseUser: FirebaseUser): Result<Unit> {
        return try {
            val userMap = User(
                id = firebaseUser.uid,
                username = firebaseUser.displayName,
                rating = 1300,
            )

            Firebase.firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userMap, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
      auth.sendPasswordResetEmail(email)
          .addOnSuccessListener { onSuccess() }
          .addOnFailureListener {onFailure(it)}

    }
}
