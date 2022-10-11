**Reverse ETL from BigQuery to Mongo**

**Purpose:** 

To push back recommendations from big query back to mongo, so that these recommendations are made use of in mongo for various purposes. 

**Details:** 

1.Create a batch load job to load product affinity data as below. We need to pass mongodb url, collection name, big query table from where data needs to be loaded, and select template as Big Query to Mongodb
![image](https://user-images.githubusercontent.com/111537542/186284990-235127eb-e6d9-4952-9d05-88c499b39cd7.png)





2.Create a batch load job to load user clustering  data(user_clusters_op) as below. We need to pass mongodb url, collection name, big query table from where data needs to be loaded, and select template as Big Query to Mongodb 
![image](https://user-images.githubusercontent.com/111537542/186256976-4dc38780-a073-4c87-b6b9-8a891f0506c9.png)



 

 
