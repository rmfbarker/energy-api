(ns energy-api.db
  (:require [next.jdbc :as jdbc]))

(def db {:dbtype "postgresql"
         :user   "postgres" :password "secret"})

(def ds (jdbc/get-datasource db))

(defn create-table []
  (jdbc/execute! ds ["CREATE TABLE devices (uuid varchar, state boolean)"]))

(defn drop-table []
  (jdbc/execute! ds ["DROP TABLE devices"]))

(defn write-device-state [device-id state]
  (println "Writing device state to DB" device-id state)
  (let [stmt (str "INSERT INTO devices(uuid,state) "
                  "VALUES('" device-id "'," state ")")]
    (jdbc/execute! ds [stmt])))

(defn get-device-state [device-id]
  (let [stmt (str "SELECT state "
                  "FROM devices "
                  "WHERE uuid = '" device-id "'")]
    (:devices/state
      (first
        (jdbc/execute! ds [stmt])))))

(comment

  (create-table)

  (write-device-state "foo2" false)
  (get-device-state "foo")

  (jdbc/execute! ds ["drop table devices"])
  (jdbc/execute! ds ["select * from devices"])

  (write-device-state "22fb64e1-ef7c-4e73-b199-6424160a612c", false)
  )