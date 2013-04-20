; --                                                              {{{1

;;      File        : xuggle-frames-to-video/core.clj
;;      Maintainer  : Felix C. Stegerman <flx@obfusk.net>
;;      Date        : 2013-04-20
;;
;;      Copyright   : Copyright (C) 2013  Felix C. Stegerman
;;      Licence     : GPLv3

; --                                                              }}}1

(ns xuggle-frames-to-video.core                                 ; {{{1
  (:gen-class)
  (:import  java.awt.Rectangle                                  ; TODO
            java.awt.Robot                                      ; TODO
            java.awt.Toolkit                                    ; TODO
            java.awt.image.BufferedImage
            java.io.File
            java.util.concurrent.TimeUnit
            javax.imageio.ImageIO
            com.xuggle.mediatool.IMediaWriter
            com.xuggle.mediatool.ToolFactory
            com.xuggle.xuggler.IRational ))                     ; }}}1

; NB: timestamps in nanoseconds

; (set! *warn-on-reflection* true)                              ; TODO

; --

(defn conv-img-to-type [img t]                                  ; {{{1
  (if (= (.getType img) t)
    img
    (let [ img' (BufferedImage. (.getWidth img) (.getHeight img) t) ]
      (.drawImage (.getGraphics img') img 0 0 nil) img' )))     ; }}}1

(defn conv-img-compat [img]
  (conv-img-to-type img BufferedImage/TYPE_3BYTE_BGR) )         ;  ???

(defn read-image [filename] (ImageIO/read (File. filename)))

(defn encode-stream [out-file stream w h frame-rate]            ; {{{1
  (let [ writer (ToolFactory/makeWriter out-file) ]
    (.addVideoStream writer 0 0 frame-rate w h)
    (doseq [[image timestamp] stream]
      (.encodeVideo writer 0 (conv-img-compat image) timestamp
        TimeUnit/NANOSECONDS)
      (println "encoded image") )                             ;  DEBUG
    (.close writer) ))                                          ; }}}1

; --

; TODO {

(def robot (Robot.))
(def toolkit (Toolkit/getDefaultToolkit))
(def screen-bounds (Rectangle. (.getScreenSize toolkit)))

(def width  (.width  (.getScreenSize toolkit)))
(def height (.height (.getScreenSize toolkit)))
(def frame-rate (IRational/make 3 1))
(def secs-to-run 30)

(defn now [] (System/nanoTime))
(defn capture-image [] (.createScreenCapture robot screen-bounds))

(defn image-capture-stream [secs]                               ; {{{1
  (let [ start (now) ]
    (for [ x (range 0 (* secs (.getDouble frame-rate))) ]
      (do (Thread/sleep (/ 1000 (.getDouble frame-rate)))
          (println "capture #" x)
          [ (capture-image) (- (now) start) ] ))))              ; }}}1

; } TODO

; --

(defn image-stream [lines]
  (for [ [i t] (partition 2 lines) ]
    [ (read-image i) (Integer. t) ] ))

(defn -main [out-file]                                          ; TODO
  ; --> (image-stream (line-seq (java.io.BufferedReader. *in*)))
  (encode-stream out-file (image-capture-stream secs-to-run)
    width height frame-rate) nil )

; vim: set tw=70 sw=2 sts=2 et fdm=marker :
