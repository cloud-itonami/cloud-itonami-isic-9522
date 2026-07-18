(ns applianceshop.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 (com-junkawasaki/root ADR-2607189300,
  Wave5 rollout ledger seq 6/7): this repo previously had NO demo page
  and no generator at all. This namespace drives the REAL actor stack
  (`applianceshop.operation` -> `applianceshop.governor` ->
  `applianceshop.store`) through a scenario adapted from this repo's
  own `applianceshop.sim` demo driver (`clojure -M:dev:run`, confirmed
  BEFORE writing this file to produce a sensible ledger against the
  real seeded ticket ids `ticket-1`..`ticket-5` -- unlike
  `cloud-itonami-isic-851`'s `schoolops.sim`, this repo's own sim
  driver uses ids that DO match `applianceshop.store/demo-data`, so it
  was safe to reuse rather than author from scratch), trimmed to a
  representative subset (one full intake->assess->screen->complete->
  return lifecycle, and four distinct HARD-hold reasons) and rendered
  deterministically -- no invented numbers, no timestamps in the page
  content, byte-identical across reruns against the same seed (verify
  by diffing two consecutive runs).

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [clojure.string :as str]
            [applianceshop.store :as store]
            [applianceshop.operation :as op]
            [langgraph.graph :as g]))

(def ^:private operator
  {:actor-id "op-1" :actor-role :repair-shop-manager :phase 3})

(defn- exec! [actor tid request]
  (g/run* actor {:request request :context operator} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}}
          {:thread-id tid :resume? true}))

(defn run-demo!
  "Runs a fresh seeded store through a scenario mixing every
  disposition this actor can reach: ticket-1 clears a full lifecycle
  -- intake (auto-commit clean at phase 3, no capital risk), a
  jurisdiction assessment (phase-gated -- not yet auto-eligible --
  approved), a post-repair safety screening (approved), a
  refrigerant-handling-certification screening (approved), a repair
  completion (ALWAYS escalates -- `:actuation/complete-repair` is
  permanently high-stakes, never auto at any phase -- approved) and an
  appliance return (ALWAYS escalates -- `:actuation/return-appliance`,
  same posture -- approved); ticket-2 HARD-holds a jurisdiction
  assessment with no official spec-basis for its (deliberately
  unregistered) jurisdiction; ticket-3 clears its own jurisdiction
  assessment (approved) but then HARD-holds a repair-completion
  attempt whose claimed parts cost (120.0) doesn't match the
  independently recomputed parts-quantity x parts-unit-price (1 x 80 =
  80.0); ticket-4 HARD-holds a safety screening that itself detects a
  failed post-repair safety test; ticket-5 HARD-holds a
  refrigerant-handling screening that itself detects an unconfirmed
  refrigerant-handling certification. Every HARD hold never reaches a
  human. Returns the resulting store -- every field read by `render`
  below is real governor/store output, not a hand-typed copy."
  []
  (let [db (store/seed-db)
        actor (op/build db)]
    (exec! actor "t1-intake" {:op :ticket/intake :subject "ticket-1"
                               :patch {:id "ticket-1" :customer "Sakura Tanaka"}})

    (exec! actor "t1-assess" {:op :jurisdiction/assess :subject "ticket-1"})
    (approve! actor "t1-assess")

    (exec! actor "t1-safety" {:op :safety/screen :subject "ticket-1"})
    (approve! actor "t1-safety")

    (exec! actor "t1-refrigerant" {:op :refrigerant/screen :subject "ticket-1"})
    (approve! actor "t1-refrigerant")

    (exec! actor "t1-complete" {:op :repair/complete :subject "ticket-1"})
    (approve! actor "t1-complete")

    (exec! actor "t1-return" {:op :appliance/return :subject "ticket-1"})
    (approve! actor "t1-return")

    (exec! actor "t2-assess" {:op :jurisdiction/assess :subject "ticket-2" :no-spec? true})

    (exec! actor "t3-assess" {:op :jurisdiction/assess :subject "ticket-3"})
    (approve! actor "t3-assess")

    (exec! actor "t3-complete" {:op :repair/complete :subject "ticket-3"})

    (exec! actor "t4-safety" {:op :safety/screen :subject "ticket-4"})

    (exec! actor "t5-refrigerant" {:op :refrigerant/screen :subject "ticket-5"})
    db))

;; ----------------------------- rendering -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- last-fact-for [ledger ticket-id]
  (last (filter #(= (:subject %) ticket-id) ledger)))

(defn- status-cell [ledger ticket-id]
  (let [f (last-fact-for ledger ticket-id)]
    (cond
      (nil? f) "<span class=\"muted\">no activity</span>"
      (= :committed (:t f)) "<span class=\"ok\">committed</span>"
      (= :approval-granted (:t f)) "<span class=\"ok\">approved &amp; committed</span>"
      (= :governor-hold (:t f))
      (let [rule (-> f :violations first :rule)]
        (str "<span class=\"critical\">HARD hold &middot; " (esc (name (or rule :unknown))) "</span>"))
      (= :approval-requested (:t f)) "<span class=\"warn\">awaiting approval</span>"
      :else "<span class=\"muted\">in progress</span>")))

(defn- lifecycle-cell [{:keys [repair-completed? appliance-returned?]}]
  (cond
    appliance-returned? "<span class=\"ok\">repaired &amp; returned</span>"
    repair-completed? "<span class=\"warn\">repaired, not yet returned</span>"
    :else "<span class=\"muted\">in repair</span>"))

(defn- ticket-row [ledger {:keys [id customer appliance jurisdiction] :as t}]
  (format "        <tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>"
          (esc id) (esc customer) (esc appliance) (esc jurisdiction)
          (lifecycle-cell t)
          (status-cell ledger id)))

(defn- ledger-row [{:keys [t op subject disposition basis]}]
  (format "        <tr><td>%s</td><td><code>%s</code></td><td>%s</td><td>%s</td></tr>"
          (esc (name t)) (esc (name (or op :n-a))) (esc subject)
          (esc (or (some->> basis (map name) (str/join ", ")) (some-> disposition name) ""))))

(def ^:private action-gate-rows
  ;; Static description of this actor's own closed op contract
  ;; (README `Ops`, `applianceshop.governor`/`applianceshop.phase`) --
  ;; documentation of fixed behavior, not runtime telemetry, so it is
  ;; legitimately hand-described rather than derived from a live run.
  ["        <tr><td><code>:ticket/intake</code></td><td><span class=\"ok\">phase-3 auto-commit when clean, no capital risk</span></td></tr>"
   "        <tr><td><code>:jurisdiction/assess</code></td><td><span class=\"warn\">phase-3: human approval (not yet auto-eligible)</span></td></tr>"
   "        <tr><td><code>:safety/screen</code></td><td><span class=\"warn\">phase-3: human approval (not yet auto-eligible)</span></td></tr>"
   "        <tr><td><code>:refrigerant/screen</code></td><td><span class=\"warn\">phase-3: human approval (not yet auto-eligible)</span></td></tr>"
   "        <tr><td><code>:repair/complete</code></td><td><span class=\"warn\">ALWAYS human approval &middot; never auto at any phase &middot; independent parts-cost recompute</span></td></tr>"
   "        <tr><td><code>:appliance/return</code></td><td><span class=\"warn\">ALWAYS human approval &middot; never auto at any phase &middot; safety-test &amp; refrigerant-certification re-checked</span></td></tr>"])

(defn render
  "Renders the full operator-console.html document from a store `db`
  that has already run `run-demo!` (or any other real scenario)."
  [db]
  (let [ledger (vec (store/ledger db))
        tickets (store/all-tickets db)
        ticket-rows (str/join "\n" (map (partial ticket-row ledger) tickets))
        ledger-rows (str/join "\n" (map ledger-row ledger))]
    (str
     "<html><head><meta charset=\"utf-8\"><title>cloud-itonami-isic-9522 &middot; household-appliance-repair</title><style>\n"
     "table { width: 100%; border-collapse: collapse; font-size: 14px; }\n"
     ".ok { color: #137a3f; }\n"
     "body { font-family: system-ui,-apple-system,sans-serif; margin: 0; color: #1a1a1a; background: #fafafa; }\n"
     "header.bar { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: #fff; border-bottom: 1px solid #e5e5e5; }\n"
     "th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #f0f0f0; }\n"
     "h2 { margin-top: 0; font-size: 15px; }\n"
     ".warn { color: #b25c00; background: #fff8e1; padding: 2px 6px; border-radius: 4px; }\n"
     "main { max-width: 980px; margin: 24px auto; padding: 0 20px; }\n"
     "header.bar h1 { font-size: 18px; margin: 0; font-weight: 600; }\n"
     ".muted { color: #888; font-size: 13px; }\n"
     ".critical { color: #fff; background: #b3261e; padding: 2px 6px; border-radius: 4px; font-weight: 600; }\n"
     ".card { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 16px; margin-bottom: 16px; }\n"
     ".err { color: #b3261e; background: #fbe9e7; padding: 2px 6px; border-radius: 4px; }\n"
     "th { font-weight: 600; color: #555; font-size: 12px; text-transform: uppercase; letter-spacing: 0.04em; }\n"
     "header.bar .badge { margin-left: auto; font-size: 12px; color: #666; }\n"
     "code { font-size: 12px; background: #f4f4f4; padding: 1px 4px; border-radius: 3px; }\n"
     "</style></head><body>\n"
     "<header class=\"bar\">\n"
     "  <h1>Household appliance &amp; electronic equipment repair (ISIC 9522) — Operator Console</h1>\n"
     "  <span class=\"badge\">read-only sample · governor-gated · repair completion/appliance return always human-approved</span>\n"
     "</header>\n"
     "<main>\n"
     "  <section class=\"card\">\n"
     "    <h2>Repair tickets</h2>\n"
     "    <p class=\"muted\">Demo snapshot — build-time-generated from <code>applianceshop.store</code> via <code>applianceshop.render-html</code> (<code>clojure -M:dev:render-html</code>), regenerated nightly.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Ticket</th><th>Customer</th><th>Appliance</th><th>Jurisdiction</th><th>Repair/return status</th><th>Last op status</th></tr></thead>\n"
     "      <tbody>\n"
     ticket-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Action gate (Repair Shop Governor)</h2>\n"
     "    <p class=\"muted\">HARD holds cannot be overridden. Parts costs are independently recomputed, never trusted from the proposal; a repair completion or appliance return is blocked outright on a failed post-repair safety test or an unconfirmed refrigerant-handling certification.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Op</th><th>Gate</th></tr></thead>\n"
     "      <tbody>\n"
     (str/join "\n" action-gate-rows) "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Audit ledger (this run)</h2>\n"
     "    <p class=\"muted\">Append-only decision-fact log — every proposal, hold and commit this scenario produced.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Fact</th><th>Op</th><th>Ticket</th><th>Basis</th></tr></thead>\n"
     "      <tbody>\n"
     ledger-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "</main>\n"
     "</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        db (run-demo!)
        html (render db)]
    (spit out html)
    (println "wrote" out "(" (count (store/ledger db)) "ledger facts,"
             (count (store/completion-history db)) "repair completions,"
             (count (store/return-history db)) "appliance returns )")))
