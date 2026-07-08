# ADR-0001: RepairOps-LLM ⊣ Repair Shop Governor architecture (household appliances)

## Status

Accepted. `cloud-itonami-isic-9522` promoted from `:blueprint` to
`:implemented` in the `kotoba-lang/industry` registry.

## Context

`cloud-itonami-isic-9522` publishes an OSS business blueprint for
repair of household appliances and home and garden equipment:
diagnosing and repairing washing machines, lawn mowers and similar
equipment for customers. Like every prior actor in this fleet, the
blueprint alone is not an implementation: this ADR records the
governed-actor architecture that promotes it to real, tested code,
following the same langgraph StateGraph + independent Governor +
Phase 0→3 rollout pattern established by `cloud-itonami-isic-6511`
(life insurance) and applied across seventy-six prior siblings, most
recently `cloud-itonami-isic-9512` (repair of communication
equipment).

This is the SECOND build to follow the fleet-wide precedent
`commrepair`/9512's own ADR-0001 established: deliberately sharing a
governor name with an already-implemented sibling (`repairshop`/9521)
when the underlying business archetype is genuinely the same, rather
than treating an exact governor-name match as automatically
disqualifying.

## Decision

### Decision 1: sharing a governor name with TWO already-implemented siblings

`cloud-itonami-isic-9522`'s own `blueprint.edn` declares
`:itonami.blueprint/governor :repair-shop-governor` -- the IDENTICAL
keyword `repairshop`/9521 (consumer electronics) AND `commrepair`/
9512 (communication equipment) already use. Following `commrepair`/
9512's own ADR-0001 Decision 1, this build treats the shared name as
an honest reflection of the SAME "repair shop" business archetype,
not a naming error, and documents it explicitly here and in this
repo's own README (rather than silently duplicating). This is the
SECOND actor in this fleet to make this specific judgment call,
following `commrepair`/9512's precedent-setting decision -- not
itself a new precedent, but a confirmation that the precedent
generalizes across more than one additional sibling.

### Decision 2: architecture mirrors `repairshop`/9521 and `commrepair`/9512 closely, with ONE genuinely new check

Given all three blueprints' business-model.md texts are near-
identical (dual-actuation "performing a repair or returning a [device/
appliance/item]", the SAME `repairshop.facts`-style consumer-product-
safety catalog shape), this build deliberately reuses the entity/op
shape (`ticket`; `:ticket/intake`, `:jurisdiction/assess`, `:safety/
screen`, `:repair/complete`, `:appliance/return`) and the `parts-cost-
matches-claim?`/`safety-test-not-passed` checks HONESTLY, as literal
reuses of the SAME real-world concerns for the SAME business
archetype -- not claimed as new. The genuinely NEW contribution is
Decision 3 below, grounded in a real, distinct regulatory concern
specific to household appliances that neither `repairshop`/9521's nor
`commrepair`/9512's own catalogs model.

### Decision 3: `refrigerant-handling-certification-unconfirmed-violations` -- the 62nd unconditional-evaluation screening grounding, a genuinely new concept

This blueprint's own named activity ("household appliances and home
and garden equipment") is broad enough to include refrigerant-
containing major appliances (refrigerators, freezers, window/portable
air conditioners) alongside its own named examples (washing machines,
lawn mowers). Servicing equipment containing fluorinated refrigerants
requires a CERTIFIED technician in every major jurisdiction -- the US
EPA's Section 608 Technician Certification under the Clean Air Act is
one of the most well-documented, longest-standing (in force since
1993) technician-certification regimes in consumer-product-repair
law. Before writing this check, every prior sibling's governor/
registry/facts namespaces were grepped for "refrigerant", "epa-608",
"f-gas" and "フロン" -- zero hits, confirming this is a genuinely new
unconditional-evaluation concept. It reuses the unconditional-
evaluation DISCIPLINE (`casualty.governor/sanctions-violations`'s
original fix) for the 62nd distinct application overall, continuing
the count established across this fleet's builds (most recently
`commrepair.governor/customer-data-consent-unconfirmed-violations` at
61st). Grounded in real refrigerant-handling-certification law: US
Clean Air Act Section 608 (40 CFR Part 82), UK Fluorinated Greenhouse
Gases Regulations 2015 (F-Gas Regulations), Germany's Chemikalien-
Klimaschutzverordnung (ChemKlimaschutzV), Japan's フロン排出抑制法.
Gates `:refrigerant/screen` and BOTH actuation ops (`:repair/
complete`/`:appliance/return` -- refrigerant handling can occur
either during diagnostic/repair work or immediately before the
appliance changes hands back to the customer).

### Decision 4: entity and op shape

The primary entity is a `ticket` (matching `repairshop`/9521's and
`commrepair`/9512's own entity name exactly, since it is the SAME
"repair ticket" concept). Six ops: `:ticket/intake` (directory
upsert, no capital risk), `:jurisdiction/assess` (per-jurisdiction
consumer-product-safety/refrigerant-handling evidence checklist,
never auto), `:safety/screen` (post-repair safety-test screening,
unconditional-evaluation discipline, never auto), `:refrigerant/
screen` (refrigerant-handling-certification screening, unconditional-
evaluation discipline, never auto), `:repair/complete` (POSITIVE,
high-stakes) and `:appliance/return` (POSITIVE, high-stakes) -- a
genuine DUAL-actuation shape on the same entity, matching
`repairshop`/9521's and `commrepair`/9512's own shape exactly.

### Decision 5: dedicated double-actuation-guard booleans

`:repair-completed?` and `:appliance-returned?` are dedicated
booleans on the `ticket` record, never a single `:status` value --
the same discipline `repairshop.governor`'s and `commrepair.
governor`'s own guards establish, informed by `cloud-itonami-isic-
6492`'s status-lifecycle bug (ADR-2607071320).

### Decision 6: Store protocol, MemStore + DatomicStore parity

`applianceshop.store/Store` is implemented by both `MemStore` (atom-
backed, default for dev/tests/demo) and `DatomicStore` (`langchain.
db`-backed), proven to satisfy the same contract in `test/
applianceshop/store_contract_test.clj` -- the same seam every
sibling actor uses so swapping the SSoT backend is a configuration
change, not a rewrite.

### Decision 7: Phase 0→3 rollout

Phase 3's `:auto` set has exactly one member, `:ticket/intake` (no
capital risk). `:jurisdiction/assess`, `:safety/screen` and
`:refrigerant/screen` are never auto-eligible at any phase (matching
every sibling's screening/verification-op posture), and BOTH
`:repair/complete` and `:appliance/return` are permanently excluded
from every phase's `:auto` set -- a structural fact, not a rollout
milestone, enforced by BOTH `applianceshop.phase` and `applianceshop.
governor`'s `high-stakes` set independently.

### Decision 8: no bespoke domain capability lib

This blueprint's own `:itonami.blueprint/required-technologies`
names no domain-specific capability beyond the generic robotics/
identity/forms/dmn/bpmn/audit-ledger stack -- there was no
capability-lib decision to make at all.

### Decision 9: mock + LLM advisor pair

`applianceshop.repairopsllm` provides `mock-advisor` (deterministic,
default everywhere -- the actor graph and governor contract run
offline) and `llm-advisor` (backed by `langchain.model/ChatModel`,
with a defensive EDN-proposal parser so a malformed LLM response
degrades to a safe low-confidence noop rather than ever auto-
completing a repair or auto-returning an appliance).

### Decision 10: no `blueprint.edn` field-sync fixes needed

Matching `photo`/7420's, `personalservice`/9609's, `edsupport`/8550's,
`headoffice`/7010's, `residential`/8790's, `cultural`/8542's,
`reserve`/6411's, `proserv`/7490's, `sportsevent`/9319's,
`recreation`/9329's, `sportsclub`/9312's, `partyops`/9492's,
`memberorg`/9499's and `commrepair`/9512's own experience, this
repo's `blueprint.edn` already had the correct `isic-` prefixed `:id`
and correctly populated `:required-technologies`/`:optional-
technologies` matching the `kotoba-lang/industry` registry's own
entry for `"9522"` exactly -- only the `:maturity` field itself
needed adding.

## Alternatives considered

- **Treating the shared governor name as blocked.** Rejected: this
  vertical is the second confirmation of `commrepair`/9512's own
  precedent (a genuinely new, well-grounded check was available; no
  governor-name-uniqueness constraint exists in this codebase).
- **Reusing `repairshop`/9521's and `commrepair`/9512's checks
  verbatim with no new contribution.** Rejected: this blueprint's own
  broad activity scope (including refrigerant-containing major
  appliances) signals a real, load-bearing regulatory concern specific
  to household-appliance repair; ignoring it would waste a legitimate
  differentiation opportunity.
- **Grounding the new check in small-engine emissions law instead**
  (relevant to the lawn-mower example specifically). Considered, but
  rejected in favor of refrigerant-handling certification: EPA
  Section 608-style certification is a far more universally
  recognized, well-documented "you need certification to touch this"
  regime in consumer-appliance-repair law than small-engine emissions
  standards (which govern manufacturing/sale more than repair
  practice).

## Consequences

- Seventy-eighth actor promoted in this fleet's registry (77
  implemented before this build).
- Establishes a genuinely NEW unconditional-evaluation-screening
  concept (refrigerant-handling-certification-unconfirmed), grep-
  verified absent from every prior sibling before the claim was
  finalized.
- Documents two honest, literal reuses of `repairshop`/9521's own
  checks for the SAME real-world concerns, not claimed as new.
- `MemStore` ‖ `DatomicStore` parity is proven by `test/
  applianceshop/store_contract_test.clj`.
- `blueprint.edn` required no field-sync fixes this time (already
  correct) -- only the `:maturity` flip itself.
- **Confirms and generalizes the fleet-wide precedent** `commrepair`/
  9512's own ADR-0001 established: a shared governor name across
  siblings, when the underlying business archetype is genuinely the
  same, is acceptable and should be documented explicitly. This keeps
  `9523`/`9524`/`9529` (the remaining repair-shop candidates) open for
  future vertical-selection turns, PROVIDED each brings its own
  genuinely differentiated check.

## References

- `orgs/cloud-itonami/cloud-itonami-isic-9522/README.md`
- `orgs/cloud-itonami/cloud-itonami-isic-9522/docs/business-model.md`
- `orgs/cloud-itonami/cloud-itonami-isic-9521/src/repairshop/governor.cljc` (`parts-cost-matches-claim?`/`safety-test-not-passed` origin)
- `orgs/cloud-itonami/cloud-itonami-isic-9512/docs/adr/0001-architecture.md` (governor-name-reuse precedent origin)
- `orgs/kotoba-lang/industry/resources/kotoba/industry/registry.edn` (entry `"9522"`)
