package com.example.taller3compumovil.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.taller3compumovil.data.User
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AccountUiState(
    var user: FirebaseUser? = null,
    var userProfile: User = User(),
)

interface AccountService {
    fun authenticate(email: String, password: String, onResult: (Throwable?) -> Unit)
    fun register(
        email: String,
        password: String,
        fullName: User,
        profilePic: Uri,
        onResult: (Throwable?) -> Unit
    )
    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Throwable?) -> Unit
    )

    fun forgotPassword(email: String, onResult: (Throwable?) -> Unit)
    fun signOut(onResult: () -> Unit)
}

interface AccountDatabaseService {
    fun getUser(onResult: (User) -> Unit)
    fun updateUser(newInfo: User, onResult: (Throwable?) -> Unit)
}

interface StorageService {
    fun uploadImage(
        imageUri: Uri,
        onResult: (String?) -> Unit
    )
}

class FirebaseViewModel(application: Application) : AndroidViewModel(application), AccountService,
    AccountDatabaseService {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var auth = Firebase.auth
    private var database = Firebase.database
    private var storage = Firebase.storage
    val userRef = database.getReference("users/${auth.currentUser?.uid}")

    override fun authenticate(
        email: String,
        password: String,
        onResult: (Throwable?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { status ->
            if (status.isSuccessful) {
                _uiState.update { currentState ->
                    currentState.copy(user = auth.currentUser)
                }
                onResult(null)
            } else {
                onResult(status.exception)
            }
        }
    }
    override fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Throwable?) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onResult(Exception("Usuario no autenticado"))
            return
        }

        // Reautenticación requerida por Firebase
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onResult(null)
                            } else {
                                onResult(updateTask.exception)
                            }
                        }
                } else {
                    onResult(reauthTask.exception)
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        currentUser: User,
        profilePic: Uri,
        onResult: (Throwable?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userRef = database.getReference("users/${auth.currentUser?.uid}")

                userRef.setValue(currentUser).addOnCompleteListener { dbTask ->
                    if (dbTask.isSuccessful) {
                        /*
                        val storageRef =
                            storage.getReference("users/${auth.currentUser?.uid}/profile.jpg")

                        storageRef.putFile(profilePic).addOnCompleteListener { uploadTask ->
                            if (uploadTask.isSuccessful) {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        user = auth.currentUser,
                                        userProfile = currentUser
                                    )
                                }
                                onResult(null)
                            } else {
                                onResult(uploadTask.exception)
                            }
                        }

                         */
                        _uiState.update { currentState ->
                            currentState.copy(
                                user = auth.currentUser,
                                userProfile = currentUser
                            )
                        }

                        onResult(null)
                    } else {
                        onResult(dbTask.exception)
                    }
                }
            } else {
                onResult(task.exception)
            }
        }
    }

    override fun forgotPassword(
        email: String,
        onResult: (Throwable?) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(null)
            } else {
                onResult(task.exception)
            }
        }
    }

    override fun signOut(onResult: () -> Unit) {
        auth.signOut()
        _uiState.update { currentState ->
            currentState.copy(user = null)
        }
        //QUITAR UBICACION
        onResult()
    }

    override fun getUser(onResult: (User) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(User())
            return
        }
        val userRef = database.getReference("users/${currentUser.uid}")

        _uiState.update { currentState ->
            currentState.copy(user = currentUser)
        }

        userRef.get().addOnSuccessListener { snapshot ->
            snapshot.getValue(User::class.java)?.let { tmpUser ->
                _uiState.update { currentState ->
                    currentState.copy(userProfile = tmpUser)
                }
                onResult(tmpUser)
            } ?: onResult(User())
        }.addOnFailureListener {
            onResult(User())
        }
    }


    override fun updateUser(
        newInfo: User,
        onResult: (Throwable?) -> Unit
    ) {
        userRef.setValue(newInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(null)
            } else {
                onResult(task.exception)
            }
        }
    }
}