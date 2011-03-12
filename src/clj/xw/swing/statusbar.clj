(ns xw.swing.statusbar
  (:import (javax.swing JLabel JPanel BorderFactory))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]))
  (:use (xw board cursor wordlist clues words))
  (:use (xw.swing events)))

(def status)

; statusbar
(defn init []
  (def status
    {:notification (JLabel. "Crossword Constructor")
     :gridlock     (JLabel. "UNLOCKED")
     :unsaved      (JLabel. " ") })

  (let [border (. BorderFactory createEtchedBorder)]
    (doseq [[_ l] status]
      (.setBorder l border))))

(defn make []
  (init)
  (let [panel (miglayout
                (JPanel.) {:gap "0 0 0 0"}
                (status :notification) :push :growx {:pad "0 0 0 0"}
                (status :gridlock) {:pad "0 0 0 0"}
                (status :unsaved) {:pad "0 0 0 0"})
        border (. BorderFactory createLoweredBevelBorder)]
    (.setBorder panel border)
    panel))

(defn update []
  (.setText (status :gridlock) (if (state :gridlock) "LOCKED" "UNLOCKED"))
  (.setText (status :unsaved) (if (state :dirty) "*" " ")))

