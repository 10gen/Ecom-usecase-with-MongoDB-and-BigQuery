CREATE OR REPLACE MODEL ecommerce_prod.user_clusters OPTIONS(model_type='kmeans', kmeans_init_method = 'KMEANS++', warm_start=TRUE) AS WITH IncrementalFlatData AS
  (SELECT orderId,
          orderDate,
          safe_cast(itemQuantity AS INT64) AS itemQuantity,
          ordertotal,
          safe_cast(uniqueItemsQuantity AS INT64) AS uniqueItemsQuantity,
          'purchase' AS event_name,
          #lineItems AS items,
          array
     (SELECT AS struct productId AS productId, productName AS productName, brand AS brand, category AS category, #json_value(trim(items, '"'), '$.price') AS price, price AS price, #json_value(trim(items, '"'), '$.quantity') AS quantity, cast(quantity AS INT64) AS quantity,
      FROM unnest(lineItems) items) items,
          ga_session_id AS gasessionid,
          engagement_time_msec AS engagementTimeMsec,
          u.* EXCEPT(_id)
   FROM
     (SELECT *
      FROM APPENDS(TABLE ecommerce_prod.orders, NULL, NULL)
      WHERE _CHANGE_TIMESTAMP > cast(DATE_ADD(CURRENT_DATE(), INTERVAL -1 DAY)AS TIMESTAMP) ) ord
   JOIN `ecommerce_prod.users` u ON ord.userId = u.userId),
                                                                                                                                            ViewStats AS
  (SELECT userId,
          COUNT(DISTINCT(gasessionid)) AS session_count,
          SUM((safe_cast(engagementTimeMsec AS FLOAT64))) AS total_time_spend_by_user_in_msec
   FROM -- Replace table name.
 IncrementalFlatData
   GROUP BY 1
   ORDER BY total_time_spend_by_user_in_msec DESC),
                                                                                                                                            OrderStats AS
  (SELECT userId,
          avg(safe_cast(items.price AS FLOAT64)) AS average_order_value,
          count(distinct(orderId)) AS no_of_orders,
   FROM IncrementalFlatData,
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
SELECT * EXCEPT(userId)
FROM UserStats