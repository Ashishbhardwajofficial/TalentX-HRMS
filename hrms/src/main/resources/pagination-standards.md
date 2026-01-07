# HRMS Pagination Standards

This document defines the pagination standards for the HRMS application to ensure consistency across all API endpoints.

## Standard Pagination Parameters

All paginated endpoints MUST use the following parameter structure:

### Request Parameters

| Parameter | Type | Default | Description | Validation |
|-----------|------|---------|-------------|------------|
| `page` | int | 0 | Page number (0-based indexing) | >= 0 |
| `size` | int | 10 | Number of items per page | 1-100 |
| `sortBy` | String | null | Field name to sort by | Optional |
| `sortDirection` | String | "asc" | Sort direction (asc/desc, case insensitive) | asc or desc |

### Response Structure

All paginated responses MUST use Spring Data's `Page<T>` structure wrapped in `ApiResponse<Page<T>>`:

```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [...],           // Array of actual data items
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false
      },
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 150,       // Total number of items across all pages
    "totalPages": 15,           // Total number of pages
    "last": false,              // Whether this is the last page
    "first": true,              // Whether this is the first page
    "numberOfElements": 10,     // Number of items in current page
    "size": 10,                 // Page size
    "number": 0,                // Current page number (0-based)
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "empty": false              // Whether the page is empty
  },
  "timestamp": "2023-12-21T10:30:00Z"
}
```

## Implementation Guidelines

### Controller Layer

Controllers MUST use the following pattern for pagination parameters:

```java
@GetMapping
public ResponseEntity<ApiResponse<Page<ResponseDTO>>> getItems(
    @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
    @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
    @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
    @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
    
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
    Page<ResponseDTO> result = service.getItems(paginationRequest);
    return ResponseEntity.ok(ApiResponse.success("Items retrieved successfully", result));
}
```

### Service Layer

Services MUST use `PaginationUtils.createPageable()` for consistent pagination handling:

```java
public Page<Entity> getItems(PaginationRequest paginationRequest) {
    Pageable pageable = PaginationUtils.createPageable(paginationRequest);
    return repository.findAll(pageable);
}
```

### Repository Layer

Repositories MUST extend `PagingAndSortingRepository` or use `Pageable` parameters:

```java
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByStatus(ItemStatus status, Pageable pageable);
    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
```

## Validation Rules

### Automatic Validation

The `PaginationRequest` class automatically validates and normalizes parameters:

- **Page**: Negative values are normalized to 0
- **Size**: Values < 1 are normalized to 1, values > 100 are capped at 100
- **Sort Direction**: Invalid values default to "asc"
- **Sort By**: Null/empty values result in unsorted results

### Manual Validation

Use `PaginationUtils.isValidPagination(page, size)` for additional validation if needed.

## Sort Direction Handling

Sort direction is handled case-insensitively:
- "asc", "ASC", "Asc" → Sort.Direction.ASC
- "desc", "DESC", "Desc" → Sort.Direction.DESC
- Any other value → Sort.Direction.ASC (default)

## Error Handling

### Invalid Parameters

Invalid pagination parameters are automatically normalized rather than returning errors:
- This provides a better user experience
- Prevents API failures due to minor parameter issues
- Ensures consistent behavior across all endpoints

### Large Page Sizes

Page sizes are capped at 100 to prevent performance issues:
- Protects against memory exhaustion
- Ensures reasonable response times
- Maintains system stability

## Best Practices

### Default Values

- Always provide sensible defaults for pagination parameters
- Use page=0, size=10 as standard defaults
- Default sort direction should be "asc"

### Sort Fields

- Validate sort fields against entity properties
- Provide meaningful default sort fields for each endpoint
- Document available sort fields in API documentation

### Performance Considerations

- Index frequently sorted columns in the database
- Consider using cursor-based pagination for very large datasets
- Monitor query performance for complex sort operations

### API Documentation

- Always document pagination parameters in OpenAPI/Swagger
- Include examples of paginated responses
- Specify available sort fields for each endpoint

## Migration Guide

### For Existing Controllers

1. Replace manual pagination parameter handling with `PaginationRequest`
2. Use `PaginationUtils.createPageable()` in services
3. Ensure consistent parameter names and defaults
4. Update API documentation

### For Existing Services

1. Replace manual `PageRequest.of()` calls with `PaginationUtils.createPageable()`
2. Remove custom sort direction parsing logic
3. Use consistent error handling patterns

## Testing

### Unit Tests

Test pagination logic using `PaginationUtilsTest` as a reference:
- Test parameter validation and normalization
- Test sort direction parsing
- Test edge cases (negative pages, large sizes, etc.)

### Integration Tests

Test paginated endpoints to ensure:
- Correct response structure
- Proper parameter handling
- Consistent behavior across endpoints

## Compliance Verification

Use the following checklist to verify pagination compliance:

- [ ] Uses standard parameter names (page, size, sortBy, sortDirection)
- [ ] Uses standard default values (0, 10, null, "asc")
- [ ] Returns `Page<T>` wrapped in `ApiResponse<Page<T>>`
- [ ] Uses `PaginationUtils.createPageable()` for Pageable creation
- [ ] Handles sort direction case-insensitively
- [ ] Validates and normalizes parameters automatically
- [ ] Documents pagination parameters in API documentation
- [ ] Includes unit tests for pagination logic