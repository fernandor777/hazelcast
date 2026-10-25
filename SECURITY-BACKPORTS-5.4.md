# Security backports to the custom 5.4 branch

Baseline: `6a0d8755870b20bd7cd1e6274c0dc2d251290342` on
`backport/jet-cve-fixes-5.4`. Upstream reference: master `7e06448064`.
This is a custom backport, not an official Hazelcast patch release.

Backports completed so far: CVE-2026-107726, GHSA-rmqf-mh9c-3gr4,
GHSA-w294-6q5q-53p8, GHSA-wxh9-6gx5-vrjg, GHSA-m4hm-fvgc-9qpm, and
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
| GHSA-m4hm-fvgc-9qpm | c5ec28ec3c | 1abf986a80; fixture cleanup ca8230527e | Backported; official advisory currently omits 5.4 from affected ranges |
| GHSA-6hfv-j8jg-ggpx | 3b07d56ee7 (CTT-1201 Traverser block) | Existing backport 50ba3b4423; direct fixture regression and positive POJO query added | Covered; focused validation passed |
| GHSA-p2qm-f9x7-x488 | Enterprise declarative pipeline | Implementation absent from Community repository | External |

## Acceptance

Each batch requires review against upstream security behavior, compilation,
and focused positive and negative regression tests. Record substantive conflict
adaptations and omitted hunks. Shared commits are applied once. Never infer
complete remediation from commit titles alone. Environment-blocked checks must
remain explicit; Enterprise-only and unverified items are not claimed fixed.

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
  `1abf986a80`. The GitHub advisory's current affected-version list starts
  below 5.5.10 and does not list 5.4; this branch carries the requested 5.4
  hardening based on the shared H2 gadget path.

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
