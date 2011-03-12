(ns xw.swing.ui
  (:import
     (javax.swing JButton JFrame JLabel JPanel JTextField JList JScrollPane
                  JSeparator SwingUtilities JToolBar JTabbedPane UIManager
                  JTextArea JTable)
     (javax.swing.table AbstractTableModel)
     (java.awt Color Font GridLayout BorderLayout FlowLayout)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter)
     (java.awt.font TextLayout FontRenderContext))
;     (com.l2fprod.common.swing StatusBar))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener make-menubar make-action)]
          [pprint :only (pprint)]
          ))
  (:require [xw.swing.statusbar :as statusbar])
  (:use (xw board cursor wordlist clues words))
  (:use (xw.swing grid events widgets)))


(require '[clojure.contrib.str-utils2 :as s])

(declare update-wordlist)
(declare update-clueword)
(declare save-file-dialog)
(declare toggle-gridlock)
(declare update-cluelist)

;;; -----------------------------------------
;;; Graphics
;;; -----------------------------------------

;; layout and widgets

; forward declarations
(def ui)
(def cluebox)
(def grid)
(def mf)
(def mainpanel)
(def gridpanel)
(def clueword)
(def clue)
(def cluesheet)
(def cluetable)
(def cluelist [])
(def wlist)
(def extended-grid-keyhandler)


; list of possible words
(def words (JList.))
(def wordlist-pattern "")

(defn make-widgets [scale]
  ; TODO: Fix padding!
  (def cluebox   (make-cluebox))

  (def gridtab
    (miglayout
      (JPanel.)
      (JPanel.) {:id :gridpanel} :growy :newline
      (JScrollPane. words) {:id :wlist :width 200 :height 450}
      cluebox :newline :span :growx))

  (let [column-names ["" "Word" "Clue"]
          table-model (proxy [AbstractTableModel] []
                        (getColumnCount [] (count column-names))
                        (getRowCount [] (count cluelist))
                        (isCellEditable [row col] false)
                        (getColumnName [col] (nth column-names col))
                        (getValueAt [row col] (get-in cluelist [row col])))
        table (doto (JTable. table-model)
                (.setGridColor java.awt.Color/DARK_GRAY))
        ]
    (def cluesheet table)
    (def cluemodel table-model))

  (def cluetab
    (miglayout
      (JPanel.) {:id :cluepanel} :growy :newline
      (JScrollPane. cluesheet)))

  (def tabs
    (doto (JTabbedPane.)
      (.addTab "Grid" gridtab)
      (.addTab "Clues" cluetab)))

  (add-tab-change-listener tabs (fn [_] (update-cluelist)))

  (def ui
    (let [panel (miglayout
                  (JPanel.)
                  (JToolBar. "Toolbar") {:id :toolbar}
                  tabs :newline :span :growx
                  (statusbar/make) :newline :span :growx)
          frame (JFrame. "Crossword Editor")
          ]
      { :frame frame :panel panel}))

  (def mf (ui :frame))
  (def mainpanel (ui :panel))
  (def gridpanel ((components gridtab) :gridpanel))
  (def wlist ((components gridtab) :wlist))
  (def toolbar ((components mainpanel) :toolbar))
  (def clueword ((components cluebox) :word))
  (def clue ((components cluebox) :clue))

  (def save-button (doto (JButton. "Save") (on-action ev (save-file-dialog))))
  (def lock-button (doto (JButton. "Lock") (on-action ev (toggle-gridlock))))
  (doto toolbar
    (.add lock-button)
    (.add save-button))

  (def grid (make-grid scale extended-grid-keyhandler))

  ; the wordlist should fill the current word in when selected
  (add-key-pressed-listener
    words
    (fn [e]
      (when (= (char-of e) "Enter")
        (when (= (current-word) wordlist-pattern)
          (let [w (first (.getSelectedValues words))]
            (set-current-word w)
            (update-wordlist)
            (.repaint words)
            (.repaint grid)
            (.requestFocus grid))))))

  ; and force an update when focused, to prevent filling in inconsistent values
  ; into the grid
  (add-focus-listener words (fn [_] (update-wordlist)))

  ; the cluebox should track whether the current clue has been saved
  ; set bgcolor to pale yellow for saved and white for dirty
  (add-action-listener
    clue
    (fn [_]
      (add-clue (.getText clueword) (.getText clue))
      (.setBackground clue pale-yellow)))

  (add-indifferent-document-listener
    (.getDocument clue)
    (fn [e] (.setBackground clue (. Color white)))))

(defn toggle-gridlock []
  (set-state :gridlock (not (state :gridlock)))
  (statusbar/update))

; keyboard handler chained from grid keyboard handler
(defn on-ctrl-key [c]
  (cond
    (= c "R") (update-wordlist)
    (= c "L") (toggle-gridlock)))

(defn extended-grid-keyhandler [e]
  (let [c (char-of e)]
    (cond
      (ctrl? e) (on-ctrl-key c))
    (statusbar/update)
    (update-clueword (current-word))))

; update widgets
(defn update-wordlist []
  (let [cw (current-word)]
  (when (not (= cw wordlist-pattern))
    (def wordlist-pattern cw)
    (let [w (take 26 (words-with cw))]
      (. words setListData (to-array w))))))

(defn update-clueword [word]
  (if (and word (s/contains? word "."))
    (do
      (.setText clueword "")
      (.setText clue "")
      (.setEditable clue false))
    (do
      (.setText clueword word)
      (.setText clue (clue-for word))
      (.setEditable clue true))))

(defn update-cluelist []
  (def cluelist (active-cluelist))
  (.fireTableStructureChanged cluemodel))

(defn exit [] (. System exit 0))

(defn reinit-ui []
  (resize-grid scale)
  (goto-origin)
  (statusbar/init)
  (statusbar/update)
  (.repaint grid))

(defn save-file-dialog []
  (file-dialog
    mf :save
    (fn [f]
      (save-to-file f)
      (statusbar/update))))

(defn load-file-dialog []
  (file-dialog
    mf :load
    (fn [f]
      (if (load-from-file f)
        (reinit-ui)
        (error-dialog "Could not load file")))))

(defn save-file-handler [_]
  (save-file-dialog))

(defn load-file-handler [_]
  (load-file-dialog))

(defn new-file-handler [n]
  (init-board n)
  (reinit-ui))

(defn show-about [_]
  (message-dialog "Hello World" "About Crossword Editor"))

(defn init-menu [frame]
  (let [menubar-spec
        [{:name     "File"
          :mnemonic KeyEvent/VK_F
          :items
          [{:name       "New"
            :mnemonic   KeyEvent/VK_N
            :items
            [{:name "11x11" :handler (fn [_] (new-file-handler 11))}
             {:name "13x13" :handler (fn [_] (new-file-handler 13))}
             {:name "15x15" :handler (fn [_] (new-file-handler 15))}]}
           {:name       "Open"
            :mnemonic   KeyEvent/VK_O
            :short-desc "Open crossword"
            :long-desc  "Open a saved crossword"
            :handler    load-file-handler}
           {:name       "Save"
            :mnemonic   KeyEvent/VK_S
            :short-desc "Save file"
            :long-desc  "Save the file to disk. No-op if file not modified"
            :handler    save-file-handler}
           {} ; <- adds a separator
           {:name     "Exit"
            :mnemonic KeyEvent/VK_X
            :handler  (fn [_] (exit))}]}
         {:name     "Help"
          :mnemonic KeyEvent/VK_H
          :items    [{:name     "About"
                      :mnemonic KeyEvent/VK_A
                      :handler  show-about}]}]
        menubar      (make-menubar menubar-spec)]
    (doto frame
      (.setJMenuBar menubar)
      (.pack)
      (.setVisible true))))

(defn init-gui [sc]
  (make-widgets sc)

  (doto mf
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.add mainpanel)
    (.pack)
    (.setVisible true))

  (init-menu mf)

  (doto wlist
    (.add words))

  (doto gridpanel
    (.setLayout (new BorderLayout))
    (.add grid (. BorderLayout CENTER)))

  (doto mf
    (.addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (exit))))
    (.pack)
    (.show))

  (doto grid
    (.repaint)
    (.requestFocus))

  (UIManager/setLookAndFeel "com.seaglasslookandfeel.SeaGlassLookAndFeel")
  )
