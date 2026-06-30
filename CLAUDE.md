# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`cust-manage-server` — "一企一册" (one-enterprise-one-book) customer management backend. Spring Boot 3.3.6 / Java 17 / MyBatis / **OceanBase in Oracle mode**. REST API under `/api/**` serving a Vue front-end.

**Front-end repo (separate directory):** `C:\Users\Administrator\vscode\cust_manage_web` — Vue 3 + Vite + Element Plus + vue-router + pinia + axios + echarts. Not inside this backend repo; edit it by absolute path. Run `npm run dev` there.

## Commands

No Maven wrapper is checked in — use the system `mvn`.

```bash
mvn compile                      # compile
mvn spring-boot:run              # run locally (port 8080, needs DB network access)
mvn test                         # run all tests
mvn test -Dtest=GroupQueryControllerTest                 # single test class
mvn test -Dtest=GroupQueryControllerTest#returns_group_detail_by_id   # single method
mvn package                      # build executable jar in target/
```

Running the app requires reaching the remote OceanBase instance configured in `src/main/resources/application.yml`. JWT secret is random per restart unless `JWT_SECRET` is set (setting it is what keeps issued tokens valid across restarts).

## Architecture

Standard layered: `controller → service interface (I*Service) → service impl (*ServiceImpl) → mapper interface → mapper XML`. Mappers are MyBatis interfaces in `mapper/` backed by XML in `src/main/resources/mapper/`. `map-underscore-to-camel-case: true`, so DB `snake_case` maps to Java `camelCase` automatically — don't hand-map column aliases unless renaming.

### Request pipeline (two interceptors, `config/WebMvcConfig.java`)

1. **`AuthInterceptor`** (order 1) — every `/api/**` path except the whitelist (`/api/auth/login`, `/api/auth/oauth`, `/api/health`). Parses the `Authorization: Bearer <jwt>` token via `IAuthService`, builds a `LoginUser`, and stores it in the `RequestContext` ThreadLocal. **Always cleared in `afterCompletion`.**
2. **`PermissionInterceptor`** (order 2) — only acts on controller methods annotated `@RequirePermission("code")`. Resolves the caller's permissions from `MenuMapper.selectPermissionCodesByRoleCodes(roles)` and 403s if the code isn't present.

Code that needs the current user reads it through `RequestContext.currentUserId()` / `.currentOrgId()` / `.currentRoles()` — never parse the token yourself.

### Row-level data scope (the central cross-cutting pattern)

Queries filter rows by who the caller is. The flow spans four layers, so changes must be made consistently:

1. `IDataScopeService.resolveDataScope(userId, orgId, roleCodes)` → a `DataScopeContext` with a `scopeType` of **`本人` / `本网格` / `本部门` / `全部`** (merged from the caller's roles' `t_yqyc_sys_data_scope` rows, most-permissive wins: 全部 > 本部门 > 本网格 > 本人). For `本部门` it also pre-computes the `deptOrgIds` list (org subtree under the nearest `部门` ancestor).
2. The service **injects** that context into the request DTO, e.g. `GroupQueryServiceImpl.applyDataScope(request)` sets `request.setDataScope(scope)`. Query DTOs (`dto/*QueryRequest`) carry a `dataScope` field for exactly this.
3. The mapper XML branches on it. See the `GroupFilter` `<sql>` in `GroupMapper.xml` for the canonical pattern — `本人` filters by `manager_user_id`, `本网格` by `bu_org_id`, `本部门` by `bu_org_id IN (...)`, `全部` adds nothing.

When adding a new queryable resource, replicate this: give the DTO a `dataScope` field, call `applyDataScope` in the service, and add the matching `<if test="query.dataScope.scopeType == ...">` branches to its mapper `<sql>` filter.

### Response envelope (`common/`)

- Every endpoint returns `ApiResponse<T>` = `{ code, message, data }`. **`code == 0` means success**; non-zero is an error code. Paged lists return `PageResponse<T>` = `{ total, records }` inside `data`.
- `GlobalExceptionHandler` maps: `ResourceNotFoundException`→HTTP 404/code 404; validation errors→HTTP 400/code 400; **`BusinessException`→HTTP 200 with the business error code** (throw `new BusinessException(code, msg)` for expected domain failures, *not* a 5xx).
- Don't swallow the envelope in new endpoints — return `ApiResponse.ok(...)`.

### Auth strategy

`AuthServiceImpl.login()` delegates to the `AuthStrategy` whose `supportedType() == LOCAL` (currently only `LocalAuthStrategy`). OA/SSO (`AuthStrategy.AuthType.OA`) is scaffolded but `auth.oa.enabled=false` — adding real SSO means a new `AuthStrategy` bean, no controller changes.

## Database conventions (important)

Target DB is **OceanBase Oracle mode**. All mappers must use Oracle syntax — see `docs/mapper-oracle-migration.md` for the full MySQL→Oracle conversion reference. In practice:

- Current time: `SYSDATE` (not `NOW()`).
- String concat: `'%' || #{kw} || '%'` (not `CONCAT(...)` — Oracle's `CONCAT` is 2-arg only).
- Conditional: `CASE WHEN ... THEN ... ELSE ... END` (not `IF()`).
- Pagination: **ROWNUM-nested subqueries** (not `LIMIT/OFFSET`). Compute `endRow = limit + offset` in Java (`<bind name="endRow" value="limit + offset"/>`) — you cannot do `#{limit} + #{offset}` arithmetic inside the SQL. See `GroupMapper.xml selectGroups` for the three-CTE template (page IDs → number → join).
- All tables are prefixed `t_yqyc_` and live in schema `szzw` (referenced as `szzw.t_yqyc_...`). Three domains: **`sys_`** (org/user/role/menu/data_scope/login_log), **`crm_`** (group/contact/opportunity), **`biz_`** (service/revenue/bc/competitor/bu_performance/ticket).
- `||` on a NULL operand yields NULL (unlike MySQL `CONCAT`) — wrap nullable columns in `NVL(col, '')` / `COALESCE`.
- **Map-result alias casing**: any `<select resultType="map">` must **double-quote its column aliases** (`AS "buName"`, not `AS buName`). In Oracle mode the JDBC driver uppercases unquoted aliases, so `AS buName` becomes the Map key `BUNAME` and the frontend (and any service-layer `map.get("buName")`) can't read it. Quoting preserves the case. Consequence: a query that sorts by such an alias can't do `ORDER BY amount` (it'd resolve to `AMOUNT` ≠ `"amount"`) — order by the underlying expression instead (`ORDER BY SUM(col) DESC`). For typed `resultType="...VO"` results this doesn't apply; `mapUnderscoreToCamelCase` handles those via bean properties.

The dashboard module's revenue queries were migrated from `t_yqyc_biz_group_revenue_monthly` to `t_yqyc_biz_bu_performance` (direct `bu_org_id` dimension, no group-table JOIN) — see `docs/dashboard-bu-performance-migration.md`. `DashboardServiceImpl` also hardcodes a few `service_id`→business mappings (1=专线, 12=语音专线, 9=ICT签约, 10=宽带渗透率, 11=关联人占比) that must match `t_yqyc_biz_service` data.

## Tests (mind the divergence)

Tests use `@ActiveProfiles("test")` → an **in-memory H2 in `MODE=MySQL`** seeded by `src/test/resources/schema.sql` + `data.sql`, exercised via `MockMvc` (no DB network needed). **Note the divergence:** the prod mapper XMLs now use Oracle syntax (above), while the H2 test profile is MySQL-compatibility, so Oracle-only constructs (`ROWNUM`, `SYSDATE`, `||`) may not exercise correctly under H2. The existing test classes cover only the group/contact/opportunity/group-detail query endpoints.

## Data seeding tooling

`data/组织与权限域.xlsx` holds seed org/user/role data. Two generators produce Oracle-compatible `INSERT` SQL (or execute over JDBC directly):

- `src/main/java/com/custmanage/server/sync/OrgPermissionSync.java` — standalone Java `main` (Apache POI). Run as documented in its class Javadoc; outputs `data_sync/insert_org_permission_oracle.sql`.
- `data_sync/sync_org_permission.py` — Python (`openpyxl`) generator of both Oracle and MySQL variants.

Passwords are seeded as `MD5('cust_manage_salt' + '123456')` — the salt and scheme are hardcoded in `LocalAuthStrategy` and must stay in sync with the sync tool. Output SQL is idempotent (DELETE in FK-reverse order, then INSERT in FK-forward order), wrapped in a PL/SQL anonymous block for atomicity.

## Docs

- `docs/一企一册系统功能设计说明书.md` — full functional design spec (Chinese).
- `docs/mapper-oracle-migration.md` — MySQL→Oracle mapper conversion reference.
- `docs/dashboard-bu-performance-migration.md` — dashboard revenue-source migration.
