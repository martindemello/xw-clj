(in-ns 'main)
(clojure/refer 'clojure)

(def N 10)
(def board (hash-map))

(def board-iter (for [i (range N) j (range N)] [i j]))

(defn letter [i j] ((board [i j]) 0))

(defn black? [i j] (= (letter i j) :black))

(defn set-number [i j n]
  (def board (assoc board [i j] [(letter i j) n])))

; numbering
(defn start-across? [i j] 
  (and 
    (not (black? i j))
    (or (= i 0) (black? (- i 1) j))
    (and (< i (- N 1)) (not (black? (+ i 1) j)))))

(defn start-down? [i j] 
  (and 
    (not (black? i j))
    (or (= j 0) (black? i (- j 1)))
    (and (< j (- N 1)) (not (black? i (+ j 1))))))

(defn renumber []
  (def n 1)
  (doseq [i j] board-iter
    (if (or (start-across? i j) (start-down? i j))
      (do 
        (set-number i j n)
        (def n (+ n 1)))
      (set-number i j nil))))

; Populate the board with empty cells
(doseq [i j] board-iter
  (def board (assoc board [i j] [" " nil])))

(defn main [args]
  (renumber)
  (doseq i (range N)
    (doseq j (range N)
      (print (board [i j])))
    (prn)))
