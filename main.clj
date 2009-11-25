(ns xw
  (:load "globals")
  (:load "board")
  (:load "cursor")
  (:load "wordlist")
  (:load "swing"))
(refer 'clojure.core)

; -----------------------------------------
; Load and Save
; -----------------------------------------

; -----------------------------------------
; main
; -----------------------------------------
;; Populate the board with empty cells
(doseq [[i j] board-iter]
  (set-board i j [:empty nil]))

(renumber)

(init-gui)
