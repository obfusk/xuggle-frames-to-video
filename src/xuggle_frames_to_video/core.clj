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

(set! *warn-on-reflection* true)                                ; TODO

(defmacro nlz [x & [msg]]
  (let [ m (or msg "returned non-zero") ]
    `(let [ #x ~x ] (if (< #x 0) (throw (Exception. ~m)) #x )) ))

(defn flip-rat [x]
  (IRational/make (.getDenominator x) (.getNumerator x)) )

(defn read-image [filename]
  (ImageIO/read (File. filename)))

(defn conv-img-to-type [img t]                                  ; {{{1
  (if (= (.getType img) t)
    img
    (let [ img' (BufferedImage. (.getWidth img) (.getHeight img) t) ]
      (.drawImage (.getGraphics img') img 0 0 nil) img' )))     ; }}}1

; --

(def robot (Robot.))                                            ; TODO
(def toolkit (Toolkit/getDefaultToolkit))                       ; TODO
(def screen-bounds (Rectangle. (.getScreenSize toolkit)))       ; TODO
(def width  (.width  (.getScreenSize toolkit)))                 ; TODO
(def height (.height (.getScreenSize toolkit)))                 ; TODO
(def frame-rate (IRational/make 3 1))                           ; TODO

(defn capture-image []                                          ; TODO
  (.createScreenCapture robot screen-bounds))

; --

; NB: duration in microseconds
(defn encode-image [out-stream-coder out-cont image duration]   ; {{{1
  (let [  image'    (conv-img-to-type image
                      BufferedImage/TYPE_3BYTE_BGR)             ; TODO
          packet    (IPacket/make)
          conv      (ConverterFactory/createConverter image'
                      IPixelFormat$Type/YUV420P)                ; TODO
          out-frame (.toPicture conv image' duration) ]
    (.setQuality outFrame 0)
    (nlz (.encodeVideo out-stream-coder packet out-frame 0))
    (when (.isComplete packet)
      (nlz (.writePacket out-cont packet)) )))                  ; }}}1

(defn encode-stream [out-file stream w h fr]                    ; {{{1
  (let  [ out-cont (IContainer/make) ]
    (nlz (.open out-cont out-file IContainer$Type/WRITE nil))
    (let [  out-stream        (.addNewStream out-cont 0)
            out-stream-coder  (.getStreamCoder out-stream)
            codec             (ICodec/findEncodingCodec
                                ICodec$ID/CODEC_ID_FLASHSV) ]   ; TODO
      (doto out-stream-coder
        (.setNumPicturesInGroupOfPictures 30)                   ; TODO
        (.setCodec codec)
        (.setBitRate 25000)                                     ; TODO
        (.setBitRateTolerance 9000)                             ; TODO
        (.setPixelType IPixelFormat$Type/YUV420P)               ; TODO
        (.setHeight h) (.setWidth w)
        (.setFlag IStreamCoder$Flags/FLAG_QSCALE, true)         ; TODO
        (.setGlobalQuality 0)
        (.setFrameRate fr) (.setTimeBase (flip-rat fr)) )       ; TODO
      (nlz (.open out-stream-coder))
      (nlz (.writeHeader out-cont))
      (doseq [[image duration] stream]
        (encode-image out-stream-coder out-cont image duration) )
      (nlz (.writeTrailer out-cont)) )))                        ; }}}1

; --

(defn image-stream [lines]
  (for [ [i d] (partition 2 lines) ]
    [ (read-image i) (Integer. d) ] ))

(defn -main [out-file]
  ; (let [ lines (line-seq (java.io.BufferedReader. *in*)) ]
  ;   (encode-stream out-file (image-stream lines))
  (encode-stream out-file
    (for [x (range 0 10)] [ (capture-image) 1e6 ]) ))           ; TODO

; vim: set tw=70 sw=2 sts=2 et fdm=marker :
