(ns energy-api.kafka
  (:require [jackdaw.streams :as js]
            [jackdaw.client :as jc]
            [jackdaw.client.log :as jcl]
            [jackdaw.admin :as ja]
            [jackdaw.serdes.edn :refer [serde]]
            [cheshire.core :as json]
            [energy-api.db :as db]))

;; The config for our Kafka Streams app
(def kafka-config
  {"application.id"            "manning-kafka-energy"
   "bootstrap.servers"         "localhost:9092"
   "default.key.serde"         "jackdaw.serdes.EdnSerde"
   "default.value.serde"       "jackdaw.serdes.EdnSerde"
   "cache.max.bytes.buffering" "0"})

;; Serdes tell Kafka how to serialize/deserialize messages
;; We'll just keep them as EDN
(def serdes
  {:key-serde   (serde)
   :value-serde (serde)})

;; Each topic needs a config. The important part to note is the :topic-name key.
(def device-events-topic
  (merge {:topic-name         "device-events"
          :partition-count    1
          :replication-factor 1
          :topic-config       {}}
         serdes))

;; An admin client is needed to do things like create and delete topics
(def admin-client (ja/->AdminClient kafka-config))

(defn publish-device-events! [device-id data]
  "Publish device charging events"
  (with-open [producer (jc/producer kafka-config serdes)]
    @(jc/produce! producer device-events-topic device-id data)))

(defn view-messages [topic]
  "View the messages on the given topic"
  (with-open [consumer (jc/subscribed-consumer
                         (assoc kafka-config "group.id" (str (java.util.UUID/randomUUID)))
                         [topic])]
    (jc/seek-to-beginning-eager consumer)
    (->> (jcl/log-until-inactivity consumer 100)
         (map :value)
         doall)))

(defn is-charging? [event-data]
  (-> event-data
      json/decode
      (get "charging")
      (> 0)))

(defn simple-device-topology [builder]
  (-> (js/kstream builder device-events-topic)
      (js/map-values (comp last clojure.string/split-lines))
      (js/map-values is-charging?)
      (js/for-each! (fn [[device-id charging-state]]
                      (db/write-device-state device-id charging-state)))))

(defn start! []
  "Starts the simple topology"
  (let [builder (js/streams-builder)]
    (simple-device-topology builder)
    (doto (js/kafka-streams builder kafka-config)
      (js/start))))

(defn stop! [kafka-streams-app]
  "Stops the given KafkaStreams application"
  (js/close kafka-streams-app))

(defn refresh-topics []
  (let [topics [device-events-topic]]
    (ja/delete-topics! admin-client topics)
    (ja/create-topics! admin-client topics)))

;; Start the topology
(def kafka-streams-app (start!))

(comment

  ;; View the purchases on the topic - there should be 4
  (view-messages device-events-topic)

  ;; Stop the topology
  (stop! kafka-streams-app))

