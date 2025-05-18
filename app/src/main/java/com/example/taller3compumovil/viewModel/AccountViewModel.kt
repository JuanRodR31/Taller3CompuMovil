package com.example.taller3compumovil.viewModel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.taller3compumovil.data.SimpleLatLng
import com.example.taller3compumovil.data.User
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
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

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: StateFlow<List<User>> = _onlineUsers.asStateFlow()

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val newPosition = SimpleLatLng(
                    location.latitude,
                    location.longitude
                )

                // Actualizar en Firebase y estado local
                updateLocation(newPosition)
                _uiState.update {
                    it.copy(
                        userProfile = it.userProfile.copy(
                            mapPosition = it.userProfile.mapPosition + newPosition
                        )
                    )
                }
            }
        }
    }

    init {
        setupOnlineUsersListener()
    }

    private fun setupOnlineUsersListener() {
        database.getReference("users")
            .orderByChild("online").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = snapshot.children.mapNotNull {
                        it.getValue(User::class.java)
                    }
                    _onlineUsers.value = users
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error
                }
            })
    }

    fun updateOnlineStatus(online: Boolean) {
        userRef.child("online").setValue(online)
    }

    fun startLocationUpdates() {
        if (checkLocationPermission()) {
            // Lógica para iniciar actualizaciones
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocation(position: SimpleLatLng) {
        userRef.child("mapPosition").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentPositions = mutableData.getValue(object : GenericTypeIndicator<List<Map<String, Any>>>() {})
                    ?: emptyList()

                val newPosition = mapOf(
                    "latitude" to position.latitude,
                    "longitude" to position.longitude,
                    "timestamp" to ServerValue.TIMESTAMP
                )

                mutableData.value = currentPositions + newPosition
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Manejar completado
            }
        })
    }


}

