(ns applianceshop.facts
  "Per-jurisdiction consumer-product-safety AND refrigerant-handling-
  certification regulatory catalog -- the G2-style spec-basis table
  the Repair Shop Governor checks every jurisdiction/assess proposal
  against ('did the advisor cite an OFFICIAL public source for this
  jurisdiction's requirements, or did it invent one?').

  This blueprint's own named activity (household appliances and home
  and garden equipment -- washing machines, lawn mowers 'and similar
  equipment') routinely includes refrigerant-containing major
  appliances (refrigerators, freezers, window/portable air
  conditioners), a real, distinct regulatory concern beyond
  `repairshop`/9521's own consumer-electronics catalog and
  `commrepair`/9512's own communication-equipment/data-privacy
  catalog: servicing equipment containing fluorinated refrigerants
  requires a CERTIFIED technician in every major jurisdiction (the US
  EPA's Section 608 Technician Certification under the Clean Air Act
  is the textbook example -- a federal requirement since 1993, one of
  the most well-documented technician-certification regimes in
  consumer-product-repair law). Each jurisdiction entry below
  therefore cites BOTH the consumer-product-safety law `repairshop.
  facts` already models AND a refrigerant-handling-certification law.

  Coverage is reported HONESTLY (see `coverage`), the same discipline
  every sibling actor's `facts` namespace uses: a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries.

  Seed values are drawn from each jurisdiction's official product-
  safety AND refrigerant/climate regulators (see `:provenance`); they
  are a STARTING catalog, not a from-scratch survey of all ~194
  jurisdictions.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the generic
  diagnostic-report/parts-used-documentation/post-repair-safety-test-
  record/refrigerant-handling-certification-record evidence set
  submitted in some form; `:legal-basis` / `:owner-authority` /
  `:provenance` are the G2 citation the governor requires before any
  :jurisdiction/assess proposal can commit."
  {"JPN" {:name "Japan"
          :owner-authority "経済産業省 (METI) / 環境省 (Ministry of the Environment)"
          :legal-basis "消費生活用製品安全法 (Consumer Product Safety Act); フロン類の使用の合理化及び管理の適正化に関する法律 (Fluorocarbons Act) -- 第一種特定製品の整備時充填回収"
          :national-spec "PSCマーク制度; フロン排出抑制法に基づく充填回収業者登録制度"
          :provenance "https://www.env.go.jp/earth/ozone/cfc/"
          :required-evidence ["故障診断書 (diagnostic report)"
                              "使用部品記録 (parts-used documentation)"
                              "修理後安全試験記録 (post-repair safety-test record)"
                              "フロン類取扱技術者資格記録 (refrigerant-handling-certification record)"]}
   "USA" {:name "United States"
          :owner-authority "U.S. Consumer Product Safety Commission (CPSC) / Environmental Protection Agency (EPA)"
          :legal-basis "Consumer Product Safety Act (15 U.S.C. §§2051 et seq.); Clean Air Act Section 608 (40 CFR Part 82) -- EPA Section 608 Technician Certification"
          :national-spec "CPSC product-safety standards; EPA Section 608 certification requirement for servicing refrigerant-containing equipment"
          :provenance "https://www.epa.gov/section608-certification"
          :required-evidence ["Diagnostic report"
                              "Parts-used documentation"
                              "Post-repair safety-test record"
                              "Refrigerant-handling-certification record"]}
   "GBR" {:name "United Kingdom"
          :owner-authority "Office for Product Safety and Standards (OPSS) / Environment Agency"
          :legal-basis "General Product Safety Regulations 2005; The Fluorinated Greenhouse Gases Regulations 2015 (UK F-Gas Regulations) -- technician certification for handling F-gases"
          :national-spec "OPSS product-safety enforcement standards; F-Gas Regulations certification scheme"
          :provenance "https://www.gov.uk/guidance/f-gas-regulations-guidance-for-users-and-producers-of-equipment"
          :required-evidence ["Diagnostic report"
                              "Parts-used documentation"
                              "Post-repair safety-test record"
                              "Refrigerant-handling-certification record"]}
   "DEU" {:name "Germany"
          :owner-authority "Marktüberwachungsbehörden der Länder / Umweltbundesamt (UBA)"
          :legal-basis "Produktsicherheitsgesetz (ProdSG); Chemikalien-Klimaschutzverordnung (ChemKlimaschutzV) -- Sachkundenachweis für Tätigkeiten an Kälteanlagen"
          :national-spec "ProdSG Marktüberwachungsanforderungen; ChemKlimaschutzV Zertifizierungspflicht"
          :provenance "https://www.umweltbundesamt.de/themen/klima-energie/fluorierte-treibhausgase-f-gase"
          :required-evidence ["Diagnosebericht (diagnostic report)"
                              "Ersatzteilnachweis (parts-used documentation)"
                              "Sicherheitsprüfungsprotokoll nach Reparatur (post-repair safety-test record)"
                              "Sachkundenachweis Kältemittel (refrigerant-handling-certification record)"]}
   "POL" {:name "Poland"
          :owner-authority "Prezes Urzędu Ochrony Konkurencji i Konsumentów (UOKiK) / Urząd Dozoru Technicznego (UDT)"
          :legal-basis "Rozporządzenie Parlamentu Europejskiego i Rady (UE) 2023/988 w sprawie ogólnego bezpieczeństwa produktów (GPSR); Ustawa z dnia 15 maja 2015 r. o substancjach zubożających warstwę ozonową oraz o niektórych fluorowanych gazach cieplarnianych (Dz.U. 2015 poz. 881), art. 20 -- obowiązek posiadania certyfikatu dla personelu wykonującego instalację, kontrolę szczelności, naprawę, konserwację lub serwisowanie stacjonarnych urządzeń chłodniczych, klimatyzacyjnych i pomp ciepła zawierających fluorowane gazy cieplarniane"
          :national-spec "GPSR ogólne wymogi bezpieczeństwa produktów -- mają zastosowanie do produktów udostępnianych na rynku niezależnie od tego, czy są nowe, używane, naprawione lub odnowione (nadzór: Prezes UOKiK, kontrole: Inspekcja Handlowa); UDT certyfikat dla personelu -- wydawany po egzaminie teoretycznym i praktycznym przed komisją egzaminacyjną jednostki oceniającej personel"
          :provenance "https://www.udt.gov.pl/uslugi-udt/szwo-i-f-gazy/certyfikat-dla-personelu"
          :required-evidence ["Raport z diagnozy usterki (diagnostic report)"
                              "Dokumentacja zużytych części zamiennych (parts-used documentation)"
                              "Protokół badania bezpieczeństwa po naprawie (post-repair safety-test record)"
                              "Certyfikat dla personelu UDT ds. czynników chłodniczych (refrigerant-handling-certification record)"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to complete a
  repair or return an appliance on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-isic-9522 R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog, not a survey of all ~194 "
                 "jurisdictions -- extend `applianceshop.facts/catalog`, "
                 "never fabricate a jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))
