(ns xw.grid
  (:use xw.common)
  (:use (clojure.contrib [duck-streams :only (slurp*)]))
  (:use clojure.contrib.math)
  (:require [clojure.string :as s]))

; load strings of the form
;   5 5
;   .....
;   .#.#.
;   .....
;   .#.#.
;   .....

(defn- list-to-vector2d [ls]
  (into [] (map (partial into []) ls)))

(defn load-from-string [s]
  (let [[xy & g] (s/split-lines s)
        [x y] (map read-string (s/split xy #" "))
        grid (list-to-vector2d g)]
    {:x x, :y y, :grid grid}))

(defn load-from-file [f]
  (load-from-string (slurp* f)))

; load compressed grids with bitmaps converted to hex
; first two chars are (char(x+32), char(y+32))
; remainder is the grid, with rows padded to four bits
(defn- strip-ws [s]
  (s/replace s #"\s" ""))

(defn- round4 [n] (int (* 4 (ceil (/ n 4)))))

(defn- lpad [s n p]
  (let [l (count s)]
    (if (< l n)
      (str (apply str (repeat (- n l) p)) s)
      s)))

(defn- hex-to-grid [s]
  (let [b (Integer/toBinaryString (Integer/parseInt s 16))
        b (lpad b 4 \0)]
    (s/replace (s/replace b \1 \#) \0 \.)))

(defn load-from-hex [f]
  (let [[x y & g] (seq (strip-ws f))
        x (- (int x) 32)
        y (- (int y) 32)
        g (map str g)
        g (apply str (map hex-to-grid g))
        g (map (partial take x) (partition (round4 x) g))
        grid (list-to-vector2d g)]
    {:x x :y y :grid grid}))
