(ns envjure-test
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.test :as t]
   [envjure :as sut]
   [yaml.core :as yaml]))

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
  (def env-config (slurp (io/resource ".ENV"))) ;; => "TEST=1\nTEST_2=2\nTEST-3=3\n"
  (t/is
   (= (sut/read-env! env-config)
      {:test   "1"
       :test-2 "2"
       :test-3 "3"}))
  (t/is (= (sut/read-env! "=1") {}))
  (t/is (= (sut/read-env! "TEST=") {}))
  (t/is (= (sut/read-env! "=") {}))
  (t/is (= (sut/read-env! "") {})))

(t/deftest json-test
  (def json-config (json/generate-string {:Test "1"
                                          :testTest "2"
                                          :test_vec [1 2 3 4]}))
  (t/is (= (sut/read-json! json-config)
           {:test "1"
            :test-test "2"
            :test-vec [1 2 3 4]}))
  (t/is (= (sut/read-json! "test") :error)))

(t/deftest yaml-test
  (def yaml-config (yaml/generate-string {:Test "1"
                                          :testTest "2"
                                          :test_vec [1 2 3 4]}))
  (t/is (= (sut/read-yaml! yaml-config)
           {:test "1"
            :test-test "2"
            :test-vec [1 2 3 4]}))
  (t/is (= (sut/read-yaml! "test") :error)))

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
