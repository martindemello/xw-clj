(ns xw.swing.widgets
  (:import
     (javax.swing JButton JFrame JLabel JPanel JTextField JList JScrollPane
                  JOptionPane JDialog JSeparator SwingUtilities BorderFactory
                  JToolBar JTabbedPane UIManager JTextArea)
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

(defn error-dialog [parent text]
  (. JOptionPane showMessageDialog parent text "Error" JOptionPane/ERROR_MESSAGE))

(defn message-dialog [parent text title]
  (. JOptionPane showMessageDialog parent text title JOptionPane/PLAIN_MESSAGE))

(defn make-cluebox []
  (let [word (JTextField. 15)
        clue (JTextField. 40)
        panel (miglayout (JPanel.)
                         word {:id :word}
                         clue {:id :clue} :growx)]
    (.setEditable word false)
    (.setEditable clue false)
    panel))


