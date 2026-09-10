(ns applianceshop.governor
  "Repair Shop Governor -- the independent compliance layer that earns
  the RepairOps-LLM the right to commit. The LLM has no notion of
  jurisdictional consumer-product-safety/refrigerant-handling law,
  whether a claimed parts cost actually equals the ticket's own parts-
  quantity times parts-unit-price, whether an appliance has actually
  passed its post-repair safety test, whether the technician servicing
  a refrigerant-containing appliance is actually certified to handle
  refrigerants, or when an act stops being a draft and becomes a real-
  world repair completion or appliance return, so this MUST be a
  separate system able to *reject* a proposal and fall back to HOLD --
  the household-appliance-repair analog of `cloud-itonami-isic-9521`'s
  `repairshop.governor` (itself the electronics-repair analog of
  `cloud-itonami-isic-6512`'s `casualty.governor`), and structurally
  closest to `cloud-itonami-isic-9512`'s `commrepair.governor`
  (communication-equipment repair).

  Seven checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them (you don't get to approve your way
  past a fabricated jurisdiction spec-basis, incomplete repair
  evidence, a parts-cost claim that doesn't match quantity times unit-
  price, an appliance returned without a passed safety test, an
  unconfirmed refrigerant-handling certification, or a double
  completion/return). The confidence/actuation gate is SOFT: it asks
  a human to look (low confidence / actuation), and the human may
  approve -- but see `applianceshop.phase`: for `:stake :actuation/
  complete-repair`/`:actuation/return-appliance` (a real repair
  completion or appliance return) NO phase ever allows auto-commit
  either. Two independent layers agree that actuation is always a
  human call.

  This vertical is structurally the closest sibling to `commrepair`/
  9512 (both share `repairshop`/9521's IDENTICAL published
  `:itonami.blueprint/governor` keyword, `:repair-shop-governor` --
  see this repo's own docs/adr/0001-architecture.md Decision 1 for why
  this is a deliberate, honest reuse of the SAME business archetype
  for a different repair-item category, following the precedent
  `commrepair`/9512's own ADR-0001 established). Household appliances
  routinely include refrigerant-containing major appliances
  (refrigerators, freezers, air conditioners), a genuinely distinct
  real-world concern neither `repairshop`/9521's nor `commrepair`/
  9512's own catalogs model -- see Decision 3 below.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source (`applianceshop.
                                       facts`), or invent one? Like
                                       `repairshop.governor`'s own
                                       actuation ops, `:repair/
                                       complete`/`:appliance/return` act
                                       directly on a pre-seeded ticket
                                       (see `applianceshop.store`'s own
                                       docstring) -- there is no
                                       'ticket is missing' failure mode
                                       to guard against here.
    2. Evidence incomplete         -- for `:repair/complete`/
                                       `:appliance/return`, has the
                                       jurisdiction actually been
                                       assessed with a full repair-
                                       evidence checklist on file?
    3. Parts cost mismatch         -- for `:repair/complete`,
                                       INDEPENDENTLY recompute whether
                                       the ticket's own `:claimed-
                                       parts-cost` equals `parts-
                                       quantity x parts-unit-price`
                                       (`applianceshop.registry/parts-
                                       cost-matches-claim?`) -- an
                                       HONEST, literal reuse of
                                       `repairshop.registry`'s/
                                       `commrepair.registry`'s own
                                       EXACT-MATCH independent-
                                       recompute check for the SAME
                                       real-world concern, not claimed
                                       as new.
    4. Safety test not passed      -- for `:appliance/return`, reported
                                       by THIS proposal itself (a
                                       `:safety/screen` that just found
                                       a failed test), or already on
                                       file for the ticket (`:safety/
                                       screen`/`:appliance/return`).
                                       Evaluated UNCONDITIONALLY (not
                                       scoped to a specific op) -- an
                                       HONEST, literal reuse of
                                       `repairshop.governor`'s/
                                       `commrepair.governor`'s own
                                       check for the SAME real-world
                                       concern (post-repair safety
                                       testing), not claimed as new.
    5. Refrigerant-handling
       certification unconfirmed     -- for `:repair/complete`/
                                       `:appliance/return`, reported by
                                       THIS proposal itself (a
                                       `:refrigerant/screen` that just
                                       found certification
                                       unconfirmed), or already on file
                                       for the ticket (`:refrigerant/
                                       screen`/`:repair/complete`/
                                       `:appliance/return`). Evaluated
                                       UNCONDITIONALLY (not scoped to a
                                       specific op), the SAME
                                       discipline `casualty.governor/
                                       sanctions-violations`'s original
                                       fix establishes -- GENUINELY NEW
                                       (grep-verified absent -- zero
                                       hits for 'refrigerant'/'epa-608'/
                                       'f-gas'/'フロン' across every
                                       prior sibling), the 62nd
                                       distinct application of this
                                       discipline overall (most
                                       recently `commrepair.governor/
                                       customer-data-consent-
                                       unconfirmed-violations` at
                                       61st). Grounded in real
                                       refrigerant-handling-
                                       certification law: US Clean Air
                                       Act Section 608 (40 CFR Part
                                       82, EPA Section 608 Technician
                                       Certification), UK F-Gas
                                       Regulations 2015, Germany's
                                       ChemKlimaschutzV, Japan's フロン
                                       排出抑制法.
    6. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:repair/complete`/
                                       `:appliance/return` (REAL acts)
                                       -> escalate.

  Two more guards, double-completion/double-return prevention, are
  enforced but NOT listed as numbered HARD checks above because they
  need no upstream comparison at all -- `already-completed-violations`/
  `already-returned-violations` refuse to complete/return the SAME
  ticket twice, off dedicated `:repair-completed?`/`:appliance-
  returned?` facts (never a `:status` value) -- the SAME 'check a
  dedicated boolean, not status' discipline every prior governor's
  guards establish, informed by `cloud-itonami-isic-6492`'s status-
  lifecycle bug (ADR-2607071320)."
  (:require [applianceshop.facts :as facts]
            [applianceshop.registry :as registry]
            [applianceshop.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Completing a real repair and returning a real appliance are the two
  real-world actuation events this actor performs -- a two-member set,
  matching `repairshop`/9521's and `commrepair`/9512's own dual-
  actuation shape."
  #{:actuation/complete-repair :actuation/return-appliance})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:repair/complete`/`:appliance/
  return`) proposal with no spec-basis citation is a HARD violation --
  never invent a jurisdiction's consumer-product-safety/refrigerant-
  handling requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :repair/complete :appliance/return} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:repair/complete`/`:appliance/return`, the jurisdiction's
  required diagnostic/parts-used/safety-test/refrigerant-certification
  evidence must actually be satisfied -- do not trust the advisor's
  self-reported confidence alone."
  [{:keys [op subject]} st]
  (when (contains? #{:repair/complete :appliance/return} op)
    (let [t (store/ticket st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction t) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(故障診断書/使用部品記録/安全試験記録/フロン類取扱技術者資格記録等)が充足していない状態での提案"}]))))

(defn- parts-cost-mismatch-violations
  "For `:repair/complete`, INDEPENDENTLY recompute whether the
  ticket's own claimed parts cost equals parts-quantity x parts-unit-
  price via `applianceshop.registry/parts-cost-matches-claim?` --
  needs no proposal inspection or stored-verdict lookup at all, an
  honest reuse of `repairshop.registry`'s/`commrepair.registry`'s own
  check."
  [{:keys [op subject]} st]
  (when (= op :repair/complete)
    (let [t (store/ticket st subject)]
      (when-not (registry/parts-cost-matches-claim? t)
        [{:rule :parts-cost-mismatch
          :detail (str subject " の申告部品代金(" (:claimed-parts-cost t)
                      ")が独立再計算値(" (registry/compute-parts-cost t) ")と一致しない")}]))))

(defn- safety-test-not-passed-violations
  "A not-passed post-repair safety test -- reported by THIS proposal
  (e.g. a `:safety/screen` that itself just found a failure), or
  already on file in the store for the ticket (`:safety/screen`/
  `:appliance/return`) -- is a HARD, un-overridable hold. Evaluated
  UNCONDITIONALLY (not scoped to a specific op) so the screening op
  itself can HARD-hold on its own finding."
  [{:keys [op subject]} proposal st]
  (let [hit-in-proposal? (= :failed (get-in proposal [:value :verdict]))
        ticket-id (when (contains? #{:safety/screen :appliance/return} op) subject)
        hit-on-file? (and ticket-id (= :failed (:verdict (store/safety-screening-of st ticket-id))))]
    (when (or hit-in-proposal? hit-on-file?)
      [{:rule :safety-test-not-passed
        :detail "修理後安全試験に合格していない機器を返却する提案は進められない"}])))

(defn- refrigerant-handling-certification-unconfirmed-violations
  "An unconfirmed refrigerant-handling certification -- reported by
  THIS proposal (e.g. a `:refrigerant/screen` that itself just found
  certification unconfirmed), or already on file in the store for the
  ticket (`:refrigerant/screen`/`:repair/complete`/`:appliance/
  return`) -- is a HARD, un-overridable hold. Evaluated
  UNCONDITIONALLY (not scoped to a specific op) so the screening op
  itself can HARD-hold on its own finding."
  [{:keys [op subject]} proposal st]
  (let [hit-in-proposal? (false? (get-in proposal [:value :certified?]))
        ticket-id (when (contains? #{:refrigerant/screen :repair/complete :appliance/return} op) subject)
        hit-on-file? (and ticket-id (false? (:refrigerant-handling-certified? (store/ticket st ticket-id))))]
    (when (or hit-in-proposal? hit-on-file?)
      [{:rule :refrigerant-handling-certification-unconfirmed
        :detail "フロン類取扱technician資格が未確認の状態での修理完了/返却提案は進められない"}])))

(defn- already-completed-violations
  "For `:repair/complete`, refuses to complete the SAME ticket's
  repair twice, off a dedicated `:repair-completed?` fact (never a
  `:status` value)."
  [{:keys [op subject]} st]
  (when (= op :repair/complete)
    (when (store/ticket-already-completed? st subject)
      [{:rule :already-completed
        :detail (str subject " は既に修理完了済み")}])))

(defn- already-returned-violations
  "For `:appliance/return`, refuses to return the SAME ticket's
  appliance twice, off a dedicated `:appliance-returned?` fact (never
  a `:status` value)."
  [{:keys [op subject]} st]
  (when (= op :appliance/return)
    (when (store/ticket-already-returned? st subject)
      [{:rule :already-returned
        :detail (str subject " は既に返却済み")}])))

(defn check
  "Censors a RepairOps-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (parts-cost-mismatch-violations request st)
                           (safety-test-not-passed-violations request proposal st)
                           (refrigerant-handling-certification-unconfirmed-violations request proposal st)
                           (already-completed-violations request st)
                           (already-returned-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
