(ns xw.swing.widgets
  (:import
     (javax.swing JButton JFrame JLabel JPanel JTextField JList JScrollPane
                  JOptionPane JDialog JSeparator SwingUtilities JFileChooser
                  BorderFactory JToolBar JTabbedPane UIManager JTextArea)
     (java.awt Color Font GridLayout BorderLayout FlowLayout)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter)
     (java.awt.font TextLayout FontRenderContext))
  (:use (clojure.contrib
          [miglayout :only (miglayout components)]
          [swing-utils :only (add-action-listener make-menubar make-action)]
          [pprint :only (pprint)]
          ))
  (:use (xw board cursor wordlist clues words))
  (:use (xw.swing grid events)))

(defn file-dialog [parent arg f]
  (let [fc (JFileChooser.)
        sel (if (= arg :load)
              (.showOpenDialog fc parent)
              (.showSaveDialog fc parent))]
    (when (= sel JFileChooser/APPROVE_OPTION)
      (f (.getSelectedFile fc)))))

(defn error-dialog [parent text]
  (. JOptionPane showMessageDialog parent text "Error" JOptionPane/ERROR_MESSAGE))

(defn message-dialog [parent text title]
  (. JOptionPane showMessageDialog parent text title JOptionPane/PLAIN_MESSAGE))

; statusbar
(defn init-status []
  (def status
    {:notification (JLabel. "Crossword Constructor")
     :gridlock     (JLabel. "UNLOCKED")
     :unsaved      (JLabel. " ") })

  (let [border (. BorderFactory createEtchedBorder)]
    (doseq [[_ l] status]
      (.setBorder l border))))

(defn make-statusbar [status]
  (let [panel (miglayout
                (JPanel.) {:gap "0 0 0 0"}
                (status :notification) :push :growx {:pad "0 0 0 0"}
                (status :gridlock) {:pad "0 0 0 0"}
                (status :unsaved) {:pad "0 0 0 0"})
        border (. BorderFactory createLoweredBevelBorder)]
    (.setBorder panel border)
    panel))

(defn make-cluebox []
  (let [word (JTextField. 15)
        clue (JTextField. 40)
        panel (miglayout (JPanel.)
                         word {:id :word}
                         clue {:id :clue} :growx)]
    (.setEditable word false)
    (.setEditable clue false)
    panel))


