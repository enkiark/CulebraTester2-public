package com.dtmilano.android.culebratester2.location

import android.util.Log
import com.dtmilano.android.culebratester2.Holder
import com.dtmilano.android.culebratester2.convertWindowHierarchyDumpToJson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.swagger.server.models.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "UiDevice"
private const val REMOVE_TEMP_FILE_DELAY = 2000L

@KtorExperimentalLocationsAPI
@Location("/uiDevice")
class UiDevice {
    @Location("/dumpWindowHierarchy")
    data class DumpWindowHierarchy(val format: String = "JSON") {
        fun response(): String {
            val output = ByteArrayOutputStream()
            Holder.uiDevice.dumpWindowHierarchy(output)
            return convertWindowHierarchyDumpToJson(output.toString())
        }
    }

    @Location("/screenshot")
    data class Screenshot(val scale: Float = 1.0F, val quality: Int = 90) {
        /**
         * Returns a screenshot as a [File] response.
         */
        fun response(): File {
            //Log.d(TAG, "getting screenshot")
            val tempFile = createTempFile()
            if (Holder.uiDevice.takeScreenshot(tempFile, scale, quality)) {
                //Log.d("UiDevice", "returning screenshot file: " + tempFile.absolutePath)
                GlobalScope.launch { delay(REMOVE_TEMP_FILE_DELAY); removeTempFile(tempFile) }
                return tempFile
            }
            throw RuntimeException("Cannot get screenshot")
        }

        /**
         * Removes a temporary file.
         */
        private fun removeTempFile(tempFile: File) {
            if (!tempFile.delete()) {
                Log.w(TAG, "Temporary file ${tempFile.absolutePath} couldn't be deleted.")
            }
        }

        /**
         * Creates a temporary file to hold the screenshot.
         */
        private fun createTempFile(): File {
            val tempDir = Holder.cacheDir
            return File.createTempFile("screenshot", "png", tempDir)
        }
    }

    @Location("/click")
    data class Click(val x: Int, val y: Int) {
        fun response(): StatusResponse {
            //Log.d("UiDevice", "clicking on ($x,$y)")
            if (Holder.uiDevice.click(x, y)) {
                return StatusResponse(StatusResponse.Status.oK)
            }
            return StatusResponse(StatusResponse.Status.eRROR, errorMessage = "Cannot click")
        }
    }

    /**
     * Gets the current package name
     * Gets the current package name
     */
    @Location("/currentPackageName")
    class CurrentPackageName {
        fun response(): io.swagger.server.models.CurrentPackageName {
            return CurrentPackageName(Holder.uiDevice.currentPackageName)
        }
    }

    /**
     * Gets the display height
     * Gets the display height
     */
    @Location("/displayHeight")
    class DisplayHeight {
        fun response(): io.swagger.server.models.DisplayHeight {
            return DisplayHeight(Holder.uiDevice.displayHeight)
        }
    }

    /**
     * Gets the display rotation
     * Gets the display rotation
     */
    @Location("/displayRotation")
    class DisplayRotation {
        fun response(): io.swagger.server.models.DisplayRotation {
            return DisplayRotation(DisplayRotationEnum.of(Holder.uiDevice.displayRotation))
        }
    }

    /**
     * Gets the display size in DP
     * Gets the display size in DP
     */
    @Location("/displaySizeDp")
    class DisplaySizeDp {
        fun response(): io.swagger.server.models.DisplaySizeDp {
            val dp = Holder.uiDevice.displaySizeDp
            return DisplaySizeDp(dp.x, dp.y)
        }
    }

    /**
     * Gets the display width
     * Gets the display width
     */
    @Location("/displayWidth")
    class DisplayWidth {
        fun response(): io.swagger.server.models.DisplayWidth {
            return DisplayWidth(Holder.uiDevice.displayWidth)
        }
    }

    /**
     * Finds an object
     * Finds an object. The object found, if any, can be later used in other call like API.click.
     * @param resourceId the resource id (optional)
     * @param uiSelector the selector sets the resource name criteria for matching. A UI element will be considered a match if its resource name exactly matches the selector parameter and all other criteria for this selector are met. The format of the selector string is &#x60;sel@[\$]value,...&#x60; Where &#x60;sel&#x60; can be one of -  clickable -  depth -  desc -  res -  text -  scrollable &#x60;@&#x60; replaces the &#x60;&#x3D;&#x60; sign that is used to separate parameters and values in the URL. If the first character of value is &#x60;$&#x60; then a &#x60;Pattern&#x60; is created. (optional)
     * @param bySelector the selector sets the resource name criteria for matching. A UI element will be considered a match if its resource name exactly matches the selector parameter and all other criteria for this selector are met. The format of the selector string is &#x60;sel@[\$]value,...&#x60; Where &#x60;sel&#x60; can be one of - clickable - depth - desc - res - text - scrollable &#x60;@&#x60; replaces the &#x60;&#x3D;&#x60; sign that is used to separate parameters and values in the URL. If the first character of value is &#x60;$&#x60; then a &#x60;Pattern&#x60; is created. (optional)
     */
    @Location("/findObject")
    class FindObject(
        val resourceId: String? = null,
        val uiSelector: String? = null,
        val bySelector: String? = null
    ) {
        fun response(): Any? {
            if (resourceId ?: uiSelector ?: bySelector == null) {
                return StatusResponse(
                    StatusResponse.Status.eRROR,
                    StatusResponse.StatusCode.ARGUMENT_MISSING.value,
                    errorMessage = "A selector must be specified"
                )
            }
            return null
        }
    }

    /**
     * Retrieves the text from the last UI traversal event received.
     * Retrieves the text from the last UI traversal event received.
     */
    @Location("/lastTraversedText")
    class LastTraversedText {
        fun response(): io.swagger.server.models.LastTraversedText {
            return LastTraversedText(Holder.uiDevice.lastTraversedText)
        }
    }

    companion object {
        fun pressKeyResponse(pressAny: () -> Boolean, name: String): StatusResponse {
            if (pressAny()) {
                return StatusResponse(StatusResponse.Status.oK)
            }
            return StatusResponse(StatusResponse.Status.eRROR, errorMessage = "Cannot press $name")
        }
    }

    /**
     * Simulates a short press on the BACK button.
     * Simulates a short press on the BACK button.
     */
    @Location("/pressBack")
    class PressBack {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressBack, "BACK")
        }
    }

    /**
     * Simulates a short press on the DELETE key.
     * Simulates a short press on the DELETE key.
     */
    @Location("/pressDelete")
    class PressDelete {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressDelete, "DELETE")
        }
    }

    /**
     * Simulates a short press on the ENTER key.
     * Simulates a short press on the ENTER key.
     */
    @Location("/pressEnter")
    class PressEnter {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressEnter, "ENTER")
        }
    }

    /**
     * Simulates a short press on the HOME button.
     * Simulates a short press on the HOME button.
     */
    @Location("/pressHome")
    class PressHome {
        fun response(): StatusResponse {
            return pressKeyResponse(Holder.uiDevice::pressHome, "HOME")
        }
    }

    /**
     * Simulates a short press using a key code.
     * Simulates a short press using a key code.
     * @param keyCode the key code of the event.
     * @param metaState an integer in which each bit set to 1 represents a pressed meta key (optional)
     */
    @Location("/pressKeyCode")
    class PressKeyCode(val keyCode: Int, val metaState: Int = 0) {
        fun response(): StatusResponse {
            if (Holder.uiDevice.pressKeyCode(keyCode, metaState)) {
                return StatusResponse(StatusResponse.Status.oK)
            }
            return StatusResponse(
                StatusResponse.Status.eRROR,
                StatusResponse.StatusCode.INTERACTION_KEY.value,
                errorMessage = "Cannot press KeyCode"
            )
        }
    }

    /**
     * Retrieves the product name of the device.
     * Retrieves the product name of the device.
     */
    @Location("/productName")
    class ProductName {
        fun response(): io.swagger.server.models.ProductName {
            return ProductName(Holder.uiDevice.productName)
        }
    }

    /**
     * Waits for the current application to idle.
     * Waits for the current application to idle.
     * @param timeout in milliseconds (optional)
     */
    @Location("/waitForIdle")
    data class WaitForIdle(val timeout: Long = 10_000) {
        fun response(): StatusResponse {
            Holder.uiDevice.waitForIdle(timeout)
            return StatusResponse(StatusResponse.Status.oK)
        }
    }

    /**
     * Waits for a window content update event to occur.
     * If a package name for the window is specified, but the current window does not have the same package name, the function returns immediately.
     * @param timeout in milliseconds
     * @param packageName the specified window package name (can be null). If null, a window update from any front-end window will end the wait (optional)
     */
    @Location("/waitForWindowUpdate")
    data class WaitForWindowUpdate(
        val timeout: Long,
        val packageName: String? = null
    ) {
        fun response(): StatusResponse {
            val t0 = System.currentTimeMillis()
            if (Holder.uiDevice.waitForWindowUpdate(packageName, timeout)) {
                return StatusResponse(StatusResponse.Status.oK)
            }
            val t1 = System.currentTimeMillis() - t0
            return StatusResponse(
                StatusResponse.Status.eRROR,
                StatusResponse.StatusCode.TIMEOUT_WINDOW_UPDATE.value,
                if (packageName != null && t1 < timeout) "Current window does not have the same package name" else "Timeout waiting for window update"
            )
        }
    }
}