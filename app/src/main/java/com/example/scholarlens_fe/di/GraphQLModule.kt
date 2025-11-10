package com.example.scholarlens_fe.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * GraphQL Module for providing Apollo Client
 * This module sets up Apollo GraphQL client for the application
 */
@Module
@InstallIn(SingletonComponent::class)
object GraphQLModule {

    /**
     * GraphQL endpoint URL
     * TODO: Update with your actual GraphQL endpoint
     * 
     * For development:
     * - Local emulator: "http://10.0.2.2:8000/graphql"
     * - Local device: "http://YOUR_LOCAL_IP:8000/graphql"
     * 
     * For production:
     * - "https://your-production-domain.com/graphql"
     */
    private const val GRAPHQL_ENDPOINT = "http://10.0.2.2:8000/graphql"
    // private const val GRAPHQL_ENDPOINT = "https://your-production-domain.com/graphql"

    /**
     * Provides HTTP logging interceptor for GraphQL requests
     */
    @Provides
    @Singleton
    @Named("GraphQLLoggingInterceptor")
    fun provideGraphQLLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides OkHttpClient for Apollo GraphQL
     * You can add custom interceptors here (e.g., authentication interceptor)
     */
    @Provides
    @Singleton
    @Named("GraphQLOkHttpClient")
    fun provideGraphQLOkHttpClient(
        @Named("GraphQLLoggingInterceptor") loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // TODO: Add authentication interceptor here when needed
            // .addInterceptor(authInterceptor)
            .build()
    }

    /**
     * Provides Apollo Client instance
     * This is the main GraphQL client used throughout the app
     */
    @Provides
    @Singleton
    fun provideApolloClient(
        @Named("GraphQLOkHttpClient") okHttpClient: OkHttpClient
    ): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(GRAPHQL_ENDPOINT)
            .okHttpClient(okHttpClient)
            // Enable automatic persisted queries (optional)
            // .enableAutoPersistedQueries()
            .build()
    }
}

