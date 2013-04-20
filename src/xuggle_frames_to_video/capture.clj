; --                                                              {{{1

;;      File        : xuggle-frames-to-video/capture.clj
;;      Maintainer  : Felix C. Stegerman <flx@obfusk.net>
;;      Date        : 2013-04-20
;;
;;      Copyright   : Copyright (C) 2013  Felix C. Stegerman
;;      Licence     : GPLv3

; --                                                              }}}1

(ns xuggle-frames-to-video.capture
  (:import java.awt.Rectangle java.awt.Robot java.awt.Toolkit) )

(def robot (Robot.))
(def toolkit (Toolkit/getDefaultToolkit))

(def screen-bounds (Rectangle. (.getScreenSize toolkit)))
(def screen-width  (.width  (.getScreenSize toolkit)))
(def screen-height (.height (.getScreenSize toolkit)))

(defn now [] (System/nanoTime))
(defn capture-image [] (.createScreenCapture robot screen-bounds))

(defn image-capture-stream [secs-to-run frame-rate]             ; {{{1
  (let [ start (now) ]
    (for [ x (range 0 (* secs-to-run (.getDouble frame-rate))) ]
      (do (Thread/sleep (/ 1000 (.getDouble frame-rate)))
          (println "capture " x)                              ;  DEBUG
          [ (capture-image) (- (now) start) ] ))))              ; }}}1

; vim: set tw=70 sw=2 sts=2 et fdm=marker :
