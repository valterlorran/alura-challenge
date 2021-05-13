(ns alura.logic
  (:import [java.util UUID]))

(defn has-limit?
  "Verify if the credit card has limit"
  [card payments amount]
  (let [total-spent (reduce + 0 (map #(:amount %) payments))]
    (< amount (- (:limit card) total-spent))))

(defn make-card-number
  "Create a random credit card number"
  []
  (take 4
        (repeatedly #(+ 1000 (rand-int 8999)))))

(defn make-cvv
  "Create a random CVV"
  []
  (+ 100 (rand-int 899)))

(defn make-card
  "Create a new card"
  [customer limit]
  {:id (UUID/randomUUID)
   :customer-id (:id customer)
   :number (make-card-number)
   :cvv (make-cvv)
   :expiration "10/28"
   :limit limit})

(defn make-customer
  "Create a new customer"
  [name cpf email]
  {:id (UUID/randomUUID)
   :name name
   :cpf cpf
   :email email
   :cards {}})

(defn make-payment
  "Make a payment for a given card"
  [customer card merchant amount category]
  {:id (UUID/randomUUID)
   :customer-id (:id customer)
   :card-id (:id card)
   :merchant merchant
   :amount amount
   :created-at (quot (System/currentTimeMillis) 1000)
   :category category})

(defn sum-all-payments
  [payments]
  (reduce + (map #(:amount %) payments)))

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
