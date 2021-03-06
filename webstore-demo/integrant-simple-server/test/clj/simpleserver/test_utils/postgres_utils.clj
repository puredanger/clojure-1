(ns simpleserver.test-utils.postgres-utils
  (:require [simpleserver.test-utils.test-data :as test-data]
            [simpleserver.test-utils.test-service :as test-service]
            [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

;; ******************************************************
;; Domain

(defn- init-product-groups-table [db product-groups]
  (log/debug "ENTER init-product-groups-table")
  (with-open [connection (jdbc/get-connection (:datasource db))]
    (doseq [pg product-groups]
      (sql/insert! connection :product_group {:id (first pg), :name (second pg)}))))

(defn- init-product-table [db products]
  (log/debug "ENTER init-product-table")
  (with-open [connection (jdbc/get-connection (:datasource db))]
    (doseq [product products]
      (sql/insert! connection :product {:id (nth product 0)
                                        :pg_id (nth product 1)
                                        :title (nth product 2)
                                        :price (Double/parseDouble (nth product 3))
                                        :a_or_d (nth product 4)
                                        :year (Integer/parseInt (nth product 5))
                                        :country (nth product 6)
                                        :g_or_l (nth product 7)}))))

(defn- empty-domain-tables [db]
  (log/debug "empty-domain-tables")
  (with-open [connection (jdbc/get-connection (:datasource db))]
    (jdbc/execute-one! connection ["DELETE FROM product"]))
  (with-open [connection (jdbc/get-connection (:datasource db))]
    (jdbc/execute-one! connection ["DELETE FROM product_group"])))

(defmethod test-service/init-domain :postgres [env]
  (log/debug "ENTER init-domain")
  (if (= (:profile env) :test)
    (let [db (get-in env [:service :domain :db])]
      (empty-domain-tables db)
      (init-product-groups-table db (test-data/product-groups))
      (doseq [pg-id (keys (test-data/product-groups))]
        (let [products (test-data/raw-products pg-id)]
          (init-product-table db products))))
    (throw (java.lang.UnsupportedOperationException. "You can reset sessions only in test environment!"))))

;; ******************************************************
;; Session

(defmethod test-service/get-sessions :postgres [env]
  (log/debug "ENTER get-sessions")
  (let [db (get-in env [:service :session :db])]
    (with-open [connection (jdbc/get-connection (:datasource db))]
      (jdbc/execute-one! connection ["SELECT token FROM session"]))))

(defmethod test-service/reset-sessions! :postgres [env]
  (log/debug "ENTER reset-sessions!")
  (if (= (:profile env) :test)
    (let [db (get-in env [:service :domain :db])]
      (with-open [connection (jdbc/get-connection (:datasource db))]
        (jdbc/execute-one! connection ["DELETE FROM session"])))
    (throw (java.lang.UnsupportedOperationException. "You can reset sessions only in test environment!")))
  )

;; ******************************************************
;; User

(defmethod test-service/get-users :postgres [env]
  (log/debug (str "ENTER -get-users"))
  )

(defmethod test-service/reset-users! :postgres [env]
  (log/debug (str "ENTER -reset-users!"))
  )

