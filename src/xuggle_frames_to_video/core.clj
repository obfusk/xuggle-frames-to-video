; --                                                              {{{1

;;      File        : xuggle-frames-to-video/core.clj
;;      Maintainer  : Felix C. Stegerman <flx@obfusk.net>
;;      Date        : 2013-04-19
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
            javax.imageio.ImageIO
            com.xuggle.xuggler.ICodec
            com.xuggle.xuggler.ICodec$ID
            com.xuggle.xuggler.ICodec$Type
            com.xuggle.xuggler.IContainer
            com.xuggle.xuggler.IContainer$Type
            com.xuggle.xuggler.IPacket
            com.xuggle.xuggler.IPixelFormat$Type
            com.xuggle.xuggler.IRational
            com.xuggle.xuggler.IStreamCoder$Flags
            com.xuggle.xuggler.video.ConverterFactory ))        ; }}}1

; (set! *warn-on-reflection* true)                              ; TODO

; --

(defmacro -nlz [x & [msg]]
  (let [ m (or msg "returned non-zero") ]
    `(let [ x# ~x ] (if (< x# 0) (throw (Exception. ~m)) x# )) ))

(defmacro -nn [x & [msg]]
  (let [ m (or msg "returned nil") ]
    `(let [ x# ~x ] (if (nil? x#) (throw (Exception. ~m)) x# )) ))

; --

(defn conv-img-to-type [img t]                                  ; {{{1
  (if (= (.getType img) t)
    img
    (let [ img' (BufferedImage. (.getWidth img) (.getHeight img) t) ]
      (.drawImage (.getGraphics img') img 0 0 nil) img' )))     ; }}}1

(defn flip-rat [x]
  (IRational/make (.getDenominator x) (.getNumerator x)) )

(defn read-image [filename]
  (ImageIO/read (File. filename)))

; --

; TODO {

(def robot (Robot.))
(def toolkit (Toolkit/getDefaultToolkit))
(def screen-bounds (Rectangle. (.getScreenSize toolkit)))
(def width  (.width  (.getScreenSize toolkit)))
(def height (.height (.getScreenSize toolkit)))
(def frame-rate (IRational/make 3 1))

(defn capture-image []
  (.createScreenCapture robot screen-bounds))

(defn now [] (System/currentTimeMillis))

(defn image-capture-stream [n]                                  ; {{{1
  (let [ start (now) ]
    (for [ x (range 0 (* n (.getDouble frame-rate))) ]
      (do (Thread/sleep (/ 1000 (.getDouble frame-rate)))
          (println "capture #" x)
          [ (capture-image) (* 1000 (- (now) start)) ] ))))     ; }}}1

; } TODO

; --

; NB: duration in microseconds
(defn encode-image [out-stream-coder out-cont image time-stamp] ; {{{1
  (let [  image'    (conv-img-to-type image
                      BufferedImage/TYPE_3BYTE_BGR)             ;  ???
          packet    (IPacket/make)
          conv      (ConverterFactory/createConverter image'
                      IPixelFormat$Type/YUV420P)                ;  ???
          out-frame (.toPicture conv image' time-stamp) ]
    (println "(encode)")
    (.setQuality out-frame 0)
    (-nlz (.encodeVideo out-stream-coder packet out-frame 0))
    (when (.isComplete packet)
      (println "(complete)")
      (-nlz (.writePacket out-cont packet)) )))                 ; }}}1

(defn encode-stream [out-file stream w h fr]                    ; {{{1
  (let  [ out-cont (IContainer/make) ]
    (-nlz (.open out-cont out-file IContainer$Type/WRITE nil))
    (let [  codec             (-nn (ICodec/guessEncodingCodec
                                    nil nil out-file nil
                                    ICodec$Type/CODEC_TYPE_VIDEO ))
            out-stream        (.addNewStream out-cont codec)
            out-stream-coder  (.getStreamCoder out-stream) ]
      (doto out-stream-coder
        (.setNumPicturesInGroupOfPictures 30)                   ;  ???
        (.setBitRate 25000)                                     ;  ???
        (.setBitRateTolerance 9000)                             ;  ???
        (.setPixelType IPixelFormat$Type/YUV420P)               ;  ???
        (.setHeight h) (.setWidth w)
        (.setFlag IStreamCoder$Flags/FLAG_QSCALE, true)         ;  ???
        (.setGlobalQuality 0)
        (.setFrameRate fr) (.setTimeBase (flip-rat fr)) )       ;  ???
      (-nlz (.open out-stream-coder nil nil))
      (-nlz (.writeHeader out-cont))
      (doseq [[image time-stamp] stream]
        (encode-image out-stream-coder out-cont image time-stamp) )
      (-nlz (.writeTrailer out-cont)) )))                       ; }}}1

; --

(defn image-stream [lines]
  (for [ [i t] (partition 2 lines) ]
    [ (read-image i) (Integer. t) ] ))

(defn -main [out-file]                                          ; TODO
  ; --> (image-stream (line-seq (java.io.BufferedReader. *in*)))
  (encode-stream out-file (image-capture-stream 100) width height
    frame-rate ))

; vim: set tw=70 sw=2 sts=2 et fdm=marker :
