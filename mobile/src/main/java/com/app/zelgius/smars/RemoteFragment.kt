package com.app.zelgius.smars

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.app.zelgius.shared.Direction
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

        viewModel.direction.observe(this, Observer{
            if(it!=null) {
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                power.text = String.format("%d%%", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        power.text = String.format("%d%%", 100)

        viewModel.obstacle.observe(this, Observer {
            if(it == true){
                top.backgroundTintList = ColorStateList.valueOf(activity!!.getColor(R.color.md_red_500))
            } else {
                top.backgroundTintList = ColorStateList.valueOf(activity!!.getColor(R.color.colorPrimaryDark))
            }
        })

        top.setOnTouchListener(TouchListener(Direction.FORWARD))
        left.setOnTouchListener(TouchListener(Direction.LEFT))
        right.setOnTouchListener(TouchListener(Direction.RIGHT))
        bottom.setOnTouchListener(TouchListener(Direction.BACKWARD))
    }

    inner class TouchListener(private val direction: Direction): View.OnTouchListener{
        override fun onTouch(v: View?, e: MotionEvent?): Boolean {
            if(viewModel.connected.value == true) {
                v?.performClick()
                when (e?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        viewModel.setDirection(direction, seekBar.progress.toFloat())
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        viewModel.setDirection(Direction.STOP, 0f)
                        return true
                    }
                }
            }

            return false
        }

    }

    companion object {

        fun newInstance(): RemoteFragment {
            val fragment = RemoteFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
