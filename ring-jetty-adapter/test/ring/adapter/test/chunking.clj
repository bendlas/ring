(ns ring.adapter.test.chunking
  (:use [ring.adapter.test.jetty :only [with-server]]
        clojure.test)
  (:import (org.apache.http.client.methods HttpGet)
           (org.apache.http.impl.client DefaultHttpClient)))

(defn- lseq-handler [{uri :uri}]
  (case uri
    "/small" {:headers {"content-type" "text/plain"}
              :body (repeat 2  "(part-n)")}
    "/large" {:headers {"content-type" "text/plain"}
              :body (repeat 1024 "(part-n)")}))

(defn chunked-response? [uri]
  (let [client (DefaultHttpClient.)
        resp (.execute client (HttpGet. uri))]
    (.isChunked (.getEntity resp))))

(deftest test-chunking
  (with-server lseq-handler {:port 4347
                             :buffer-size 1024}
    (testing "No chunking in small responses"
      (is (chunked-response? "http://localhost:4347/large"))
      (is (not (chunked-response? "http://localhost:4347/small"))))))
