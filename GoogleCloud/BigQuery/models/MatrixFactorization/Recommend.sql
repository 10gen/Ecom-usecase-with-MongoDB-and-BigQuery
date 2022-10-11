#standardSQL
SELECT *
FROM ML.RECOMMEND(MODEL ecommerce_prod.product_affinity) ## RECOMMEND
FOR a USER #standardSQL
SELECT *
FROM ML.RECOMMEND(MODEL ecommerce_prod.product_affinity,
                    (SELECT '1019527.5799124267' AS userId))
ORDER BY predicted_rating_confidence DESC
LIMIT 5