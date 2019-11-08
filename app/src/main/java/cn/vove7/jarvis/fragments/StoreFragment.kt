package cn.vove7.jarvis.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.vove7.jarvis.R


class StoreFragment : androidx.fragment.app.Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_store, container, false)
    }


    companion object {

        @JvmStatic
        fun newInstance() = StoreFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
