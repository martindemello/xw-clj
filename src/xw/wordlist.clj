(ns xw.wordlist
  (:use [clojure.contrib.str-utils2 :only (split)])
  (:use xw.globals))

; -----------------------------------------
; Wordlist
; -----------------------------------------

(def wordlist (split (slurp "csw.txt") #"\n"))

(defn words-with [re]
  (filter #(re-matches (re-pattern re) %) wordlist))
