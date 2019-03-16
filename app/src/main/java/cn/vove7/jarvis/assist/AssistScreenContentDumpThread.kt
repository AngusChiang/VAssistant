package cn.vove7.jarvis.assist

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

import cn.vove7.vtp.log.Vog

@RequiresApi(api = Build.VERSION_CODES.M)
internal class AssistScreenContentDumpThread(
        val context: Context, private val data: Bundle?,
        private val structure: AssistStructure?,
        private val content: AssistContent?) : Thread() {

    override fun run() {
        val json = JSONObject()

        try {
            if (data != null)
                json.put("data", dumpBundle(data, JSONObject()))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        try {
            json.put("content", dumpContent(JSONObject()))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        try {
            json.put("structure", dumpStructure(JSONObject()))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Vog.d("run ---> ${json.toString(2)}")
    }

    @Throws(JSONException::class)
    fun dumpBundle(b: Bundle, json: JSONObject): JSONObject {
        val keys = b.keySet()

        for (key in keys) {
            json.put(key, wrap(b.get(key)))
        }

        return json
    }

    @Throws(JSONException::class)
    private fun dumpContent(json: JSONObject): JSONObject {
        val extras = JSONObject()
        if (content == null) return json
        if (content.extras != null) {
            json.put("extras", extras)
            dumpBundle(content.extras, extras)
        }

        if (content.intent != null) {
            json.put("intent", content.intent.toUri(Intent.URI_INTENT_SCHEME))
        }

        json.put("structuredData", wrap(content.structuredData))
        json.put("webUri", wrap(content.webUri))

        return json
    }

    @Throws(JSONException::class)
    private fun dumpStructure(json: JSONObject): JSONObject {
        return json.put("windows", dumpStructureWindows(JSONArray()))
    }

    @Throws(JSONException::class)
    private fun dumpStructureWindows(windows: JSONArray): JSONArray {
        if (structure == null) return windows
        for (i in 0 until structure.windowNodeCount) {
            windows.put(dumpStructureWindow(structure.getWindowNodeAt(i), JSONObject()))
        }

        return windows
    }

    @Throws(JSONException::class)
    private fun dumpStructureWindow(window: AssistStructure.WindowNode, json: JSONObject): JSONObject {
        json.put("displayId", wrap(window.displayId))
        json.put("height", wrap(window.height))
        json.put("left", wrap(window.left))
        json.put("title", wrap(window.title))
        json.put("top", wrap(window.top))
        json.put("width", wrap(window.width))
        json.put("root", dumpStructureNode(window.rootViewNode, JSONObject()))

        return json
    }

    @Throws(JSONException::class)
    private fun dumpStructureNode(node: AssistStructure.ViewNode, json: JSONObject): JSONObject {
        json.put("accessibilityFocused", wrap(node.isAccessibilityFocused))
        json.put("activated", wrap(node.isActivated))
        json.put("alpha", wrap(node.alpha))
        json.put("assistBlocked", wrap(node.isAssistBlocked))
        json.put("checkable", wrap(node.isCheckable))
        json.put("checked", wrap(node.isChecked))
        json.put("className", wrap(node.className))
        json.put("clickable", wrap(node.isClickable))
        json.put("contentDescription", wrap(node.contentDescription))
        json.put("contextClickable", wrap(node.isContextClickable))
        json.put("elevation", wrap(node.elevation))
        json.put("enabled", wrap(node.isEnabled))

        if (node.extras != null) {
            json.put("extras", dumpBundle(node.extras, JSONObject()))
        }

        json.put("focusable", wrap(node.isFocusable))
        json.put("focused", wrap(node.isFocused))
        json.put("height", wrap(node.height))
        json.put("hint", wrap(node.hint))
        json.put("id", wrap(node.id))
        json.put("idEntry", wrap(node.idEntry))
        json.put("idPackage", wrap(node.idPackage))
        json.put("idType", wrap(node.idType))
        json.put("left", wrap(node.left))
        json.put("longClickable", wrap(node.isLongClickable))
        json.put("scrollX", wrap(node.scrollX))
        json.put("scrollY", wrap(node.scrollY))
        json.put("isSelected", wrap(node.isSelected))
        json.put("text", wrap(node.text))
        json.put("textBackgroundColor", wrap(node.textBackgroundColor))
        json.put("textColor", wrap(node.textColor))
        json.put("textLineBaselines", wrap(node.textLineBaselines))
        json.put("textLineCharOffsets", wrap(node.textLineCharOffsets))
        json.put("textSelectionEnd", wrap(node.textSelectionEnd))
        json.put("textSelectionStart", wrap(node.textSelectionStart))
        json.put("textSize", wrap(node.textSize))
        json.put("textStyle", wrap(node.textStyle))
        json.put("top", wrap(node.top))
        json.put("transformation", wrap(node.transformation))
        json.put("visibility", wrap(node.visibility))
        json.put("width", wrap(node.width))

        json.put("children", dumpStructureNodes(node, JSONArray()))

        return json
    }

    @Throws(JSONException::class)
    private fun dumpStructureNodes(node: AssistStructure.ViewNode, children: JSONArray): JSONArray {
        for (i in 0 until node.childCount) {
            children.put(dumpStructureNode(node.getChildAt(i), JSONObject()))
        }
        return children
    }

    private fun wrap(thingy: Any?): Any {

        return try {
            if (thingy == null) return JSONObject.NULL
            if (thingy is CharSequence) {
                JSONObject.wrap(thingy.toString())
            } else JSONObject.wrap(thingy)
        } catch (e: Exception) {
            JSONObject.NULL
        }
    }
}