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
  (:import  java.awt.image.BufferedImage
            java.io.File
            java.util.concurrent.TimeUnit
            javax.imageio.ImageIO
            com.xuggle.mediatool.IMediaWriter
            com.xuggle.mediatool.ToolFactory
            com.xuggle.xuggler.IRational ))                     ; }}}1

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

(defn image-stream [lines]
  (for [ [i t] (partition 2 lines) ]
    (do (println "image:" i)                                  ;  DEBUG
        (println "time: " t)                                  ;  DEBUG
        [ (read-image i) (Long. t) ] )))

(defn -main [out-file w h fps]                                  ; {{{1
  (let [  s   (image-stream (line-seq (java.io.BufferedReader. *in*)))
          wi  (Integer. w), hi (Integer. h)
          fr  (IRational/make (Double. fps)) ]
    (println "output file:" out-file)
    (println "width:      " wi)
    (println "height:     " hi)
    (println "frame-rate: " fr)
    (encode-stream out-file s wi hi fr)
    (println "done.") nil ))                                    ; }}}1

; vim: set tw=70 sw=2 sts=2 et fdm=marker :
