(ns xw.wordlist
  (:use [xw trie]))

(require '[clojure.contrib.str-utils2 :as s])

; -----------------------------------------
; Wordlist
; -----------------------------------------

(defn read-wordlist [filename] (s/split (slurp filename) #"\n"))

(def dict (build-dict (read-wordlist "/usr/local/share/xw/csw.txt")))

(defn words-with [patt]
  (pattern dict patt))
