package selenium.factory;

import io.appium.java_client.remote.MobileCapabilityType;

/**
 * The extended Appium Capabilities
 * @author qren
 * @since 230
 */
@SuppressWarnings("WeakerAccess")
public interface CustomizedAppiumCapabilityType extends MobileCapabilityType {
  /**
   * Enable "real", non-javascript-based web taps in Safari
   */
  String NATIVE_WEB_TAP = "nativeWebTap";

  /**
   * Java package of the Android application that want to run.
   */
  String APP_PACKAGE = "appPackage";

  /**
   * Activity name for the Android activity that want to launch from test package
   */
  String APP_ACTIVITY = "appActivity";
}
