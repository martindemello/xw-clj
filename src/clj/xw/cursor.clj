(ns xw.cursor
  (:use (xw board words)))

(require '[clojure.contrib.str-utils2 :as s])
 
;cursor movement
(defn goto-origin []
  (def current-x 0)
  (def current-y 0)
  (def current-dir :across))
(goto-origin)
(defn across? [] (= current-dir :across))
(defn inc-pos [i] (rem (+ i 1) N))
(defn dec-pos [i] (if (= i 0) (- N 1) (- i 1)))
(defn move-down  [] (def current-y (inc-pos current-y)))
(defn move-up    [] (def current-y (dec-pos current-y)))
(defn move-right [] (def current-x (inc-pos current-x)))
(defn move-left  [] (def current-x (dec-pos current-x)))
(defn move-forward [] (if (across?) (move-right) (move-down)))
(defn move-back [] (if (across?) (move-left) (move-up)))
(defn move-to [x y]
  (def current-x x)
  (def current-y y))

(defn flip-dir [] (def current-dir (if (across?) :down :across)))

; do something in current square
(defn symm [i j]
  (let [M (- N 1)]
    [ [i j], [j (- M i)], [(- M i) (- M j)], [(- M j) i] ]))

;; symmetrically add or remove a black square
(defn place-symm [blk]
  (doseq [[i j] (symm current-x current-y)] (set-letter i j blk))
  (renumber))

(defn place-letter [c]
  (when (black? current-x current-y) (place-symm :empty))
  (set-letter current-x current-y c))

; current word/pattern
(defn current-word-squares []
  (word-squares current-x current-y current-dir))

(defn current-word []
  (apply str (map #(apply re-char %) (current-word-squares))))

(defn set-current-word [w]
  (doseq [[i j l] (map conj (current-word-squares) w)]
    (set-letter i j (str l))))

(defn delete-current-word []
  (let [x current-x y current-y dir current-dir]
    (delete-word x y dir)))
