# Security backports to the custom 5.4 branch

Baseline: `6a0d8755870b20bd7cd1e6274c0dc2d251290342` on
`backport/jet-cve-fixes-5.4`. Upstream reference: master `7e06448064`.
This is a custom backport, not an official Hazelcast patch release.

Backports completed so far: CVE-2026-107726 and GHSA-rmqf-mh9c-3gr4.
Other advisories below are not remediated on this branch. Remaining Jet path
preparations are isolated in `/tmp/hz-security-paths` and are not integrated.

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
- Baseline negative control: standalone temporary-file fixture confirmed
  `ReflectionHelper.extractValue(ReadFilesP.metaSupplier(...),
  "traverser.next", true)` reads the fixture without starting Jet.
  Reproducer: `/tmp/JetReadBaseline.java`; a durable regression test must
  prove the restricted query policy rejects this path after integration.

## Advisory ledger

| Advisory | Source commits / batch | Backport and evidence | Status |
| --- | --- | --- | --- |
| CVE-2026-107726 | 17e7422009, 361979da12, d76c6a6b65, 35befe6ef3 | fa31b943cd, 25ebe096e6, 0ca0941602, dfd9673240; adaptations 787a9b73b5, 4cf9311831 | Backported; validation below |
| GHSA-w294-6q5q-53p8 | c8a4cd9823, 715446f4fc, 3e452c98f0, a9fa45b765 | Pending | Pending |
| GHSA-jcpf-8phq-8mmj | 3d978c3b3c, 914b22f305, 960de839b7, 55f4a4a4f7, 525342c9eb, 64331b0f0e | Pending | Pending |
| GHSA-wxh9-6gx5-vrjg | 9646d046f5, 260c09e0ef, 1de94ba4b8, 02a3c5ac59, 12e0ebf476 | Pending | Pending |
| GHSA-rmqf-mh9c-3gr4 | bdc0f1bf38 | ee4046a1ac | Backported; focused validation passed |
| GHSA-74r8-7r86-phxq | 3b07d56ee7, 14c64970f6; context checks 644560ce61, 7af6ddea7e, 2bb02d562c | Mapping inferred; regression evidence required | Unverified |
| GHSA-jpwr-2cr3-32fm | 1d3b4eee45, c5cc4b386c | Pending | Pending |
| GHSA-m4hm-fvgc-9qpm | c5ec28ec3c | Pending | Pending |
| GHSA-6hfv-j8jg-ggpx | Candidate 3b07d56ee7; same context-check batch | Exact advisory mapping unconfirmed | Unverified |
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
