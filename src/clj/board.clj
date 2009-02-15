(def board (hash-map))
(def board-iter (for [j (range N) i (range N)] [i j]))

; accessors
(defn letter [i j] ((board [i j]) 0))
(defn number [i j] ((board [i j]) 1))
(defn black? [i j] (= (letter i j) :black))
(defn white? [i j] (not (black? i j)))
(defn blank? [i j] (= (letter i j) :empty))
(defn numbered? [i j] (not (= (number i j) nil)))
(defn set-board [i j p] (def board (assoc board [i j] p)))
(defn set-letter [i j l] (set-board i j [l (number i j)]))
(defn set-number [i j n] (set-board i j [(letter i j) n]))

; numbering
(defn start-across? [i j]
  (and
    (white? i j)
    (or (= i 0) (black? (- i 1) j))
    (and (< i M) (white? (+ i 1) j))))

(defn start-down? [i j]
  (and
    (white? i j)
    (or (= j 0) (black? i (- j 1)))
    (and (< j M) (white? i (+ j 1)))))

(defn start-sqr? [i j]
  (or (start-across? i j) (start-down? i j)))

(defn renumber []
  (reduce (fn [n [i j]]
            (if (start-sqr? i j) 
              (do (set-number i j n) (inc n))
              (do (set-number i j nil) n))) 1 board-iter))
