package cn.vove7.common.utils

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.app.ActivityCompat
import cn.vove7.common.BuildConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.view.editor.MultiSpan
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


/**
 * # ExpandedFuns
 * 扩展函数集合
 * @author Administrator
 * 2018/10/25
 */

/**
 * 代码块运行于UI线程
 * @param action () -> Unit
 */
fun runOnUi(action: () -> Unit) {
    val mainLoop = Looper.getMainLooper()
    if (mainLoop == Looper.myLooper()) {
        action.invoke()
    } else {
        try {
            Handler(mainLoop).post(action)
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
    }
}

inline fun runInCatch(log: Boolean = false, block: () -> Unit) {
    try {
        block.invoke()
    } catch (e: Throwable) {
        if (log) e.log()
        else e.printStackTrace()
    }
}

fun runOnNewHandlerThread(
        name: String = "anonymous", delay: Long = 0,
        autoQuit: Boolean = true, run: () -> Unit): HandlerThread {

    return HandlerThread(name).apply {
        start()
        Vog.d("runOnNewHandlerThread ---> $name")
        Handler(looper).postDelayed({
            runWithClock(name, run)
            if (autoQuit)
                quitSafely()
        }, delay)
    }
}

/**
 * 循环执行等待结果；超时返回空
 * eg用于视图搜索
 * @param waitMillis Long
 * @param run () -> T 返回空时，重新执行，直到超时
 * @return T
 */
fun <T> whileWaitTime(waitMillis: Long, run: () -> T?): T? {
    val begin = System.currentTimeMillis()
    val ct = Thread.currentThread()
    do {
        run.invoke()?.also {
            //if 耗时操作
            return it
        }
    } while (System.currentTimeMillis() - begin < waitMillis && !ct.isInterrupted)
    return null
}

/**
 * 循环执行等待run结果；超过次数返回空
 *
 * @param waitCount Int
 * @param run () -> T?
 * @return T? 执行结果
 */
fun <T> whileWaitCount(waitCount: Int, run: () -> T?): T? {
    var count = 0
    val ct = Thread.currentThread()
    while (count++ < waitCount && !ct.isInterrupted) {
        run.invoke()?.also {
            //if 耗时操作
            return it
        }
    }
    return null
}

fun runWithClock(tag: String? = null, block: () -> Unit) {
    if (BuildConfig.DEBUG) {
        val b = System.currentTimeMillis()
        block.invoke()
        val e = System.currentTimeMillis()
        Vog.d("[$tag]执行结束 用时 ${prettyMillisTime(e - b)}")
    } else {
        block.invoke()
    }
}

/**
 * 解析x时x分x秒x毫秒
 * @param millis Long
 * @return String
 */
fun prettyMillisTime(millis: Long): String = buildString {
    (millis / (60 * 60 * 1000)).also { if (it > 0) append("${it}h") }
    var t = millis % (60 * 60 * 1000)
    (t / (60 * 1000)).also { if (it > 0) append("${it}m") }
    t %= 60 * 1000
    (t / 1000).also { append("$it.") }
    t %= 1000
    append("${t}s")
}

fun formatNow(pat: String = "yyyy-MM-dd HH:mm:ss"): String = SimpleDateFormat(pat, Locale.getDefault()).format(Date())

fun Context.startActivityOnNewTask(intent: Intent) {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun View.boundsInScreen(): Rect {
    val rect = Rect()
    val arr = IntArray(2)
    getLocationOnScreen(arr)
    rect.left = arr[0]
    rect.right = arr[0] + width
    rect.top = arr[1]
    rect.bottom = arr[1] + height
    return rect
}

/**
 * 是否为输入法
 * @receiver AppInfo
 * @param context Context
 * @return Boolean
 */
fun AppInfo.isInputMethod(context: Context): Boolean {
    return try {
        //获取输入法列表
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val methodList = imm.inputMethodList
        methodList.map {
            it.packageName
        }.contains(packageName)
    } catch (e: Throwable) {
        e.log()
        false
    }
}

val appActivityCache = hashMapOf<String, Array<String>>()

//fixme packageManager died
fun AppInfo.activities(): Array<String> {
    synchronized(AppInfo::class) {
        try {
            appActivityCache[packageName]?.let {
                return it
            }
            val pm = GlobalApp.APP.packageManager
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            val acs = pkgInfo.activities
            val rList = mutableListOf<String>()
            acs?.forEach { ac ->
                ac.name?.also {
                    rList.add(it)
                }
            }
            return rList.toTypedArray()
        } catch (e: Throwable) {
            return emptyArray()
        }
    }
}


fun AppInfo.hasMicroPermission(): Boolean {
    if (packageName == GlobalApp.APP.packageName) return false//排除自身

    return try {
        val pm = GlobalApp.APP.packageManager
        (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(Manifest.permission.RECORD_AUDIO, packageName)).also {
            Vog.d("$name 麦克风权限: $it")
        }
    } catch (e: Exception) {
        GlobalLog.err(e)
        false
    }
}

fun AppInfo.name(): String? {
    return try {
        name
    } catch (e: Throwable) {
        e.log()
        null
    }
}

fun View.toggleVisibility(toggleVisibility: Int = View.GONE) {
    runOnUi {
        visibility = if (visibility == toggleVisibility) {
            View.VISIBLE
        } else {
            toggleVisibility
        }
    }
}

fun View.isVisibility(): Boolean = visibility == View.VISIBLE

fun View.gone() {
    runOnUi {
        visibility = View.GONE
    }
}

fun View.show() {
    runOnUi {
        visibility = View.VISIBLE
    }
}

fun View.inVisibility() {
    runOnUi {
        visibility = View.INVISIBLE
    }
}


fun View.setPadding(pad: Int) {
    setPadding(pad, pad, pad, pad)
}

fun Intent.newTask(): Intent {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

fun Intent.newDoc(): Intent {
    newTask()
    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    return this
}

fun Intent.clearTask(): Intent {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

/**
 * 当没有key时,设置map[k] = v 并返回v
 * @receiver Map<K, V>
 * @param k K
 * @param v V
 * @return V
 */
inline fun <reified K, reified V> HashMap<K, V>.getOrSetDefault(k: K, v: V): V {
    return if (containsKey(k)) {
        get(k)!!
    } else {
        this[k] = v
        v
    }
}

/**
 * 兼容检测权限
 * @receiver Context
 * @param p String
 */
fun Context.checkPermission(p: String): Boolean {
    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        checkSelfPermission(p)
    else ActivityCompat.checkSelfPermission(this, p)

    return result == PackageManager.PERMISSION_GRANTED
}

fun Context.color(@ColorRes id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(id)
    } else {
        resources.getColor(id)
    }
}

val String.asColor: Int get() = Color.parseColor(this)

/**
 * 截取Activity
 * @param activity Activity
 * @param removeStatusBar Boolean
 * @return Bitmap?
 */
fun activityShot(activity: Activity, removeStatusBar: Boolean = false): Bitmap? {
    Vog.d("$activity")
    val view = activity.window.decorView
    view.setWillNotCacheDrawing(false)
    view.buildDrawingCache()
    val rect = Rect()

    view.getWindowVisibleDisplayFrame(rect)
    val statusbarHeight = rect.top
    val winManager = activity.windowManager

    val outMetrics = DisplayMetrics()
    winManager.defaultDisplay.getMetrics(outMetrics)

    val w = outMetrics.widthPixels
    val h = outMetrics.heightPixels

    //去除状态栏
    val bm = Bitmap.createBitmap(view.drawingCache, 0,
            if (removeStatusBar) statusbarHeight else 0, w, h - statusbarHeight)

    Vog.d("${bm?.width} x ${bm?.height}")

    view.destroyDrawingCache()
    view.setWillNotCacheDrawing(true)
    return bm
}


operator fun String.times(number: Int): String {
    return buildString {
        for (i in 1..number) {
            append(this@times)
        }
    }
}

fun <K, V> HashMap<K, V>.getAndRemove(k: K): V? {
    return get(k)?.also {
        remove(k)
    }
}

fun Animator.listener(lis: AnimatorListener.() -> Unit) {
    val al = AnimatorListener()
    lis.invoke(al)
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationEnd(animation: Animator?) {
            al._onEnd?.invoke()
        }

        override fun onAnimationRepeat(animation: Animator?) {
            al._onRepeat?.invoke()
        }

        override fun onAnimationCancel(animation: Animator?) {
            al._onCancel?.invoke()
        }

        override fun onAnimationStart(animation: Animator?) {
            al._onStart?.invoke()
        }
    }
    )
}

/**
 *
 * @receiver Animation
 */
fun Animation.listener(lis: AnimationListener.() -> Unit) {
    val al = AnimationListener()
    lis.invoke(al)
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
            al._onRepeat?.invoke()
        }

        override fun onAnimationEnd(animation: Animation?) {
            al._onEnd?.invoke()
        }

        override fun onAnimationStart(animation: Animation?) {
            al._onStart?.invoke()
        }
    })
}

class AnimatorListener : AnimationListener() {
    var _onCancel: (() -> Unit)? = null

    fun onCancel(c: () -> Unit) {
        _onCancel = c
    }
}

open class AnimationListener {
    var _onEnd: (() -> Unit)? = null
    var _onStart: (() -> Unit)? = null
    var _onRepeat: (() -> Unit)? = null

    fun onEnd(e: () -> Unit) {
        _onEnd = e
    }

    fun onStart(s: () -> Unit) {
        _onStart = s
    }

    fun onRepeat(r: () -> Unit) {
        _onRepeat = r
    }
}

fun EditText.content(): String = this.text.toString()


fun File.broadcastImageFile() {
    GlobalApp.APP.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            Uri.fromFile(this)))
}

fun Activity.finishAndRemoveTaskCompat() {
    if (Build.VERSION.SDK_INT < 21) {
        finish()
    } else {
        finishAndRemoveTask()
    }
}


infix fun Array<String>.anyIn(s: String): Boolean {
    forEach {
        if (it in s) return true
    }
    return false
}

//上次点击时间
private var lastClickTime = 0L

/**
 * 是否快速点击
 * @param interval 间隔 Int 默认500ms
 * @return Any?
 */
@Suppress("RedundantUnitExpression")
fun isQuickClick(interval: Int = 300): Unit? {
    val now = System.currentTimeMillis()
    return (if (now - lastClickTime > interval) {
        Unit
    } else null).also {
        lastClickTime = now
    }
}

/**
 * 设置防抖动点击事件
 * @receiver View
 * @param clickAction Function1<View, Unit>
 */
fun View.onClick(clickAction: () -> Unit) {
    setOnClickListener {
        isQuickClick() ?: return@setOnClickListener
        clickAction()
    }
}


fun View.fadeIn(duration: Long = 500) {
    animation?.cancel()
    visibility = View.VISIBLE
    startAnimation(AlphaAnimation(0f, 1f).apply {
        setDuration(duration)
    })
}

fun View.fadeOut(
        duration: Long = 800,
        endStatus: Int = View.GONE,
        end: Function0<Unit>? = null
) {
    startAnimation(AlphaAnimation(1f, 0f).apply {
        this.duration = duration
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                visibility = endStatus
                end?.invoke()
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
    })
}

operator fun Intent.contains(key: String): Boolean = hasExtra(key)

@Suppress("UNCHECKED_CAST")
operator fun <T> Intent.get(key: String, def: T): T {
    return extras?.get(key) as T? ?: def
}

inline fun <reified ACT> Context.startActivity(noinline intentBuilder: (Intent.() -> Unit)? = null) {
    startActivity(Intent(this, ACT::class.java).also {
        if (intentBuilder != null) it.apply(intentBuilder)
    })
}

fun Intent.putArgs(vararg args: Pair<String, Any>) {
    putExtras(bundle(*args))
}

/**
 * 构建 Bundle
 * 示例：
 * bundle(1 to "a", 2 to 2)
 * @param extras Array<out Pair<String, Any>>
 * @return Bundle
 */
fun bundle(vararg extras: Pair<String, Any>): Bundle {
    return Bundle().apply {
        extras.forEach {
            when (val value = it.second) {
                is Int -> putInt(it.first, value)
                is IntArray -> putIntArray(it.first, value)
                is Long -> putLong(it.first, value)
                is LongArray -> putLongArray(it.first, value)
                is Float -> putFloat(it.first, value)
                is FloatArray -> putFloatArray(it.first, value)
                is Byte -> putByte(it.first, value)
                is ByteArray -> putByteArray(it.first, value)
                is Char -> putChar(it.first, value)
                is CharArray -> putCharArray(it.first, value)
                is String -> putString(it.first, value)
                is ArrayList<*> -> putStringArrayList(it.first, value as java.util.ArrayList<String>?)
                is CharSequence -> putCharSequence(it.first, value)
                is Serializable -> putSerializable(it.first, value)
//                is IBinder -> putBinder(it.first, value)
//                is Size -> putSize(it.first, value)
            }
        }

    }
}

operator fun String.div(any: Any?): String = format(any)


/**
 *
 * @receiver String
 * @param selectedString String
 * @param fontSize Int dip
 * @param color Int? @ColorInt
 * @param underLine Boolean
 * @param typeface Int?
 * @return SpannableStringBuilder
 */
fun String.span(selectedString: String = this, fontSize: Int? = null, color: Int? = null,
                underLine: Boolean = false, typeface: Int? = null): SpannableStringBuilder {
    return MultiSpan(this, selectedString, color = color, fontSize = fontSize,
            underLine = underLine, typeface = typeface).build()
}

fun String.spanColor(colorString: String): SpannableStringBuilder {
    return spanColor(colorString.asColor)
}

fun String.spanColor(color: Int): SpannableStringBuilder {
    return MultiSpan(this, color = color).build()
}

/**
 * 计算图片文件尺寸
 * @receiver File
 * @return Pair<Int, Int> h to w
 */
fun File.calImageSize(): Pair<Int, Int> {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)//这里的bitmap是个空
    return options.outHeight to options.outWidth
}


fun Drawable.toBitmap(): Bitmap {
    val w = intrinsicWidth
    val h = intrinsicHeight
    val config = if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888
    else Bitmap.Config.RGB_565

    val bitmap = Bitmap.createBitmap(w, h, config)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, w, h)
    draw(canvas);
    return bitmap
}



val ActionNode.regParamSet: Set<String> get() {
        val set = mutableSetOf<String>()
        @Suppress("RegExpRedundantEscape")//运行时解析错误
        val reg = "@\\{#?([^}.]+)\\}".toRegex()
        regs?.map { it.regStr }?.forEach { s ->
            reg.findAll(s).map { it.groupValues[1] }.forEach { set.add(it) }
        }
        return set
    }