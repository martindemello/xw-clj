(ns xw.swing.ui
  (:import
     (javax.swing JButton JFrame JLabel JPanel JTextField JList JScrollPane
                  JOptionPane JDialog JSeparator SwingUtilities JFileChooser
                  BorderFactory JToolBar)
     (javax.swing.event DocumentListener ListSelectionListener)
     (java.awt Color Font GridLayout BorderLayout FlowLayout)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter FocusListener FocusAdapter)
     (java.awt.font TextLayout FontRenderContext))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener make-menubar make-action)]))
  (:use (xw board cursor wordlist clues))
  (:use (xw.swing grid events)))

(require '[clojure.contrib.str-utils2 :as s])

(declare update-wordlist)
(declare update-statusbar)
(declare update-clueword)

;;; -----------------------------------------
;;; Graphics
;;; -----------------------------------------

;; layout and widgets

; forward declarations
(def statusbar)
(def ui)
(def cluebox)
(def grid)
(def mf)
(def mainpanel)
(def gridpanel)
(def clueword)
(def clue)
(def wlist)
(def extended-grid-keyhandler)


; list of possible words
(def words (JList.))
(def wordlist-pattern "")

; statusbar
(defn init-status []
  (def status
    {:notification (JLabel. "Crossword Constructor")
     :gridlock     (JLabel. "UNLOCKED")
     :unsaved      (JLabel. " ") }))

(init-status)

(let [border (. BorderFactory createEtchedBorder)]
  (doseq [[_ l] status]
    (.setBorder l border)))

(defn make-widgets [scale]
  ; TODO: Fix padding!
  (def statusbar
    (let [panel (miglayout
                  (JPanel.) {:gap "0 0 0 0"}
                  (status :notification) :push :growx {:pad "0 0 0 0"}
                  (status :gridlock) {:pad "0 0 0 0"}
                  (status :unsaved) {:pad "0 0 0 0"})
          border (. BorderFactory createLoweredBevelBorder)]
      (.setBorder panel border)
      panel))

  (def cluebox
    (let [word (JTextField. 15)
          clue (JTextField. 40)
          panel (miglayout (JPanel.)
                           word {:id :word}
                           clue {:id :clue} :growx)]
      (.setEditable word false)
      (.setEditable clue false)
      panel))

  (def ui
    (let [panel (miglayout
                  (JPanel.)
                  (JToolBar. "Toolbar") {:id :toolbar}
                  (JPanel.) {:id :gridpanel} :growy :newline
                  (JScrollPane. words) {:id :wlist :width 200 :height 450}
                  cluebox :newline :span :growx
                  statusbar :newline :span :growx)
          frame (JFrame. "Crossword Editor")
          ]
      { :frame frame :panel panel}))

  (def mf (ui :frame))
  (def mainpanel (ui :panel))
  (def gridpanel ((components mainpanel) :gridpanel))
  (def toolbar ((components mainpanel) :toolbar))
  (def clueword ((components cluebox) :word))
  (def clue ((components cluebox) :clue))
  (def wlist ((components mainpanel) :wlist))

  (doto toolbar
    (.add (JButton. "Hello"))
    (.add (JButton. "World")))

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
  (.addFocusListener
    words
    (proxy [FocusAdapter] []
      (focusGained [e] (update-wordlist))))

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

; keyboard handler chained from grid keyboard handler
(defn on-ctrl-key [c]
  (cond
    (= c "R") (update-wordlist)
    (= c "L") (set-state :gridlock (not (state :gridlock)))))

(defn extended-grid-keyhandler [e]
  (let [c (char-of e)]
    (cond
      (ctrl? e) (on-ctrl-key c))
    (update-statusbar)
    (update-clueword (current-word))))

; update widgets
(defn update-statusbar []
  (.setText (status :gridlock) (if (state :gridlock) "LOCKED" "UNLOCKED"))
  (.setText (status :unsaved) (if (state :dirty) "*" " ")))

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

(defn exit [] (. System exit 0))

(defn save-file-handler [_]
  (let [fc (JFileChooser.)]
    (when (= (.showSaveDialog fc mf) JFileChooser/APPROVE_OPTION)
      (let [f (.getSelectedFile fc)]
        (save-to-file f)
        (update-statusbar)))))

(defn load-file-handler [_]
  (let [fc (JFileChooser.)]
    (when (= (.showOpenDialog fc mf) JFileChooser/APPROVE_OPTION)
      (let [f (.getSelectedFile fc)]
        (if (load-from-file f)
          (do
            (resize-grid scale)
            (goto-origin)
            (init-status))
          (. JOptionPane showMessageDialog mf "Could not load file" "Error" JOptionPane/ERROR_MESSAGE))
        (update-statusbar)
        (.repaint grid)))))

(defn new-file-handler [n]
  (init-board n)
  (resize-grid scale)
  (goto-origin)
  (init-status)
  (update-statusbar)
  (.repaint grid))

(defn show-about [_]
  (. JOptionPane showMessageDialog mf "Hello World" "About Crossword Editor" JOptionPane/PLAIN_MESSAGE))

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
    (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
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
  )
