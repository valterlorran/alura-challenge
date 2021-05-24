(ns alura.logic
  (:import [java.util UUID]))

(defn has-limit?
  "Verify if the credit card has limit"
  [card payments amount]
  (let [total-spent (reduce + 0 (map #(:payment/amount %) payments))]
    (< amount (- (:card/limit card) total-spent))))

(defn make-card-number
  "Create a random credit card number"
  []
  (clojure.string/join " " (take 4
                                 (repeatedly #(+ 1000 (rand-int 8999))))))

(defn make-cvv
  "Create a random CVV"
  []
  (str (+ 100 (rand-int 899))))

(defn make-card
  "Create a new card"
  [customer limit]
  {:card/id         (UUID/randomUUID)
   ;:card/customer-id (:customer/id customer)
   :card/number     (make-card-number)
   :card/cvv        (make-cvv)
   :card/expiration "10/28"
   :card/limit      (bigint limit)})

(defn make-customer
  "Create a new customer"
  [name cpf email]
  {:customer/id (UUID/randomUUID)
   :customer/name name
   :customer/cpf cpf
   :customer/email email})

(defn make-payment
  "Make a payment for a given card"
  [merchant amount]
  {:payment/id         (UUID/randomUUID)
   :payment/merchant   merchant
   :payment/amount     amount
   :payment/created-at (bigint (quot (System/currentTimeMillis) 1000))})

(defn make-category
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
