
CREATE OR REPLACE TABLE ecommerce_prod.prod_affinity_op OPTIONS() AS #standardSQL
SELECT *
FROM ML.RECOMMEND(MODEL ecommerce_prod.product_affinity)