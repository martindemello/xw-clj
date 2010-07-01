(ns xw.wordlist
  (:use [xw trie]))

(require '[clojure.contrib.str-utils2 :as s])

; -----------------------------------------
; Wordlist
; -----------------------------------------

(defn read-wordlist [filename] (s/split (slurp filename) #"\n"))

(def trie (build-trie (read-wordlist "csw.txt")))

(defn words-with [patt]
  (pattern trie patt))
