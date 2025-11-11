# 🔷 GraphQL API Specification

## Tổng quan

Backend cung cấp GraphQL API để tìm kiếm và khớp học bổng (Scholarships). GraphQL API cho phép kết hợp keyword search và filters trong một query, đồng thời cung cấp type-safe schema.

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

---

## 🔍 Query 1: searchEs

### Description

Unified search query cho phép kết hợp keyword search và structured filters trong một query. Hỗ trợ 4 modes:
- **No query, no filters**: Trả về tất cả scholarships, có thể sort theo deadline
- **Keyword-only**: Chỉ search theo keyword
- **Filters-only**: Chỉ filter (không có keyword)
- **Keyword + Filters**: Kết hợp cả 2, intersect results và preserve keyword ranking

### Signature

```graphql
searchEs(
  collection: String!
  q: String
  filter: ScholarshipFilter
  inter_field_operator: InterFieldOperator = AND
  sort_by_deadline: Boolean = false
  sort_order: SortOrder = ASC
  size: Int = 10
  offset: Int = 0
): SearchResult!
```

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `collection` | String | ✅ Yes | - | Tên collection/index (thường là `"scholar_lens"` hoặc `"scholarships"`) |
| `q` | String | ❌ No | `null` | Từ khóa tìm kiếm (full-text search) |
| `filter` | ScholarshipFilter | ❌ No | `null` | Filter object với các fields: name, university, field_of_study, amount |
| `inter_field_operator` | InterFieldOperator | ❌ No | `AND` | Toán tử kết hợp các filters: `AND` hoặc `OR` |
| `sort_by_deadline` | Boolean | ❌ No | `false` | Có sort theo deadline (close_time) không |
| `sort_order` | SortOrder | ❌ No | `ASC` | Thứ tự sort: `ASC` hoặc `DESC` |
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
  score: Float
  source: ScholarshipSource
}

type ScholarshipSource {
  name: String
  university: String
  open_time: String
  close_time: String
  amount: String
  field_of_study: String
  url: String
  days_until_deadline: String  # Computed field: số ngày còn lại hoặc "Expired"
}
```

**Lưu ý về `days_until_deadline`:**
- Trả về số ngày còn lại trước deadline (dạng string)
- Trả về `"Expired"` nếu deadline đã qua
- Trả về `null` nếu không có `close_time`

### ScholarshipFilter Input Type

```graphql
input ScholarshipFilter {
  name: String
  university: String
  field_of_study: String
  amount: String
}
```

**Lưu ý:**
- Tất cả fields trong `ScholarshipFilter` đều optional
- Chỉ cần cung cấp các fields muốn filter
- Mỗi field sẽ được filter với operator `OR` (có thể filter nhiều giá trị trong cùng field)

### Enums

```graphql
enum InterFieldOperator {
  AND
  OR
}

enum SortOrder {
  ASC
  DESC
}
```

### Examples

#### Example 1: Keyword Search Only

```graphql
query SearchByKeyword {
  searchEs(
    collection: "scholar_lens"
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
        university
        open_time
        close_time
        amount
        field_of_study
        url
        days_until_deadline
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
    collection: "scholar_lens"
    filter: {
      university: "MIT"
      field_of_study: "Computer Science"
    }
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
        university
        field_of_study
        amount
        days_until_deadline
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
    collection: "scholar_lens"
    q: "engineering"
    filter: {
      university: "MIT"
      field_of_study: "Computer Science"
    }
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
        university
        field_of_study
        amount
        days_until_deadline
      }
    }
  }
}
```

**Variables:**
```json
{}
```

#### Example 4: Sort by Deadline

```graphql
query SortByDeadline {
  searchEs(
    collection: "scholar_lens"
    filter: {
      field_of_study: "Engineering"
    }
    sort_by_deadline: true
    sort_order: ASC
    size: 10
    offset: 0
  ) {
    total
    items {
      id
      source {
        name
        close_time
        days_until_deadline
      }
    }
  }
}
```

#### Example 5: Get All Scholarships (No Query, No Filters)

```graphql
query GetAllScholarships {
  searchEs(
    collection: "scholar_lens"
    sort_by_deadline: true
    sort_order: ASC
    size: 20
    offset: 0
  ) {
    total
    items {
      id
      source {
        name
        university
        close_time
        days_until_deadline
      }
    }
  }
}
```

---

## 🎯 Query 2: matchScholarships

### Description

Recommend scholarships dựa trên user profile. Query này tự động convert user profile thành filters và tìm scholarships phù hợp. Sử dụng OR operator giữa các filters để có kết quả đa dạng hơn.

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
  name: String                    # Scholarship name keyword search
  university: [String!]           # List of preferred universities
  field_of_study: String          # Desired field of study
  min_amount: String              # Minimum scholarship amount
  max_amount: String              # Maximum scholarship amount
  deadline_after: String          # Only scholarships closing after this date (DD/MM/YYYY)
  deadline_before: String         # Only scholarships closing before this date (DD/MM/YYYY)
}
```

**Lưu ý:**
- Tất cả fields đều optional
- `university` là array để có thể filter nhiều universities
- `deadline_after` và `deadline_before` sử dụng format `DD/MM/YYYY`

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
  matchedFields: [String!]!       # List of reasons why this scholarship matched
  summaryName: String
  summaryStartDate: String
  summaryEndDate: String
  summaryAmount: String
}
```

**Lưu ý về `matchedFields`:**
- Trả về danh sách các lý do tại sao scholarship này match với profile
- Format: `["field_of_study_match:Engineering", "university_match:MIT", ...]`
- Giúp frontend hiển thị lý do recommendation

### Examples

#### Example 1: Match với Profile đầy đủ

```graphql
query MatchScholarships {
  matchScholarships(
    profile: {
      name: "engineering"
      university: ["MIT", "Stanford"]
      field_of_study: "Computer Science"
      min_amount: "1000"
      deadline_after: "01/01/2024"
      deadline_before: "31/12/2024"
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
      university: ["MIT", "Harvard"]
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

**Lưu ý:** Nếu không có profile, query sẽ trả về empty result vì không có filters để apply.

---

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
  "query": "query SearchScholarships { searchEs(collection: \"scholar_lens\", q: \"engineering\") { total items { id source { name } } } }",
  "variables": {},
  "operationName": "SearchScholarships"
}
```

Hoặc sử dụng variables:

```json
{
  "query": "query SearchScholarships($collection: String!, $q: String) { searchEs(collection: $collection, q: $q) { total items { id source { name } } } }",
  "variables": {
    "collection": "scholar_lens",
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
            "university": "Various UK Universities",
            "open_time": "01/09/2024",
            "close_time": "01/11/2024",
            "amount": "Full tuition + living expenses",
            "field_of_study": "All fields",
            "url": "https://example.com",
            "days_until_deadline": "45"
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

---

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

#### Query Example - searchEs

```typescript
import { gql, useQuery } from '@apollo/client';

const SEARCH_SCHOLARSHIPS = gql`
  query SearchScholarships(
    $collection: String!
    $q: String
    $filter: ScholarshipFilter
    $size: Int
    $offset: Int
  ) {
    searchEs(
      collection: $collection
      q: $q
      filter: $filter
      size: $size
      offset: $offset
    ) {
      total
      items {
        id
        score
        source {
          name
          university
          open_time
          close_time
          amount
          field_of_study
          url
          days_until_deadline
        }
      }
    }
  }
`;

function SearchComponent() {
  const { loading, error, data } = useQuery(SEARCH_SCHOLARSHIPS, {
    variables: {
      collection: "scholar_lens",
      q: "engineering",
      filter: {
        university: "MIT",
        field_of_study: "Computer Science"
      },
      size: 10,
      offset: 0
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
          <p>University: {item.source.university}</p>
          <p>Days until deadline: {item.source.days_until_deadline}</p>
        </div>
      ))}
    </div>
  );
}
```

#### Query Example - matchScholarships

```typescript
const MATCH_SCHOLARSHIPS = gql`
  query MatchScholarships(
    $profile: UserProfileInput
    $size: Int
    $offset: Int
  ) {
    matchScholarships(
      profile: $profile
      size: $size
      offset: $offset
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
`;

function MatchComponent() {
  const { loading, error, data } = useQuery(MATCH_SCHOLARSHIPS, {
    variables: {
      profile: {
        field_of_study: "Computer Science",
        university: ["MIT", "Stanford"],
        deadline_after: "01/01/2024",
        deadline_before: "31/12/2024"
      },
      size: 10,
      offset: 0
    }
  });

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error.message}</p>;

  return (
    <div>
      <p>Total: {data.matchScholarships.total}</p>
      {data.matchScholarships.items.map(item => (
        <div key={item.id}>
          <h3>{item.summaryName}</h3>
          <p>Match Score: {item.esScore}</p>
          <p>Matched Fields: {item.matchedFields.join(", ")}</p>
        </div>
      ))}
    </div>
  );
}
```

### React Query / Fetch Example

```typescript
async function searchScholarships(
  collection: string,
  q?: string,
  filter?: {
    name?: string;
    university?: string;
    field_of_study?: string;
    amount?: string;
  },
  size: number = 10,
  offset: number = 0
) {
  const token = await getFirebaseToken();
  
  const query = `
    query SearchScholarships(
      $collection: String!
      $q: String
      $filter: ScholarshipFilter
      $size: Int
      $offset: Int
    ) {
      searchEs(
        collection: $collection
        q: $q
        filter: $filter
        size: $size
        offset: $offset
      ) {
        total
        items {
          id
          score
          source {
            name
            university
            close_time
            days_until_deadline
          }
        }
      }
    }
  `;

  const response = await fetch('http://YOUR_IP:8000/graphql', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      query,
      variables: {
        collection,
        q,
        filter,
        size,
        offset
      }
    })
  });

  const result = await response.json();
  return result.data.searchEs;
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
    "query": "query { searchEs(collection: \"scholar_lens\", q: \"engineering\", size: 10) { total items { id score source { name university days_until_deadline } } } }"
  }'

# Match query
curl -X POST http://YOUR_IP:8000/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "query { matchScholarships(profile: { field_of_study: \"Engineering\", university: [\"MIT\"] }, size: 10) { total items { id esScore summaryName matchedFields } } }"
  }'
```

---

## 🔄 Pagination

### Using offset-based Pagination

```graphql
# Page 1
query SearchPage1 {
  searchEs(
    collection: "scholar_lens"
    q: "engineering"
    size: 10
    offset: 0
  ) {
    total
    items { id source { name } }
  }
}

# Page 2
query SearchPage2 {
  searchEs(
    collection: "scholar_lens"
    q: "engineering"
    size: 10
    offset: 10
  ) {
    total
    items { id source { name } }
  }
}
```

### Using MatchResult Pagination

```graphql
query MatchWithPagination {
  matchScholarships(
    profile: {
      field_of_study: "Engineering"
    }
    size: 10
    offset: 0
  ) {
    total
    hasNextPage
    nextOffset
    items { id summaryName }
  }
}

# Next page - use nextOffset from previous response
query MatchNextPage {
  matchScholarships(
    profile: {
      field_of_study: "Engineering"
    }
    size: 10
    offset: 10  # Use nextOffset from previous query
  ) {
    total
    hasNextPage
    nextOffset
    items { id summaryName }
  }
}
```

---

## 🎯 Best Practices

### 1. Field Selection

Chỉ request các fields cần thiết để giảm response size:

```graphql
# ✅ Good - chỉ lấy fields cần thiết
query {
  searchEs(collection: "scholar_lens", q: "engineering") {
    total
    items {
      id
      source {
        name
        university
      }
    }
  }
}

# ❌ Bad - lấy tất cả fields (không cần thiết)
query {
  searchEs(collection: "scholar_lens", q: "engineering") {
    total
    items {
      id
      score
      source {
        name
        university
        open_time
        close_time
        amount
        field_of_study
        url
        days_until_deadline
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
    collection: "scholar_lens"
    q: "engineering"
    filter: {
      university: "MIT"
    }
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

### 5. Date Format

**Lưu ý quan trọng về date format:**
- `open_time` và `close_time` trong database sử dụng format `DD/MM/YYYY`
- `deadline_after` và `deadline_before` trong `UserProfileInput` cũng sử dụng format `DD/MM/YYYY`
- `days_until_deadline` là computed field, trả về số ngày còn lại hoặc `"Expired"`

---

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
- Kiểm tra collection name (thường là `"scholar_lens"`)
- Kiểm tra keyword search có match không
- Với `matchScholarships`, nếu không có profile sẽ trả về empty result

### 4. Date Format Issues

**Symptom:** Filters không hoạt động với dates

**Solution:**
- Đảm bảo sử dụng format `DD/MM/YYYY` cho `deadline_after` và `deadline_before`
- Ví dụ: `"01/01/2024"` không phải `"2024-01-01"`

---

## 📊 Comparison: GraphQL vs REST API

| Feature | GraphQL | REST API |
|---------|---------|----------|
| **Combine search + filter** | ✅ 1 query | ❌ Cần 2 requests |
| **Field selection** | ✅ Chỉ lấy fields cần | ❌ Trả về tất cả |
| **Type-safe** | ✅ Schema-based | ❌ Manual validation |
| **Simplicity** | ❌ Phức tạp hơn | ✅ Đơn giản |
| **Debugging** | ❌ Khó hơn | ✅ Dễ (cURL/Postman) |
| **Learning curve** | ❌ Cần học GraphQL | ✅ Quen thuộc |

---

## 📚 References

- [GraphQL Documentation](https://graphql.org/learn/)
- [Apollo Client Documentation](https://www.apollographql.com/docs/react/)
- [Strawberry GraphQL](https://strawberry.rocks/)

---

## 📝 Complete Query Examples

### Full Search Query với tất cả fields

```graphql
query FullSearch {
  searchEs(
    collection: "scholar_lens"
    q: "engineering master"
    filter: {
      university: "MIT"
      field_of_study: "Computer Science"
    }
    inter_field_operator: AND
    sort_by_deadline: true
    sort_order: ASC
    size: 20
    offset: 0
  ) {
    total
    items {
      id
      score
      source {
        name
        university
        open_time
        close_time
        amount
        field_of_study
        url
        days_until_deadline
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
      name: "engineering"
      university: ["MIT", "Stanford", "Harvard"]
      field_of_study: "Computer Science"
      min_amount: "1000"
      max_amount: "50000"
      deadline_after: "01/01/2024"
      deadline_before: "31/12/2024"
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
**Version:** 2.0
