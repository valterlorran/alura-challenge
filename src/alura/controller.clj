(ns alura.controller
  (:require [alura.logic :as logic]
            [alura.db :as db])
  (:use clojure.pprint))

(def conn (db/create-conn))

(db/create-schema conn)

(pprint (db/find-all-customer conn))

(defn add-credit-card
  [customer]
  (let [card (logic/make-card customer 10000)]
    (db/add-card conn card customer)
    card))

(defn get-month-invoice
  [customer]
  (let [payments (db/get-payments conn customer)
        amount (logic/sum-all-payments payments)]
    {:amount amount
     :payments payments}))

(defn add-payment
  [customer card data]
  (let [payments (:payments (get-month-invoice customer))
        amount (:amount data)]
    (pprint payments)
    (if (logic/has-limit? card payments amount)
      (let [payment (logic/make-payment (:merchant data)
                                        (:amount data))
            category (db/find-category-by-name conn (:category data))]
        (db/add-payment conn payment customer card category)
        payment)
      false)))

(defn get-amount-spent-by-category
  [customer category]
  (let [payments (db/get-payments conn customer)]
    {:category category
     :amount (logic/sum-all-payments payments)}))

(defn create-customer
  [data]
  (let [customer (logic/make-customer (:name data)
                                      (:cpf data)
                                      (:email data))]
    (db/add-customer conn customer)
    customer))

(defn list-payments
  [customer]
  (let [payments (db/get-payments conn customer)]
    payments))

(defn search-by-amount
  [customer amount]
  (let [payments (db/get-payments conn customer)]
    {:payments payments
     :amount (logic/sum-all-payments payments)}))

(defn get-grouped-payments
  [customer]
  (let [payments (db/get-payments conn customer)]
    (logic/group-payments-by-category payments)))

(defn populate-categories
  []
  (db/transact conn [(logic/make-category "Electronics")
                     (logic/make-category "Software")
                     (logic/make-category "Supermarket")]))