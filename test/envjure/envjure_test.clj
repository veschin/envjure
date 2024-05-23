(ns envjure-test
  (:require [clojure.test :as t]
            [envjure :as sut]
            [clojure.java.io :as io]))

(t/deftest normalize-test
  (t/is (= (sut/normalize "test_string") :test-string))
  (t/is (= (sut/normalize "test-string") :test-string))
  (t/is (= (sut/normalize "testString") :test-string))
  (t/is (= (sut/normalize "TestString") :test-string)))

(t/deftest coerce-test
  (t/is (= (sut/coerce {:test-int :int}
                       {:test-int "1"})
           {:test-int 1}))
  (t/is (= (sut/coerce {:test-int     #(Integer/parseInt %)
                        :test-keyword keyword}
                       {:test-int  "1"
                        :test-keyword "keyword"
                        :test "some"})
           {:test-int  1
            :test-keyword :keyword
            :test "some"})))

(t/deftest env-test
  (let [env-str (slurp (io/resource ".ENV"))] ;; => "TEST=1\nTEST_2=2\nTEST-3=3\n"
    (t/is
     (= (sut/read-env! env-str)
        {:test   "1"
         :test-2 "2"
         :test-3 "3"}))
    (t/is (= (sut/read-env! "=1") {}))
    (t/is (= (sut/read-env! "TEST=") {}))
    (t/is (= (sut/read-env! "=") {}))
    (t/is (= (sut/read-env! "") {}))))

(t/deftest gen-test
  (sut/gen! {:test "t"
             :test2 "t2"})
  (t/is (= (var-get #'envjure-test/config-)
           {:test "t"
            :test2 "t2"}))
  (t/is (= (var-get #'envjure-test/test) "t"))
  (t/is (= (var-get #'envjure-test/test2) "t2")))

(comment
  #_(remove-ns 'envjure-test)
  )
