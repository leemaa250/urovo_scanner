import 'dart:async';

import 'package:flutter/services.dart';

class UrovoScanner {
  static const MethodChannel _channel = MethodChannel('urovo_scanner');
  static const EventChannel _eventChannel = EventChannel('urovo_scanner/barcodes');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> get deviceName async {
    final String? device = await _channel.invokeMethod('getDeviceName');
    return device;
  }

  static Future<bool?> get isUrovoDevice async {
    final bool? isUrovo = await _channel.invokeMethod('isUrovoDevice');
    return isUrovo;
  }

  static Stream<String?> get barcodeStream {
    var stream = _eventChannel.receiveBroadcastStream();
    return stream.map((data) {
      try {
        return data as String;
      } catch (e) {
        return null;
      }
    });
  }
}
