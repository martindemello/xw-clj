(ns xw.board
  (:use (clojure.contrib
          [duck-streams :only (slurp*)])))

(def N)
(def board (atom {}))
(def board-iter)
(declare init-board)

(def state {:gridlock nil, :dirty nil})

; accessors
(defn letter [i j] ((@board [i j]) 0))
(defn number [i j] ((@board [i j]) 1))
(defn black? [i j] (= (letter i j) :black))
(defn white? [i j] (not (black? i j)))
(defn blank? [i j] (= (letter i j) :empty))
(defn numbered? [i j] (not (= (number i j) nil)))
(defn set-state [k v] (def state (assoc state k v)))
(defn set-board [i j p]
  (when (not (and (state :gridlock) (or (= (p 0) :black) (black? i j))))
    (swap! board assoc [i j] p)
    (set-state :dirty true)))
(defn set-letter [i j l] (set-board i j [l (number i j)]))
(defn set-number [i j n] (set-board i j [(letter i j) n]))

; numbering
(defn start-across? [i j]
  (and
    (white? i j)
    (or (= i 0) (black? (- i 1) j))
    (and (< i (- N 1)) (white? (+ i 1) j))))

(defn start-down? [i j]
  (and
    (white? i j)
    (or (= j 0) (black? i (- j 1)))
    (and (< j (- N 1)) (white? i (+ j 1)))))

(defn start-sqr? [i j]
  (or (start-across? i j) (start-down? i j)))

(defn renumber []
  (reduce (fn [n [i j]]
            (if (start-sqr? i j) 
              (do (set-number i j n) (inc n))
              (do (set-number i j nil) n))) 1 board-iter))

; serialise/deserialise
(defn cell-to-str [[i j]]
  (let [l (letter i j)]
    (cond
      (= l :empty) "."
      (= l :black) "#"
      true l)))

(defn str-to-cell [s]
  (cond
    (= s ".") :empty
    (= s "#") :black
    true s))

(defn explode [s] (map str (seq s)))

(defn join
  ([xs]     (apply str xs))
  ([sep xs] (join (interpose sep xs))))

(defn join-lines [s] (map str (filter #(not (= % \newline)) s)))

(defn board-to-str []
  (let [rows (map join (partition N (map cell-to-str board-iter)))]
    (join "\n" rows)))

(defn fill-cell [[i j] c] (set-letter i j (str-to-cell c)))

; TODO: better error-handling for invalid savefiles
(defn read-board-from-str [s]
  (let [size (.indexOf s "\n")]
    (if (and (> size 2) (< size 16))
      (do
        (init-board size)
        (dorun (map fill-cell board-iter (join-lines s)))
        (renumber)
        true)
      false)))

(defn new-board [_ n]
  (reduce #(assoc %1 %2 [:empty nil]) {} (for [j (range n) i (range n)] [i j])))

(defn init-board [n]
  (swap! board new-board n)
  (def N n)
  (def board-iter (for [j (range N) i (range N)] [i j]))
  (def state {:gridlock nil, :dirty nil})
  (renumber))

(defn save-to-file [f]
  (spit f (board-to-str))
  (set-state :dirty nil))

(defn load-from-file [f]
  (let [ret (read-board-from-str (slurp* f))]
    (when ret
      (set-state :dirty nil))
    ret))
