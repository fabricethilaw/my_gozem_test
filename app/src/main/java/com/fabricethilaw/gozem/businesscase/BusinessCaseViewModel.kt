package com.fabricethilaw.gozem.businesscase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabricethilaw.gozem.network.Api
import com.fabricethilaw.gozem.network.restapi.model.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BusinessCaseViewModel : ViewModel() {
    private val _profileContent = MutableLiveData<ProfileContent>()
    val profileContent: LiveData<ProfileContent> = _profileContent
    private val _mapContent = MutableLiveData<LocationContent>()
    val mapContent: LiveData<LocationContent> = _mapContent
    private val _informationContent = MutableLiveData<InformationContent>()
    val informationContent: LiveData<InformationContent> = _informationContent

    fun signIn(
        email: String,
        password: String,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        // Classic way to make api requests with retrofit callbacks
        Api.webservice.authenticate(LoginPayload(email, password))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        onSuccess.invoke(response.body()!!.message)
                    } else {
                        onError.invoke(response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    onError.invoke(t.message.toString())
                }
            })

    }

    fun register(
        name: String,
        email: String,
        password: String,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        // Using retrofit with coroutines, straightforward (no boilerplate code).
        viewModelScope.launch {
            try {
                val response: Response<AuthResponse> =
                    Api.webservice.register(RegistrationPayload(name, email, password))
                if (response.isSuccessful) {
                    onSuccess.invoke(response.body()!!.message)
                } else {
                    onError(response.errorBody().toString())
                }

            } catch (e: Exception) {
                onError.invoke(e.message.toString())
            }
        }
    }


    fun getProfile(onError: (String) -> Unit, onSuccess: (InformationContent?) -> Unit) {
        viewModelScope.launch {
             try {
                 val resp = Api.webservice.getData()
                 if (resp.isSuccessful) {
                     onSuccess(setHomeData(resp.body()!!))
                     Log.i("resp", resp.body()!!.toString())
                 } else {
                     Log.i("error", "request not 200")
                     onError(resp.errorBody().toString())
                 }
             } catch (e: Exception) {
                 onError(e.message.toString())
             }
        }
    }

    private fun setHomeData(data: List<ProfileItemResponse>): InformationContent? {
        data.firstOrNull { it.type == "profile" }.run {
            _profileContent.value = this?.content?.mapToProfileContent()
            Log.i("profile", _profileContent.value.toString())
        }
        data.firstOrNull { it.type == "map" }.run {
            _mapContent.value = this?.content?.mapToLocationContent()
            Log.i("map", _mapContent.value.toString())
        }
        return data.firstOrNull { it.type == "data" }?.content?.mapToInformationContent()
    }
}
