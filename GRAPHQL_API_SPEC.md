# 🔷 GraphQL API Specification

## Tổng quan

Backend cung cấp GraphQL API để tìm kiếm và khớp học bổng (Scholarships). GraphQL API cho phép combine keyword search và filters trong một query, đồng thời cung cấp type-safe schema.

## 🔗 GraphQL Endpoint

```
POST http://YOUR_IP:8000/graphql
```

**Lưu ý:** Endpoint là `/graphql` (không có prefix `/api/v1/es_gql`)

## 🔐 Authentication

Tất cả GraphQL queries yêu cầu Firebase Authentication token trong header:

```
Authorization: Bearer {firebase_id_token}
Content-Type: application/json
```

## 📋 GraphQL Schema

### Queries

Backend cung cấp 2 queries chính:

1. **`searchEs`** - Unified search combining keyword and structured filters
2. **`matchScholarships`** - Recommend scholarships for a given user profile

## 🔍 Query 1: searchEs

### Description

Unified search query cho phép combine keyword search và structured filters trong một query. Hỗ trợ 3 modes:
- **Keyword-only**: Chỉ search theo keyword
- **Filters-only**: Chỉ filter (không có keyword)
- **Keyword + Filters**: Combine cả 2, intersect results và preserve keyword ranking

### Signature

```graphql
searchEs(
  collection: String!
  q: String
  filters: [FilterInput!]
  inter_field_operator: InterFieldOperator = AND
  size: Int = 10
  offset: Int = 0
): SearchResult!
```

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `collection` | String | ✅ Yes | - | Tên collection (thường là `"scholarships"`) |
| `q` | String | ❌ No | `null` | Từ khóa tìm kiếm (full-text) |
| `filters` | [FilterInput!] | ❌ No | `[]` | Danh sách filters |
| `inter_field_operator` | InterFieldOperator | ❌ No | `AND` | Toán tử kết hợp các filters: `AND` hoặc `OR` |
| `size` | Int | ❌ No | `10` | Số lượng kết quả trả về (1-100) |
| `offset` | Int | ❌ No | `0` | Vị trí bắt đầu (dùng cho pagination) |

### Return Type

```graphql
type SearchResult {
  total: Int!
  items: [SearchHit!]!
}

type SearchHit {
  id: String!
  score: Float!
  source: ScholarshipSource
}

type ScholarshipSource {
  name: String
  country: String
  startDate: String
  endDate: String
  amount: String
  daysUntilDeadline: Int
}
```

### Examples

#### Example 1: Keyword Search Only

```graphql
query SearchByKeyword {
  searchEs(
    collection: "scholarships"
    q: "engineering"
    size: 10
    offset: 0
  ) {
    total
    items {
      id
      score
      source {
        name
        country
        startDate
        endDate
        amount
        daysUntilDeadline
      }
    }
  }
}
```

**Variables:**
```json
{}
```

#### Example 2: Filters Only

```graphql
query FilterScholarships {
  searchEs(
    collection: "scholarships"
    filters: [
      {
        field: "Country"
        stringValues: ["Hà Lan", "Đức"]
        operator: OR
      }
      {
        field: "Funding_Level"
        stringValues: ["Toàn phần"]
        operator: OR
      }
    ]
    inter_field_operator: AND
    size: 10
    offset: 0
  ) {
    total
    items {
      id
      score
      source {
        name
        country
        startDate
        endDate
        amount
      }
    }
  }
}
```

**Variables:**
```json
{}
```

#### Example 3: Keyword + Filters (Combined)

```graphql
query SearchWithFilters {
  searchEs(
    collection: "scholarships"
    q: "engineering"
    filters: [
      {
        field: "Country"
        stringValues: ["UK", "Hà Lan"]
        operator: OR
      }
      {
        field: "Funding_Level"
        stringValues: ["Toàn phần", "Bán phần"]
        operator: OR
      }
    ]
    inter_field_operator: AND
    size: 20
    offset: 0
  ) {
    total
    items {
      id
      score
      source {
        name
        country
        startDate
        endDate
        amount
        daysUntilDeadline
      }
    }
  }
}
```

**Variables:**
```json
{}
```

#### Example 4: Filter with Integer Values

```graphql
query FilterWithIntValues {
  searchEs(
    collection: "scholarships"
    filters: [
      {
        field: "Min_GPA"
        intValues: [3, 4]
        operator: OR
      }
    ]
    size: 10
    offset: 0
  ) {
    total
    items {
      id
      score
      source {
        name
        amount
      }
    }
  }
}
```

### FilterInput Type

```graphql
input FilterInput {
  field: String!
  stringValues: [String!]
  intValues: [Int!]
  floatValues: [Float!]
  operator: IntraFieldOperator = OR
}

enum IntraFieldOperator {
  AND
  OR
}

enum InterFieldOperator {
  AND
  OR
}
```

**Lưu ý:**
- Chỉ cần cung cấp một trong các `*_values` (stringValues, intValues, floatValues)
- Nếu cung cấp nhiều, chúng sẽ được merge
- `operator` (IntraFieldOperator) áp dụng cho các giá trị trong cùng một filter
- `inter_field_operator` áp dụng giữa các filters khác nhau

### Common Filter Fields

| Field | Type | Example Values |
|-------|------|----------------|
| `Country` | String | `"Hà Lan"`, `"Đức"`, `"UK"`, `"USA"` |
| `Funding_Level` | String | `"Toàn phần"`, `"Bán phần"`, `"Học phí"` |
| `Scholarship_Type` | String | `"Master"`, `"PhD"`, `"Bachelor"` |
| `Application_Mode` | String | `"Online"`, `"Offline"`, `"Both"` |
| `Eligible_Fields` | String | `"Engineering"`, `"Computer Science"` |
| `Min_GPA` | Int/Float | `3`, `3.5`, `4` |

## 🎯 Query 2: matchScholarships

### Description

Recommend scholarships dựa trên user profile. Query này tự động convert user profile thành filters và tìm scholarships phù hợp.

### Signature

```graphql
matchScholarships(
  profile: UserProfileInput
  size: Int = 10
  offset: Int = 0
): MatchResult!
```

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `profile` | UserProfileInput | ❌ No | `null` | User profile với preferences |
| `size` | Int | ❌ No | `10` | Số lượng kết quả trả về |
| `offset` | Int | ❌ No | `0` | Vị trí bắt đầu (pagination) |

### UserProfileInput Type

```graphql
input UserProfileInput {
  gpa_range_4: Float
  degree: String
  field_of_study: String
  desired_scholarship_type: [String!]
  desired_funding_level: [String!]
  desired_application_mode: [String!]
  deadline_after: String
  deadline_before: String
}
```

### Return Type

```graphql
type MatchResult {
  total: Int!
  items: [MatchItem!]!
  hasNextPage: Boolean!
  nextOffset: Int
  warnings: [String!]
}

type MatchItem {
  id: String!
  esScore: Float!
  matchScore: Float!
  matchedFields: [String!]!
  summaryName: String
  summaryStartDate: String
  summaryEndDate: String
  summaryAmount: String
}
```

### Examples

#### Example 1: Match với Profile đầy đủ

```graphql
query MatchScholarships {
  matchScholarships(
    profile: {
      gpa_range_4: 3.5
      degree: "Bachelor"
      field_of_study: "Engineering"
      desired_scholarship_type: ["Master", "PhD"]
      desired_funding_level: ["Toàn phần"]
      desired_application_mode: ["Online"]
      deadline_after: "2024-01-01"
      deadline_before: "2024-12-31"
    }
    size: 10
    offset: 0
  ) {
    total
    hasNextPage
    nextOffset
    warnings
    items {
      id
      esScore
      matchScore
      matchedFields
      summaryName
      summaryStartDate
      summaryEndDate
      summaryAmount
    }
  }
}
```

**Variables:**
```json
{}
```

#### Example 2: Match với Profile đơn giản

```graphql
query MatchSimpleProfile {
  matchScholarships(
    profile: {
      field_of_study: "Computer Science"
      desired_countries: ["UK", "USA"]
    }
    size: 20
    offset: 0
  ) {
    total
    items {
      id
      esScore
      matchedFields
      summaryName
      summaryAmount
    }
  }
}
```

#### Example 3: Match không có Profile (trả về tất cả)

```graphql
query MatchAll {
  matchScholarships(
    size: 10
    offset: 0
  ) {
    total
    items {
      id
      esScore
      summaryName
    }
  }
}
```

## 📡 HTTP Request Format

### Request Headers

```http
POST /graphql HTTP/1.1
Host: YOUR_IP:8000
Content-Type: application/json
Authorization: Bearer {firebase_id_token}
```

### Request Body

```json
{
  "query": "query SearchScholarships { ... }",
  "variables": {
    "collection": "scholarships",
    "q": "engineering"
  },
  "operationName": "SearchScholarships"
}
```

### Response Format

**Success Response:**
```json
{
  "data": {
    "searchEs": {
      "total": 150,
      "items": [
        {
          "id": "doc_id_123",
          "score": 8.5,
          "source": {
            "name": "Chevening Scholarship",
            "country": "UK",
            "startDate": "2024-01-01",
            "endDate": "2024-12-31",
            "amount": "Toàn phần",
            "daysUntilDeadline": 365
          }
        }
      ]
    }
  }
}
```

**Error Response:**
```json
{
  "errors": [
    {
      "message": "Invalid token",
      "locations": [{"line": 2, "column": 3}],
      "path": ["searchEs"]
    }
  ],
  "data": null
}
```

## 💻 Frontend Integration Examples

### JavaScript/TypeScript (Apollo Client)

#### Setup Apollo Client

```typescript
import { ApolloClient, InMemoryCache, createHttpLink } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';

const httpLink = createHttpLink({
  uri: 'http://YOUR_IP:8000/graphql',
});

const authLink = setContext((_, { headers }) => {
  const token = getFirebaseToken(); // Get Firebase ID token
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : "",
    }
  }
});

const client = new ApolloClient({
  link: authLink.concat(httpLink),
  cache: new InMemoryCache()
});
```

#### Query Example

```typescript
import { gql, useQuery } from '@apollo/client';

const SEARCH_SCHOLARSHIPS = gql`
  query SearchScholarships($collection: String!, $q: String, $filters: [FilterInput!]) {
    searchEs(collection: $collection, q: $q, filters: $filters, size: 10, offset: 0) {
      total
      items {
        id
        score
        source {
          name
          country
          startDate
          endDate
          amount
          daysUntilDeadline
        }
      }
    }
  }
`;

function SearchComponent() {
  const { loading, error, data } = useQuery(SEARCH_SCHOLARSHIPS, {
    variables: {
      collection: "scholarships",
      q: "engineering",
      filters: [
        {
          field: "Country",
          stringValues: ["Hà Lan", "Đức"],
          operator: "OR"
        }
      ]
    }
  });

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error.message}</p>;

  return (
    <div>
      <p>Total: {data.searchEs.total}</p>
      {data.searchEs.items.map(item => (
        <div key={item.id}>
          <h3>{item.source.name}</h3>
          <p>Country: {item.source.country}</p>
          <p>Days until deadline: {item.source.daysUntilDeadline}</p>
        </div>
      ))}
    </div>
  );
}
```

### Kotlin (Apollo Kotlin)

#### Setup Apollo Client

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
    implementation("com.apollographql.apollo3:apollo-http-cache:3.8.2")
}

// Apollo Client
val apolloClient = ApolloClient.Builder()
    .serverUrl("http://YOUR_IP:8000/graphql")
    .addHttpHeader("Authorization", "Bearer $token")
    .build()
```

#### Query Example

```kotlin
// GraphQL Query (SearchScholarships.graphql)
query SearchScholarships($collection: String!, $q: String, $filters: [FilterInput!]) {
  searchEs(collection: $collection, q: $q, filters: $filters, size: 10, offset: 0) {
    total
    items {
      id
      score
      source {
        name
        country
        startDate
        endDate
        amount
        daysUntilDeadline
      }
    }
  }
}

// Kotlin Code
suspend fun searchScholarships(
    query: String? = null,
    filters: List<FilterInput>? = null
): SearchScholarshipsQuery.Data {
    val response = apolloClient.query(
        SearchScholarshipsQuery(
            collection = "scholarships",
            q = query,
            filters = filters?.map { filter ->
                FilterInput(
                    field = filter.field,
                    stringValues = filter.stringValues,
                    operator = filter.operator
                )
            }
        )
    ).execute()
    
    return response.dataAssertNoErrors
}
```

### cURL Example

```bash
# Get Firebase ID token first
TOKEN="your-firebase-id-token"

# Search query
curl -X POST http://YOUR_IP:8000/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "query { searchEs(collection: \"scholarships\", q: \"engineering\", size: 10) { total items { id score source { name country } } } }"
  }'

# Match query
curl -X POST http://YOUR_IP:8000/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "query { matchScholarships(profile: { field_of_study: \"Engineering\", desired_funding_level: [\"Toàn phần\"] }, size: 10) { total items { id esScore summaryName } } }"
  }'
```

## 🔄 Pagination

### Using offset-based Pagination

```graphql
query SearchPage1 {
  searchEs(collection: "scholarships", q: "engineering", size: 10, offset: 0) {
    total
    items { id source { name } }
  }
}

query SearchPage2 {
  searchEs(collection: "scholarships", q: "engineering", size: 10, offset: 10) {
    total
    items { id source { name } }
  }
}
```

### Using MatchResult Pagination

```graphql
query MatchWithPagination {
  matchScholarships(profile: {...}, size: 10, offset: 0) {
    total
    hasNextPage
    nextOffset
    items { id summaryName }
  }
}

# Next page
query MatchNextPage {
  matchScholarships(profile: {...}, size: 10, offset: 10) {
    total
    hasNextPage
    nextOffset
    items { id summaryName }
  }
}
```

## 🎯 Best Practices

### 1. Field Selection

Chỉ request các fields cần thiết để giảm response size:

```graphql
# ✅ Good - chỉ lấy fields cần thiết
query {
  searchEs(collection: "scholarships", q: "engineering") {
    total
    items {
      id
      source {
        name
        country
      }
    }
  }
}

# ❌ Bad - lấy tất cả fields (không cần thiết)
query {
  searchEs(collection: "scholarships", q: "engineering") {
    total
    items {
      id
      score
      source {
        name
        country
        startDate
        endDate
        amount
        daysUntilDeadline
      }
    }
  }
}
```

### 2. Combine Search + Filters

Sử dụng GraphQL để combine keyword search và filters trong 1 query:

```graphql
# ✅ Good - combine trong 1 query
query {
  searchEs(
    collection: "scholarships"
    q: "engineering"
    filters: [
      { field: "Country", stringValues: ["UK"], operator: OR }
    ]
  ) {
    total
    items { id source { name } }
  }
}
```

### 3. Error Handling

```typescript
const { loading, error, data } = useQuery(SEARCH_QUERY, {
  variables: { ... },
  onError: (error) => {
    if (error.graphQLErrors) {
      error.graphQLErrors.forEach(({ message }) => {
        console.error('GraphQL error:', message);
      });
    }
    if (error.networkError) {
      console.error('Network error:', error.networkError);
    }
  }
});
```

### 4. Token Refresh

Đảm bảo token được refresh trước khi expire:

```typescript
// Refresh token before making GraphQL request
const token = await firebaseAuth.currentUser?.getIdToken(true);
apolloClient.setLink(authLink.concat(httpLink));
```

## 🔍 GraphQL Playground / GraphiQL

Backend có thể có GraphQL Playground để test queries. Truy cập:

```
http://YOUR_IP:8000/graphql
```

Nếu có GraphiQL, có thể test queries trực tiếp trong browser.

## 📊 Comparison: GraphQL vs REST API

| Feature | GraphQL | REST API |
|---------|---------|----------|
| **Combine search + filter** | ✅ 1 query | ❌ Cần 2 requests |
| **Field selection** | ✅ Chỉ lấy fields cần | ❌ Trả về tất cả |
| **Type-safe** | ✅ Schema-based | ❌ Manual validation |
| **Simplicity** | ❌ Phức tạp hơn | ✅ Đơn giản |
| **Debugging** | ❌ Khó hơn | ✅ Dễ (cURL/Postman) |
| **Learning curve** | ❌ Cần học GraphQL | ✅ Quen thuộc |

## 🚨 Common Issues

### 1. Authentication Error

**Symptom:** `401 Unauthorized` hoặc `Invalid token`

**Solution:**
- Đảm bảo token được gửi trong header: `Authorization: Bearer {token}`
- Refresh token nếu đã expire
- Kiểm tra token format

### 2. Invalid Query Syntax

**Symptom:** `Syntax Error` trong GraphQL response

**Solution:**
- Kiểm tra query syntax
- Đảm bảo variables đúng type
- Sử dụng GraphQL Playground để validate

### 3. Empty Results

**Symptom:** `total: 0` hoặc `items: []`

**Solution:**
- Kiểm tra filters có đúng không
- Kiểm tra collection name
- Kiểm tra keyword search có match không

## 📚 References

- [GraphQL Documentation](https://graphql.org/learn/)
- [Apollo Client Documentation](https://www.apollographql.com/docs/react/)
- [Apollo Kotlin Documentation](https://www.apollographql.com/docs/kotlin/)
- [Strawberry GraphQL](https://strawberry.rocks/)

## 📝 Complete Query Examples

### Full Search Query với tất cả fields

```graphql
query FullSearch {
  searchEs(
    collection: "scholarships"
    q: "engineering master"
    filters: [
      {
        field: "Country"
        stringValues: ["UK", "Hà Lan", "Đức"]
        operator: OR
      }
      {
        field: "Funding_Level"
        stringValues: ["Toàn phần", "Bán phần"]
        operator: OR
      }
      {
        field: "Scholarship_Type"
        stringValues: ["Master", "PhD"]
        operator: OR
      }
    ]
    inter_field_operator: AND
    size: 20
    offset: 0
  ) {
    total
    items {
      id
      score
      source {
        name
        country
        startDate
        endDate
        amount
        daysUntilDeadline
      }
    }
  }
}
```

### Full Match Query

```graphql
query FullMatch {
  matchScholarships(
    profile: {
      gpa_range_4: 3.5
      degree: "Bachelor"
      field_of_study: "Computer Science"
      desired_scholarship_type: ["Master", "PhD"]
      desired_funding_level: ["Toàn phần"]
      desired_application_mode: ["Online"]
      deadline_after: "2024-01-01"
      deadline_before: "2024-12-31"
    }
    size: 20
    offset: 0
  ) {
    total
    hasNextPage
    nextOffset
    warnings
    items {
      id
      esScore
      matchScore
      matchedFields
      summaryName
      summaryStartDate
      summaryEndDate
      summaryAmount
    }
  }
}
```

---

**Last Updated:** 2024
**Version:** 1.0

