(ns xw.swing.cluesheet
  (:import 
     (javax.swing JPanel JTextField JTable)
     (javax.swing.table AbstractTableModel)
     (java.awt Color))
  (:use (xw clues words))
(:use (xw.swing events common)))


(def model)
(def view)
(def cluelist [])

(defn make []
  (let [column-names ["Sq" "Word" "Clue"]
        column-widths [48 200 600]
        table-model (proxy [AbstractTableModel] []
                      (getColumnCount [] (count column-names))
                      (getRowCount [] (count cluelist))
                      (isCellEditable [row col] false)
                      (getColumnName [col] (nth column-names col))
                      (getValueAt [row col] (get-in cluelist [row col])))
        table (JTable. table-model)
        ]

    (doto table
      (.setGridColor java.awt.Color/DARK_GRAY)
      (.setRowHeight 24)
      (.setAutoCreateColumnsFromModel false)
      (.setAutoResizeMode JTable/AUTO_RESIZE_LAST_COLUMN))

    (doto (. table getTableHeader)
      (.setReorderingAllowed false)
      (.setResizingAllowed false))

    (doseq [[col width] (map vector [0 1 2] column-widths)]
      (doto (.getColumn (.getColumnModel table) col)
        (.setPreferredWidth width)))

    (def view table)
    (def model table-model))
  view)

(defn update-cluelist []
  (def cluelist (active-cluelist))
  (.fireTableStructureChanged model))

