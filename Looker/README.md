
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








