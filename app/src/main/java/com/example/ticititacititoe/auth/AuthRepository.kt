package com.example.ticititacititoe.auth
import kotlin.Result
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
      auth.sendPasswordResetEmail(email)
          .addOnSuccessListener { onSuccess() }
          .addOnFailureListener {onFailure(it)}
          
    }
}