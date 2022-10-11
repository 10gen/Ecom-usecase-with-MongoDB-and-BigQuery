# Ecom-usecase-with-MongoDB-and-BigQuery

**Machine Learning Models**


**User Clustering**

Loyalty programs often categorize users into discrete segments based on the user’s purchase behavior and site engagement, such as Platinum / Gold / Silver / Bronze tiers.  

In this example, we showcase how a ML driven clustering model can be applied to achieve user clustering.
K-means clustering
K-means is an unsupervised learning technique identifying customer segments, so model training does not require labels nor split data for training or evaluation.

 
**Step 1 :  Cluster Users based on following attributes**

The following attributes can reliably predict user behavior, and can be used to cluster users into various Loyalty levels:

Session count
Total time spent
Average order value
No of orders


 
Step 1 - Preprocess to training data**
           
WITH
ViewStats AS (
 
SELECT
 user_pseudo_id,
 COUNT(
   DISTINCT(SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'ga_session_id'))
   AS session_count,
 SUM(
   (SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'engagement_time_msec'
   )) AS total_time_spend_by_user_in_msec
 
 FROM
 -- Replace table name.
 `ecommerce_public.events_flat_data`
 
 GROUP BY
 1 ORDER bY total_time_spend_by_user_in_msec desc
)
,
 
OrderStats as (
 
 
SELECT
 user_pseudo_id,
     avg(items.price) as average_order_value,
   count(*) as no_of_orders,
 FROM
 `ecommerce_public.events_flat_data`,
 UNNEST(items) AS items
WHERE
 event_name = 'purchase'
 GROUP BY
 1 ORDER bY no_of_orders desc
 
)
,
#Select * from OrderStats
 
UserStats as (
select v.*, o.average_order_value,o.no_of_orders from ViewStats v FULL OUTER JOIN OrderStats o ON v.user_pseudo_id = o.user_pseudo_id
order by o.no_of_orders desc
)
 
SELECT
 *
  EXCEPT(user_pseudo_id)
FROM
 UserStats
 
 

  ![image](https://user-images.githubusercontent.com/111537542/185979651-0e5b9c5d-1de9-4721-917b-7a54b8037ca2.png)



**Step 2: Create a k-means model**


 CREATE OR REPLACE MODEL
 ecommerce_public.user_clusters OPTIONS(model_type='kmeans',kmeans_init_method = 'KMEANS++',
   NUM_CLUSTERS=4 ) AS
WITH
ViewStats AS (
 
SELECT
 user_pseudo_id,
 COUNT(
   DISTINCT(SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'ga_session_id'))
   AS session_count,
 SUM(
   (SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'engagement_time_msec'
   )) AS total_time_spend_by_user_in_msec
 
 FROM
 -- Replace table name.
 `ecommerce_public.events_flat_data`
 
 GROUP BY
 1 ORDER bY total_time_spend_by_user_in_msec desc
)
,
 
OrderStats as (
 
 
SELECT
 user_pseudo_id,
     avg(items.price) as average_order_value,
   count(*) as no_of_orders,
 FROM
 `ecommerce_public.events_flat_data`,
 UNNEST(items) AS items
WHERE
 event_name = 'purchase'
 GROUP BY
 1 ORDER bY no_of_orders desc
 
)
,
#Select * from OrderStats
 
UserStats as (
select v.*, o.average_order_value,o.no_of_orders from ViewStats v FULL OUTER JOIN OrderStats o ON v.user_pseudo_id = o.user_pseudo_id
order by o.no_of_orders desc
)
 
SELECT
 *
  EXCEPT(user_pseudo_id)
FROM
 UserStats
 

 ![image](https://user-images.githubusercontent.com/111537542/185979873-30426e48-8414-440a-b704-dea4af1c9e6f.png)


**
Step 3: Use the ML.PREDICT function to predict a user’s cluster
**
Query
 
 
 
 WITH
ViewStats AS (
 
SELECT
 user_pseudo_id,
 COUNT(
   DISTINCT(SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'ga_session_id'))
   AS session_count,
 SUM(
   (SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'engagement_time_msec'
   )) AS total_time_spend_by_user_in_msec
   #ecommerce.getAverageOrderValue((userId)) as average_order_value,
   #ecommerce.getNoOfOrders((userId)) as no_of_orders,
 FROM
 -- Replace table name.
 `ecommerce_public.events_flat_data`
 
 GROUP BY
 1 ORDER bY total_time_spend_by_user_in_msec desc
)
,
 
OrderStats as (
 
 
SELECT
 user_pseudo_id,
     avg(items.price) as average_order_value,
   count(*) as no_of_orders,
 FROM
 `ecommerce_public.events_flat_data`,
 UNNEST(items) AS items
WHERE
 event_name = 'purchase'
 GROUP BY
 1 ORDER bY no_of_orders desc
 
)
,
#Select * from OrderStats
 
UserStats as (
select v.*, o.average_order_value,o.no_of_orders from ViewStats v FULL OUTER JOIN OrderStats o ON v.user_pseudo_id = o.user_pseudo_id
order by o.no_of_orders desc
)
 
 
SELECT
 * EXCEPT(nearest_centroids_distance)
FROM
  ML.PREDICT( MODEL ecommerce_public.user_clusters,
   (
   SELECT
     *
   FROM
     UserStats
   WHERE user_pseudo_id = '5053424.7664335544'
   ))
 
 
Output

 ![image](https://user-images.githubusercontent.com/111537542/185980129-98c39e85-b0cd-4a32-b0b8-33fe824499d8.png)


**Step 4: Use your model to make data-driven decisions**


 ![image](https://user-images.githubusercontent.com/111537542/185980205-d6221d1f-c221-4edc-9774-4d1d2d4a2366.png)



**Inference**

Cluster 1 has more orders, session count and total time engaged making them a premium customer. We can offer discount offers to encourage them.
Cluster 2 has the highest average order value, but very less no of orders. These are potential customers who tend to spend more and are very selective in the purchase. We can recommend highly rated products so that they can buy more thus increasing their order count. Also they have less engagement time , we can send some advertising messages to increase engagement and thereby increase the number of orders.
Cluster 3 has more orders than 2 but very less session count. Here again we can send messages to increase user engagement.
Cluster 4 has very less session count as well as low no of orders. This segment can be characterized as our bronze segment.




**Product Recommendation**

A common use case for eCommerce is product recommendations.   Customers who are presented with contextual personalized products tend to have higher average order values.  Most retailers showcase “Customers who bought this also bought these”  during the purchase process, which helps the user to make quick purchase decisions based on recommendations.  For contextual personalization, it is important that personalization also takes into account the context – which products has the customer been viewing recently.  Other promotions, such as “10% Off Product X” can be targeted to the customer based on the interest shown in a particular category / product.

Collaborative filtering is a method of making automatic predictions about the interests of a user by collecting preferences or taste information from many users.  This example uses matrix factorization for creating a recommendation engine.

**Matrix factorization**

Matrix factorization (collaborative filtering) is one of the most common and effective methods of creating recommendation systems.

There are two types of matrix factorization based on the kind of user feedback:

1)  Explicit
With explicit feedback, the dataset must indicate a user’s preference for a product, like a rating between 1 and 5 stars.

2)  Implicit
Implicit preferences for the user need to be determined based on their purchase behavior.

Pre-process the data to training Data

With matrix factorization, in order to train the model, you will need a table with userId, itemId, and the products viewed based on the click-stream events.




**
Recommendations

Product affinity - Based on product bought together historically ( Implicit feedback)


Step 1 - Preprocess to training data
  **
Query:
 
WITH UniqueUsersList As ( ## get all unique  users
select distinct(user_pseudo_id) from `ecommerce_public.events_flat_data`
),
UserPurchaseEvents AS ( ## get all purchase orders
   SELECT
     user_pseudo_id,
     items.item_name
   FROM
     `ecommerce_public.events_flat_data`,
     UNNEST(items) AS items
   WHERE  event_name = 'purchase' group by 1,2
)
,
 
ProductBoughtTogetherHistorically as ( ## get all items which are bought together
  select user_pseudo_id as prevUserId, STRING_AGG(items.item_name, ', ') AS productsBoughtTogether   from `ecommerce_public.events_flat_data`, UNNEST(items) AS items WHERE  event_name =         'purchase' and ecommerce.unique_items > 1 group by 1
)
,
#select * from ProductBoughtTogetherHistorically
UserProductRecommendations AS ( ## for all items purchased by user in an order prepare the list of other items bought together
select user_pseudo_id,cust_prev_purchase,split(productsBoughtTogether,',') AS product from
(
 select b.*,a.* from
   ProductBoughtTogetherHistorically a,
 
 
(select user_pseudo_id,item_name AS cust_prev_purchase from UserPurchaseEvents  where user_pseudo_id in
(select user_pseudo_id from UniqueUsersList )
) b
where a.productsBoughtTogether like CONCAT('%',
            CAST(b.cust_prev_purchase AS STRING),'%' )  ## Compare each itemn in an order with orders bought historically and prepare a list of items bought together
and user_pseudo_id != prevUSerId   ## remove if the product in an order is same as the compared order historically
)
)
,
#select * from UserProductRecommendations
UserProductMatrix as
(select UserProductRecommendations.user_pseudo_id,
#cust_prev_purchase,
product_recommended,
ecommerce_public.getratingbasedonProduct(TRIM(product_recommended)) as rating
from
UserProductRecommendations CROSS JOIN UNNEST(UserProductRecommendations.product) AS product_recommended
where TRIM(product_recommended) <> TRIM(UserProductRecommendations.cust_prev_purchase)  ###  , remove if the previous order is the same as the current order
group by user_pseudo_id, product_recommended
order by user_pseudo_id) 
select * from UserProductMatrix l
where not exists
(
  select 1 from UserPurchaseEvents
where user_pseudo_id = l.user_pseudo_id and  Trim(item_name) = Trim(l.product_recommended)## check if an item in the recommendation list is already bought by the user, if so remove it
)
order by user_pseudo_id
 
### function used
 
# CREATE FUNCTION ecommerce_public.getratingbasedonProduct(product STRING)
 #AS  ((
 #select sum(cast(items.quantity as FLOAT64)) from `ecommerce_public.events_flat_data`, UNNEST(items) AS items WHERE  event_name = 'purchase' and items.item_name = product
# ))
 


**Training data**




 ![image](https://user-images.githubusercontent.com/111537542/185980374-3da16eeb-fbf1-4ee9-92ab-8f78d43ffcff.png)





**
Step 2 : Create a BigQuery ML model
**

Query:
 
## Create Model
 
#standardSQL
CREATE OR REPLACE MODEL ecommerce_public.product_affinity
OPTIONS
 (model_type='matrix_factorization',
  feedback_type='implicit',
  user_col='user_pseudo_id',
  item_col= 'product_recommended',
  rating_col='rating',
  l2_reg=30,
  num_factors=15) AS
 
 
WITH UniqueUsersList As ( ## get all unique  users
select distinct(user_pseudo_id) from `ecommerce_public.events_flat_data`
),
UserPurchaseEvents AS ( ## get all purchase orders
   SELECT
     user_pseudo_id,
     items.item_name
   FROM
     `ecommerce_public.events_flat_data`,
     UNNEST(items) AS items
   WHERE  event_name = 'purchase' group by 1,2
)
,
 
ProductBoughtTogetherHistorically as ( ## get all items which are bought together
  select user_pseudo_id as prevUserId, STRING_AGG(items.item_name, ', ') AS productsBoughtTogether   from `ecommerce_public.events_flat_data`, UNNEST(items) AS items WHERE  event_name =         'purchase' and ecommerce.unique_items > 1 group by 1
)
,
#select * from ProductBoughtTogetherHistorically
UserProductRecommendations AS ( ## for all items purchased by user in an order prepare the list of other items bought together
select user_pseudo_id,cust_prev_purchase,split(productsBoughtTogether,',') AS product from
(
 select b.*,a.* from
   ProductBoughtTogetherHistorically a,
 
 
(select user_pseudo_id,item_name AS cust_prev_purchase from UserPurchaseEvents  where user_pseudo_id in
(select user_pseudo_id from UniqueUsersList )
) b
where a.productsBoughtTogether like CONCAT('%',
            CAST(b.cust_prev_purchase AS STRING),'%' )  ## Compare each itemn in an order with orders bought historically and prepare a list of items bought together
and user_pseudo_id != prevUSerId   ## remove if the product in an order is same as the compared order historically
)
)
,
#select * from UserProductRecommendations
UserProductMatrix as
(select UserProductRecommendations.user_pseudo_id,
#cust_prev_purchase,
product_recommended,
ecommerce_public.getratingbasedonProduct(TRIM(product_recommended)) as rating
from
UserProductRecommendations CROSS JOIN UNNEST(UserProductRecommendations.product) AS product_recommended
where TRIM(product_recommended) <> TRIM(UserProductRecommendations.cust_prev_purchase)  ###  , remove if the previous order is the same as the current order
group by user_pseudo_id, product_recommended
order by user_pseudo_id) 
select * from UserProductMatrix l
where not exists
(
  select 1 from UserPurchaseEvents
where user_pseudo_id = l.user_pseudo_id and  Trim(item_name) = Trim(l.product_recommended)## check if an item in the recommendation list is already bought by the user, if so remove it
)
order by user_pseudo_id
 

**Step 3 : Evaluate **

Query

 
## evaluate
 
#standardSQL
SELECT
 *
FROM
 ML.EVALUATE(MODEL ecommerce_public.product_affinity,
   (
   WITH UniqueUsersList As ( ## get all unique  users
select distinct(user_pseudo_id) from `ecommerce_public.events_flat_data`
),
UserPurchaseEvents AS ( ## get all purchase orders
   SELECT
     user_pseudo_id,
     items.item_name
   FROM
     `ecommerce_public.events_flat_data`,
     UNNEST(items) AS items
   WHERE  event_name = 'purchase' group by 1,2
)
,
 
ProductBoughtTogetherHistorically as ( ## get all items which are bought together
  select user_pseudo_id as prevUserId, STRING_AGG(items.item_name, ', ') AS productsBoughtTogether   from `ecommerce_public.events_flat_data`, UNNEST(items) AS items WHERE  event_name =         'purchase' and ecommerce.unique_items > 1 group by 1
)
,
#select * from ProductBoughtTogetherHistorically
UserProductRecommendations AS ( ## for all items purchased by user in an order prepare the list of other items bought together
select user_pseudo_id,cust_prev_purchase,split(productsBoughtTogether,',') AS product from
(
 select b.*,a.* from
   ProductBoughtTogetherHistorically a,
 
 
(select user_pseudo_id,item_name AS cust_prev_purchase from UserPurchaseEvents  where user_pseudo_id in
(select user_pseudo_id from UniqueUsersList )
) b
where a.productsBoughtTogether like CONCAT('%',
            CAST(b.cust_prev_purchase AS STRING),'%' )  ## Compare each itemn in an order with orders bought historically and prepare a list of items bought together
and user_pseudo_id != prevUSerId   ## remove if the product in an order is same as the compared order historically
)
)
,
#select * from UserProductRecommendations
UserProductMatrix as
(select UserProductRecommendations.user_pseudo_id,
#cust_prev_purchase,
product_recommended,
ecommerce_public.getratingbasedonProduct(TRIM(product_recommended)) as rating
from
UserProductRecommendations CROSS JOIN UNNEST(UserProductRecommendations.product) AS product_recommended
where TRIM(product_recommended) <> TRIM(UserProductRecommendations.cust_prev_purchase)  ###  , remove if the previous order is the same as the current order
group by user_pseudo_id, product_recommended
order by user_pseudo_id) 
select * from UserProductMatrix l
where not exists
(
  select 1 from UserPurchaseEvents
where user_pseudo_id = l.user_pseudo_id and  Trim(item_name) = Trim(l.product_recommended)## check if an item in the recommendation list is already bought by the user, if so remove it
)
order by user_pseudo_id))
 
 ![image](https://user-images.githubusercontent.com/111537542/185981119-0549bde6-889f-4362-850c-1aec2c18f323.png)



**Step 4 : Recommend **


Top 5 recommendations for a user
 
## RECOMMEND for a user
 
 
#standardSQL
SELECT
 *
FROM
 ML.RECOMMEND(MODEL ecommerce_public.product_affinity,
   (
   select '2534169.4418845320' as user_pseudo_id)) order by predicted_rating_confidence desc LIMIT 5
 
 ![image](https://user-images.githubusercontent.com/111537542/185981171-bdc94145-0c37-4624-b0b9-e38004da9fb9.png)




**Model Management - Vertex AI**

​​
We will need a Google Cloud Platform project with billing enabled to run this.
To create a project, follow the instructions here.
Step 1: Enable the Compute Engine API
Navigate to Compute Engine and select Enable if it isn't already enabled. You'll need this to create your notebook instance.
Step 2: Enable the Vertex AI API
Navigate to the Vertex AI section of your Cloud Console and click Enable Vertex AI API.

 ![image](https://user-images.githubusercontent.com/111537542/185981237-0e47a34f-e173-49c8-b924-290c7ece622d.png)

 
Step 3: Create a Notebooks instance
We'll use Notebooks to get predictions after we've deployed our model. From the Vertex AI section of your Cloud Console, click on Notebooks:
From there, select New Instance. Then select the TensorFlow Enterprise 2.3 instance type without GPUs:
 
  ![image](https://user-images.githubusercontent.com/111537542/185981376-00d37d42-609e-440a-a384-b1ed1bb01ff9.png)

 ![image](https://user-images.githubusercontent.com/111537542/185981415-a511919e-d4dc-46e5-8254-22fbd2c0b7eb.png)


Use the default options and then click Create. 
 
 
 
 
 
 
 
**Train a BigQuery ML model
 **
Step 1: Create a BigQuery dataset in your project
To train a model in BigQuery ML, you'll need to create a dataset within your project to store this model. Click on your project in the left menu bar, and then select Create Dataset:
Step 2: Run a CREATE MODEL query
In the BigQuery Query editor, run the following CREATE MODEL query to create and train a BigQuery ML model on the public dataset we'll be using
 
  ![image](https://user-images.githubusercontent.com/111537542/185981471-a883d752-be76-496f-9260-41c99b7b0f76.png)



** 
Export your BigQuery ML model**
With a trained BQML model, we can use the BQML SQL syntax to get predictions or we can export the model to deploy it elsewhere. Here we'll export our model so that we can deploy it to Vertex AI to scalably serve the model and get predictions.
Step 1: Create a Cloud Storage Bucket for your model
In the model details, click Export Model:

 ![image](https://user-images.githubusercontent.com/111537542/185981534-23b3b1f5-3f91-47e3-95c4-974097d57b55.png)


This will prompt you to enter the Google Cloud Storage (GCS) location where you'd like your model's assets to be exported. If you don't have a GCS bucket yet, don't worry! We're about to create one. First, click Browse:
 
 ![image](https://user-images.githubusercontent.com/111537542/185981591-e90a3080-e036-4192-ae43-948c3e638644.png)

 
 
 
Then click the + icon to create a new bucket:
 
  ![image](https://user-images.githubusercontent.com/111537542/185981627-c0faaa61-11b7-4275-bc97-27a75019bbf9.png)


Give it a unique name (Storage bucket names need to be globally unique). Click Continue. In the next step, under Location type select Region and choose any of the regions from the dropdown:
 
  ![image](https://user-images.githubusercontent.com/111537542/185981675-0fc2c92f-4c8a-467b-a643-79e1a0e9fa8d.png)


Use the default storage class, and under access control make sure Uniform is selected:
  ![image](https://user-images.githubusercontent.com/111537542/185981743-099f0c4a-42cc-4959-aa99-2b0456dc4246.png)


Click continue and use the defaults for the rest of the options. Then click Create.

**Step 2: Export the BQML model**
With your new bucket created, enter model-assets (or anything you'd like) in the Name field and then click Select:


 ![image](https://user-images.githubusercontent.com/111537542/185981828-75800aaf-d67e-46e3-af71-7bc4e8aaa308.png)

Then click Export. This will create a job in BigQuery to export your model in TensorFlow's SavedModel format to the newly created GCS bucket you specified. This will take about a minute to export.
While your model is being exported, navigate to the Storage section of your Cloud console. When your job completes, you should see your model assets exported to the bucket you just created under a model-assets subdirectory:

 ![image](https://user-images.githubusercontent.com/111537542/185981881-00c4e9c4-465b-4778-a1d7-9e766ddc3a29.png)

 
Export your BigQuery ML model
With a trained BQML model, we can use the BQML SQL syntax to get predictions or we can export the model to deploy it elsewhere. Here we'll export our model so that we can deploy it to Vertex AI to scalably serve the model and get predictions.
Step 1: Create a Cloud Storage Bucket for your model
In the model details, click Export Model:

 ![image](https://user-images.githubusercontent.com/111537542/185981965-364fd5b1-05c5-4667-b8a6-7fbe448336ed.png)


This will prompt you to enter the Google Cloud Storage (GCS) location where you'd like your model's assets to be exported. If you don't have a GCS bucket yet, don't worry! We're about to create one. First, click Browse:
 ![image](https://user-images.githubusercontent.com/111537542/185982017-b0040f51-69ff-4b71-82b2-de0154a58b72.png)



Then click the + icon to create a new bucket:

 ![image](https://user-images.githubusercontent.com/111537542/185982041-521c7451-4979-4533-b3c0-d811d92818c1.png)


Give it a unique name (Storage bucket names need to be globally unique). Click Continue. In the next step, under Location type select Region and choose any of the regions from the dropdown:

 ![image](https://user-images.githubusercontent.com/111537542/185982075-a941b5f4-fd3f-418b-85b9-cf9e232602b3.png)


Use the default storage class, and under access control make sure Uniform is selected:

 ![image](https://user-images.githubusercontent.com/111537542/185982133-bed59db8-9519-4a27-834d-e9d540f99249.png)


Click continue and use the defaults for the rest of the options. Then click Create.
Step 2: Export the BQML model
With your new bucket created, enter model-assets (or anything you'd like) in the Name field and then click Select:
 
 
**Import the model to Vertex AI**
In this step we'll reference the GCS storage location where we just exported our model assets to create and import the model to Vertex AI.
Step 1: Import the model
In your Cloud console, navigate to the Vertex AI Models section. From there, select Import:

 ![image](https://user-images.githubusercontent.com/111537542/185982208-422f60dd-15bf-455f-a0df-62b2f69f23fd.png)


In the first step, give your model the name predict_default. Select the same region where you created your bucket (either us-central1, europe-west4, or asia-east1. Then click Continue. In Model settings, keep "Import model artifacts into a new pre-built container" selected.
In the Model framework dropdown, select TensorFlow. Then select 2.3 as the framework version.
In the Model artifact location field, click Browse, click into the GCS bucket you just created, and click on the model-assets directory:

 ![image](https://user-images.githubusercontent.com/111537542/185982248-9dfb5d16-82f0-460e-9c55-4139b4ab8908.png)


Then click Import. It will take a few minutes to import your model. Once it has been created, you'll see it in the models section of your Cloud console:
 
  ![image](https://user-images.githubusercontent.com/111537542/185982273-7ee8ecf7-e8db-48a2-9bad-9207444b9993.png)

 ![image](https://user-images.githubusercontent.com/111537542/185982308-273b7561-59b0-4340-8b9f-b53649fc23e1.png)


Then click Export. This will create a job in BigQuery to export your model in TensorFlow's SavedModel format to the newly created GCS bucket you specified. This will take about a minute to export.
While your model is being exported, navigate to the Storage section of your Cloud console. When your job completes, you should see your model assets exported to the bucket you just created under a model-assets subdirectory:
 ![image](https://user-images.githubusercontent.com/111537542/185982363-a50fb02a-ac94-4e72-9010-b76e220487f9.png)

 
**Deploy the model to an endpoint**
Now that we've uploaded our model, the next step is to create an Endpoint in Vertex. A Model resource in Vertex can have multiple endpoints associated with it, and you can split traffic between endpoints.
Step 1: Creating an endpoint
On your model page, navigate to the Deploy and test tab and click Deploy to endpoint:

 ![image](https://user-images.githubusercontent.com/111537542/185982453-bf12e788-4443-4e24-936d-c4d2830ce60a.png)


Give your endpoint a name, like default_pred_v1, leave the traffic splitting settings as is, and then select a machine type for your model deployment. We used an n1-highcpu-2 here, but you can choose whichever machine type you'd like.
Then select Done and click Continue. Leave the selected location settings as is and then click Deploy. Your endpoint will take a few minutes to deploy. When it is completed you'll see a green check mark next to it:

  ![image](https://user-images.githubusercontent.com/111537542/185982479-a68fe7ef-fbb4-4217-8dac-2dc46c5e0cff.png)

 ![image](https://user-images.githubusercontent.com/111537542/185982575-95b9c239-a299-4fdc-838a-deb2fb69cb21.png)

 

**Testing model Online**

**UI**

 ![image](https://user-images.githubusercontent.com/111537542/185982697-d6ea21b6-4dae-4b2b-837c-052aca9b5cf5.png)


**Google Cloud Function**

Request

curl -m 70 -X POST https://us-central1-ecomm-analysis.cloudfunctions.net/UserCluster \
-H "Authorization:bearer $(gcloud auth print-identity-token)" \
-H "Content-Type:application/json" \
-d '{"userId":"2534169.4418845320"}'



 

**REST API**

Request 

curl \
-X POST \
-H "Authorization: Bearer $(gcloud auth print-access-token)" \
-H "Content-Type: application/json" \
https://us-central1-aiplatform.googleapis.com/v1/projects/ecomm-analysis/locations/us-central1/endpoints/6123971903456542720:predict \
-d '{"instances": [{"user_pseudo_id":"2534169.4418845320"}]}'


Sample req
{"instances": [{"average_order_value":12.8,"no_of_orders":60,"session_count":8,"total_time_spend_by_user_in_msec": 2224770}]}

Sample resp
{
 "predictions": [
   {
     "nearest_centroid_id": [
       3
     ],
     "centroid_distances": [
       17.12813603951723,
       142.2885807575315,
       14.73747037975442,
       15.66436157266043
     ],
     "centroid_ids": [
       1,
       2,
       3,
       4
     ]
   }
 ],
 "deployedModelId": "5967942407382106112",
 "model": "projects/591702499920/locations/us-central1/models/7915265461103624192",
 "modelDisplayName": "userclustermodel",
 "modelVersionId": "1"
}


 ![image](https://user-images.githubusercontent.com/111537542/185982827-c76ce543-d132-40c8-9250-b8892381bfbd.png)


**Model updating with only incremental records **

Based on real time increamental changes to Bigquery table data, our model also has to be retrained on an increamental basis.
BigML model provides an option 'warm_start' from which we can train our existing model with new data

<img width="452" alt="image" src="https://user-images.githubusercontent.com/111537542/186464729-e826d816-314b-4fb3-aeec-5392bf05477b.png">

Bigquery change history lets us track the history of changes to a BigQuery table using APPENDS TVF.


The APPENDS TVF returns a table of all rows appended to a table for a given time range. The following operations add rows to the APPENDS change history:

	CREATE TABLE DDL statement
	INSERT DML statement
	MERGE DML statement
	Loading data into BigQuery
	Streaming ingestion
	
<img width="452" alt="image" src="https://user-images.githubusercontent.com/111537542/186465831-207e5f82-6509-47b1-bbc2-f9d091463b5a.png">

Once we are able to get only the increamental records we can retrain the existing model.


<img width="452" alt="image" src="https://user-images.githubusercontent.com/111537542/186466181-f267ee3e-a8d9-48e1-bf95-1d6f5c0a58fc.png">


Once we have the increamental training model query we can schedule it in Bigquery scheduled queries as below

<img width="452" alt="image" src="https://user-images.githubusercontent.com/111537542/186467228-aab8dba4-3d3e-42d3-a407-4c6579f44403.png">


Note : Currently only K-Means model is supported for retraining.
Matrix model require to purchase flex slots for model creation so it can't be scheduled.


**Business Analytics **



**Looker – Mongo DB Integration**
 
The general steps for setting up a Google BigQuery Standard SQL or Google BigQuery Legacy SQL connection are:
Create a service account with access to the Google project and download the JSON credentials certificate.
Set up the Looker connection to your database.
Test the connection.

 
Creating a service account and downloading the JSON credentials certificate
You must have Google Cloud admin permissions to create a service account. Google has documentation on creating a service account and generating a private key.
Open the credentials page in the Google Cloud Platform API Manager and, if necessary, select your project:

 ![image](https://user-images.githubusercontent.com/111537542/185982992-f9c46741-4cba-4e4d-a2d9-c5be772c556f.png)


Click CREATE CREDENTIALS and choose Service account:

 ![image](https://user-images.githubusercontent.com/111537542/185983033-7405a907-1daf-470a-879b-ab22b12a60d8.png)

	
Enter a name for the new service account, optionally add a description, and click CREATE:
 ![image](https://user-images.githubusercontent.com/111537542/185983071-9990c17d-4d93-4d9d-8672-7165aef60f96.png)


Your service account requires two Google BigQuery predefined roles:
BigQuery > BigQuery Data Editor
BigQuery > BigQuery Job User
Select the first role in the Select a role field, then click ADD ANOTHER ROLE and select the second role:

 ![image](https://user-images.githubusercontent.com/111537542/185983107-1e33aada-48fa-468a-9c33-814aba0c3f7d.png)


After selecting both roles, click CONTINUE:

 ![image](https://user-images.githubusercontent.com/111537542/185983150-94361e7e-035d-4f33-a2cd-557033bf6e58.png)


Click CREATE KEY:

 ![image](https://user-images.githubusercontent.com/111537542/185983193-4ca09f51-9d76-44d8-be79-e2fb42cf954e.png)


Select JSON and click CREATE:

  ![image](https://user-images.githubusercontent.com/111537542/185983457-5a216c67-2e81-461d-b6f5-6f2ffa6a3556.png)


The JSON key will be saved to your computer. BE SURE TO REMEMBER WHERE IT IS SAVED. After noting the download location, click CLOSE:

 ![image](https://user-images.githubusercontent.com/111537542/185983499-59471d30-a4a1-4eb3-a493-3d5254d7ca71.png)


Click DONE:

 ![image](https://user-images.githubusercontent.com/111537542/185983547-1b0eda2f-e866-4c60-b681-e51debb71bf2.png)



Find the email address corresponding to the service account. You will need this to configure the Looker connection to BigQuery.
 
 
 
  ![image](https://user-images.githubusercontent.com/111537542/185983656-f4f18eb2-b5fc-45e6-bfa6-2f65dd9f4501.png)

 
  
 
2)	Set up the Looker connection to your database.
 
  ![image](https://user-images.githubusercontent.com/111537542/185983719-e33c1eeb-88e8-4d51-8582-8a437276303f.png)

 
Use the service account created in above step and Json generated as an authenticate mechanism
 
 
3)	Test the connection
 
 
  ![image](https://user-images.githubusercontent.com/111537542/185983756-1ff67fbd-43a2-4277-b16e-70c9b157cd55.png)

 
Creating Projects

 ![image](https://user-images.githubusercontent.com/111537542/185983798-bab65f42-cf7d-4d5e-b6be-4baeb265bf87.png)

 
 ![image](https://user-images.githubusercontent.com/111537542/185983846-93f5877f-0a2f-4668-96ee-216364402b8b.png)
 
 


 
 
Configuring Projects

 
 ![image](https://user-images.githubusercontent.com/111537542/185983877-0249ae22-0366-4f69-a0fd-90ad7e481bf1.png)

 ![image](https://user-images.githubusercontent.com/111537542/185983983-c1177119-6dcd-4d01-9069-fdd75d3bbc60.png)










 Create Views from table

 

 ![image](https://user-images.githubusercontent.com/111537542/185984015-29cb6c8f-cf38-4c29-a7e9-c8f29925e1cd.png)






 
Sample view file
 
 ![image](https://user-images.githubusercontent.com/111537542/185984040-2ffbf0d8-13ea-4b0d-90cb-428352342209.png)

 
 
 
 
 
 
 
Dashboard

 
  ![image](https://user-images.githubusercontent.com/111537542/185984083-a6a66810-c0d7-407f-b227-058cb4329fcc.png)

 
 
 

 
 
 
 
 

Sales Distribution

 ![image](https://user-images.githubusercontent.com/111537542/185984123-7f33cdce-e0e5-40be-a847-dc52ef562bbc.png)


Sales Trend
Sales Distribution by Category




 ![image](https://user-images.githubusercontent.com/111537542/185984182-760c110e-7934-486a-960e-da8492e2c458.png)








Products with repeat orders



 ![image](https://user-images.githubusercontent.com/111537542/185984210-da70c34b-37a7-435c-bd1b-efb6d63e4f5a.png)









