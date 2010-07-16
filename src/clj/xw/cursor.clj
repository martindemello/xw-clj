(ns xw.cursor
  (:use (xw board)))

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

(defn current-word-squares []
  (word-squares current-x current-y current-dir))

(defn current-word []
  (apply str (map #(apply re-char %) (current-word-squares))))

(defn set-current-word [w]
  (doseq [[i j l] (map conj (current-word-squares) w)]
    (set-letter i j (str l))))

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

(defn delete-current-word []
  (let [x current-x y current-y dir current-dir]
    (delete-word x y dir)))
