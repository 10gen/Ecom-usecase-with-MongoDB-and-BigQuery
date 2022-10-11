**Cloud Function Trigger to load data from CSV to Big Query** 

 **Purpose:** 

To load clickstream(events) data from csv to BigQuery dataset we will be using dataflow batch job. This job needs to be triggered whenever clickstream.csv file is uploaded into specific cloud storage bucket. To accomplish this we use cloud function. 

**Details**

1.Navigate to Cloud Function section in GCP 

2.Click on ‘Create Function’ 
![image](https://user-images.githubusercontent.com/111537542/186037518-778db2dc-1728-4bcb-ac12-c3611ac61a39.png)

3.Select 1st Gen , specify function name, region. 

4.Select Trigger as ‘Cloud Storage’, event type as ‘on finalising, creating  file in selected bucket’. 

5.Select bucket name, where when file is uploaded, it will invoke cloud function. 

6.Click on Save and then click on Next 

7.In Next screen, select runtime as ‘Python 3.7’ and entry point as ‘startDataflowProcess’. Add code from main.py in code base over here and  click on Deploy. 

  ![image](https://user-images.githubusercontent.com/111537542/186039115-1e0c74fe-ce56-40f1-aa64-ebb547c7882a.png)


8.Main.py contains code to generate dataflow batch job with unique name(appending datetimestamp), reads all required parameters like bucket name, file name, transform js function path in cloud storage, transform function name, dataflow template location in cloud storage, big query table where csv data is to be loaded and big query temporary loading directory. 

9.This will deploy cloud function that keeps listening if a file with name specifed in main.py is uploaded into storage bucket. If it finds file is uploaded, then it createsdataflow batch job and triggers it.

**Note:** Delete file with name in cloud storage bucket if it already exists and then upload file with same name. Then cloud function gets triggered. 
