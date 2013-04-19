(defproject xuggle-frames-to-video "0.0.1-SNAPSHOT"
  :description  "..."
  :url          "https://github.com/..."

  :licenses [ { :name "GPLv3", :distribution :repo
                :url "http://www.opensource.org/licenses/GPL-3.0" } ]

  :repositories {
    "xuggle" "http://xuggle.googlecode.com/svn/trunk/repo/share/java"
  }

  :dependencies [ [org.clojure/clojure "1.4.0"]
                  [xuggle/xuggle-xuggler "5.4"] ]

  :main xuggle-frames-to-video.core )
