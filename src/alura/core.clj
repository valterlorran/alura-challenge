(ns alura.core
  (:require [alura.controller :as controller]
            [alura.logic :as logic]
            [alura.db :as db])
  (:use clojure.pprint))

; Creates a new customer
(def customer
  (controller/create-customer
    {:name "valter lorran"
     :cpf "12898747699"
     :email "usersomething@nubank.com.br"}))

; Add a card to the customer
(def card
  (controller/add-credit-card customer))

; Simulates some payments
(controller/add-payment customer
                        card
                        {:merchant "Supermarket"
                         :amount   2000
                         :category "supermarket"})

(controller/add-payment customer
                        card
                        {:merchant "Supermarket 2"
                         :amount   1000
                         :category "supermarket"})

(controller/add-payment customer
                        card
                        {:merchant "Apple"
                         :amount   1500
                         :category "technology"})

; Gets the amount spent by category
(println ">>> get-amount-spent-by-category")
(pprint (controller/get-amount-spent-by-category customer "supermarket"))

; Show all the payments for a give customer
(println ">>> payments")
(pprint (controller/list-payments customer))

; Get the total spent this month
(println ">>> monthly invoice")
(pprint (controller/get-month-invoice customer))

; Search by amount
(println ">>> search by amount")
(pprint (controller/search-by-amount customer 1000))

