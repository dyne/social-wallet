(ns social-wallet.components.pagination)

(defn pagination
  [total current uri]
  (let [start-page 1
        per-page 10]
    [:nav
     [:ul.pagination.justify-content-end
      ;; TODO: for now 10 per page fixed, maybe config
      ;; +1 because range starts from 0 and + 1 because per-page fits x times in total but we need x+1 pages
      (for [p (range start-page (+ 2 (quot total per-page)))]
        (if (= (str current) (str p))
          [:li.page-item.active [:a.page-link {:href "#"} p]]
          [:li.page-item [:a.page-link {:href (str uri "?page=" p)} p]]))]]))