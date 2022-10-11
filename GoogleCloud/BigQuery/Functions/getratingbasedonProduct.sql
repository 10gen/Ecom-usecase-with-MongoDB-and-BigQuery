CREATE FUNCTION ecommerce_prod.getratingbasedonProduct(product STRING) AS (
                                                                             (SELECT sum(cast(items.quantity AS FLOAT64))
                                                                              FROM `ecommerce_prod.events_flat_data`,
                                                                                   UNNEST(items) AS items
                                                                              WHERE event_name = 'purchase'
                                                                                AND items.productName = product ))