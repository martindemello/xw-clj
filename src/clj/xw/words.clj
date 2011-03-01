(ns xw.words
  (:use (xw board)))

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
