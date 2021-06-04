(ns alura.logic
  (:import [java.util UUID])
  (:require [schema.core :as s]
            [alura.model :refer :all]))

(defn has-limit?
  "Verify if the credit card has limit"
  [card payments amount]
  (let [total-spent (reduce + 0 (map #(:payment/amount %) payments))]
    (< amount (- (:card/limit card) total-spent))))

(s/defn make-card-number :- CardNumber
  "Create a random credit card number"
  []
  (clojure.string/join " " (take 4
                                 (repeatedly #(+ 1000 (rand-int 8999))))))

(s/defn make-cvv :- Cvv
  "Create a random CVV"
  []
  (str (+ 100 (rand-int 899))))

(s/defn make-card :- Card
  "Create a new card"
  [customer limit]
  {:card/id         (UUID/randomUUID)
   :card/number     (make-card-number)
   :card/cvv        (make-cvv)
   :card/expiration "10/28"
   :card/limit      (bigint limit)})

(s/defn make-customer :- Customer
  "Create a new customer"
  [name cpf email]
  {:customer/id (UUID/randomUUID)
   :customer/name name
   :customer/cpf cpf
   :customer/email email})

(s/defn make-payment :- Payment
  "Make a payment for a given card"
  [merchant amount]
  {:payment/id         (UUID/randomUUID)
   :payment/merchant   merchant
   :payment/amount     amount
   :payment/created-at (bigint (quot (System/currentTimeMillis) 1000))})

(s/defn make-category :- Category
  "Creates a category"
  [name]
  {:category/id (UUID/randomUUID)
   :category/name name})

(defn sum-all-payments
  [payments]
  (reduce + (map #(:payment/amount %) payments)))

(defn sum-payments-by-category
  [[category payments]]

  {:category category
   :quantity (count payments)
   :amount (reduce + 0 (map #(:amount %) payments))})

(defn group-payments-by-category
  [payments]
  (->> payments
       (group-by :category)
       (map sum-payments-by-category)))

(defn convert-cents-to-reais
  [cents]
  (if (> cents 0)
    (/ cents 100)
    (throw (Exception. "Cents cannot be less than one"))))
