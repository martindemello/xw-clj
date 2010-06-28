(ns xw.swing.ui
  (:import
     (javax.swing JButton JFrame JLabel JPanel JTextField JList JScrollPane
                  JOptionPane JDialog JSeparator SwingUtilities JFileChooser
                  BorderFactory)
     (javax.swing.event DocumentListener)
     (java.awt Color Font GridLayout BorderLayout FlowLayout)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter FocusListener FocusAdapter)
     (java.awt.font TextLayout FontRenderContext))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-key-typed-listener add-action-listener
                                                     make-menubar
                                                     make-action)]))
  (:use (xw board cursor wordlist clues))
  (:use (xw.swing grid)))

(require '[clojure.contrib.str-utils2 :as s])

(declare update-wlist)
(declare update-status)
(declare update-clueword)

;;; -----------------------------------------
;;; Graphics
;;; -----------------------------------------

;; layout and widgets

; list of possible words
(def words (JList.))

; statusbar
(def status
  {:notification (JLabel. "hello")
   :gridlock     (JLabel. "UNLOCKED")
   :unsaved      (JLabel. " ") })

(let [border (. BorderFactory createEtchedBorder)]
  (doseq [[_ l] status]
    (.setBorder l border)))

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

(defn update-status []
  (.setText (status :gridlock) (if (state :gridlock) "LOCKED" "UNLOCKED"))
  (.setText (status :unsaved) (if (state :dirty) "*" " ")))

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
                (JPanel.) {:id :gridpanel} :growy
                (JScrollPane. words) {:id :wlist :width 200 :height height}
                cluebox :newline :span :growx
                statusbar :newline :span :growx)
        frame (JFrame. "Crossword Editor")
        ]
    { :frame frame :panel panel}))

(def mf (ui :frame))
(def mainpanel (ui :panel))
(def gridpanel ((components mainpanel) :gridpanel))
(def clueword ((components cluebox) :word))
(def clue ((components cluebox) :clue))
(def wlist ((components mainpanel) :wlist))

; keyboard handler chained from grid keyboard handler
(defn on-ctrl-key [c]
  (cond
    (= c "R") (update-wlist)
    (= c "L") (set-state :gridlock (not (state :gridlock)))))

(defn extended-grid-keyhandler [e]
  (let [c (char-of e)]
    (cond
      (ctrl? e) (on-ctrl-key c))
    (update-status)
    (update-clueword (current-word))))

(def grid (make-grid extended-grid-keyhandler))

(add-action-listener clue
                     (fn [_]
                       (add-clue (.getText clueword) (.getText clue))
                       (.setBackground clue pale-yellow)))

(.. clue getDocument
  (addDocumentListener
    (proxy [DocumentListener] []
      (insertUpdate  [e] (.setBackground clue (. Color white)))
      (removeUpdate  [e] (.setBackground clue (. Color white)))
      (changedUpdate [e] (.setBackground clue (. Color white))))))

(def update-wlist #(let [w (take 26 (words-with (current-word)))]
                     (. words setListData (to-array w))))

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
        (update-status)))))

(defn load-file-handler [_]
  (let [fc (JFileChooser.)]
    (when (= (.showOpenDialog fc mf) JFileChooser/APPROVE_OPTION)
      (let [f (.getSelectedFile fc)]
        (load-from-file f)
        (update-status)
        (.repaint grid)))))

(defn new-file-handler [_]
  (new-board)
  (update-status)
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
            :short-desc "New crossword"
            :long-desc  "Start a new crossword"
            :handler    new-file-handler}
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

(defn init-gui []
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