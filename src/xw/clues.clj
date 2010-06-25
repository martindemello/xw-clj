(ns xw.clues)

(def clues {})

(defn add-clue [word clue]
  (def clues (assoc clues word clue)))

(defn clue-for [word]
  (or (clues word) ""))
