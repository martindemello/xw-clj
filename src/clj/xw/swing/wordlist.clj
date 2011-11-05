(ns xw.swing.wordlist
  (:import
     (javax.swing JList JScrollPane BorderFactory)
     (java.awt.event FocusAdapter)
     (java.awt Color))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener do-swing)]))
  (:use (xw clues cursor wordlist))
  (:use (xw.swing events common))
  (:require [xw.swing.grid :as grid]))

; list of possible words
(def wordbox)
(def words (JList.))
(def pattern "")
(def wlist-focused? false)

(defn update []
  (let [cw (current-word)]
  (when (not (= cw pattern))
    (def pattern cw)
    (let [w (take 26 (words-with cw))]
      (. words setListData (to-array w))))))

; the wordlist should fill the current word in when selected
(add-key-pressed-listener
  words
  (fn [e]
    (when (= (char-of e) "Enter")
      (when (= (current-word) pattern)
        (let [w (first (.getSelectedValues words))]
          (set-current-word w)
          (update)
          (.repaint words)
          (grid/repaint)
          (grid/request-focus))))))

(defn make []
  (let [wpanel (JScrollPane. words)]
    (doto words
      ; force an update when focused, to prevent filling in
      ; inconsistent values into the grid
      (.addFocusListener
        (proxy [FocusAdapter] []
          (focusGained [e]
                       (do-swing (update))
                       (.setBorder words red-border)
                       (def wlist-focused? true))
          (focusLost [e]
                     (.setBorder words black-border)
                     (def wlist-focused? false)))))
    (def wordbox wpanel)
    wpanel))
