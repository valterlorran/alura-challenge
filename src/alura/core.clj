(ns alura.core
  (:require [alura.controller :as controller]
            [alura.logic :as logic]
            [alura.db :as db])
  (:use clojure.pprint))

;(db/clear-db)

;(controller/populate-categories)

; Creates a new customer
(def customer
  (controller/create-customer
    {:name "valter lorran"
     :cpf "12898747699"
     :email "usersomething@nubank.com.br"}))

(def customer2
  (controller/create-customer
    {:name "João Something"
     :cpf "29832103823"
     :email "joao.something@nubank.com.br"}))

(def customer3
  (controller/create-customer
    {:name "Tereza Justina"
     :cpf "82382398998"
     :email "tereza.justina@nubank.com.br"}))

; Add a card to the customer
(def card
  (controller/add-credit-card customer))

(pprint (db/find-customer-by-id controller/conn (:customer/id customer)))
(pprint card)

; Simulates some payments
(controller/add-payment customer
                        card
                        {:merchant "Supermarket"
                         :amount   2000N
                         :category "Supermarket"})

(controller/add-payment customer
                        card
                        {:merchant "Supermarket 2"
                         :amount   1000N
                         :category "Supermarket"})

(controller/add-payment customer
                        card
                        {:merchant "Apple"
                         :amount   1500N
                         :category "Software"})

; Show all the payments for a given customer
(println ">>> payments")
;(pprint (controller/list-payments customer))
(println ">>> find without purchase")
(pprint (db/find-clients-without-purchase controller/conn))

