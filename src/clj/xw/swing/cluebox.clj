(ns xw.swing.cluebox
  (:import 
     (javax.swing JPanel JTextField)
     (java.awt Color))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener)]
          ))
  (:use (xw clues))
  (:use (xw.swing events common)))

(require '[clojure.contrib.str-utils2 :as s])

(declare cluebox)
(declare clue)
(declare clueword)

(defn make []
  (def clue (JTextField. 40))
  (def clueword (JTextField. 15))
  (.setEditable clueword false)
  (.setEditable clue false)

  ; the cluebox should track whether the current clue has been saved
  ; set bgcolor to pale yellow for saved and white for dirty
  (add-action-listener
    clue
    (fn [_]
      (add-clue (.getText clueword) (.getText clue))
      (.setBackground clue pale-yellow)))

  (add-indifferent-document-listener
    (.getDocument clue)
    (fn [e] (.setBackground clue (. Color white))))
  
  (def cluebox 
    (miglayout (JPanel.)
               clueword {:id :word}
               clue {:id :clue} :growx))

  cluebox)

(defn update [word]
  (if (and word (s/contains? word "."))
    (do
      (.setText clueword "")
      (.setText clue "")
      (.setEditable clue false))
    (do
      (.setText clueword word)
      (.setText clue (clue-for word))
      (.setEditable clue true))))
