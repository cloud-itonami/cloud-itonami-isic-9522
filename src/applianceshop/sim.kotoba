(ns applianceshop.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a clean ticket through
  intake -> jurisdiction assessment -> post-repair safety screening ->
  refrigerant-handling-certification screening -> repair-completion
  proposal (always escalates) -> human approval -> commit, then
  through appliance-return proposal (always escalates) -> human
  approval -> commit, then shows five HARD holds (a jurisdiction with
  no spec-basis, a claimed parts cost that doesn't match parts-
  quantity times unit-price, a failed post-repair safety test, an
  unconfirmed refrigerant-handling certification [screened directly
  via `:refrigerant/screen` -- never via an actuation op against an
  unscreened ticket -- see this actor's own governor ns docstring /
  the lesson `parksafety`'s ADR-2607071922 Decision 5, `eldercare`'s,
  `museum`'s, `conservation`'s, `salon`'s, `entertainment`'s,
  `casework`'s, `hospital`'s, `facility`'s, `school`'s, `association`'s,
  `leasing`'s, `behavioral`'s, `secondary`'s, `card`'s, `water`'s,
  `telecom`'s, `aerospace`'s, `recovery`'s, `consulting`'s, `union`'s,
  `congregation`'s, `fab`'s, `energy`'s, `care`'s, `navigator`'s,
  `learning`'s, `banking`'s, `advertising`'s, `polling`'s, `research`'s,
  `design`'s, `nursing`'s, `sports`'s, `alliedhealth`'s, `laundry`'s,
  `holdco`'s, `photo`'s, `personalservice`'s, `edsupport`'s,
  `headoffice`'s, `residential`'s, `cultural`'s, `reserve`'s,
  `proserv`'s, `sportsevent`'s, `recreation`'s, `sportsclub`'s,
  `partyops`'s, `memberorg`'s and `commrepair`'s ADR-0001s already
  recorded], and a double completion/return of an already-processed
  ticket) that never reach a human at all, and prints the audit ledger
  + the draft repair-completion and appliance-return records."
  (:require [langgraph.graph :as g]
            [applianceshop.store :as store]
            [applianceshop.operation :as op]))

(def operator {:actor-id "op-1" :actor-role :repair-technician :phase 3})

(defn- exec! [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn -main [& _]
  (let [db (store/seed-db)
        actor (op/build db)]
    (println "== ticket/intake ticket-1 (JPN, clean; 1 part x 45 = claimed-parts-cost 45.0) ==")
    (println (exec! actor "t1" {:op :ticket/intake :subject "ticket-1"
                                :patch {:id "ticket-1" :customer "Sakura Tanaka"}} operator))

    (println "== jurisdiction/assess ticket-1 (escalates -- human approves) ==")
    (println (exec! actor "t2" {:op :jurisdiction/assess :subject "ticket-1"} operator))
    (println (approve! actor "t2"))

    (println "== safety/screen ticket-1 (clean; escalates -- human approves) ==")
    (println (exec! actor "t3" {:op :safety/screen :subject "ticket-1"} operator))
    (println (approve! actor "t3"))

    (println "== refrigerant/screen ticket-1 (clean; escalates -- human approves) ==")
    (println (exec! actor "t4" {:op :refrigerant/screen :subject "ticket-1"} operator))
    (println (approve! actor "t4"))

    (println "== repair/complete ticket-1 (always escalates -- actuation/complete-repair) ==")
    (let [r (exec! actor "t5" {:op :repair/complete :subject "ticket-1"} operator)]
      (println r)
      (println "-- human repair technician approves --")
      (println (approve! actor "t5")))

    (println "== appliance/return ticket-1 (always escalates -- actuation/return-appliance) ==")
    (let [r (exec! actor "t6" {:op :appliance/return :subject "ticket-1"} operator)]
      (println r)
      (println "-- human repair technician approves --")
      (println (approve! actor "t6")))

    (println "== jurisdiction/assess ticket-2 (no spec-basis -> HARD hold) ==")
    (println (exec! actor "t7" {:op :jurisdiction/assess :subject "ticket-2" :no-spec? true} operator))

    (println "== jurisdiction/assess ticket-3 (escalates -- human approves; sets up the parts-cost-mismatch test) ==")
    (println (exec! actor "t8" {:op :jurisdiction/assess :subject "ticket-3"} operator))
    (println (approve! actor "t8"))

    (println "== repair/complete ticket-3 (claimed-parts-cost 120.0 != 1 x 80 = 80.0 -> HARD hold) ==")
    (println (exec! actor "t9" {:op :repair/complete :subject "ticket-3"} operator))

    (println "== safety/screen ticket-4 (failed post-repair safety test -> HARD hold, never reaches a human) ==")
    (println (exec! actor "t10" {:op :safety/screen :subject "ticket-4"} operator))

    (println "== refrigerant/screen ticket-5 (unconfirmed refrigerant-handling certification -> HARD hold, never reaches a human) ==")
    (println (exec! actor "t11" {:op :refrigerant/screen :subject "ticket-5"} operator))

    (println "== repair/complete ticket-1 AGAIN (double-completion -> HARD hold) ==")
    (println (exec! actor "t12" {:op :repair/complete :subject "ticket-1"} operator))

    (println "== appliance/return ticket-1 AGAIN (double-return -> HARD hold) ==")
    (println (exec! actor "t13" {:op :appliance/return :subject "ticket-1"} operator))

    (println "== audit ledger ==")
    (doseq [f (store/ledger db)] (println f))

    (println "== draft repair-completion records ==")
    (doseq [r (store/completion-history db)] (println r))

    (println "== draft appliance-return records ==")
    (doseq [r (store/return-history db)] (println r))))
