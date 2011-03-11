(ns xw.words
  (:use (xw board clues)))

(require '[clojure.contrib.str-utils2 :as s])

(defn re-char [i j]
  (let [l (letter i j)]
    (cond
      (= l :empty) "."
      (= l :black) "#"
      true l)))

(defn word-boundary? [i j]
  (let [M (- N 1)]
    (or (black? i j) (< i 0) (< j 0) (> i M) (> j M))))

(defn in-word? [i j]
  (not (word-boundary? i j)))

(defn collect-ac [s j]
  (map (fn [k] [k j]) (take-while #(in-word? % j) s)))

(defn collect-dn [i s]
  (map (fn [k] [i k]) (take-while #(in-word? i %) s)))

(defn ac-word [i j]
  (let [l (reverse (range 0 i))
        r (range i N)]
    (concat (reverse (collect-ac l j)) (collect-ac r j))))

(defn dn-word [i j]
  (let [u (reverse (range 0 j))
        d (range j N)]
    (concat (reverse (collect-dn i u)) (collect-dn i d))))

(defn word-squares [x y dir]
  (cond
    (black? x y) nil
    (= dir :across) (ac-word x y)
    true (dn-word x y)))

(defn str-word [squares]
  (apply str (map #(apply re-char %) squares)))

(defn crossing-word [x y dir]
  (if (= dir :across)
    (dn-word x y)
    (ac-word x y)))

(defn delete-word [x y dir]
  (doseq [[i j] (word-squares x y dir)]
    (let [ws (crossing-word i j dir)
          empty? (fn [i] (apply blank? i))]
      (when (or (= 1 (count ws))
                (some empty? ws))
        (set-letter i j :empty)))))

(defn get-words [[x y]]
  (vec (remove nil?
          [(when (start-across? x y) [\A (number x y) (str-word (ac-word x y))])
           (when (start-down? x y) [\D (number x y) (str-word (dn-word x y))])])))

(defn all-words []
  (remove empty? (apply concat (map get-words board-iter))))

(defn partial-word? [w]
  (s/contains? w "."))

(defn partial-clue? [cl]
  (partial-word? (cl 2)))

(defn complete-words []
  (remove partial-clue? (all-words)))

(defn active-cluelist []
  (vec (map (fn [[l n w]] [(str n l) w (clue-for w)])
            (complete-words))))
