# Business Model: Repair of household appliances and home and garden equipment

## Classification

- Repository: `cloud-itonami-isic-9522`
- ISIC Rev.5: `9522`
- Activity: repair of household appliances and home and garden equipment -- diagnosing and repairing washing machines, lawn mowers and similar equipment for customers
- Social impact: community access, data sovereignty, transparent audit

## Customer

- independent appliance-repair shops
- cooperative repair collectives
- community right-to-repair programs

## Offer

- appliance intake
- diagnostic/quote proposal
- repair-completion proposal
- immutable audit ledger

## Revenue

- self-host setup: one-time implementation fee
- managed hosting: monthly subscription per shop
- support: monthly retainer with SLA
- migration: import from an incumbent repair-shop system
- per-repair fee

## Trust Controls

- no repair is performed and no appliance is returned without human sign-off
- a fabricated diagnostic forces a hold, not an override
- every repair path is auditable
- emergency manual override paths remain outside LLM control
- a claimed parts cost that doesn't match the actual quantity-times-
  unit-price calculation, a failed post-repair safety test, or an
  unconfirmed refrigerant-handling technician certification -- each
  forces a hold, not an override
- a ticket's repair cannot be completed or its appliance returned
  twice: a double-completion/double-return attempt is held off this
  actor's own ticket facts alone, with no upstream comparison needed

## Repair Shop Governor: decision rule

`blueprint.edn` fixes `:itonami.blueprint/governor` to `:repair-shop-
governor` -- the SAME governor keyword `repairshop`/9521 (consumer
electronics) and `commrepair`/9512 (communication equipment) already
use, a deliberate, honest reuse of the same business archetype for a
different repair-item category (see this repo's own `docs/adr/0001-
architecture.md` Decision 1). This is not a generic "review step," it
is the one gate the TWO real-world acts this business performs
(completing a real repair, returning a real appliance to the
customer) must pass. The governor sits between the RepairOps-LLM and
execution, per the README's Core Contract:

```text
RepairOps-LLM -> Repair Shop Governor -> hold, proceed, or human approval
```

**Approves**: routine household-appliance-repair actions proposed
against a ticket that already has a consented diagnostic evidence
checklist on file, satisfied required evidence, a matching parts-cost
claim, a passed post-repair safety test, and a confirmed refrigerant-
handling technician certification. These proceed straight to the
ticket ledger.

**Rejects or escalates**: the governor refuses to let the advisor
complete a repair or return an appliance on its own authority when any
of the following hold -- a fabricated jurisdiction spec-basis;
incomplete evidence; a parts-cost mismatch; a failed safety test; an
unconfirmed refrigerant-handling certification; a double-completion/
double-return attempt. A clean completion/return proposal still
always routes to a human -- `:actuation/complete-repair`/`:actuation/
return-appliance` are never auto-committed, at any rollout phase.
