(defproject xuggle-frames-to-video "0.0.1-SNAPSHOT"
  :description  "turn a stream of frame+timestamp into a video"
  :url          "https://github.com/noxqsgit/xuggle-frames-to-video"

  :licenses [ { :name "GPLv3", :distribution :repo
                :url "http://www.opensource.org/licenses/GPL-3.0" } ]

  :repositories {
    "xuggle" "http://xuggle.googlecode.com/svn/trunk/repo/share/java"
  }

  :dependencies [ [org.clojure/clojure "1.4.0"]
                  [xuggle/xuggle-xuggler "5.4"] ]

  :main xuggle-frames-to-video.core )
