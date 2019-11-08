package cn.vove7.jarvis.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

@SuppressLint("ValidFragment")
/**
 * # WelcomeFragment
 *
 * @author 17719247306
 * 2018/8/29
 */
class WelcomeFragment(@LayoutRes val staticLayoutId: Int) : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(staticLayoutId, container,false)

        return v
    }
}