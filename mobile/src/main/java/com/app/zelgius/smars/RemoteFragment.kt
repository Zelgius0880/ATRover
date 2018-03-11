package com.app.zelgius.smars

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_remote.*


/**
 * A simple [Fragment] subclass.
 * Use the [RemoteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RemoteFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_remote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forward.setOnTouchListener(TouchListener(MainViewModel.Command.FORWARD))
        left.setOnTouchListener(TouchListener(MainViewModel.Command.LEFT))
        right.setOnTouchListener(TouchListener(MainViewModel.Command.RIGHT))
        backward.setOnTouchListener(TouchListener(MainViewModel.Command.BACKWARD))
    }

    companion object {

        fun newInstance(): RemoteFragment {
            val fragment = RemoteFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    inner class TouchListener(private val direction: MainViewModel.Command): View.OnTouchListener{
        override fun onTouch(v: View?, e: MotionEvent?): Boolean {
            if(viewModel.connected.value == true) {
                v?.performClick()
                when (e?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        viewModel.setDirection(direction)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        viewModel.setDirection(MainViewModel.Command.STOP)
                        return true
                    }
                }
            }

            return false
        }

    }
}// Required empty public constructor
