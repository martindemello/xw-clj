; thanks to Greg Fodor for the basic idea
; http://stackoverflow.com/questions/1452680/clojure-how-to-generate-a-trie

(ns xw.trie
  (:use (clojure.contrib [seq-utils :only (flatten)])))

(require '[clojure.contrib.str-utils2 :as s])

(defn add-to-trie [trie word]
  (let [x (seq word)]
    (assoc-in trie x (merge (get-in trie x) {:val word :eow true}))))

(defn in-trie? [trie word]
  (:eow (get-in trie (seq word)) false))

(defn skeys [trie] (sort (filter char? (keys trie))))

(defn build-trie [coll]
  (reduce add-to-trie {} coll))

(defn pattern-match [trie patt acc]
  (let [c (first patt)
        r (rest patt)]
    (cond
      (empty? patt) (if (:eow trie) (conj acc (:val trie)) acc)
      (= c \.) (concat acc (map (fn [x] (pattern-match (trie x) r acc)) (skeys trie)))
      :else (if (trie c) (concat acc (pattern-match (trie c) r acc)) acc))))

(defn pattern [trie word]
  (flatten (pattern-match trie (seq word) [])))
