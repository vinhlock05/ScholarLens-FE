# GraphQL Implementation (ScholarLens FE)

## Overview
- Uses Apollo Kotlin (`GraphQLModule`) to talk to backend GraphQL endpoint (`/graphql`).
- Specification taken from `GRAPHQL_API_SPEC.md` (searchEs & matchScholarships).
- REST search pipeline has been removed; all data flows now go through GraphQL.

## Schema & Generated Artifacts
- Schema: `app/src/main/graphql/com/example/scholarlens_fe/schema.graphqls`
  - Aligns with backend spec (searchEs filter/sort fields, match profile input, deadline metadata).
- Operations:
  - `SearchScholarships.graphql` – unified search with keyword, filter object & deadline sorting.
  - `MatchScholarships.graphql` – scholarship recommendations for user profile.
- Regenerate models after schema/query edits:
  ```bash
  ./gradlew :app:generateApolloSources --no-daemon
  ```

## Repository Workflow (`ScholarshipRepository`)
- **searchOrFilterScholarships**
  - Builds GraphQL filter + sort args from `ScholarshipFilter` domain model.
  - Attaches Firebase token when available.
  - Converts results into trimmed `Scholarship` domain objects.
  - Saves first-page results (no filters & no deadline sorting) to `ScholarshipCache` for offline use.
- **matchScholarships**
  - Accepts optional `MatchProfile` domain object.
  - Sends to backend via `MatchScholarshipsQuery` and maps into `MatchResult` / `MatchItem` models.
  - All profile fields use camelCase to match backend GraphQL schema:
    - `name: String?`
    - `universities: List<String>?` -> GraphQL `university: [String!]`
    - `fieldOfStudy: String?` -> GraphQL `fieldOfStudy`
    - `minAmount: String?` -> GraphQL `minAmount`
    - `maxAmount: String?` -> GraphQL `maxAmount`
    - `deadlineAfter: String?` -> GraphQL `deadlineAfter`
    - `deadlineBefore: String?` -> GraphQL `deadlineBefore`
- Offline fallback: if network fails, uses cached results; deadline sorting works on cached data via a lightweight helper.

## Domain Models
- `Scholarship` now retains only fields exposed by GraphQL (`name`, `university`, `openDate`, `closeDate`, `amount`, `fieldOfStudy`, `url`, `daysUntilDeadline`, `score`).
- `ScholarshipFilter` tracks keyword, university, field, amount, and deadline sort order.
- New match models:
  - `MatchProfile` mirrors `UserProfileInput` for recommendations.
  - `MatchResult` / `MatchItem` mirror GraphQL match payload.
- `MatchScholarshipsUseCase` exposes repository match call for future UI integration.

## Usage Examples

### Search (camelCase)
```kotlin
// Build filter
val filter = ScholarshipFilter(
  keyword = "engineering",
  university = "MIT",
  fieldOfStudy = "Computer Science",
  sortByDeadline = true,
  sortOrder = SortOrder.DESC
)

// Execute
val result = searchScholarshipsUseCase(filter, size = 10, offset = 0)
```

### Match (camelCase profile)
```kotlin
val profile = MatchProfile(
  name = "John Doe",
  universities = listOf("MIT", "Harvard"),
  fieldOfStudy = "Engineering",
  minAmount = "1000",
  maxAmount = "20000",
  deadlineAfter = "2025-01-01",
  deadlineBefore = "2025-12-31"
)

val match = matchScholarshipsUseCase(profile, size = 10, offset = 0)
```

## UI Updates
- `HomeViewModel` & `HomeScreen` now speak GraphQL data:
  - Filter row provides dropdowns for `University`, `Field`, `Amount`, with a deadline sort toggle.
  - Active filter chips updated to the new fields.
  - Cards display trimmed scholarship info (`name`, `university`, `field`, `amount`). Links use `url`.
  - Replaced ISO date formatter with smart parser that handles ISO + `dd/MM/yyyy` formats.
  - Added deadline badge: shows `<N> days left` or `Expired` according to `days_until_deadline`.

## Removal of Legacy REST Layer
- Deleted `ScholarshipApiService` and `ScholarshipMapper`; `NetworkModule` no longer exposes REST search service.
- Repository no longer constructs filter payloads for REST endpoints.

## Testing Notes
- After schema/query edits always run `:app:generateApolloSources` and a full build (`:app:assembleDebug`) to ensure generated models stay in sync.
- Offline behaviour can be verified by toggling network; GraphQL call falls back to cached list when available.

