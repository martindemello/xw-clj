(ns xw.swing.wordlist
  (:import 
     (javax.swing JList)
     (java.awt Color))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener)]
          ))
  (:use (xw clues cursor wordlist))
  (:use (xw.swing events))
  (:require [xw.swing.grid :as grid]))

; list of possible words
(def words (JList.))
(def pattern "")

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

; and force an update when focused, to prevent filling in inconsistent values
; into the grid
(add-focus-listener words (fn [_] (update)))
