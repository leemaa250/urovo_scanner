package com.example.urovo_scanner

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.device.ScanManager
import android.device.scanner.configuration.PropertyID
import android.graphics.BitmapFactory
import android.os.Message
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding





/** UrovoScannerPlugin */
class UrovoScannerPlugin: FlutterPlugin, MethodCallHandler,ActivityAware {
  private val ACTION_CAPTURE_IMAGE = "scanner_capture_image_result"
  private val ACTION_DECODE = ScanManager.ACTION_DECODE // default action
  private val BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG
  private val BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG
  private val BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG
  private val DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG


  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var channel : MethodChannel? = null
  private var eventChannel: EventChannel? = null
  private var barcodeSink: EventChannel.EventSink? = null
  private  var isUrovoDevice:Boolean = false;
  private  var context: Context? = null
  private lateinit var activity: Activity

  private lateinit var scanManager: ScanManager

  private val barcodeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      // Get scan results, including string and byte data etc.

      // Get scan results, including string and byte data etc.
      val barcode = intent.getByteArrayExtra(DECODE_DATA_TAG)
      val barcodeLen = intent.getIntExtra(BARCODE_LENGTH_TAG, 0)
      val temp = intent.getByteExtra(BARCODE_TYPE_TAG, 0.toByte())
      val barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG)
      val scanResult = String(barcode!!, 0, barcodeLen)
      barcodeSink?.success(scanResult)

    }
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {


    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "urovo_scanner")
    channel?.setMethodCallHandler(this)
    // if not on an Urovo device ignore setup
    isUrovoDevice =  isUrovoDevice()

    if(isUrovoDevice){
      scanManager = ScanManager()
      scanManager.switchOutputMode(0)

      eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "urovo_scanner/barcodes");
      eventChannel?.setStreamHandler(
        object : EventChannel.StreamHandler {
          override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink) {
            barcodeSink = eventSink

          }
          override fun onCancel(p0: Any?) {
            barcodeSink = null
          }
        }
      )
      registerBarcodeReceiver(true)
    }
  }

  private fun registerBarcodeReceiver(register: Boolean) {

    if (register) {
      val filter = IntentFilter()
      val idbuf =
        intArrayOf(PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG)
      val value_buf: Array<String?> = scanManager.getParameterString(idbuf)
      if (value_buf != null && value_buf[0] != null && value_buf[0] != "") {
        filter.addAction(value_buf[0])
      } else {
        filter.addAction(ACTION_DECODE)
      }
      context?.registerReceiver(barcodeReceiver, filter)
    } else {
      scanManager.stopDecode()
      context?.unregisterReceiver(barcodeReceiver)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      val resp = "Android ${android.os.Build.VERSION.RELEASE}"
      result.success(resp)
    }else if(call.method == "getDeviceName"){
      result.success(deviceName())
    } else if(call.method == "isUrovoDevice") {
      val isUrovoDevice = isUrovoDevice()
      result.success(isUrovoDevice)
    }else if(call.method == "startScanner"){
      if(isUrovoDevice){
        scanManager.openScanner()
        scanManager.switchOutputMode(0)
        val result: Boolean = scanManager.startDecode()
      }

    }else if(call.method == "stopScanner"){
      if(isUrovoDevice) {
        scanManager.stopDecode()
        scanManager.closeScanner()
      }
    }
    else {

      result.notImplemented()
    }
  }
  private fun  isUrovoDevice():Boolean{
    val deviceName = deviceName().toLowerCase()
    val devices = listOf("urovo","dt50","dt50q","dt50s")
    var isUrovoDevice = false;
    for (device in devices){
      if(deviceName.contains(device)){
        isUrovoDevice = true
      }
    }
    return isUrovoDevice
  }
  private fun  deviceName(): String {
    return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel?.setMethodCallHandler(null)
    eventChannel?.setStreamHandler(null)
    if(isUrovoDevice) {   registerBarcodeReceiver(false)}

  }

  override fun onDetachedFromActivity() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }

}
