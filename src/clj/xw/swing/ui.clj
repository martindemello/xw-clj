(ns xw.swing.ui
  (:import
     (javax.swing JFrame JPanel JScrollPane JTabbedPane UIManager)
     (java.awt Color BorderLayout)
     (java.awt.event WindowAdapter))
;     (com.l2fprod.common.swing StatusBar))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener make-menubar make-action)]
          [pprint :only (pprint)]
          ))
  (:require [xw.swing.grid :as grid])
  (:require [xw.swing.statusbar :as statusbar])
  (:require [xw.swing.file :as file])
  (:require [xw.swing.cluebox :as cluebox])
  (:require [xw.swing.wordlist :as wordlist])
  (:require [xw.swing.cluesheet :as cluesheet])
  (:require [xw.swing.toolbar :as toolbar])
  (:require [xw.swing.menu :as menu])
  (:use (xw board cursor wordlist clues words))
  (:use (xw.swing grid events widgets)))


(require '[clojure.contrib.str-utils2 :as s])

(declare update-wordlist)
(declare toggle-gridlock)

;;; -----------------------------------------
;;; Graphics
;;; -----------------------------------------

;; layout and widgets

; forward declarations
(def mf)
(def mainpanel)
(def gridpanel)
(def extended-grid-keyhandler)
(def on-grid-change)

(defn make-widgets [scale]
  ; TODO: Fix padding!

  (def gridtab
    (miglayout
      (JPanel.)
      (JPanel.) {:id :gridpanel} :growy :newline
      (wordlist/make) {:width 200 :height 455}
      (cluebox/make) :newline :span :growx))

  (def cluetab
    (miglayout
      (JPanel.) {:id :cluepanel} :growy :newline
      (JScrollPane. (cluesheet/make))))

  (def tabs
    (doto (JTabbedPane.)
      (.addTab "Grid" gridtab)
      (.addTab "Clues" cluetab)
      (add-tab-change-listener (fn [_] (cluesheet/update-cluelist)))))

  (def mf (JFrame. "Crossword Editor"))

  (def toolbar-buttons
    [["Save" #(file/save-file-dialog mf)]
     ["Lock" toggle-gridlock]])

  (def mainpanel
    (miglayout
      (JPanel.)
      (toolbar/make toolbar-buttons)
      tabs :newline :span :growx
      (statusbar/make) :newline :span :growx))

  (def gridpanel ((components gridtab) :gridpanel))

  (grid/make scale extended-grid-keyhandler on-grid-change))

(defn toggle-gridlock []
  (set-state :gridlock (not (state :gridlock)))
  (statusbar/update))

;; keyboard handler chained from grid keyboard handler
;; to handle app-level keystrokes (mostly ctrl-<key> shortcuts)

(defn on-ctrl-key [c]
  (cond
    (= c "R") (wordlist/update)
    (= c "L") (toggle-gridlock)))

(defn extended-grid-keyhandler [e]
  (let [c (char-of e)]
    (cond
      (ctrl? e) (on-ctrl-key c))))

(defn on-grid-change []
  (statusbar/update)
  (cluebox/update (current-word)))

(defn exit [] (. System exit 0))

(defn reinit-ui []
  (grid/resize grid/scale)
  (goto-origin)
  (statusbar/init)
  (statusbar/update)
  (grid/repaint))

(defn init-gui [sc]
  (make-widgets sc)

  (doto mf
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.add mainpanel)
    (.pack)
    (.setVisible true))

  (menu/make mf)

  (doto wordlist/wordbox
    (.add wordlist/words))

  (doto gridpanel
    (.setLayout (new BorderLayout))
    (.add grid/grid (. BorderLayout CENTER)))

  (doto mf
    (.addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (exit))))
    (.pack)
    (.show))

  (grid/repaint)
  (grid/request-focus)

  )
