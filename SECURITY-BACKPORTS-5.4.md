# Security backports to the custom 5.4 branch

Baseline: `6a0d8755870b20bd7cd1e6274c0dc2d251290342` on
`backport/jet-cve-fixes-5.4`. Upstream reference: master `7e06448064`.
This is a custom backport, not an official Hazelcast patch release.

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
- Baseline negative control: standalone temporary-file fixture confirmed
  `ReflectionHelper.extractValue(ReadFilesP.metaSupplier(...),
  "traverser.next", true)` reads the fixture without starting Jet.
  Reproducer: `/tmp/JetReadBaseline.java`; a durable regression test must
  prove the restricted query policy rejects this path after integration.

## Advisory ledger

| Advisory | Source commits / batch | Backport and evidence | Status |
| --- | --- | --- | --- |
| CVE-2026-107726 | 17e7422009, 361979da12, d76c6a6b65, 35befe6ef3 | Pending | In progress |
| GHSA-w294-6q5q-53p8 | c8a4cd9823, 715446f4fc, 3e452c98f0, a9fa45b765 | Pending | Pending |
| GHSA-jcpf-8phq-8mmj | 3d978c3b3c, 914b22f305, 960de839b7, 55f4a4a4f7, 525342c9eb, 64331b0f0e | Pending | Pending |
| GHSA-wxh9-6gx5-vrjg | 9646d046f5, 260c09e0ef, 1de94ba4b8, 02a3c5ac59, 12e0ebf476 | Pending | Pending |
| GHSA-rmqf-mh9c-3gr4 | bdc0f1bf38 | Pending | Pending |
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
