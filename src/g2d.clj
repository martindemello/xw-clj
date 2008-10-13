(in-ns 'viz)
(clojure/refer 'clojure)

(import '(java.awt Graphics2D Panel Dimension Color FontMetrics
		   BasicStroke RenderingHints Shape Font Polygon)
	'(java.awt.image BufferedImage)
	'(java.awt.geom Ellipse2D Ellipse2D$Float Arc2D Arc2D$Float
			Rectangle2D$Float CubicCurve2D$Float QuadCurve2D$Float)
	'(javax.swing JFrame JPanel)
	'(java.awt.event MouseMotionListener MouseListener MouseEvent))


(def frame-env)
(def width)
(def height)
(def ppmouseX)
(def ppmouseY)
(def pmouseX)
(def pmouseY)
(def mouseX)
(def mouseY)
(def mouseButton)

(defn with-ns [f]
  (if (not (namespace f))
    (symbol "viz" (name f))
    f))

(defn newColor 
  ([] nil)
  ([r g b]
   (new Color r g b))
  ([r g b a]
   (new Color r g b a))
  ([x]
   (new Color x x x))
  ([x a]
   (new Color x x x a)))

(defn background [& args]
  (let [gr (@frame-env :graphics)
	color (apply newColor args)]
    (. gr (setColor color))
    (. gr (setBackground color))
    (. gr
       (fillRect 0 0 width height))
    true))

(defn stroke [& args]
  (dosync 
   (alter frame-env assoc :strokeColor (apply newColor args))))

(defn fill [& args]
  (dosync 
   (alter frame-env assoc :fillColor (if (first args)
				       (apply newColor args)
				       nil))))

(defn line [x1 y1 x2 y2]
  (let [gr (@frame-env :graphics)]
    (. gr (setColor (@frame-env :strokeColor)))
    (. gr
       (drawLine x1 y1 x2 y2))))

(defn point [x y]
      (let [gr (@frame-env :graphics)]
	(. gr (setColor (@frame-env :strokeColor)))
	(. gr
	   (drawLine x y x y))))

(defn framerate [fps]
  (dosync 
   (alter frame-env assoc :framerate fps)))

(defn line-width [wi]
  (let [gr (@frame-env :graphics)
	stroke (@frame-env :stroke-style)
	new-stroke (new BasicStroke
			(float wi)
			(. stroke (getEndCap))
			(. stroke (getLineJoin)))]
    (dosync 
     (alter frame-env assoc :stroke-style new-stroke))
    (. gr
       (setStroke new-stroke))))

(defn line-join [join]
  (let [gr (@frame-env :graphics)
	stroke (. gr (getStroke))
	new-stroke (new BasicStroke
			(. stroke (getLineWidth))
			(. stroke (getEndCap))
			(cond (= join 'BEVEL)
			      (. BasicStroke JOIN_BEVEL)
			      (= join 'MITER)
			      (. BasicStroke JOIN_MITER)
			      (= join 'ROUND)
			      (. BasicStroke JOIN_ROUND)))]
    (dosync
     (alter frame-env assoc :stroke-style new-stroke))
    (. gr (setStroke new-stroke))))

(defn line-cap [cap]
  (let [gr (@frame-env :graphics)
	stroke (. gr (getStroke))
	new-stroke (new BasicStroke
			(. stroke (getLineWidth))
			(cond (= cap 'ROUND)
			      (. BasicStroke CAP_ROUND)
			      (= cap 'SQUARE)
			      (. BasicStroke CAP_BUTT)
			      (= cap 'PROJECT)
			      (. BasicStroke CAP_SQUARE))
			(. stroke (getLineJoin)))]
    (dosync
     (alter frame-env assoc :stroke-style new-stroke))
    (. gr (setStroke new-stroke))))

(defn smooth [val]
  (let [gr (@frame-env :graphics)
	aliasing-hint (if val
			(. RenderingHints VALUE_ANTIALIAS_ON)
			(. RenderingHints VALUE_ANTIALIAS_OFF))]
    (dosync 
     (alter frame-env assoc :smoothing aliasing-hint))
    (. gr
       (setRenderingHint 
	(. RenderingHints KEY_ANTIALIASING)
	aliasing-hint))))

(defn paintshape [shape]
  (let [gr (@frame-env :graphics)]
    (if (@frame-env :fillColor)
      (do (. gr (setColor (@frame-env :fillColor)))
	  (. gr (fill shape))))
    (if (@frame-env :strokeColor)
      (do (. gr (setColor (@frame-env :strokeColor)))
	  (. gr (draw shape))))))

(defn ellipse [x y w h]
  (let [gr (@frame-env :graphics)
	ellipse (new Ellipse2D$Float (- x (/ w 2)) (- y (/ h 2)) w h)]
    (paintshape ellipse)))

(defn arc [x y w h b e] 
  (let [gr (@frame-env :graphics)
	arc (new Arc2D$Float x y w h b e (. Arc2D PIE))]
    (paintshape arc)))

(defn rect [x y w h] 
  (let [gr (@frame-env :graphics)
	rect (new Rectangle2D$Float x y w h)]
    (paintshape rect)))

(defn triangle [x1 y1 x2 y2 x3 y3]
  (let [gr (@frame-env :graphics)
	triangle (new Polygon)]
    (. triangle (addPoint x1 y1))
    (. triangle (addPoint x2 y2))
    (. triangle (addPoint x3 y3))
    (println triangle)
    (paintshape triangle)))

(defn quad [x1 y1 x2 y2 x3 y3 x4 y4]
  (let [gr (@frame-env :graphics)
	quad (new Polygon)]
    (. quad (addPoint x1 y1))
    (. quad (addPoint x2 y2))
    (. quad (addPoint x3 y3))
    (. quad (addPoint x4 y4))
    (println quad)
    (paintshape quad)))

(defn cubiccurve [x1 y1 cx1 cy1 cx2 cy2 x2 y2]
  (let [gr (@frame-env :graphics)
	cubiccurve (new CubicCurve2D$Float x1 y1 cx1 cy1 cx2 cy2 x2 y2)]
    (paintshape cubiccurve)))

(defn quadcurve [x1 y1 cx1 cy1 x2 y2]
  (let [gr (@frame-env :graphics)
	quadcurve (new QuadCurve2D$Float x1 y1 cx1 cy1 x2 y2)]
    (paintshape quadcurve)))

(defn newFont 
  ([name]
     (let [font (if (@frame-env :font)
		  (@frame-env :font)
		  (. (@frame-env :graphics) (getFont)))
	   newfont (new Font name (. font (getStyle)) (. font (getSize)))]
       newfont))
  ([name style size]
     (let [newstyle (cond (= style 'PLAIN) (. Font PLAIN)
			  (= style 'BOLD)  (. Font BOLD)
			  (= style 'ITALIC)(. Font ITALIC))
	   newfont (new Font name newstyle size)]
       newfont)))

(defn font [& args]
  (let [font (apply newFont args)]
    (dosync
     (alter frame-env assoc :font font))))

(defn- decode-alignment [align direction]
  (if (= 'height direction)
    (if (contains? {'TOP true 'CENTER true 'BOTTOM true} align)
      align
      nil)
    (if (contains? {'LEFT true 'CENTER true 'RIGHT true} align)
      align
      nil)))

(defn text-alignment [& args]
  (dosync
   (alter frame-env assoc :text-x-alignment (decode-alignment (first args) 'width))
   (if (= 2 (count args))
     (alter frame-env assoc :text-y-alignment (decode-alignment (second args) 'height)))))

(defn text [text x y]
  (let [gr (@frame-env :graphics)]
    (if (@frame-env :font)
      (. gr (setFont (@frame-env :font))))
    (. gr (setColor (@frame-env :strokeColor)))
    (let [fm (. gr (getFontMetrics))
	  ra (. fm (getStringBounds text gr))
	  x-pos (cond (= (@frame-env :text-x-alignment) 'CENTER)
		      (- x (int (/ (. ra (getWidth)) 2)))
		      (= (@frame-env :text-x-alignment) 'RIGHT)
		      (- x (int (. ra (getWidth))))
		      true x)
	  y-pos (cond (= (@frame-env :text-y-alignment) 'TOP)
		      (+ y (int (. ra (getHeight))))
		      (= (@frame-env :text-y-alignment) 'CENTER)
		      (+ y (int (/ (. ra (getHeight)) 2)))
		      true y)]
      (. gr (drawString text x-pos y-pos)))))

(defn render [g]
  (let [env @frame-env
	run-once-routines (env :run-once-routines)
	render-routines (env :render-routines)]
    (dosync (alter frame-env assoc :graphics g))
    (. g (setStroke (env :stroke-style)))
    (. g (setRenderingHint 
	  (. RenderingHints KEY_ANTIALIASING)
	  (env :smoothing)))
    (if run-once-routines (run-once-routines))
    (dosync (alter frame-env assoc :run-once-routines nil))
    (if render-routines (render-routines))))

(defn make-panel [env]
  (proxy [JPanel MouseListener MouseMotionListener] []
    (paint [g] 
	   (binding [frame-env env
		     width ((@env :vars) `width)
		     height ((@env :vars) `height)
		     mouseX ((@env :vars) `mouseX)
		     mouseY ((@env :vars) `mouseY)
		     pmouseX ((@env :vars) `pmouseX)
		     pmouseY ((@env :vars) `pmouseY)
		     ppmouseX ((@env :vars) `ppmouseX)
		     ppmouseY ((@env :vars) `ppmouseY)
		     mouseButton ((@env :vars) `mouseButton)]
	      (render g)))
    (mouseClicked [e]
		  (if (env :mouse-click-routines)
		    (do
		      (dosync 
		       (alter env assoc :run-once-routines
			      (env :mouse-click-routines)))
		      (. this repaint))))
    (mouseReleased [e]
		   (dosync 
		    (alter env assoc 
			   :vars 
			   (assoc (env :vars)
			     `mouseButton nil))
		    (if (env :mouse-release-routines)
		      (alter env assoc :run-once-routines
			     (env :mouse-click-routines)))
		    (. this repaint)))
    (mousePressed [e]
		  (dosync 
		   (alter env assoc 
			  :vars
			  (assoc (env :vars) 
			    `mouseButton (. e (getButton))))
		   (if (env :mouse-press-routines)
		     (alter env assoc :run-once-routines
			    (env :mouse-press-routines)))
		   (. this repaint)))
    (mouseEntered [e]
		  (if (env :mouse-enter-routines)
		    (do
		      (dosync 
		       (alter env assoc :run-once-routines
			      (env :mouse-enter-routines)))
		      (. this repaint))))
    (mouseExited [e]
		 (if (env :mouse-exit-routines)
		    (do
		      (dosync 
		       (alter env assoc :run-once-routines
			      (env :mouse-exit-routines)))
		      (. this repaint))))
    (mouseMoved [e]
		(dosync
		  (if (env :mouse-move-routines)
		    (do
		      (alter env assoc :run-once-routines
			     (env :mouse-move-routines)))
		      (. this repaint))
		  (alter env assoc :vars
			 (assoc (env :vars) 
			   `ppmouseX ((env :vars) `pmouseX)
			   `ppmouseY ((env :vars) `pmouseY)
			   `pmouseX ((env :vars) `mouseX)
			   `pmouseY ((env :vars) `mouseY)
			   `mouseX (. e (getX))
			   `mouseY (. e (getY))))))
    (mouseDragged [e]
		(dosync
		  (if (env :mouse-drag-routines)
		    (do
		      (alter env assoc :run-once-routines
			     (env :mouse-drag-routines)))
		    (. this repaint))
		 (alter env assoc :vars
			(assoc (env :vars) 
			  `ppmouseX ((env :vars) `pmouseX)
			  `ppmouseY ((env :vars) `pmouseY)
			  `pmouseX ((env :vars) `mouseX)
			  `pmouseY ((env :vars) `mouseY)
			  `mouseX (. e (getX))
			  `mouseY (. e (getY))))))))

(defn new-panel [width height]
  (let [env (ref {:vars {`width width
			 `height height
			 `ppmouseX 0
			 `ppmouseY 0
			 `pmouseX 0
			 `pmouseY 0
			 `mouseX 0
			 `mouseY 0
			 `mouseButton nil}
		  :strokeColor (new Color 255 255 255)
		  :stroke-style (new BasicStroke
				     1.0
				     (. BasicStroke CAP_ROUND)
				     (. BasicStroke JOIN_ROUND))
		  :smoothing  (. RenderingHints VALUE_ANTIALIAS_OFF)
		  :framerate 2
		  :running true})
	panel (make-panel env)]
    (. panel (setPreferredSize 
	      (new Dimension width height)))
    (. panel (addMouseMotionListener panel))
    (. panel (addMouseListener panel))
    (dosync
     (alter env assoc :panel panel))
    (agent env)))
    
(defn new-frame [panel]
  (doto (new JFrame) 
    (add panel)
    (pack)
    (show)))

(defn rendering [env]
  (when (env :running)
    (send-off *agent* #'rendering))
  (. (env :panel) (repaint))
  (. Thread (sleep (/ 1000 (env :framerate))))
  env)

(defn lookup [v env]
  (cond (instance? Number v) v
	(and (symbol? v) 
	     (not (namespace v))) ((env :vars) (symbol "viz" (name v)))
	(symbol? v) ((env :vars) v)))

(defmacro arg-lookup [fun & args]
  `(~fun ~@(map (fn [x] (list 'lookup x 'env)) args)))

(defn set-render-routines [env routines]
  (dosync
   (alter env assoc :render-routines routines))
  env)

(defn set-routines [env routines functions]
  (dosync
   (alter env assoc routines functions))
  env)

(defmacro draw [agent & functions]
  `(send ~agent
	 #'set-render-routines
	 (fn [] ~@functions)))

(defmacro once [agent & functions]
  `(send ~agent
	 #'set-routines :run-once-routines
	 (fn [] ~@functions)))

(defmacro on-click [agent & functions]
  `(send ~agent
	 #'set-routines :mouse-click-routines
	 (fn [] ~@functions)))

(defmacro on-press [agent & functions]
  `(send ~agent
	 #'set-routines :mouse-press-routines
	 (fn [] ~@functions)))

(defmacro on-release [agent & functions]
  `(send ~agent
	 #'set-routines :mouse-release-routines
	 (fn [] ~@functions)))

(defmacro on-move [agent & functions]
  `(send ~agent
	 #'set-routines :mouse-move-routines
	 (fn [] ~@functions)))

(defmacro on-drag [agent & functions]
  `(send ~agent
	 #'set-routines :mouse-drag-routines
	 (fn [] ~@functions)))

(defn launch [width height & functions]
  (let [renderer (new-panel width height)]
    (new-frame (@renderer :panel))
    (send renderer #'set-routines :run-once-routines (first functions))
    (send-off renderer rendering)
    renderer))

(defmacro setup [width height & functions]
  `(launch ~width ~height ~(fn [] ~@functions)))
    
 (def fr (setup 600 600 (background 0)))
 (draw fr 
       (smooth true)
       (line-width 10)
       (framerate 30)
       (background 0)
       (let [x1 (- width (/ mouseX 3))
	     x2 (/ mouseX 1.5)
	     x3 (/ mouseX 2)
	     x4 mouseX
	     y1 (- height (/ mouseY 3))
	     y2 (/ mouseY 2)
	     y3 (/ mouseY 1.5)
	     y4 mouseY]
	 (fill false)
	 (cubiccurve x1 y1 (/ (+ x1 x2) 2) y1 (/ (+ x1 x2) 2) y2 x2 y2)
	 (cubiccurve x1 y1 x1 (/ (+ y1 y3) 2) x3 (/ (+ y1 y3) 2) x3 y3)
	 (quadcurve x2 y2 x4 y2 x4 y4)
	 (quadcurve x3 y3 x3 y4 x4 y4)
	 (fill 150 150 200)
	 (ellipse x1 y1 50 50)
	 (ellipse x2 y2 50 50)
	 (ellipse x3 y3 50 50)
	 (ellipse x4 y4 50 50)))

 (draw fr
       (smooth true)
       (framerate 60)
       (fill false)
       (line-width 10)
       (line-join 'BEVEL)
       (background 0 80)
       (stroke 200 100 100)
       (quadcurve ppmouseX ppmouseY pmouseX pmouseY mouseX mouseY))

 (draw fr
       (background 0)
       (if mouseButton
	 (text (print-str "button" mouseButton "pressed") mouseX mouseY)))

 (draw fr
       (background 0)
       (framerate 30)
       (smooth true)
       (stroke 128)
       (line-cap 'ROUND)
       (line-width 10)
       (cubiccurve 20 20 
		   mouseX mouseY
		   (- width mouseX) (- height mouseY)
		   (- width 20) (- height 20))
       (cubiccurve 20 (- height 20)
		   mouseY mouseX
		   (- height mouseY) (- width mouseX)
		   (- width 20) 20))

(on-click fr (smooth true) 
	  (line-width 5) 
	  (font "Times" 'BOLD 30)
	  (text-alignment 'CENTER 'CENTER)
	  (background 0) 
	  (ellipse mouseX (+ mouseY 5) 100 50)
	  (text (vvar mouseX) mouseX mouseY))

 (on-click fr (stroke 255)
	   (smooth true)
	   (line-width 3)
	   (text-alignment 'CENTER 'CENTER)
	   (dosync alter foo concat (list mouseX mouseY))
	   (point mouseX mouseY)
	   (if (= 2 (count foo))
	     (do (background 0 200)
		 (text "From here" mouseX mouseY)))
	   (if (= 4 (count foo))
	     (do (stroke 100 100 200) 
		 (apply line foo)))
	   (if (= 8 (count foo))
	     (do (stroke 255)
		 (apply cubiccurve foo) 
		 (stroke 100 100 200)
		 (line-width 3)
		 (apply line (nthrest foo 4))
		 (stroke 250 0 0)
		 (fill 128 128 128)
		 (ellipse mouseX mouseY 70 30)
		 (text "To here" mouseX mouseY)
		 (ellipse (first foo) (second foo) 70 30)
		 (text "From here" (first foo) (second foo))
		 (fill nil))))

