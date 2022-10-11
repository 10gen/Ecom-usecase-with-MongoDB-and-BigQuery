
CREATE OR REPLACE TABLE ecommerce_prod.user_clusters_op OPTIONS() AS ## Training DATA WITH ViewStats AS
  (SELECT userId,
          COUNT(DISTINCT(gasessionid)) AS session_count,
          SUM((safe_cast(engagementTimeMsec AS FLOAT64))) AS total_time_spend_by_user_in_msec
   FROM -- Replace table name.
 `ecommerce_prod.events_flat_data`
   GROUP BY 1
   ORDER BY total_time_spend_by_user_in_msec DESC),
                                                                                           OrderStats AS
  (SELECT userId,
          avg(safe_cast(items.price AS FLOAT64)) AS average_order_value,
          count(distinct(orderId)) AS no_of_orders,
   FROM `ecommerce_prod.events_flat_data`,
        UNNEST(items) AS items
   WHERE event_name = 'purchase'
   GROUP BY 1
   ORDER BY no_of_orders DESC),
                                                                                           #Select *
FROM OrderStats UserStats AS
  (SELECT v.*,
          o.average_order_value,
          o.no_of_orders
   FROM ViewStats v
   FULL OUTER JOIN OrderStats o ON v.userId = o.userId
   ORDER BY o.no_of_orders DESC)
SELECT * EXCEPT(nearest_centroids_distance)
FROM ML.PREDICT(MODEL ecommerce_prod.user_clusters,
                  (SELECT *
                   FROM UserStats))