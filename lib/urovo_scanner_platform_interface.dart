import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'urovo_scanner_method_channel.dart';

abstract class UrovoScannerPlatform extends PlatformInterface {
  /// Constructs a UrovoScannerPlatform.
  UrovoScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static UrovoScannerPlatform _instance = MethodChannelUrovoScanner();

  /// The default instance of [UrovoScannerPlatform] to use.
  ///
  /// Defaults to [MethodChannelUrovoScanner].
  static UrovoScannerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [UrovoScannerPlatform] when
  /// they register themselves.
  static set instance(UrovoScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
