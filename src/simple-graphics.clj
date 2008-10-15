(in-ns 'main)
(clojure/refer 'clojure)

(import
  '(java.awt BasicStroke Color Dimension Graphics Graphics2D RenderingHints)
  '(java.awt.geom AffineTransform Ellipse2D FlatteningPathIterator GeneralPath
                 Line2D PathIterator Point2D)
  '(java.awt.event WindowAdapter WindowEvent)
  '(java.awt.image BufferedImage)
  '(java.awt.font TextLayout FontRenderContext)
  '(javax.swing JFrame JPanel))

(def X 800)
(def Y 800)

(defn render [g]
  (let [img (new BufferedImage X Y (. BufferedImage TYPE_INT_ARGB))
        bg (. img getGraphics)]
    (. bg (setColor (. Color black)))
    (let [n 600]
      (doseq i (range 0 (+ n 40) 40)
        (. bg drawLine 0 i n i)
        (. bg drawLine i 0 i n)))

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))


(def panel (doto (proxy [JPanel] [] (paint [g] (render g)))
             (setBackground (. Color white))
             (setPreferredSize (new Dimension X Y))))

(def frame
  (doto (new JFrame "xwe")
    (add panel) (pack) (show)
    (addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (. System exit 0))))))
