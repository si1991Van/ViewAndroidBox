package com.mindorks.example.ubercaranimation.sevices

import com.google.gson.GsonBuilder
import com.mindorks.example.ubercaranimation.model.BaseResponse
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiService {
    const val BASE_URL = "http://165.232.34.235:8814/"

    val service: AppRepository by lazy {
        val gson = GsonBuilder().setLenient().create()
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(makeOkHttpClient(true)).build().create<AppRepository>(AppRepository::class.java)
    }

    private fun makeOkHttpClient(isDebug: Boolean): OkHttpClient? {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                        chain: Array<java.security.cert.X509Certificate>, authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                        chain: Array<java.security.cert.X509Certificate>, authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory


            val logging = HttpLoggingInterceptor()
            logging.level =
                    if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            return OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor { chain: Interceptor.Chain ->
                        val request =
                                chain.request().newBuilder().addHeader("Content-Type", "application/json")
                                        .build()
                        chain.proceed(request)
                    }.addInterceptor(logging)
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    interface AppRepository {
        @GET("carPosInfo")
        fun getLocation(): Single<BaseResponse>






    }
}