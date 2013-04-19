(ns xuggle-frames-to-video.core                                 ; {{{1
  (:gen-class)
  (:import  java.awt.image.BufferedImage
            java.awt.Rectangle
            java.awt.Robot
            java.awt.Toolkit
            java.io.File
            javax.imageio.ImageIO
            com.xuggle.xuggler.ICodec
            com.xuggle.xuggler.ICodec$ID
            com.xuggle.xuggler.ICodec$Type
            com.xuggle.xuggler.IContainer
            com.xuggle.xuggler.IContainer$Type
            com.xuggle.xuggler.IPacket
            com.xuggle.xuggler.IPixelFormat
            com.xuggle.xuggler.IPixelFormat$Type
            com.xuggle.xuggler.IRational
          ; com.xuggle.xuggler.IStream
            com.xuggle.xuggler.IStreamCoder
            com.xuggle.xuggler.IStreamCoder$Flags
          ; com.xuggle.xuggler.IVideoPicture
            com.xuggle.xuggler.video.ConverterFactory
          ; com.xuggle.xuggler.video.IConverter
  ))                                                            ; }}}1

(defn read-image [filename] (ImageIO/read (File. filename)))

(def robot (Robot.))
(def toolkit (Toolkit/getDefaultToolkit))
(def screenBounds (Rectangle. 800 600))

(defn capture-image [] (.createScreenCapture robot screenBounds))

(defn convert-to-type [img tp]
  (if (= (.getType img) tp)
    img
    (let [ img' (BufferedImage. (.getWidth img) (.getHeight img) tp) ]
      (.drawImage (.getGraphics img') img 0 0 nil) ) ))

(defn encode-image [outStreamCoder outContainer image duration] ; {{{1
  (let [  packet    (IPacket/make)
          image'    (convert-to-type image
                      BufferedImage/TYPE_3BYTE_BGR)
          conv      (ConverterFactory/createConverter image'
                      IPixelFormat$Type/YUV420P)
          outFrame  (.toPicture conv image' duration) ]
    (.setQuality outFrame 0)
    (.encodeVideo outStreamCoder packet outFrame 0)
    (when (.isComplete packet)
      (.writePacket outContainer packet) )))                    ; }}}1

(defn encode-stream [outFile stream]                            ; {{{1
  (let  [ outContainer (IContainer/make) ]
    (.open outContainer outFile IContainer$Type/WRITE nil)
    (let [  outStream       (.addNewStream outContainer 0)
            outStreamCoder  (.getStreamCoder outStream)
            codec           (ICodec/guessEncodingCodec
                              nil nil outFile nil
                              ICodec$Type/CODEC_TYPE_VIDEO)
            codec           (ICodec/findEncodingCodec
                              ICodec$ID/CODEC_ID_FLASHSV) ]
      (doto outStreamCoder
        (.setNumPicturesInGroupOfPictures 30)
        (.setCodec codec)
        (.setBitRate 25000)
        (.setBitRateTolerance 9000)
        (.setPixelType IPixelFormat$Type/YUV420P)
        (.setHeight 600)
        (.setWidth 800)
        (.setFlag IStreamCoder$Flags/FLAG_QSCALE, true)
        (.setGlobalQuality 0)
        (.setFrameRate  (IRational/make 3 1)) ; ???
        (.setTimeBase   (IRational/make 1 3)) ; ???
        (.open) )
      (.writeHeader outContainer)
      (doseq [x stream]
        (let [  [imgFile durStr]  (clojure.string/split x #"\s+")
              ; image             (read-image imgFile)
                image             (capture-image)
                duration          (Integer. durStr) ]
          (encode-image outStreamCoder outContainer image duration) ))
      (.writeTrailer outContainer) )))                          ; }}}1

(defn -main [outFile]
  (encode-stream outFile (line-seq (java.io.BufferedReader. *in*)) ))
