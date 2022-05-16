package com.example.livelinesalpha.ui.home.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livelinesalpha.ui.home.model.Coin
import com.example.livelinesalpha.ui.home.retrofit.CryptoApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel :ViewModel(){

    private val TAG = "HomeViewModel"
    var coinsData = MutableLiveData<Coin>()
    var coinsDataError = MutableLiveData<String>()

    fun getCryptoAsset() {
        val api = CryptoApi.retrofitService.getAssetInfo()

        api.enqueue(object: Callback<Coin> {
            override fun onResponse(call: Call<Coin>, response: Response<Coin>) {
                if(response.body() == null) {
                    coinsDataError.value = "Something went wrong! \nPlease try again"
                } else {
                    coinsData.value = response.body()
                }
            }

            override fun onFailure(call: Call<Coin>, t: Throwable) {
                coinsDataError.value = "Unable to fetch data! \nPlease try again"
                Log.d(TAG, "Api Failure :" + t.message)
            }
        })
    }

}