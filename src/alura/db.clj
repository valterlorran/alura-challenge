(ns alura.db)

(defprotocol StorageClient
  "Protocol for simple storage mechanism; simple but not practical in any way"
  (read-all   [storage]           "Return the entire contents of storage")
  (put!       [storage update-fn] "Mutate the storage with the provided function")
  (find-by-query [storage find-fn] "Return the result of a filter")
  (clear-all! [storage]           "Clear the storage"))

(defrecord InMemoryStorage [storage]
  StorageClient
  (read-all [_this] @storage)
  (find-by-query
    [_this find-fn]
    (filter find-fn (vals @storage)))
  (put! [_this update-fn] (swap! storage update-fn)))


(defn new-in-memory []
  (->InMemoryStorage (atom {})))

(def customer-db (new-in-memory))
(def card-db (new-in-memory))
(def payment-db (new-in-memory))

(defn add
  [db data]
  (put! db #(assoc % (:id data) data)))

(defn get-payments
  [get-fn]
  (find-by-query payment-db get-fn))

(defn get-cards
  [get-fn]
  (find-by-query card-db get-fn))

(defn add-customer
  [customer]
  (add customer-db customer))

(defn add-payment
  [payment]
  (add payment-db payment))

(defn add-card
  [card]
  (add card-db card))