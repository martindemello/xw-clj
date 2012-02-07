(ns xw.swing.cluebox
  (:import
     (javax.swing JPanel JTextField)
     (java.awt.event FocusAdapter)
     (java.awt Color Insets))
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
  (def clue (JTextField. 41))
  (def clueword (JTextField. 15))

  (doto clue
    (.setEditable false)
    (.setFocusable false)
    (.setMargin (Insets. 4 4 4 4)))

  (doto clueword
    (.setEditable false)
    (.setFocusable false)
    (.setMargin (Insets. 4 4 4 4)))

  ; the cluebox should track whether the current clue has been saved
  ; set bgcolor to pale yellow for saved and white for dirty
  (add-action-listener
    clue
    (fn [_]
      (add-clue (.getText clueword) (.getText clue))
      (.setBackground clue pale-yellow)))

  (add-indifferent-document-listener
    (.getDocument clue)
    (fn [e] (.setBackground clue white)))

  ; for some reason, we need to keep setting the margin when focus is lost and
  ; gained, otherwise it resets to 0 when the main window loses focus
  (.addFocusListener clue
    (proxy [FocusAdapter] []
      (focusGained [e]
                   (.setMargin (Insets. 4 4 4 4))
                   (.setBorder clue red-border))
      (focusLost [e]
                 (.setMargin (Insets. 4 4 4 4))
                 (.setBorder clue black-border))))

  (def cluebox
    (miglayout (JPanel.)
               clueword {:id :word}
               clue {:id :clue} :growx))

  cluebox)

(defn deactivate []
  (doto clue
    (.setText "")
    (.setFocusable false)
    (.setEditable false))
  (doto clueword
    (.setBackground nil)
    (.setText "")))

(defn activate [word]
  (doto clue
    (.setText (clue-for word))
    (.setFocusable true)
    (.setEditable true))
  (doto clueword
    (.setBackground pale-yellow)
    (.setText word)))

(defn update [word]
  (if (or
        (= word "")
        (s/contains? word "."))
    (deactivate)
    (activate word)))
