(clojure.core/import
  '(java.util.regex Pattern))

; -----------------------------------------
; Wordlist
; -----------------------------------------

(def wordlist (seq (.split (slurp "csw.txt") "\n")))

(defn words-with [re-string]
  (if re-string
    (let [regex (. Pattern compile re-string)]
      (filter #(re-matches regex %) wordlist))
    []))
