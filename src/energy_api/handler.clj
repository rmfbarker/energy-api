(ns energy-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :refer [run-jetty]]
            [cheshire.core :as json]
            [energy-api.kafka :as k]
            [energy-api.db :as db]
            [ring.util.response :as r]))

(defroutes
  app-routes
  (GET "/api/event/:device-id" [device-id]
    (println "Get charge state for device" device-id)
    (let [charging-state (db/get-device-state device-id)]
      (if (nil? charging-state)
        (r/not-found "")
        (r/content-type
          (r/response
            (json/encode
              {:charging charging-state}))
          "application/json"))))

  (POST "/api/event/:device-id" [device-id :as r]
    (println "Received data for Device ID" device-id)
    (let [request-body (slurp (:body r))]
      (k/publish-device-events! device-id request-body))
    "You posted some data")

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults
                               [:security :anti-forgery] false))))

(defonce web-server (run-jetty #'app {:port  3000
                                      :join? false}))

(defn stop-server []
  (.stop web-server))

(defn restart-server []
  (stop-server)
  (alter-var-root #'web-server (run-jetty #'app {:port  3000
                                                 :join? false})))

(comment

  (use 'energy-api.handler :reload)


  (def charging-event {:processor4_temp  193,
                       :moduleL_temp     207,
                       :charging         84,
                       :processor2_temp  151,
                       :charging_source  "solar",
                       :moduleR_temp     151,
                       :SoC_regulator    26.541264,
                       :inverter_state   4,
                       :processor1_temp  12,
                       :current_capacity 10973,
                       :device_id        "22fb64e1-ef7c-4e73-b199-6424160a612c",
                       :processor3_temp  180})

  )