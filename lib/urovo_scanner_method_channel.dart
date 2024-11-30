import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'urovo_scanner_platform_interface.dart';

/// An implementation of [UrovoScannerPlatform] that uses method channels.
class MethodChannelUrovoScanner extends UrovoScannerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('urovo_scanner');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
