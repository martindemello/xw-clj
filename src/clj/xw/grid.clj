(ns xw.grid
  (:use xw.common)
  (:use (clojure.contrib [duck-streams :only (slurp*)]))
  (:require [clojure.string :as s]))

(defn load-from-string [s]
  (let [[xy & g] (s/split-lines s)
        [x y] (map read-string (s/split xy #" "))
        grid (into [] (map (partial into []) g))]
    {:x x, :y y, :grid grid}))

(defn load-from-file [f]
  (load-from-string (slurp* f)))
