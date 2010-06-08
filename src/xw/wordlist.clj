(ns xw.wordlist
  (:use xw.globals))

(require '[clojure.contrib.str-utils2 :as s])

; -----------------------------------------
; Wordlist
; -----------------------------------------

(def wordlist (s/split (slurp "csw.txt") #"\n"))

(defn words-with [re]
  (if re
    (filter #(re-matches (re-pattern re) %) wordlist)
    '()))
