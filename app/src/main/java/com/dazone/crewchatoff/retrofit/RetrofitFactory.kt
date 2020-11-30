package com.dazone.crewchatoff.retrofit

import android.provider.SyncStateContract
import com.dazone.crewchatoff.BuildConfig
import com.dazone.crewchatoff.constant.Constants
import com.dazone.crewchatoff.utils.CrewChatApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitFactory {
    /**Add Header *{AccessToken}*/
    private val authInterceptor = Interceptor { chain ->
        val newUrl = chain
                .request().url
                .newBuilder()
                .build()

        val newRequest = chain.request()
                .newBuilder()
                .url(newUrl)
                .build()

        chain.proceed(newRequest)
    }


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    //Not loggin the authkey if not Debug
    private val client =
            if (BuildConfig.DEBUG) {
                OkHttpClient().newBuilder()
                        .addInterceptor(authInterceptor)
                        .addInterceptor(loggingInterceptor)
                        .build()
            } else {
                OkHttpClient().newBuilder()
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor(authInterceptor)
                        .build()
            }

    fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private fun getDomain(): String {
        val domain = CrewChatApplication.getInstance().prefs.serverSite
                ?: ""
        return if (domain.startsWith("http")) domain else "http://$domain"
    }

    val apiService: DazoneService = retrofit(getDomain())
            .create(DazoneService::class.java)

    val apiServiceNonBaseUrl: DazoneService = retrofit("http://mobileupdate.crewcloud.net")
            .create(DazoneService::class.java)
}

