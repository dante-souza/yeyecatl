---
name: "quality-gates"
description: "Merge/release quality gates for Yeyecatl. Use when defining CI, reviewing completion or preparing a release."
---

# Skill — Quality Gates

## Pull request gates
A change should not merge unless relevant gates pass:

1. build succeeds
2. unit tests pass
3. Android lint/static checks pass
4. formatting passes
5. no Critical/High review finding remains
6. permission/privacy impact reviewed when applicable
7. deterministic RF logic has tests
8. Makefile entry points remain functional

## Suggested Makefile aggregation

```text
make check
```

should eventually aggregate the repository's stable local validation path.

## Release gates
In addition to PR gates:
- signed/reproducible release path documented
- release notes
- device smoke test
- permission manifest review
- export schema compatibility check when applicable
