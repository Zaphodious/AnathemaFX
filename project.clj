(defproject anathemafx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main anathemafx.core
  :java-source-paths ["src/java"]
  :source-paths      ["src/clojure"]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/core.async "0.2.395"]
                 [io.replikativ/superv.async "0.2.3"]
                 [lytek "0.8.1-SNAPSHOT"]
                 [com.rpl/specter "0.13.1"]
                 [draconic-fx "0.5.0-SNAPSHOT"]
                 ;; https://mvnrepository.com/artifact/org.controlsfx/controlsfx
                 [net.mikera/imagez "0.12.0"]
                 [org.controlsfx/controlsfx "8.40.11"]
                 ])
