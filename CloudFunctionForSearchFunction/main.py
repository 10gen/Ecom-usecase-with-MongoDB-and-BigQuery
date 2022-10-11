from pymongo import MongoClient
from flask import make_response
import json

connection_string = "mongodb+srv://mongotobq:M0ngoToBqPoc@mongo-bq.kn30v.mongodb.net/"


def connectToMongo(request):
    querystring = request.args.get('q')
    client = MongoClient(connection_string)
    dbname = client["ecommerce"]
    collectionobj = dbname["products"]
    ipjson =  '[ { "$search": {"index": "default", "text": {"query": "","path":{"wildcard": "*"},"fuzzy": {"maxEdits": 1.0}}}}]'
    json_object = json.loads(ipjson)
    json_object[0]['$search']['text']['query']=querystring
    item_details = collectionobj.aggregate(json_object)
    return json.dumps(list(item_details),default=str)
