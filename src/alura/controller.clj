(ns alura.controller
  (:require [alura.logic :as logic]
            [alura.db :as db]))

(defn add-credit-card
  [customer]
  (let [card (logic/make-card customer 10000)]
    (db/add-card card)
    card))

(defn add-payment
  [customer card data]
  (let [payments (db/get-payments #(= (:customer-id %) (:id customer)))
        amount (:amount data)]
    (if (logic/has-limit? card payments amount)
      (let [payment (logic/make-payment customer
                                        card
                                        (:merchant data)
                                        (:amount data)
                                        (:category data))]
        (db/add-payment payment)
        payment)
      false)))

(defn get-amount-spent-by-category
  [customer category]
  (let [payments (db/get-payments #(and (= (:category %) category)
                                        (= (:customer-id %) (:id customer))))]
    {:category category
     :amount (logic/sum-all-payments payments)}))

(defn create-customer
  [data]
  (let [customer (logic/make-customer (:name data)
                                      (:cpf data)
                                      (:email data))]
    (db/add-customer customer)
    customer))

(defn list-payments
  [customer]
  (let [payments (db/get-payments #(= (:customer-id %) (:id customer)))]
    payments))

(defn get-month-invoice
  [customer]
  (let [payments (db/get-payments #(= (:customer-id %) (:id customer)))
        amount (logic/sum-all-payments payments)]
    {:amount amount
     :payments payments}))

(defn search-by-amount
  [customer amount]
  (let [payments (db/get-payments #(and (= (:customer-id %) (:id customer))
                                       (= (:amount %) amount)))]
    {:payments payments
     :amount (logic/sum-all-payments payments)}))

(defn get-grouped-payments
  [customer]
  (let [payments (db/get-payments #(= (:customer-id %) (:id customer)))]
    (logic/group-payments-by-category payments)))