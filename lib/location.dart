import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class LocationDemo extends StatefulWidget {
  const LocationDemo({super.key});

  @override
  State<LocationDemo> createState() => _LocationState();
}

class _LocationState extends State<LocationDemo> {
  dynamic platform = MethodChannel('background_chanel');
  static const stream = EventChannel('background_chanel/eventChannel');
  late StreamSubscription _streamSubscription;
  dynamic dataList = [];

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    _startListener();
  }

  void _startListener() {
    _streamSubscription = stream.receiveBroadcastStream().listen(_listenStream);
  }

  void _listenStream(value) {
    print(value);
    dataList.add(value);
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Location Demo"),
        centerTitle: true,
        primary: true,
        backgroundColor: Colors.teal,
      ),
      body: Center(
        child: Column(
          children: [
            ElevatedButton(
                onPressed: () async {
                  await getCurrentLocation();
                },
                 child: const Text("Execute Native code to get Location")
            ),
            Expanded(
              child: ListView.builder(
                itemCount: dataList.length,
                shrinkWrap: true,
                itemBuilder: (context, index) {
                  return Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: ListTile(
                      tileColor: Colors.teal,
                      dense: true,
                      leading: Text(index.toString()),
                      title: Text(dataList[index]["latitude"].toString()),
                      subtitle: Text(dataList[index]["longitude"].toString()),
                      trailing: Text(DateTime.now().toString()),
                    ),
                  );
                },
              ),
            )
          ],
        ),
      ),
    );
  }


  /*
  * This method call native android code for location
  * */

  getCurrentLocation() async {
    try {
      await platform.invokeMethod('getCurrentLocation');
    } catch (e) {
      return 'Failed to get location: $e';
    }
  }


}
