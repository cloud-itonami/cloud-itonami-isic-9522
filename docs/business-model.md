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

| Package | Customer | Price shape |
|---|---|---|
| Self-host starter | shop owner / lead technician | setup fee + optional support retainer |
| Managed Starter | one independent appliance-repair shop (single site, ~10 technicians) | ¥22,000/月 flat |

**Market-anchored (2026-08-10)**: benchmarked against 6 real field-service
products. **All 6 publish real numbers**, 4 of them on the vendor's own
pricing page — a striking contrast with this fleet's other 2026-08-10
verticals (`0610` upstream petroleum and `9200` gambling compliance), where
the industry-specific incumbents publish nothing at all. **In household
appliance repair, published pricing is the default.** At the assumed
10-technician shop, converting at ~¥150/$:

| Product | Published price | ~JPY/月 at 10 techs |
|---|---|---|
| [Jobber](https://www.getjobber.com/pricing/) (Grow) | `$199/mo` month-to-month, 10 users included, `$29/mo` each extra | ¥29,850 |
| [Housecall Pro](https://www.housecallpro.com/pricing/) (Essentials) | `$189/mo` monthly / `$149/mo` annual (seat count not shown) | ¥28,350 |
| [Service Fusion](https://www.servicefusion.com/pricing/) (Starter) | `$245/month` monthly / `$208/month` annual, unlimited users | ¥36,750 |
| [cyzen](https://www.cyzen.cloud/plan) (Standard) | 月額 ¥2,400〜/ID（年間契約）＋初期費用 ¥100,000 | ¥24,000 + 初期¥100,000 |
| [esm service](https://www.aspicjapan.org/asu/article/22705) (Basic) | 月額 3,500円/ID | ¥35,000 |
| [CSOne](https://www.aspicjapan.org/asu/article/22705) | 月額 24,000円/3IDまで、初期費用なし | n/a — only the ≤3-ID price is published, so it cannot be extrapolated to 10 |

**¥22,000/月 sits at the very bottom of the measured ¥24,000–¥36,750 band,
just under the cheapest published comparator.** That placement follows from
an asymmetry of scope, not from undercutting: Jobber, Housecall Pro and
Service Fusion sell scheduling, dispatch, routing, quote delivery, invoicing,
payments, customer messaging and marketing, and this actor has none of them.
What it has instead is appliance intake, the diagnostic/quote proposal, the
independent parts-cost recompute, the refrigerant-handling-certification and
post-repair-safety-test gates, the completion/return approval gate, and the
immutable ledger. **The Governor's permanent refusal is not a reason to
charge more** — it is priced as the differentiator that sits *on top of* a
deliberately narrow floor price, not as a premium that lifts it. The tier is
flat, so at the upper end of the assumed customer range (20 technicians) it
stays ¥22,000 while esm service reaches ¥70,000/月 and Jobber Grow reaches
`$199 + 10 × $29 = $489` ≒ ¥73,350/月; and unlike cyzen there is no ¥100,000
初期費用 to start.

**Subscribe (2026-08-10)**: a live Stripe Payment Link for the Managed
Starter tier (¥22,000/月 flat) is available now —
[**subscribe to Managed Starter**](https://buy.stripe.com/dRmaEYbiL2526Hm3HIeEo02).
This is a no-code Stripe-hosted checkout; nothing in this repo's actor code
changed. Fulfilment is manual today — after subscribing, contact gftdcojp to
arrange managed-tenant setup. **No shop has claimed or subscribed to this
tier yet — this is a live, working checkout with zero paid tenants, not a
claim of existing revenue.**

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
