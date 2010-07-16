; thanks to Greg Fodor for the basic idea
; http://stackoverflow.com/questions/1452680/clojure-how-to-generate-a-trie

(ns xw.trie)

(require '[clojure.contrib.str-utils2 :as s])

(defn add-to-trie [trie word]
  (let [x (seq word)]
    (assoc-in trie x (merge (get-in trie x) {:val word :eow true}))))

(defn in-trie? [trie word]
  (:eow (get-in trie (seq word)) false))

(defn skeys [trie] (sort (filter char? (keys trie))))

(defn pattern-match [trie patt acc]
  (let [c (first patt)
        r (rest patt)]
    (cond
      (empty? patt) (if (:eow trie) (conj acc (:val trie)) acc)
      (= c \.) (concat acc (map (fn [x] (pattern-match (trie x) r acc)) (skeys trie)))
      :else (if (trie c) (concat acc (pattern-match (trie c) r acc)) acc))))

(defn pattern-trie [trie word]
  (flatten (pattern-match trie (seq word) [])))

(defn empty-dict []
  (vec (map (fn [_] {}) (range 0 20))))

(defn add-to-dict [dict word]
  (let [n (- (count word) 1)
        trie (dict n)]
    (assoc dict n (add-to-trie trie word))))

(defn build-dict [coll]
  (reduce add-to-dict (empty-dict) coll))

(defn pattern [dict word]
  (let [trie (dict (- (count word) 1))]
    (pattern-trie trie word)))
