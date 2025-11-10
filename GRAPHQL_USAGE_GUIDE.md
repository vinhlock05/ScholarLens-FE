# 📚 GraphQL Usage Guide - Apollo Client for Android

Hướng dẫn chi tiết về cách sử dụng Apollo GraphQL trong ứng dụng ScholarLens Android.

## 📋 Mục lục

1. [Tổng quan](#tổng-quan)
2. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
3. [Download GraphQL Schema](#download-graphql-schema)
4. [Tạo GraphQL Queries](#tạo-graphql-queries)
5. [Tạo GraphQL Mutations](#tạo-graphql-mutations)
6. [Sử dụng trong Repository](#sử-dụng-trong-repository)
7. [Sử dụng trong Use Cases](#sử-dụng-trong-use-cases)
8. [Sử dụng trong ViewModel](#sử-dụng-trong-viewmodel)
9. [Xử lý Errors](#xử-lý-errors)
10. [Best Practices](#best-practices)

---

## 🎯 Tổng quan

Project đã được setup với Apollo GraphQL Client. Apollo sẽ tự động generate Kotlin models từ GraphQL schema và queries/mutations của bạn.

### Đã được setup:

- ✅ Apollo GraphQL plugin trong `build.gradle.kts`
- ✅ Apollo Client được inject qua Hilt (`GraphQLModule.kt`)
- ✅ GraphQL schema file location (`app/src/main/graphql/`)
- ✅ Apollo dependencies (runtime + coroutines support)

### Cần làm:

- ⏳ Download GraphQL schema từ backend
- ⏳ Tạo GraphQL queries/mutations
- ⏳ Implement Repository sử dụng Apollo Client
- ⏳ Implement Use Cases
- ⏳ Sử dụng trong ViewModel

---

## 📁 Cấu trúc thư mục

```
app/src/main/
├── graphql/
│   └── com/example/scholarlens_fe/
│       ├── schema.graphqls          # GraphQL schema (download từ backend)
│       ├── queries/                 # GraphQL queries
│       │   ├── GetScholarships.graphql
│       │   └── SearchScholarships.graphql
│       └── mutations/               # GraphQL mutations
│           ├── CreateScholarship.graphql
│           └── UpdateScholarship.graphql
└── java/com/example/scholarlens_fe/
    ├── di/
    │   └── GraphQLModule.kt        # Apollo Client setup
    └── data/
        └── repository/
            └── ScholarshipGraphQLRepository.kt  # Repository sử dụng Apollo
```

---

## 📥 Download GraphQL Schema

### Cách 1: Sử dụng Apollo CLI (Recommended)

1. **Cài đặt Apollo CLI** (nếu chưa có):
   ```bash
   npm install -g @apollo/rover
   ```

2. **Download schema từ backend**:
   ```bash
   # Từ project root
   cd app/src/main/graphql/com/example/scholarlens_fe/
   
   # Download schema (thay đổi endpoint nếu cần)
   rover graph introspect http://10.0.2.2:8000/graphql --output schema.graphqls
   
   # Hoặc nếu backend có introspection endpoint khác
   rover graph introspect http://10.0.2.2:8000/graphql --header "Authorization: Bearer YOUR_TOKEN" --output schema.graphqls
   ```

### Cách 2: Sử dụng curl

```bash
# Từ project root
curl -X POST http://10.0.2.2:8000/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ __schema { types { name } } }"}' \
  > app/src/main/graphql/com/example/scholarlens_fe/schema.graphqls
```

### Cách 3: Từ GraphQL Playground/GraphiQL

1. Mở GraphQL Playground tại `http://10.0.2.2:8000/graphql`
2. Click vào tab **Schema**
3. Copy toàn bộ schema
4. Paste vào file `app/src/main/graphql/com/example/scholarlens_fe/schema.graphqls`

### Sau khi download schema:

1. **Sync Gradle** để Apollo generate code:
   ```
   File > Sync Project with Gradle Files
   ```

2. **Build project** để generate Kotlin models:
   ```
   Build > Make Project
   ```

---

## 📝 Tạo GraphQL Queries

### Bước 1: Tạo file query

Tạo file trong thư mục `app/src/main/graphql/com/example/scholarlens_fe/queries/`

**Ví dụ: `GetScholarships.graphql`**

```graphql
query GetScholarships($limit: Int, $offset: Int) {
  scholarships(limit: $limit, offset: $offset) {
    id
    name
    university
    country
    fundingType
    description
    deadline
    amount
    eligibleFields
  }
}
```

**Ví dụ: `SearchScholarships.graphql`**

```graphql
query SearchScholarships($keyword: String!, $country: String, $fundingType: String) {
  searchScholarships(keyword: $keyword, country: $country, fundingType: $fundingType) {
    id
    name
    university
    country
    fundingType
    description
    deadline
    amount
    eligibleFields
    score
  }
}
```

### Bước 2: Build project

Sau khi tạo query file, build project để Apollo generate Kotlin code:

```
Build > Make Project
```

Apollo sẽ generate:
- `GetScholarshipsQuery` class
- `SearchScholarshipsQuery` class
- Data models tương ứng

### Bước 3: Sử dụng trong code

Xem phần [Sử dụng trong Repository](#sử-dụng-trong-repository)

---

## ✏️ Tạo GraphQL Mutations

### Bước 1: Tạo file mutation

Tạo file trong thư mục `app/src/main/graphql/com/example/scholarlens_fe/mutations/`

**Ví dụ: `CreateScholarship.graphql`**

```graphql
mutation CreateScholarship($input: ScholarshipInput!) {
  createScholarship(input: $input) {
    id
    name
    university
    country
    fundingType
    description
    deadline
    amount
    eligibleFields
  }
}
```

**Ví dụ: `UpdateScholarship.graphql`**

```graphql
mutation UpdateScholarship($id: ID!, $input: ScholarshipInput!) {
  updateScholarship(id: $id, input: $input) {
    id
    name
    university
    country
    fundingType
    description
    deadline
    amount
    eligibleFields
  }
}
```

### Bước 2: Build project

Tương tự như queries, build project để generate code.

---

## 🔧 Sử dụng trong Repository

### Ví dụ: ScholarshipGraphQLRepository

```kotlin
package com.example.scholarlens_fe.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.scholarlens_fe.domain.model.Scholarship
import com.example.scholarlens_fe.graphql.GetScholarshipsQuery
import com.example.scholarlens_fe.graphql.SearchScholarshipsQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScholarshipGraphQLRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    /**
     * Get scholarships using GraphQL query
     */
    suspend fun getScholarships(
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Scholarship>> {
        return try {
            val response: ApolloResponse<GetScholarshipsQuery.Data> = apolloClient
                .query(GetScholarshipsQuery(limit = limit, offset = offset))
                .execute()

            if (response.data != null && response.errors.isNullOrEmpty()) {
                val scholarships = response.data!!.scholarships.map { scholarship ->
                    // Map GraphQL model to domain model
                    Scholarship(
                        id = scholarship.id,
                        scholarshipName = scholarship.name,
                        country = scholarship.country,
                        fundingLevel = scholarship.fundingType,
                        description = scholarship.description,
                        deadline = scholarship.deadline,
                        amount = scholarship.amount,
                        eligibleFields = scholarship.eligibleFields
                    )
                }
                Result.success(scholarships)
            } else {
                val errorMessage = response.errors?.firstOrNull()?.message
                    ?: "Unknown error occurred"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search scholarships using GraphQL query
     */
    suspend fun searchScholarships(
        keyword: String,
        country: String? = null,
        fundingType: String? = null
    ): Result<List<Scholarship>> {
        return try {
            val response: ApolloResponse<SearchScholarshipsQuery.Data> = apolloClient
                .query(
                    SearchScholarshipsQuery(
                        keyword = keyword,
                        country = country,
                        fundingType = fundingType
                    )
                )
                .execute()

            if (response.data != null && response.errors.isNullOrEmpty()) {
                val scholarships = response.data!!.searchScholarships.map { scholarship ->
                    Scholarship(
                        id = scholarship.id,
                        scholarshipName = scholarship.name,
                        country = scholarship.country,
                        fundingLevel = scholarship.fundingType,
                        description = scholarship.description,
                        deadline = scholarship.deadline,
                        amount = scholarship.amount,
                        eligibleFields = scholarship.eligibleFields,
                        score = scholarship.score
                    )
                }
                Result.success(scholarships)
            } else {
                val errorMessage = response.errors?.firstOrNull()?.message
                    ?: "Unknown error occurred"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 🎯 Sử dụng trong Use Cases

### Ví dụ: SearchScholarshipsUseCase (GraphQL version)

```kotlin
package com.example.scholarlens_fe.domain.usecase

import com.example.scholarlens_fe.data.repository.ScholarshipGraphQLRepository
import com.example.scholarlens_fe.domain.model.Scholarship
import javax.inject.Inject

class SearchScholarshipsUseCase @Inject constructor(
    private val repository: ScholarshipGraphQLRepository
) {
    suspend operator fun invoke(
        keyword: String,
        country: String? = null,
        fundingType: String? = null
    ): Result<List<Scholarship>> {
        return repository.searchScholarships(keyword, country, fundingType)
    }
}
```

---

## 📱 Sử dụng trong ViewModel

### Ví dụ: HomeViewModel sử dụng GraphQL

```kotlin
package com.example.scholarlens_fe.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scholarlens_fe.domain.usecase.SearchScholarshipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchScholarshipsUseCase: SearchScholarshipsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun searchScholarships(keyword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            searchScholarshipsUseCase(keyword).fold(
                onSuccess = { scholarships ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scholarships = scholarships,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error loading scholarships"
                    )
                }
            )
        }
    }

    data class HomeUiState(
        val scholarships: List<Scholarship> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
```

---

## ⚠️ Xử lý Errors

### Cách 1: Check response.errors

```kotlin
val response: ApolloResponse<GetScholarshipsQuery.Data> = apolloClient
    .query(GetScholarshipsQuery())
    .execute()

if (response.errors != null && response.errors!!.isNotEmpty()) {
    // Handle GraphQL errors
    val errorMessage = response.errors!!.first().message
    // Log or show error to user
}
```

### Cách 2: Try-catch cho network errors

```kotlin
try {
    val response = apolloClient.query(query).execute()
    // Process response
} catch (e: ApolloException) {
    // Handle Apollo-specific errors
    when (e) {
        is ApolloNetworkException -> {
            // Network error
        }
        is ApolloHttpException -> {
            // HTTP error (4xx, 5xx)
        }
        else -> {
            // Other errors
        }
    }
} catch (e: Exception) {
    // Handle other exceptions
}
```

### Cách 3: Xử lý errors trong Repository

```kotlin
suspend fun getScholarships(): Result<List<Scholarship>> {
    return try {
        val response = apolloClient.query(GetScholarshipsQuery()).execute()

        when {
            response.data == null -> {
                Result.failure(Exception("No data received"))
            }
            !response.errors.isNullOrEmpty() -> {
                val errorMessage = response.errors!!.joinToString { it.message }
                Result.failure(Exception(errorMessage))
            }
            else -> {
                val scholarships = mapToDomainModels(response.data!!)
                Result.success(scholarships)
            }
        }
    } catch (e: ApolloNetworkException) {
        Result.failure(Exception("Network error: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("Error: ${e.message}"))
    }
}
```

---

## 🔐 Authentication với GraphQL

### Thêm Authorization header

Cập nhật `GraphQLModule.kt` để thêm authentication interceptor:

```kotlin
@Provides
@Singleton
@Named("GraphQLOkHttpClient")
fun provideGraphQLOkHttpClient(
    @Named("GraphQLLoggingInterceptor") loggingInterceptor: HttpLoggingInterceptor,
    tokenStorage: TokenStorage  // Inject TokenStorage
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenStorage.getToken()
            
            val requestBuilder = originalRequest.newBuilder()
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(loggingInterceptor)
        .build()
}
```

---

## 💡 Best Practices

### 1. Sử dụng Fragments

Tạo fragments để tái sử dụng fields:

**`ScholarshipFragment.graphql`**
```graphql
fragment ScholarshipFragment on Scholarship {
  id
  name
  university
  country
  fundingType
  description
  deadline
  amount
  eligibleFields
}
```

**Sử dụng trong query:**
```graphql
query GetScholarships {
  scholarships {
    ...ScholarshipFragment
  }
}
```

### 2. Sử dụng Variables

Luôn sử dụng variables thay vì hardcode values:

```graphql
# ❌ Bad
query GetScholarships {
  scholarships(limit: 20, offset: 0) {
    id
    name
  }
}

# ✅ Good
query GetScholarships($limit: Int, $offset: Int) {
  scholarships(limit: $limit, offset: $offset) {
    id
    name
  }
}
```

### 3. Error Handling

Luôn check cả `response.data` và `response.errors`:

```kotlin
if (response.data != null && response.errors.isNullOrEmpty()) {
    // Success
} else {
    // Handle errors
}
```

### 4. Map GraphQL models to Domain models

Giữ Domain layer độc lập với GraphQL:

```kotlin
// Map GraphQL model to domain model
val domainModel = DomainModel(
    id = graphqlModel.id,
    name = graphqlModel.name,
    // ... map other fields
)
```

### 5. Sử dụng Coroutines

Apollo hỗ trợ coroutines, sử dụng `execute()` trong coroutine scope:

```kotlin
viewModelScope.launch {
    val response = apolloClient.query(query).execute()
    // Process response
}
```

### 6. Cache Management

Apollo có built-in cache. Sử dụng cache policies:

```kotlin
apolloClient
    .query(query)
    .fetchPolicy(FetchPolicy.CacheFirst) // Use cache first
    .execute()
```

Available cache policies:
- `CacheFirst`: Use cache if available, otherwise fetch from network
- `NetworkFirst`: Fetch from network first, fallback to cache
- `CacheOnly`: Only use cache
- `NetworkOnly`: Only fetch from network

---

## 🔄 Migration từ REST API sang GraphQL

### Bước 1: Giữ REST API hoạt động

Không xóa REST API ngay, giữ cả hai để test.

### Bước 2: Tạo GraphQL Repository mới

Tạo repository mới sử dụng GraphQL, không thay thế repository cũ.

### Bước 3: Test GraphQL Repository

Test kỹ GraphQL repository trước khi migrate.

### Bước 4: Update Use Cases

Update use cases để sử dụng GraphQL repository.

### Bước 5: Remove REST API

Sau khi đã test và confirm GraphQL hoạt động tốt, có thể remove REST API code.

---

## 📚 Tài liệu tham khảo

- [Apollo Android Documentation](https://www.apollographql.com/docs/android/)
- [Apollo Kotlin Guide](https://www.apollographql.com/docs/kotlin/)
- [GraphQL Best Practices](https://graphql.org/learn/best-practices/)

---

## ❓ Troubleshooting

### Lỗi: "Cannot find generated classes"

**Giải pháp:**
1. Sync Gradle: `File > Sync Project with Gradle Files`
2. Build project: `Build > Make Project`
3. Clean project: `Build > Clean Project`, sau đó build lại

### Lỗi: "Schema file not found"

**Giải pháp:**
1. Đảm bảo file `schema.graphqls` tồn tại trong `app/src/main/graphql/com/example/scholarlens_fe/`
2. Download schema từ backend (xem phần [Download GraphQL Schema](#download-graphql-schema))

### Lỗi: "Query/Mutation not found"

**Giải pháp:**
1. Đảm bảo query/mutation file có extension `.graphql`
2. Build project để Apollo generate code
3. Check package name trong `build.gradle.kts` Apollo configuration

### Lỗi: "Network error"

**Giải pháp:**
1. Check GraphQL endpoint URL trong `GraphQLModule.kt`
2. Check network connectivity
3. Check backend server đang chạy
4. Check authentication token (nếu có)

---

## ✅ Checklist

- [ ] Download GraphQL schema từ backend
- [ ] Tạo GraphQL queries cần thiết
- [ ] Tạo GraphQL mutations cần thiết
- [ ] Build project để generate code
- [ ] Tạo Repository sử dụng Apollo Client
- [ ] Tạo Use Cases sử dụng Repository
- [ ] Update ViewModels sử dụng Use Cases
- [ ] Test GraphQL queries/mutations
- [ ] Handle errors properly
- [ ] Add authentication (nếu cần)
- [ ] Update documentation

---

**Chúc bạn code vui vẻ! 🚀**
