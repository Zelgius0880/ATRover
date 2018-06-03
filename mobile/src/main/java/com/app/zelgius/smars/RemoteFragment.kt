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
                angleText.text = String.format("%.2f°", it.angle)
                powerText.text = String.format("%.2f%%", it.power)
            }
        })


        viewModel.obstacle.observe(this, Observer {
            if(it == true){
                remote.backgroundTintList = ColorStateList.valueOf(activity!!.getColor(R.color.md_red_500))
            } else {
                remote.backgroundTintList = ColorStateList.valueOf(activity!!.getColor(R.color.colorPrimaryDark))
            }
        })

        joystick.setJoystickListener(object : JoystickListener{
            override fun onDown() {

            }

            override fun onDrag(degrees: Float, offset: Float) {
                angleText.text = String.format("%.2f°", degrees)
                powerText.text = String.format("%.2f%%", offset*100)

                viewModel.setDirection(degrees, offset)
            }

            override fun onUp() {
                angleText.text = String.format("%.2f°", 0.0)
                powerText.text = String.format("%.2f%%", 0.0)

                viewModel.setDirection(0.0f, 0.0f)
            }

        })
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
