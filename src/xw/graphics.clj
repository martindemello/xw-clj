(ns xw.graphics)

; coordinate manipulations
(defn add2 [u v] [(+ (u 0) (v 0)) (+ (u 1) (v 1))])
(defn rot-90 ([[i j]] [(- j) i]))
(defn translate [poly x0 y0] (map #(add2 [x0 y0] %) poly))

