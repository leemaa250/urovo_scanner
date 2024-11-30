import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:urovo_scanner/urovo_scanner.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _deviceName = 'Unknown';
  bool _isUrovo = false;
  String _barcode = 'Nothing scanned yet';

  late StreamSubscription<String?> _barcodeStreamSubscription;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  @override
  void dispose() {
    _barcodeStreamSubscription.cancel();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String deviceName;
    bool isUrovo;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      deviceName = await UrovoScanner.deviceName ?? 'Unknown device version';
    } on PlatformException {
      deviceName = 'Failed to get device name.';
    }

    try {
      isUrovo = await UrovoScanner.isUrovoDevice ?? false;
    } on PlatformException {
      isUrovo = false;
    }
    _isUrovo = isUrovo;
    _deviceName = deviceName;

    //listen for scanned barcodes
    _barcodeStreamSubscription = UrovoScanner.barcodeStream.listen((barcode) {
      if (barcode != null) {
        if (mounted) {
          setState(() {
            _barcode = barcode;
          });
        }
      }
    });
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('PTT Example '),
        ),
        body: Column(
          children: [
            Expanded(
              child: Center(
                child: Text(
                  _barcode,
                  style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                ),
              ),
            ),
            Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [Text('Running on: $_deviceName\n'), Text('Is Urovo Device $_isUrovo')],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
