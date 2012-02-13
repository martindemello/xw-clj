(ns xw.common)

(defn explode [s] (map str (seq s)))

(defn join
  ([xs]     (apply str xs))
  ([sep xs] (join (interpose sep xs))))

(defn join-lines [s] (map str (filter #(not (= % \newline)) s)))


