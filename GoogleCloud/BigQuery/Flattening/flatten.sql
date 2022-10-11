### flatten DATA
CREATE OR REPLACE TABLE ecommerce_prod.events_flat_data AS
SELECT orderId,
       orderDate,
       safe_cast(itemQuantity AS INT64) AS itemQuantity,
       ordertotal,
       safe_cast(uniqueItemsQuantity AS INT64) AS uniqueItemsQuantity,
       'purchase' AS event_name,
       #lineItems AS items,
       array
  (SELECT AS struct productId AS productId, productName AS productName, safe_cast(quantity AS INT64) AS quantity, brand AS brand, category AS category, #json_value(trim(items, '"'), '$.price') AS price, safe_cast(price AS FLOAT64) AS price, #json_value(trim(items, '"'), '$.quantity') AS quantity,
   FROM unnest(lineItems) items) items,
       NULL AS gaSessionId,
       NULL AS engagementTimeMsec,
       u.* EXCEPT(_id)
FROM `ecommerce_prod.orders` ord
JOIN `ecommerce_prod.users` u ON ord.userId = u.userId
UNION ALL
SELECT NULL AS orderId,
       eventDate AS orderDate,
       safe_cast(uniqueItems AS INT64) AS itemQuantity,
       NULL AS ordertotal,
       safe_cast(uniqueItems AS INT64) AS uniqueItemsQuantity,
       eventName,
       items,
       gaSessionId,
       engagementTimeMsec,
       u.* EXCEPT(_id)
FROM
  (SELECT gaSessionId,
          eventDate,
          eventTimestamp,
          eventName,
          userId,
          uniqueItems,
          engagementTimeMsec,
          ARRAY_AGG(STRUCT(productId, productName, safe_cast(quantity AS INT64) AS quantity, brand, category, safe_cast(price AS FLOAT64) AS price)) AS items
   FROM `ecommerce_prod.events`
   GROUP BY gaSessionId,
            eventDate,
            eventTimestamp,
            eventName,
            userId,
            uniqueItems,
            engagementTimeMsec) EVENTS
JOIN `ecommerce_prod.users` u ON events.userId = u.userId