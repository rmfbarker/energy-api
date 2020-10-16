(defproject energy-api "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [fundingcircle/jackdaw "0.7.6"]
                 [seancorfield/next.jdbc "1.1.588"]
                 [org.postgresql/postgresql "42.2.17"]
                 ]

  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler energy-api.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
