# Security backports to the custom 5.4 branch

Baseline: `6a0d8755870b20bd7cd1e6274c0dc2d251290342` on
`backport/jet-cve-fixes-5.4`. Upstream reference: master `7e06448064`.
This is a custom backport, not an official Hazelcast patch release.

Backports completed so far: CVE-2026-107726, GHSA-rmqf-mh9c-3gr4,
GHSA-w294-6q5q-53p8, GHSA-wxh9-6gx5-vrjg, GHSA-m4hm-fvgc-9qpm,
GHSA-jcpf-8phq-8mmj, GHSA-74r8-7r86-phxq, GHSA-jpwr-2cr3-32fm, and
GHSA-6hfv-j8jg-ggpx.
Other advisories below are not remediated on this branch.

## Environment and validation

- Java target: 17; installed runtime used initially: Temurin 21.0.11.
- Maven wrapper: 3.9.3; Jackson baseline: 2.14.2.
- Existing branch fixes and untracked `cp-status.md` are preserved.
- Baseline core reactor `test-compile` passed with Maven offline,
  `-T1 -pl hazelcast -am -DskipTests -Dcheckstyle.skip`.
- Baseline full `validate` stopped at an uncached
  `com.hazelcast.jet:hazelcast-jet-kafka:5.4.0` dependency in `hazelcast-sql`.
- Logs: `/tmp/hz-security-baseline.log` and
  `/tmp/hz-security-baseline-compile.log`.
- Baseline focused tests passed: 100 tests, zero failures/errors/skips
  (`ClientExceptionFactoryTest`, `SerializationClassNameFilterTest`,
  `ReflectionHelperTest`); `/tmp/hz-security-baseline-tests.log`.
- Baseline SQL reactor `test-compile` passed after allowing dependency
  resolution; `/tmp/hz-security-baseline-sql.log`.
- Initial negative control: standalone temporary-file fixture confirmed
  `ReflectionHelper.extractValue(ReadFilesP.metaSupplier(...),
  "traverser.next", true)` reads the fixture without starting Jet before the
  Traverser block was integrated. The durable regression below confirms the
  default policy now rejects the same path.

## Advisory ledger

| Advisory | Source commits / batch | Backport and evidence | Status |
| --- | --- | --- | --- |
| CVE-2026-107726 | 17e7422009, 361979da12, d76c6a6b65, 35befe6ef3 | fa31b943cd, 25ebe096e6, 0ca0941602, dfd9673240; adaptations 787a9b73b5, 4cf9311831 | Backported; validation below |
| GHSA-w294-6q5q-53p8 | c8a4cd9823, 715446f4fc, 3e452c98f0, a9fa45b765 | 5257a32136, 49569ff9ea, c5a22009d1, 47d7564c0c; adaptation edb3b44376 | Backported; focused validation passed |
| GHSA-jcpf-8phq-8mmj | 3d978c3b3c, 914b22f305, 960de839b7, 55f4a4a4f7, 525342c9eb, 64331b0f0e | 59087a168c, 08e33669a4, 8659284aa2, 3554a7c36d, dd622c2dee, f6ab3ff988; adaptation e8909c878a | Backported; focused validation passed |
| GHSA-wxh9-6gx5-vrjg | 9646d046f5, 260c09e0ef, 1de94ba4b8, 02a3c5ac59, 12e0ebf476 | 0af3999447 | Backported; focused validation passed |
| GHSA-rmqf-mh9c-3gr4 | bdc0f1bf38 | ee4046a1ac | Backported; focused validation passed |
| GHSA-74r8-7r86-phxq | 3b07d56ee7; Predicate policy hunk from 14c64970f6 | 50ba3b4423, 8c748e012a | Backported; focused validation passed |
| GHSA-jpwr-2cr3-32fm | 1d3b4eee45, c5cc4b386c | 87cb15f82c, 0148d3a060 | Backported; focused validation passed |
| GHSA-m4hm-fvgc-9qpm | c5ec28ec3c | 1abf986a80; fixture cleanup ca8230527e | Backported; advisory lists <5.5.10 as affected but no 5.4 fixed release |
| GHSA-6hfv-j8jg-ggpx | 3b07d56ee7 (CTT-1201 Traverser block) | Existing backport 50ba3b4423; direct fixture regression and positive POJO query added | Covered; focused validation passed |
| GHSA-p2qm-f9x7-x488 | Enterprise declarative pipeline | Implementation absent from Community repository | External |

## Acceptance

Each batch requires review against upstream security behavior, compilation,
and focused positive and negative regression tests. Record substantive conflict
adaptations and omitted hunks. Shared commits are applied once. Never infer
complete remediation from commit titles alone. Environment-blocked checks must
remain explicit; Enterprise-only and unverified items are not claimed fixed.

## Dependency CVE subtasks

The user-provided dependency inventory contains 45 unique CVEs, while the
[MVNRepository page for Hazelcast 5.4.0](https://mvnrepository.com/artifact/com.hazelcast/hazelcast/5.4.0)
reported 40 dependency findings when reviewed on 2026-10-24. Reconcile that
five-finding count difference with the scanner/export that produced the list.
The entries below track each supplied CVE individually and give a consolidated
upgrade target per library family.

### Scope and triage notes

- The source POM declares `tomcat-embed-core:10.1.19`, AssertJ `3.25.3`,
  PostgreSQL JDBC `42.7.2`, and Microsoft JDBC `12.6.1.jre11` in test scope.
  These do not establish a vulnerability in the published Hazelcast runtime
  dependency graph; confirm the resolved artifact/SBOM before treating them as
  production exposure.
- Log4j API/Core are provided and optional in the Hazelcast module; Log4j Core
  is also used in tests. Confirm whether the affected Log4j jars are present in
  the Hazelcast distribution or the deployment's runtime classpath.
- JLine reader/terminal are provided and optional. The POM comment says they
  are packaged in the server distribution, so verify the actual packaged jar
  and prioritize that distribution path.
- Recommended versions are upstream fixed/current versions for the associated
  dependency line, not Hazelcast release versions. Upstream guidance checked:
  [Tomcat 10.1 security fixes](https://tomcat.apache.org/security-10.html),
  [Log4j security advisories](https://logging.apache.org/security.html),
  [JLine 3.30.15 release](https://github.com/jline/jline3/releases/tag/jline-3.30.15),
  [pgjdbc advisory](https://github.com/pgjdbc/pgjdbc/security/advisories/GHSA-98qh-xjc8-98pq),
  [Microsoft JDBC release notes](https://learn.microsoft.com/en-us/sql/connect/jdbc/release-notes-for-the-jdbc-driver?view=sql-server-ver17),
  and [NVD CVE-2026-24400](https://nvd.nist.gov/vuln/detail/CVE-2026-24400).
- Status for all rows: **Open** — resolve artifact/scope applicability first;
  if present in a shipped/runtime classpath, apply or backport the vendor fix,
  then verify the dependency inventory and regression coverage. Do not mark a
  CVE fixed solely because a test-only dependency was updated.

| Subtask | CVE | Library / impact | Recommended dependency version | Current repo evidence and follow-up |
| --- | --- | --- | --- | --- |
| [ ] | CVE-2026-77420 | JLine — history regex denial of service | `org.jline` 3.30.15 or later on 3.x | POM 3.25.1, provided/optional; verify server-distribution jar and upgrade if bundled. |
| [ ] | CVE-2026-68525 | Apache Tomcat — FORM auth redirect may bypass POST-only access constraint | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-65905 | Apache Tomcat — one DIGEST-authenticated request can be replayed within the nonce window | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-65182 | Apache Tomcat — constraint ordering can bypass a more restrictive path rule | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-49844 | Apache Log4j — malformed JSON for non-finite values | `org.apache.logging.log4j` 2.26.1 or later | Provided/optional and test use; check distribution/runtime jars. |
| [ ] | CVE-2026-43515 | Apache Tomcat — security constraint bypass | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-43514 | Apache Tomcat — non-constant-time AJP secret comparison enables local-network timing attack | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-43513 | Apache Tomcat — case-sensitive LockOutRealm user matching weakens brute-force lockout | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-43512 | Apache Tomcat — DIGEST auth accepts unknown user with password `null` | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-42498 | Apache Tomcat — WebSocket client sends auth header to redirect target | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-42198 | PostgreSQL JDBC — SCRAM authentication CPU exhaustion | `org.postgresql:postgresql` 42.7.11 or later | POM 42.7.2, test scope; upgrade test/deployment copies as applicable. |
| [ ] | CVE-2026-41293 | Apache Tomcat — invalid HTTP/2 request headers reach Servlet APIs | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-41284 | Apache Tomcat — unbounded unauthenticated WebDAV LOCK/PROPFIND request bodies | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-34487 | Apache Tomcat — Kubernetes bearer token may be written to cloud-membership logs | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph and cloud membership use. |
| [ ] | CVE-2026-34483 | Apache Tomcat — JSON access-log injection with relaxed path/query characters | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-34480 | Apache Log4j Core — malformed XML layout output | `org.apache.logging.log4j` 2.26.1 or later | Provided/optional and test use; check distribution/runtime jars. |
| [ ] | CVE-2026-34478 | Apache Log4j Core — RFC 5424 layout CRLF injection | `org.apache.logging.log4j` 2.26.1 or later | Provided/optional and test use; check distribution/runtime jars. |
| [ ] | CVE-2026-34477 | Apache Log4j Core — TLS hostname verification bypass | `org.apache.logging.log4j` 2.26.1 or later | Provided/optional and test use; check distribution/runtime jars. |
| [ ] | CVE-2026-29145 | Apache Tomcat — client-certificate OCSP checks may soft-fail despite strict setting | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-25854 | Apache Tomcat — LoadBalancerDrainingValve can issue attacker-controlled redirect | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph and valve use. |
| [ ] | CVE-2026-24880 | Apache Tomcat — invalid chunk extensions can enable proxy/backend request smuggling | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-24734 | Apache Tomcat / Tomcat Native — OCSP revocation bypass | Tomcat 10.1.61+; Native 1.3.5+ or 2.0.12+ if used | Tomcat is test scope; verify whether Tomcat Native is present in any shipped image. |
| [ ] | CVE-2026-24733 | Apache Tomcat — HTTP/0.9 security constraint bypass | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2026-24400 | AssertJ — XML external entity processing | `org.assertj:assertj-core` 3.27.7 or later | POM 3.25.3, test scope; check build/test exposure only. |
| [ ] | CVE-2025-68161 | Apache Log4j Core — SocketAppender TLS hostname verification | `org.apache.logging.log4j` 2.26.1 or later | Provided/optional and test use; check distribution/runtime jars. |
| [ ] | CVE-2025-66614 | Apache Tomcat — client certificate authentication bypass | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-61795 | Apache Tomcat — multipart upload temporary files may exhaust disk before GC cleanup | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-59250 | Microsoft SQL Server JDBC — spoofing via input validation | `com.microsoft.sqlserver:mssql-jdbc` 12.6.5.jre11 or later | POM 12.6.1.jre11, test scope; update test/deployment copies as applicable. |
| [ ] | CVE-2025-55754 | Apache Tomcat — ANSI escape sequences in console logs can manipulate terminal/clipboard | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-55752 | Apache Tomcat — Rewrite Valve path traversal can bypass constraints; RCE if PUT is enabled | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph and rewrite/PUT configuration. |
| [ ] | CVE-2025-55668 | Apache Tomcat — Rewrite Valve can enable session fixation | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph and Rewrite Valve use. |
| [ ] | CVE-2025-53506 | Apache Tomcat — HTTP/2 stream resource exhaustion can cause denial of service | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-52520 | Apache Tomcat — multipart upload size-limit integer overflow can cause denial of service | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-49125 | Apache Tomcat — PreResources/PostResources alternate-path security constraint bypass | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-49124 | Apache Tomcat — Windows installer executable side-loading | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; check whether installer is distributed/used. |
| [ ] | CVE-2025-48989 | Apache Tomcat — HTTP/2 reset-stream attack can exhaust memory | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-48988 | Apache Tomcat — excessive multipart parts can exhaust memory | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-46701 | Apache Tomcat — CGI servlet security constraint bypass on case-insensitive filesystems | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph and CGI servlet use. |
| [ ] | CVE-2025-31651 | Apache Tomcat — Rewrite Valve rules may be bypassed for some configurations | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph and Rewrite Valve use. |
| [ ] | CVE-2025-31650 | Apache Tomcat — invalid HTTP priority headers can leak memory and cause denial of service | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2025-24813 | Apache Tomcat — partial PUT temporary-file path issue; conditional information disclosure or RCE | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; check write-enabled Default Servlet configuration if Tomcat is used. |
| [ ] | CVE-2024-56337 | Apache Tomcat — incomplete mitigation for write-enabled Default Servlet RCE | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; Tomcat 10.1.34+ plus Java-specific property guidance if applicable. |
| [ ] | CVE-2024-50379 | Apache Tomcat — JSP compilation TOCTOU can execute uploaded JSP on case-insensitive filesystems | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; check write-enabled Default Servlet configuration if Tomcat is used. |
| [ ] | CVE-2024-38286 | Apache Tomcat — TLS handshake abuse can cause OutOfMemoryError | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |
| [ ] | CVE-2024-34750 | Apache Tomcat — excessive HTTP/2 headers can leave connections open and cause denial of service | `tomcat-embed-*` 10.1.61 or later | POM 10.1.19, test scope; confirm runtime graph before remediation. |

### Completion criteria

For each row, record the resolved coordinates/version and scope from the 5.4.0
published POM, distribution contents, and deployment SBOM; compare against the
vendor's affected ranges; then mark **Not applicable** with evidence, or record
the upgrade/backport commit and verification evidence. Do not group away CVE
IDs when reporting completion. The declarative-pipeline finding remains
Enterprise-only and is tracked separately in the direct advisory ledger.

## CVE-2026-107726 implementation and checks

- Preserved configurable Zero Config Compact serialization restrictions,
  default blocked classes, nested restriction checking, GenericRecord fallback,
  and the upstream compatibility relaxation for explicit serializer overrides.
- Ported member/client/Spring XML schemas and the YAML JSON schema to the 5.4
  resource names; kept the 5.4 collection/mock schema-service test helpers.
- Kept the direct builder allow/block methods and combined their restrictions
  with configuration filters. Removed unavailable 5.7 builder property calls.
- Omitted unrelated 5.7 client-routing tests and corrected null-filter API docs:
  default restrictions remain active when no explicit filter is configured.
- Core production/test compilation passed (Java 17 release target).
- Focused Compact/filter/config generation/member and client XML/YAML suite:
  **713 tests, zero failures/errors, four skipped** (existing XML/YAML cases).
  Logs: `/tmp/hz-security-batch-1-compile.log`,
  `/tmp/hz-security-batch-1-tests.log`.
- Spring reactor production/test compilation passed; targeted member/client
  application-context tests passed: **91 tests, zero failures/errors/skips**.
  Logs: `/tmp/hz-security-batch-1-spring-compile.log`,
  `/tmp/hz-security-batch-1-spring-tests.log`.
- Spring follow-up fixes the client test's SerializationConfig declaration and
  updates its schema reference from 5.3 to 5.4; historical schemas are unchanged.
- Removed unused test imports and allowed original 2024 and upstream 2026
  copyright headers in the branch's Checkstyle header rule.
- Integrated source matches the tested worktree. Final affected-module
  `clean validate` passed, including Checkstyle and dependency/bytecode
  enforcement: `./mvnw -B -o -T1 -pl hazelcast,hazelcast-spring,hazelcast-spring-tests -am clean validate`.
  Log: `/tmp/hz-security-final-validate.log`. The initial full-repository
  attempt exposed the corrected header/import issues; it was not rerun for
  unrelated modules after the user limited work to the current advisory.
- Final focused totals: **804 tests run, zero failures/errors, four skips**.
  The first CVE batch was completed before resuming work on GHSA-rmqf-mh9c-3gr4.

## GHSA-rmqf-mh9c-3gr4 implementation and checks

- Ported upstream exception class validation to the 5.4 client exception
  factory: reflected classes must extend `Throwable` before construction;
  invalid classes are logged and restored as `UndefinedErrorCodeException`.
- Adapted the factory constructor and member/client call sites to inject the
  existing logging service. Added top-level and nested invalid-class tests.
- Focused `ClientExceptionFactoryTest` and
  `ClientExceptionFactoryValidationTest` passed: **95 tests, zero
  failures/errors**. Surefire reports are in
  `/tmp/hz-security-errors/hazelcast/target/surefire-reports/`.
- Upstream source: `bdc0f1bf38318c82b542a1aeb9e573efc77855e2`; backport:
  `ee4046a1ac`.

## GHSA-w294-6q5q-53p8 implementation and checks

- Added a configurable reflective attribute lookup policy with controlled
  access as the default, including blocks for sensitive classes and static or
  void accessors. Added URL to the default blocklist, wrapped policy failures
  as `QueryException`, and verified array component classes.
- Added HazelcastProperties plumbing required to select the policy while
  preserving the 5.4 direct serialization allowlist/blocklist hooks and
  secure default ReflectionHelper overloads. Removed unused builder methods
  that exist only in newer Hazelcast versions.
- Core reactor `test-compile` passed. Focused reflection, policy, extractor,
  predicate, and projection tests passed: **157 tests, zero failures/errors
  or skips**. Logs: `/tmp/hz-security-predicate-compile.log` and
  `/tmp/hz-security-predicate-tests.log`.
- Upstream commits: `c8a4cd982369d564788028370b7ed0d64b366eb8`,
  `715446f4fc03257e3878aea29ee58f8ea4b82808`,
  `3e452c98f00a99bc870fdae4a6b381edab491761`, and
  `a9fa45b765111164495b201038eaca38bb777399`.

## GHSA-wxh9-6gx5-vrjg implementation and checks

- Added the Jackson Jr reader/writer class-filter extension. The default
  blocklist rejects Hazelcast internal/configuration and Spring context/beans
  types plus all `DataSource` implementations during JSON reads and writes.
  The existing system property remains the explicit opt-out.
- Adapted Jackson Jr 3 extension APIs to the branch's Jackson Jr 2.14.2 API
  without changing dependency versions. Removed the built-in serializable
  MongoDB and Hadoop mapper closures; explicit Mongo mapping functions remain
  supported. No public 5.7-only helper API was added.
- JSON security and existing `JsonUtil` tests passed: **31 tests, zero
  failures/errors**. Mongo mapper tests passed: **2 tests, zero
  failures/errors**. Offline MongoDB/Hadoop reactor test compilation and a
  final Hadoop/core compile passed.
- The upstream Mongo change removes serializable built-in mapper closures;
  it does not add a separate BSON class blocklist. BSON POJO decoding remains
  explicit through the configured Mongo codec.
- Source commits: `9646d046f59eab323cd30d7db0a7238cdcd1bd8d`,
  `260c09e0ef175d1fce1039032c2db3743eaa1f14`,
  `1de94ba4b8c9e8f319afc3377b08744118acb6fb`,
  `02a3c5ac5960edea15042dfc184b41f877875ff3`, and
  `12e0ebf476e765394730c251e5d291d20c6ce90c`. Backport: `0af3999447`.

## GHSA-m4hm-fvgc-9qpm implementation and checks

- Activated Java serialization filtering when no explicit filter config is
  supplied. The default blacklist rejects `org.h2.*`,
  `org.springframework.beans.*`, and Jackson databind introspection classes;
  the existing `defaultsDisabled` setting remains the opt-out. The common
  serialization builder wires the filter into member and client services.
- Added member/client H2 deserialization regressions and retained the benign
  class round-trip. The existing H2 JDBC SQL connector test opts out because
  it uses H2 classes for its connector test path.
- Core compile/test-compile passed. Focused H2 and filter tests passed:
  **12 tests, zero failures/errors**. `InsertJdbcSqlConnectorTest` passed:
  **8 tests, zero failures/errors**. Logs: `/tmp/hz-security-h2-compile.log`,
  `/tmp/hz-security-h2-tests.log`, and `/tmp/hz-security-h2-sql-tests.log`.
- Upstream source: `c5ec28ec3cddc529f7e47e3a3184db083a59cf0d`; backport:
  `1abf986a80`. The GitHub advisory lists `<5.5.10` as affected, which
  includes 5.4, but does not list a fixed 5.4 release.

## GHSA-jcpf-8phq-8mmj implementation and checks

- Restricted `JSON_QUERY` target classes; applied the reflective filter
  during SQL UDT reconstruction; rejected fieldless custom `QueryDataType`
  deserialization; used the default SQL blocklist when no explicit filter is
  configured; checked the resolved UDT class name; and checked SQL availability
  before decoding request parameters.
- Adapted test imports/helpers to Hazelcast 5.4. SQL reactor test compilation
  passed. Focused reflective filter, SQL UDT/default/malformed-type/JSON and
  SQL-disabled tests passed: **84 tests**. POJO and Hazelcast object upsert
  reconstruction tests passed: **16 tests**. Total: **100 tests, zero
  failures/errors/skips**.
- Upstream commits: `3d978c3b3c` (`JsonQueryTarget` target restrictions),
  `914b22f305` (UDT filtering), `960de839b7` (fieldless custom type guard),
  `55f4a4a4f7` (default SQL filter), `525342c9eb` (resolved UDT name), and
  `64331b0f0e` (SQL-enabled check before parameter decoding). Backport commits
  begin at `59087a168c`; 5.4 test adaptation: `e8909c878a`.

## GHSA-74r8-7r86-phxq implementation and checks

- Extended the controlled reflective lookup policy to block Jet `Traverser`
  and Jackson databind introspection classes, then added the Predicate API
  block for `org.springframework.beans.*`. The latter is the Predicate
  policy hunk from a larger upstream commit; unrelated SQL and compact
  serialization hunks were not needed for this advisory.
- Added negative tests for Traverser and Spring Beans, a positive
  full-access-policy test, and adapted Jackson `MemberKey` to the 5.4
  Jackson 2 package.
- Focused `ReflectionHelperTest` passed: **29 tests, zero failures/errors
  or skips**. Surefire report:
  `/tmp/hz-predicate-api/hazelcast/target/surefire-reports/TEST-com.hazelcast.query.impl.getters.ReflectionHelperTest.xml`.
- Upstream sources: `3b07d56ee70e5fc7767b6d6a3510a438dc0991e6` and the
  Predicate policy hunk from `14c64970f64bc8cf608510ef7228bd0e18c70cf5`.
  Backports: `50ba3b4423` and `8c748e012a`.

## GHSA-jpwr-2cr3-32fm implementation and checks

- Validate custom classpath paths against their trusted base and reject
  absolute/rooted, traversal, and symlink escapes. Hardened `rollByDate` file
  initialization so the formatted date path is checked before writing.
- Adapted 5.4 logger, Jackson Jr import, and job status assertion APIs.
  `resolveInDir` now uses the symlink-aware resolver because lexical path
  containment alone allowed an intermediate symlink to escape the base.
- Core main/test compilation passed. `IOUtilPathValidationTest` passed
  **26 tests** (two Windows-only skips); `WriteFilePTest` passed **20 tests**.
- Upstream sources: `1d3b4eee45ade5ee1ab30659ec2201d8da7066a8` and
  `c5cc4b386c389ad5797d0aa60f5dcccea990d7b5`. Backports:
  `87cb15f82c` and `0148d3a060`.

## GHSA-6hfv-j8jg-ggpx implementation and checks

- No additional production change was needed. The existing default reflective
  lookup policy rejects `Traverser` implementations; backport `50ba3b4423`
  carries upstream commit `3b07d56ee70e5fc7767b6d6a3510a438dc0991e6`.
- Added a regression using `ReadFilesP.metaSupplier` over a temporary harmless
  file. Full-access reflection reads its contents via `traverser.next`, while
  the default `ReflectionHelper.extractValue(..., "traverser.next", true)`
  rejects the path. A separate positive test confirms normal POJO fields remain
  queryable.
- Focused `ReflectionHelperTest` passed: **31 tests, zero failures/errors or
  skips**. Main and test compilation passed with Java 21 targeting release 17.
- The regression and positive case are a test/ledger-only change; no Jet
  file-read production paths or other advisories were changed here.

## Dependency CVEs: severity-ordered report

This report covers the 45 dependency CVEs supplied for Hazelcast 5.4.0. Current
versions below are those declared in the Hazelcast 5.4.0 source POMs. The
Tomcat, database-driver, and AssertJ versions shown here are test-scoped.
Log4j is provided/optional, while JLine is provided/optional and noted in the
POM as packaged in the server distribution. Confirm the published artifact,
distribution contents, and deployment SBOM before treating each finding as
production exposure.

Severity is ordered by CVSS where cited. For Tomcat CVEs where this report
does not give a CVSS category, it maps the Tomcat project's impact labels as
Important → High, Moderate → Medium, and Low → Low. The Tomcat project records
its impact levels and fixed releases in its [Tomcat 10 security advisories](https://tomcat.apache.org/security-10.html);
the current Tomcat 10.1 release is [10.1.61](https://tomcat.apache.org/).

### Critical

| Severity | CVE | Library (current version) | Suggested upgrade |
| --- | --- | --- | --- |
| Critical (CVSS 9.8) | [CVE-2024-50379](https://nvd.nist.gov/vuln/detail/CVE-2024-50379) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Critical (CVSS 9.8) | [CVE-2024-56337](https://nvd.nist.gov/vuln/detail/CVE-2024-56337) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61`; check Java-specific configuration if applicable |
| Critical (CVSS 9.8) | [CVE-2025-24813](https://nvd.nist.gov/vuln/detail/CVE-2025-24813) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |

The Tomcat CVEs above have configuration-dependent exploit conditions; CVSS
classifies them as Critical. See the [CVE-2025-24813 NVD detail](https://nvd.nist.gov/vuln/detail/CVE-2025-24813),
[CVE-2024-50379 NVD detail](https://nvd.nist.gov/vuln/detail/CVE-2024-50379),
and [CVE-2024-56337 advisory](https://security.netapp.com/advisory/ntap-20250103-0002/).

### High

| Severity | CVE | Library (current version) | Suggested upgrade |
| --- | --- | --- | --- |
| High (CVSS 8.2) | [CVE-2026-24400](https://nvd.nist.gov/vuln/detail/CVE-2026-24400) | AssertJ (`3.25.3`, test scope) | `3.27.7` or later |
| High (CVSS 8.2; Tomcat: Important) | [CVE-2026-65182](https://nvd.nist.gov/vuln/detail/CVE-2026-65182) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (CVSS 7.5) | [CVE-2026-42198](https://nvd.nist.gov/vuln/detail/CVE-2026-42198) | PostgreSQL JDBC (`42.7.2`, test scope) | `42.7.11` or later |
| High | [CVE-2025-59250](https://nvd.nist.gov/vuln/detail/CVE-2025-59250) | Microsoft SQL Server JDBC (`12.6.1.jre11`, test scope) | `12.6.5.jre11` or later |
| High (Tomcat: Important) | [CVE-2025-55752](https://nvd.nist.gov/vuln/detail/CVE-2025-55752) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (Tomcat: Important) | [CVE-2025-53506](https://nvd.nist.gov/vuln/detail/CVE-2025-53506) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (Tomcat: Important) | [CVE-2025-48989](https://nvd.nist.gov/vuln/detail/CVE-2025-48989) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (Tomcat: Important) | [CVE-2025-48988](https://nvd.nist.gov/vuln/detail/CVE-2025-48988) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (Tomcat: Important) | [CVE-2025-31650](https://nvd.nist.gov/vuln/detail/CVE-2025-31650) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (Tomcat: Important) | [CVE-2024-38286](https://nvd.nist.gov/vuln/detail/CVE-2024-38286) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| High (Tomcat: Important) | [CVE-2024-34750](https://nvd.nist.gov/vuln/detail/CVE-2024-34750) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |

AssertJ's NVD record rates CVE-2026-24400 High and lists 3.27.7 as fixed.
The [pgjdbc advisory](https://github.com/pgjdbc/pgjdbc/security/advisories/GHSA-98qh-xjc8-98pq)
rates CVE-2026-42198 High and fixes it in 42.7.11. Microsoft's [JDBC driver
release notes](https://learn.microsoft.com/en-us/sql/connect/jdbc/release-notes-for-the-jdbc-driver?view=sql-server-ver17)
list the fixed 12.6.5 line for CVE-2025-59250.

### Medium

| Severity | CVE | Library (current version) | Suggested upgrade |
| --- | --- | --- | --- |
| Medium (CVSS 5.5) | [CVE-2026-77420](https://nvd.nist.gov/vuln/detail/CVE-2026-77420) | JLine (`3.25.1`, provided/optional) | `3.30.15` or later on 3.x |
| Medium (CVSS 4.0 6.3) | [CVE-2026-49844](https://nvd.nist.gov/vuln/detail/CVE-2026-49844) | Log4j API (`2.23.0`, provided/optional) | Log4j `2.26.1` or later |
| Medium (Tomcat: Moderate) | [CVE-2026-43515](https://nvd.nist.gov/vuln/detail/CVE-2026-43515) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Medium (Tomcat: Moderate) | [CVE-2026-43512](https://nvd.nist.gov/vuln/detail/CVE-2026-43512) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Medium (CVSS 4.0 6.9) | [CVE-2026-34480](https://nvd.nist.gov/vuln/detail/CVE-2026-34480) | Log4j Core (`2.23.0`, provided/optional) | Log4j `2.26.1` or later |
| Medium (CVSS 4.0 6.9) | [CVE-2026-34478](https://nvd.nist.gov/vuln/detail/CVE-2026-34478) | Log4j Core (`2.23.0`, provided/optional) | Log4j `2.26.1` or later |
| Medium (CVSS 4.0 6.3) | [CVE-2026-34477](https://nvd.nist.gov/vuln/detail/CVE-2026-34477) | Log4j Core (`2.23.0`, provided/optional) | Log4j `2.26.1` or later |
| Medium (Tomcat: Moderate) | [CVE-2026-29145](https://nvd.nist.gov/vuln/detail/CVE-2026-29145) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Medium (Tomcat: Moderate) | [CVE-2026-24734](https://nvd.nist.gov/vuln/detail/CVE-2026-24734) | Apache Tomcat (`10.1.19`, test scope); Tomcat Native not declared | Tomcat `10.1.61`; if Native is used, `1.3.5+` or `2.0.12+` |
| Medium (Tomcat: Moderate) | [CVE-2025-66614](https://nvd.nist.gov/vuln/detail/CVE-2025-66614) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Medium (Tomcat: Moderate) | [CVE-2025-55668](https://nvd.nist.gov/vuln/detail/CVE-2025-55668) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Medium (Tomcat: Moderate) | [CVE-2025-49125](https://nvd.nist.gov/vuln/detail/CVE-2025-49125) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Medium (CVSS 4.0 6.3) | [CVE-2025-68161](https://nvd.nist.gov/vuln/detail/CVE-2025-68161) | Log4j Core (`2.23.0`, provided/optional) | Log4j `2.26.1` or later |

JLine's fix is 3.30.15. Log4j lists fixes across 2.25.x and 2.26.x, so 2.26.1
covers this set. The Log4j CVSS 4.0 ratings are listed on the [Apache Log4j
security advisories](https://logging.apache.org/security.html); JLine details
are in the [CVE-2026-77420 advisory](https://access.redhat.com/security/cve/cve-2026-77420).

### Low

| Severity | CVE | Library (current version) | Suggested upgrade |
| --- | --- | --- | --- |
| Low (Tomcat: Low) | [CVE-2026-68525](https://nvd.nist.gov/vuln/detail/CVE-2026-68525) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-65905](https://nvd.nist.gov/vuln/detail/CVE-2026-65905) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-43514](https://nvd.nist.gov/vuln/detail/CVE-2026-43514) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-43513](https://nvd.nist.gov/vuln/detail/CVE-2026-43513) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-42498](https://nvd.nist.gov/vuln/detail/CVE-2026-42498) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-41293](https://nvd.nist.gov/vuln/detail/CVE-2026-41293) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-41284](https://nvd.nist.gov/vuln/detail/CVE-2026-41284) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-34487](https://nvd.nist.gov/vuln/detail/CVE-2026-34487) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-34483](https://nvd.nist.gov/vuln/detail/CVE-2026-34483) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-25854](https://nvd.nist.gov/vuln/detail/CVE-2026-25854) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-24880](https://nvd.nist.gov/vuln/detail/CVE-2026-24880) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2026-24733](https://nvd.nist.gov/vuln/detail/CVE-2026-24733) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2025-61795](https://nvd.nist.gov/vuln/detail/CVE-2025-61795) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2025-55754](https://nvd.nist.gov/vuln/detail/CVE-2025-55754) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2025-52520](https://nvd.nist.gov/vuln/detail/CVE-2025-52520) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2025-49124](https://nvd.nist.gov/vuln/detail/CVE-2025-49124) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2025-46701](https://nvd.nist.gov/vuln/detail/CVE-2025-46701) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |
| Low (Tomcat: Low) | [CVE-2025-31651](https://nvd.nist.gov/vuln/detail/CVE-2025-31651) | Apache Tomcat (`10.1.19`, test scope) | Tomcat `10.1.61` |

Current versions are declared in the [root POM](pom.xml), [Hazelcast POM](hazelcast/pom.xml),
and [SQL module POM](hazelcast-sql/pom.xml). The CVEs and suggested versions
should be marked remediated only after verifying actual resolved coordinates
and scopes in the published artifact, distribution, and deployment SBOM.
