(def mf (MainFrame.))
(def gridpanel (.gridpanel mf))
(def words (.wordlist mf))
(def gpanel  (proxy [JPanel] [] (paint [g] (render g))))
(def update-wlist #(let [w (words-with (current-word))]
                       (. words setListData (to-array w))))

(def key-listener 
  (proxy [KeyAdapter] []
    (keyPressed [e]
                (let [c (char-of e)]
                  (board-action c)
                  (update-wlist)
                  (. gpanel repaint)))))

(defn init-gui []
  (doto gpanel
    (.setBackground (. Color white))
    (.setPreferredSize (new Dimension width height))
    (.repaint))

  (doto gridpanel
    (.setLayout (new BorderLayout))
    (.add gpanel (. BorderLayout CENTER)))

  (doto mf
    (.addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (. System exit 0))))
    (.setFocusable 'true)
    (.addKeyListener key-listener)
    (.pack)
    (.show))

  (update-wlist))

