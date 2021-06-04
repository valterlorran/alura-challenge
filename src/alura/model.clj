(ns alura.model
  (:require [schema.core :as s])
  (:import [java.util UUID]))

(s/def Cvv s/Str)
(s/def CardNumber s/Str)
(s/def Cpf s/Str)
(s/def Merchant s/Str)

(s/def Card
  {:card/id         s/Uuid
   :card/number     CardNumber
   :card/cvv        Cvv
   :card/expiration s/Str
   :card/limit      s/Num})

(s/def Customer
  {:customer/id s/Uuid
   :customer/name s/Str
   :customer/cpf Cpf
   :customer/email s/Str})

(s/def Payment
  {:payment/id         s/Uuid
   :payment/merchant   Merchant
   :payment/amount     s/Num
   :payment/created-at s/Num})

(s/def Category
  {:category/id         s/Uuid
   :category/name   s/Str})
