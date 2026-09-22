---
name: spring-data-redis
description: >
  Implement or review Redis caching, Spring Session storage, rate limiting, and
  other Spring Data Redis integrations in this repository. Use only after the
  relevant spec confirms Redis is required; covers hexagonal placement, Gradle
  dependencies, key and TTL design, JSON serialization, atomicity, and tests.
---

# Spring Data Redis

Use the smallest Redis feature that satisfies the confirmed requirement. Redis is an external
technology, not a domain concept or an architecture layer.

## Project gates and placement

- Follow the repository's six-stage spec workflow. Do not add Redis dependencies or production
  code before the boundary and tests have been confirmed.
- Put Redis access in the confirmed outbound adapter, normally
  `:<context>:adapters:out:redis`. Create that module only when the capability needs it.
- Keep connection properties, serializer beans, cache managers, and adapter wiring in the
  deployment unit's `container`.
- Keep Redis, Spring Cache, TTL, keys, sessions, and rate-limit implementation details out of
  Domain Core. Do not expose `RedisTemplate`, cache DTOs, or Redis types through domain APIs.
- Define a technology-neutral application output port only when the use case explicitly needs an
  external state capability. Keep purely HTTP/session infrastructure at the edge.
- Cache protocol DTOs or dedicated cache records, never JPA entities, lazy proxies, or aggregate
  instances.
- Redis must not become the authoritative store unless the confirmed business design explicitly
  says so.

## Choose one integration style

- Ordinary method-result caching: Spring Cache with `RedisCacheManager`.
- Explicit keys, atomic commands, counters, or custom TTL behavior: `StringRedisTemplate` or a
  typed `RedisTemplate` in the Redis adapter.
- HTTP session storage: Spring Session Redis and Spring Boot auto-configuration.
- Distributed locks: use the already version-managed Redisson dependency only after the spec
  demonstrates a cross-instance lock is required.

Do not manage the same key through both Spring Cache and manual `RedisTemplate` code.

## Gradle dependencies

Add dependencies only to the adapter or container that uses them. Versions come from the Spring
Boot BOM already configured by the repository.

For caching or direct Redis access:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
}
```

For Redis-backed HTTP sessions instead:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-session-data-redis")
}
```

Do not add every starter preemptively.

## Configuration

Use environment-backed configuration and never commit credentials:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2s
  cache:
    type: redis
```

Use `@EnableCaching` only when Spring Cache is selected. Let Spring Boot auto-configure Spring
Session; do not add `@EnableRedisHttpSession` or `@EnableRedisWebSession` unless the confirmed
design requires replacing Boot's auto-configuration. On the current Spring Session line, use
`spring.session.data.redis.*` for Redis-specific session settings.

## Serialization

- Use string serialization for keys.
- Prefer `JacksonJsonRedisSerializer<T>` when a cache has one known value type.
- Use `GenericJacksonJsonRedisSerializer` only when heterogeneous object values are actually
  required.
- Never use Java native serialization.
- Jackson 3 uses `tools.jackson` types. Do not use the deprecated
  `GenericJackson2JsonRedisSerializer` in new code.
- Default typing is off for `GenericJacksonJsonRedisSerializer`. If it is required, restrict it to
  trusted project DTO packages; never call `enableUnsafeDefaultTyping()`.

Example for a genuinely heterogeneous template:

```java
@Configuration(proxyBeanMethods = false)
class RedisConfig {

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        var template = new RedisTemplate<String, Object>();
        var keySerializer = new StringRedisSerializer();
        var valueSerializer = jsonSerializer();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        return template;
    }

    private RedisSerializer<Object> jsonSerializer() {
        var validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.portal.job.")
                .allowIfSubType("java.util.")
                .build();

        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(validator)
                .build();
    }
}
```

Do not create a generic `JsonMapper` bean merely to customize Redis serialization; scope Redis
serialization to its Redis configuration.

## Keys and expiry

Use stable, namespaced, versioned keys:

```text
{service}:{purpose}:v{schema}:{identifier}
job-posting:detail:v1:01J00000000000000000000000
job-posting:list:v1:status:published
identity:session:v1:{sessionId}
gateway:rate-limit:v1:{hashedClientId}
```

- Include a schema version when serialized values may evolve.
- Do not place raw secrets, tokens, email addresses, or other avoidable personal data in keys.
- Set an explicit expiry for cache, session, idempotency, lock, and rate-limit keys.
- Derive TTL from allowed staleness and recovery behavior; do not apply one global TTL blindly.
- Add bounded jitter when many related keys could otherwise expire together.
- Negative caching requires an explicitly confirmed, short TTL.

## Consistency and concurrency

- Use cache-aside: read cache, load from the authoritative port on miss, then populate the cache.
- After a successful authoritative write, evict or refresh the affected key. Do not update the
  cache before a database transaction commits.
- A cache failure must follow the confirmed fail-open or fail-closed policy. Caching normally
  fails open; sessions, locks, and rate limits require an explicit decision.
- `@Cacheable(sync = true)` only suppresses duplicate recomputation within one application
  instance. It is not a fleet-wide guarantee.
- Introduce a distributed lock only when duplicate work is both likely and materially harmful.
  Every lock needs an owner token, bounded lease, and owner-checked release.
- Implement rate-limit increment and first-expiry assignment atomically with a Redis Lua script
  or an equivalent native atomic operation. Never issue separate `INCR` and `EXPIRE` commands.

## Spring Cache example

Keep per-cache TTLs explicit and disable null caching unless negative caching was confirmed:

```java
@Bean
RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    var defaults = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaults)
            .withCacheConfiguration("jobPostings", defaults.entryTtl(Duration.ofMinutes(5)))
            .build();
}
```

Use `@Cacheable`, `@CachePut`, and `@CacheEvict` only outside Domain Core. Cache names and SpEL
keys are public operational contracts: centralize them per adapter instead of duplicating string
literals across methods.

## Tests

- Keep domain and application unit tests independent of Redis.
- Test the Redis adapter against a real Redis container; do not mock serialization, expiry, Lua,
  or transaction behavior.
- Cover key construction, JSON round-trip type, TTL, cache hit/miss, eviction after successful
  writes, concurrent rate-limit boundaries, and the confirmed Redis-unavailable policy.
- Verify correctness without assuming a warm cache. Cache loss must not corrupt authoritative
  business state.

## Review checklist

- Redis was approved by the relevant spec rather than introduced speculatively.
- Dependencies and configuration live only in the required adapter/container.
- Keys are namespaced, versioned, free of avoidable sensitive data, and have explicit expiry.
- Values use typed or safely constrained Jackson 3 JSON serialization.
- Database entities and domain aggregates are not cached.
- Write invalidation occurs only after the authoritative write succeeds.
- Multi-command concurrency paths are atomic.
- Failure mode, stampede risk, observability, and integration tests are explicit.

## References

- [Spring Data Redis reference](https://docs.spring.io/spring-data/redis/reference/)
- [Spring Data Redis 4 Jackson 3 migration](https://docs.spring.io/spring-data/redis/reference/upgrading.html)
- [Spring Session with Redis](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)
