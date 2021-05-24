(ns alura.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]))
(def db-uri "datomic:dev://localhost:4334/nubank-credit-card")
(defn create-conn []
    (d/create-database db-uri)
    (d/connect db-uri))

(defn clear-db []
  (d/delete-database db-uri))

(def schema
  [{:db/ident       :customer/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Account ID"}
   {:db/ident       :customer/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Account name"}
   {:db/ident       :customer/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Account email"}
   {:db/ident       :customer/cpf
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Account CPF"}
   {:db/ident       :customer/cards
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Credit cards"}
   {:db/ident       :customer/payments
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Payments"}

   ;; Card schema
   {:db/ident       :card/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Card ID"}
   {:db/ident       :card/number
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Card number"}
   {:db/ident       :card/cvv
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Card cvv"}
   {:db/ident       :card/expiration
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Card expiration date"}
   {:db/ident       :card/limit
    :db/valueType   :db.type/bigint
    :db/cardinality :db.cardinality/one
    :db/doc         "Card Limit"}

   ;category
   {:db/ident       :category/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Category ID"}
   {:db/ident       :category/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Category name"}

   ;payments
   {:db/ident       :payment/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Payment ID"}
   {:db/ident       :payment/card
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Card id"}
   {:db/ident       :payment/merchant
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Merchant"}
   {:db/ident       :payment/amount
    :db/valueType   :db.type/bigint
    :db/cardinality :db.cardinality/one
    :db/doc         "Amount paid"}
   {:db/ident       :payment/created-at
    :db/valueType   :db.type/bigint
    :db/cardinality :db.cardinality/one
    :db/doc         "Payment creation date"}
   {:db/ident       :payment/category
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Reference to category"}
   ])

(defn create-schema [conn]
  @(d/transact conn schema))

(defn readable-array
  [data-list]
  (->> data-list
       (map first)))

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
  [conn data]
  @(d/transact conn [data]))

(defn get-payments
  [conn customer]
  (readable-array (d/q
                    '[:find (pull ?payments [*])
                      :in $ ?customer-id
                      :where [?customer :customer/id ?customer-id]
                             [?customer :customer/payments ?payments]]
                    (d/db conn) (:customer/id customer))))

(defn get-cards
  [get-fn]
  (find-by-query card-db get-fn))

(defn add-customer
  [conn customer]
  (add conn customer))

(defn add-payment
  [conn payment customer card category]
  conj
  (add conn
       (conj payment {:payment/card     [:card/id (:card/id card)]
                      :payment/category [:category/id (:category/id category)]}))
  @(d/transact conn [[:db/add
                      [:customer/id (:customer/id customer)]
                      :customer/payments
                      [:payment/id (:payment/id payment)]]]))

(defn transact
  [conn transactions]
  (d/transact conn transactions))

(defn add-card
  [conn card customer]
  (println "adding card" (:customer/id customer) card)
  (add conn card)
  @(d/transact conn [[:db/add
                      [:customer/id (:customer/id customer)]
                      :customer/cards
                      [:card/id (:card/id card)]]]))

(defn find-customer-by-id [conn id]
  (d/q
    '[:find (pull ?customer [*])
      :in $ ?id
      :where [?customer :customer/id ?id]]
    (d/db conn) id))

(defn find-by-email [conn email]
  (d/q
    '[:find (pull ?account [*])
      :in $ ?email
      :where [?account :account/email ?email]]
    (d/db conn) email))


(defn find-card-by-customer [conn customer-id-request]
  (d/q
    '[:find ?card
      :in $ ?customer-id
      :where [?customer :customer/id ?customer-id]]
    (d/db conn) customer-id-request))

(defn find-category-by-name [conn category-name]
  (ffirst (d/q
            '[:find (pull ?category [*])
              :in $ ?category-name
              :where [?category :category/name ?category-name]]
            (d/db conn) category-name)))

(defn find-all-customer
  [conn]
  (readable-array (d/q
                    '[:find (pull ?category [*])
                      :in $
                      :where [?category :category/name ?category-name]]
                    (d/db conn))))

(defn find-clients-without-purchase
  [conn]
  (readable-array (d/q
                    '[:find (pull ?customer [*])
                      :in $
                      :where [?customer :customer/id]
                             (not [_ :customer/payments ?customer])]
                    (d/db conn))))

