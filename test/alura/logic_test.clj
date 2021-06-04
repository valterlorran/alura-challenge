(ns alura.logic-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :refer (defspec)]
            [clojure.test.check.properties :as prop]
            [alura.logic :refer :all]))

(deftest has-limit?-test
  (testing "Has enough limit for purchase"
    (let [card {:card/limit 1000}
          payments [{:payment/amount 100}]
          amount 800]
      (is (has-limit? card payments amount))))
  (testing "Not enough limit for purchase"
    (let [card {:card/limit 1000}
          payments [{:payment/amount 100}]
          amount 1000]
      (is (not (has-limit? card payments amount))))))

(deftest basic-data-generation
  (testing "Testing the credit card numbers"
    (is (= 19 (count (make-card-number)))))
  (testing "Make cvv number"
    (is (= 3 (count (make-cvv)))))
  (testing "Convert to reais error zero"
    (is (thrown? Exception (convert-cents-to-reais 0))))
  (testing "Convert to reais error negative"
    (is (thrown? Exception (convert-cents-to-reais -1)))))

(defspec make-category-test 50
  (prop/for-all [category-name gen/string]
                (contains? (make-category category-name)
                           :category/id)))


(defspec multiple-has-limit?-test 50
         (prop/for-all [limit (gen/large-integer* {:min 1000 :max 99999999})
                        amount (gen/large-integer* {:min 1 :max 999})]
                       (let [card {:card/limit limit}
                             payments []]
                         (has-limit? card payments amount))))

(defspec multiple-has-no-limit 50
         (prop/for-all [limit (gen/large-integer* 1000)
                        amount (gen/large-integer* {:min 1001})]
                       (let [card {:card/limit limit}
                             payments []]
                         (not (has-limit? card payments amount)))))

(defspec test-payments-total-sum 50
         (prop/for-all [payments (gen/vector (gen/large-integer* {:min 1 :max 999999}))]
                       (let [total (reduce + 0 payments)
                             map-payments (fn [payment]
                                            {:payment/amount payment})]
                         (= total (sum-all-payments (map map-payments payments))))))

(defn convert-cents-to-reais-ignoring-error
  [cents]
  (try
       (* 100 (convert-cents-to-reais cents))
       (catch Exception e
         cents)))

(defspec test-convert-cents-to-reais 50
         (prop/for-all [cents gen/large-integer]
                       (= cents (convert-cents-to-reais-ignoring-error cents))))

