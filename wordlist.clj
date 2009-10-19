(use '[clojure.contrib.str-utils2 :only (split)])
; -----------------------------------------
; Wordlist
; -----------------------------------------

(def wordlist (split (slurp "csw.txt") #"\n"))

(defn words-with [re]
  (filter #(re-matches (re-pattern re) %) wordlist))
