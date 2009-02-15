; cursor movement
(def current-x 0)
(def current-y 0)
(def current-dir :across)
(defn across? [] (= current-dir :across))
(defn inc-pos [i] (rem (+ i 1) N))
(defn dec-pos [i] (if (= i 0) (- N 1) (- i 1)))
(defn move-down  [] (def current-y (inc-pos current-y)))
(defn move-up    [] (def current-y (dec-pos current-y)))
(defn move-right [] (def current-x (inc-pos current-x)))
(defn move-left  [] (def current-x (dec-pos current-x)))
(defn move-forward [] (if (across?) (move-right) (move-down)))
(defn move-back [] (if (across?) (move-left) (move-up)))
(defn flip-dir [] (def current-dir (if (across?) :down :across)))

; do something in current square
(defn symm [i j] [ [i j] [j (- M i)] [(- M i) (- M j)] [(- M j) i] ])

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
  (or (black? i j) (< i 0) (< j 0) (> i M) (> j M)))

(defn in-word? [i j]
  (not (word-boundary? i j)))

(defn collect-ac [s j]
  (apply str (map #(re-char % j) (take-while #(in-word? % j) s))))

(defn collect-dn [i s]
  (apply str (map #(re-char i %) (take-while #(in-word? i %) s))))

(defn ac-word [i j]
  (let [l (reverse (range 0 i))
        r (range i N)]
    (apply str (concat (reverse (collect-ac l j)) (collect-ac r j)))))

(defn dn-word [i j]
  (let [u (reverse (range 0 j))
        d (range j N)]
    (apply str (concat (reverse (collect-dn i u)) (collect-dn i d)))))

(defn current-word []
  (cond
    (black? current-x current-y) nil
    (across?) (ac-word current-x current-y)
    true (dn-word current-x current-y)))

