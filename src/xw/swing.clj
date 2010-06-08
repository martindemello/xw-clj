(ns xw.swing
  (:import
     (javax.swing JButton JFrame JLabel JPanel JTextField JList JScrollPane
                  JSeparator SwingUtilities JFileChooser)
     (java.awt BasicStroke Color Dimension Graphics Font Graphics2D RenderingHints
               GridLayout BorderLayout FlowLayout Polygon)
     (java.awt.geom AffineTransform Ellipse2D FlatteningPathIterator GeneralPath
                    Line2D PathIterator Point2D)
     (java.awt.image BufferedImage)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter)
     (java.awt.font TextLayout FontRenderContext))
  (:use (clojure.contrib
                    [duck-streams :only (spit)]
                    [miglayout :only (miglayout components)]
                    [swing-utils :only (add-key-typed-listener make-menubar make-action)]))
  (:use (xw globals board cursor wordlist)))


; -----------------------------------------
; Graphics
; -----------------------------------------

(def scale 30)
(def n (* N scale))
(def height (+ n 5))
(def width (+ n 5))
(def letter-font (new Font "Arial" (. Font PLAIN) 18))
(def number-font (new Font "Serif" (. Font PLAIN) 9))
(def text-color (. Color black))
(def ared   (new Color 255 0 0 192))
(def ablue  (new Color 0 0 255 192))
(def agreen (new Color 0 128 0 64))

; coordinate manipulations
(defn add2 [u v] [(+ (u 0) (v 0)) (+ (u 1) (v 1))])
(defn rot-90 ([[i j]] [(- j) i]))
(defn translate [poly x0 y0] (map #(add2 [x0 y0] %) poly))

(defn topleft [x y]
  [(* x scale) (* y scale)])

(defn topleft-inner [x y]
  (add2 (topleft x y) [1 1]))

(defn inner-square [i j]
  (+ i 1) (+ j 1) (- scale 1) (- scale 1))

(defmacro in-square [[i j] bg x y & body]
  `(let [[~i ~j] (topleft ~x ~y)]
     (doto ~bg
       ~@body)))

(defn draw-letter [bg x y l]
  (in-square [i j] bg x y
             (.setColor text-color)
             (.setFont letter-font)
             (.drawString l (+ i 11) (+ j (- scale 6)))))

(defn draw-number [bg x y n]
  (in-square [i j] bg x y
             (.setColor text-color)
             (.setFont number-font)
             (.drawString (pr-str n) (+ i 2) (+ j 12))))

(defn fill-square [bg x y color]
  (in-square [i j] bg x y
             (.setColor color)
             (.fillRect (+ i 1) (+ j 1) (- scale 1) (- scale 1))))

(defn border-square [bg x y color]
  (in-square [i j] bg x y
             (.setColor color)
             (.drawRect (+ i 1) (+ j 1) (- scale 2) (- scale 2))))

(def arrow
  (let [f (- scale 2) ; full, half, one-third, two-thirds
        h (/ f 2)
        o (/ f 3)
        t (/ (* f 2) 3)]
    [[h 0] [f h] [h f] [h t] [0 t] [0 o] [h o] [h 0]]))

(def downarrow (map rot-90 arrow))

(defn add-poly [bg poly col]
  (let [po (new Polygon)]
    (doseq [[i j] poly] (. po addPoint i j))
    (. bg setColor col)
    (. bg fillPolygon po)))

(defn arrow-ac [bg x0 y0]
  (add-poly bg (translate arrow x0 y0) ared))

(defn arrow-dn [bg x0 y0]
  (add-poly bg (translate downarrow (+ x0 scale) y0) ablue))

(defn draw-cursor [bg x y]
  (let [[i j] (topleft-inner x y)]
    ((if (across?) arrow-ac arrow-dn) bg i j)))

(defn black-square [bg x y]
  (fill-square bg x y (. Color black)))

(defn white-square [bg x y]
  (fill-square bg x y (. Color white))
  (when-not (blank? x y)
    (draw-letter bg x y (letter x y)))
  (when (numbered? x y)
    (draw-number bg x y (number x y))))

(defn square [bg x y]
  (if (black? x y)
    (black-square bg x y)
    (white-square bg x y)))

(defn render [g]
  (let [img (new BufferedImage width height (. BufferedImage TYPE_INT_ARGB))
        bg (. img getGraphics)]

    (doseq [[i j] board-iter]
      (square bg i j))

    (draw-cursor bg current-x current-y)

    (doseq [[x y] (symm current-x current-y)]
      (fill-square bg x y agreen))

    (. bg (setColor (. Color black)))
    (doseq [i (range 0 (+ n scale) scale)]
      (. bg drawLine 0 i n i)
      (. bg drawLine i 0 i n))

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))

;; keyboard handling
(defn char-of [e] (. KeyEvent getKeyText (. e getKeyCode)))
(defn modifier [e] (. e getModifiers))
(defn modtext [e] (. KeyEvent getKeyModifiersText (modifier e)))
(def CTRL (. InputEvent CTRL_MASK))
(defn ctrl? [e] (= CTRL (bit-and (modifier e) CTRL)))

(declare update-wlist)

(defn on-ctrl-key [c]
  (cond
    (= c "R") (update-wlist)))

(defn board-action [c]
  (cond
    (re-matches #"^[A-Za-z]$" c) (do (place-letter c) (move-forward))
    (= c "Space") (do (place-symm :black) (move-forward))
    (= c "Backspace") (do (move-back) (place-letter :empty))
    (= c "Delete") (place-letter :empty)
    (= c "Down")  (move-down)
    (= c "Up")    (move-up)
    (= c "Right") (move-right)
    (= c "Left")  (move-left)
    (= c "Enter") (flip-dir)))

;; layout and widgets
(def words (JList.))
(defn xw-ui []
  (let [panel (miglayout (JPanel.)
                         (JPanel.) {:id :gridpanel} :growy
                         (JScrollPane. words) {:id :wlist :width 200 :height height} )
        frame (JFrame. "Crossword Editor")
        ]
    { :frame frame :panel panel}))

(def ui (xw-ui))
(def mf (ui :frame))
(def panel (ui :panel))
(def gridpanel ((components panel) :gridpanel))
(def wlist ((components panel) :wlist))
(def gpanel  (proxy [JPanel] [] (paint [g] (render g))))
(def update-wlist #(let [w (take 26 (words-with (current-word)))]
                     (. words setListData (to-array w))))

(defn exit [] (. System exit 0))

(def key-listener
  (proxy [KeyAdapter] []
    (keyPressed [e]
                (let [c (char-of e)]
                  (cond
                    (ctrl? e) (on-ctrl-key c)
                    true (board-action c))
                  (. gpanel repaint)))))

(def mouse-listener
  (proxy [MouseAdapter] []
     (mouseClicked [e]
                   (. mf requestFocus))))

(defn save-file-handler [_]
  (let [fc (JFileChooser.)]
    (when (= (.showSaveDialog fc mf) JFileChooser/APPROVE_OPTION)
      (let [f (.getSelectedFile fc)]
        (println f)
        (spit f (board-to-str))))))

(defn load-file-handler [_]
  (str-to-board (slurp "test.xw"))
  (renumber)
  (. gpanel repaint))

(defn new-file-handler [_] (print "hello"))
(defn show-about [_] (print "world"))

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
    (.add panel)
    (.pack)
    (.setVisible true))

  (init-menu mf)

  (doto wlist
    (.add words))

  (doto gpanel
    (.setBackground (. Color white))
    (.setPreferredSize (new Dimension width height))
    (.repaint))

  (doto gridpanel
    (.setLayout (new BorderLayout))
    (.add gpanel (. BorderLayout CENTER)))

  (doto mf
    (.addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (exit))))
    (.setFocusable 'true)
    (.addKeyListener key-listener)
    (.addMouseListener mouse-listener)
    (.pack)
    (.show))

  (. mf requestFocus)
  )
