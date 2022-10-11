function transform(line) {
	var values = line.split(',');

	var obj = new Object();
	obj._id = values[0];
	obj.brand = values[1];
	obj.category = values[2];
	obj.engagementTimeMsec = values[3];
	obj.eventBundleSequenceId = values[4];
	obj.eventDate = values[5];
	obj.eventName = values[6];
	obj.eventTimestamp = values[7];
	obj.gaSessionId = values[8];
	obj.price = values[9];
	obj.productId = values[10];
	obj.productName = values[11];
	obj.quantity = values[12];
	obj.uniqueItems = values[13];
	obj.userId = values[14];
	obj.variant = values[15];
	var jsonString = JSON.stringify(obj);

	return jsonString;
}