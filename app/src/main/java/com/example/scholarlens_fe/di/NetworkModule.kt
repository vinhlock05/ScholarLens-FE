package com.example.scholarlens_fe.di

import com.example.scholarlens_fe.data.api.AuthApiService
import com.example.scholarlens_fe.data.interceptor.AuthInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.example.scholarlens_fe.data.api.ClovaOCRService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import android.content.Context

/**
 * Network module for providing API services
 * Based on AUTHENTICATION_FLOW.md
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Base URL for backend API
     * TODO: Replace with actual backend URL or use BuildConfig
     *
     * To set your backend URL:
     * 1. Replace "YOUR_IP" with your actual backend server IP address
     * 2. Or use BuildConfig to set it based on build variant
     * 3. Example: "http://192.168.1.100:8000/" for local development
     */
    private const val BASE_URL = "http://223.130.163.63:8000/" // Development
    // private const val BASE_URL = "https://your-production-domain.com/" // Production

    /**
     * Provides HTTP logging interceptor
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides OkHttpClient with AuthInterceptor
     * AuthInterceptor adds Authorization header to all requests
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add auth interceptor first
            .addInterceptor(loggingInterceptor) // Then logging interceptor
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Gson instance
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Provides Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides AuthApiService
     */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("clova_retrofit")
    fun provideClovaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://8qs1a8pxk0.apigw.ntruss.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideClovaOCRService(@Named("clova_retrofit") retrofit: Retrofit): ClovaOCRService {
        return retrofit.create(ClovaOCRService::class.java)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}

