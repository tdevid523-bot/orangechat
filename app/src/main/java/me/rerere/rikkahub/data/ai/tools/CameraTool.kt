package me.rerere.rikkahub.data.ai.tools

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import me.rerere.ai.core.InputSchema
import me.rerere.ai.core.Tool
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.service.CameraService
import java.io.File

fun createCameraTool(context: Context): Tool {
    val cameraService = CameraService(context)

    return Tool(
        name = "camera_capture",
        description = "Take a photo with the device camera and return the image for visual analysis. The AI can then describe what it sees, identify objects, scenes, people, text, and more. Use this to understand the visual environment around the user.",
        parameters = {
            InputSchema.Obj(
                properties = buildJsonObject {
                    putJsonObject("flash") {
                        put("type", "boolean")
                        put("description", "Whether to use flash (default false)")
                    }
                    putJsonObject("front_camera") {
                        put("type", "boolean")
                        put("description", "Whether to use front camera (default false)")
                    }
                }
            )
        },
        execute = { args ->
            val params = args.jsonObject
            try {
                val useFlash = params["flash"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                val useFrontCamera = params["front_camera"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false

                // Take photo using CameraService (suspend function)
                val result = runBlocking {
                    cameraService.capturePhoto(
                        useFrontCamera = useFrontCamera,
                        enableFlash = useFlash
                    )
                }

                if (!result.success || result.imageData == null) {
                    return@Tool listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", result.error ?: "Failed to capture photo. Camera may be in use or permission not granted.")
                        }.toString()
                    ))
                }

                // Save image to file and return as UIMessagePart.Image so AI can see it
                val cameraDir = File(context.filesDir, "camera_captures").apply { mkdirs() }
                val imageFile = File(cameraDir, "capture_${System.currentTimeMillis()}.jpg")
                imageFile.outputStream().use { output ->
                    output.write(result.imageData)
                }

                listOf(
                    UIMessagePart.Text(
                        buildJsonObject {
                            put("success", true)
                            put("message", "Photo captured successfully. The image is attached for visual analysis.")
                        }.toString()
                    ),
                    UIMessagePart.Image(
                        url = "file://${imageFile.absolutePath}"
                    )
                )
            } catch (e: Exception) {
                listOf(UIMessagePart.Text(
                    buildJsonObject {
                        put("success", false)
                        put("error", "Camera capture failed: ${e.message}")
                    }.toString()
                ))
            }
        }
    )
}