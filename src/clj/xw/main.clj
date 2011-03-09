(ns xw.main
  (:gen-class)
  (:use xw.board)
  (:use xw.swing.ui)
  (:import (javax.swing SwingUtilities)))
                                        
; -----------------------------------------
; main
; -----------------------------------------
;; Populate the board with empty cells

(defn run-app []
  (init-board 15)
  (init-gui 30))

(defn -main []
  (SwingUtilities/invokeLater run-app))
