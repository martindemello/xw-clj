(ns xw.swing.widgets
  (:import (javax.swing JOptionPane JDialog)))

(defn error-dialog [parent text]
  (. JOptionPane showMessageDialog parent text "Error" JOptionPane/ERROR_MESSAGE))

(defn message-dialog [parent text title]
  (. JOptionPane showMessageDialog parent text title JOptionPane/PLAIN_MESSAGE))

(defn dialog [child]
  (doto (JDialog.)
    (.add child)
    (.pack)
    (.setVisible true)))
