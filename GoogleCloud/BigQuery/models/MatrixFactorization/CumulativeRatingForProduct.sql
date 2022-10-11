CREATE 
OR REPLACE TABLE ecommerce_prod.products_rating AS 
SELECT distinct
(product_recommended) as products,
   avg(predicted_rating_confidence) as rating 
FROM
   `ecomm - analysis.ecomm_prod.prod_affinity_op` 
group by
   product_recommended 
order by
   rating desc