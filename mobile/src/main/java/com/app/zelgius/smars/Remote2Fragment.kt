package com.app.zelgius.smars

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.app.zelgius.shared.Direction
import kotlinx.android.synthetic.main.fragment_remote_2.*


/**
 * A simple [Fragment] subclass.
 * Use the [Remote2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Remote2Fragment : Fragment() {

    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_remote_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.direction.observe(this, Observer{
            if(it!=null) {
            }
        })

        power1.text = String.format("%d%%", 0)
        power2.text = String.format("%d%%", 0)

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                power1.text = String.format("%d%%", progress - 100)

                if(progress - 100 >= 0){
                    seekBar1.progressTintList = (ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.md_green_500)))
                    cardView2.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.md_green_200))
                } else {
                    seekBar1.progressTintList = (ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.md_red_500)))
                    cardView2.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.md_red_200))
                }

                viewModel.setPower(progress -100f, seekBar2.progress -100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                power2.text = String.format("%d%%", progress - 100)

                if(progress - 100 >= 0){
                    seekBar2.progressTintList = (ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.md_green_500)))
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.md_green_200))
                } else {
                    seekBar2.progressTintList = (ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.md_red_500)))
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.md_red_200))
                }
                viewModel.setPower(seekBar1.progress -100f, progress -100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

   /*     seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                power.text = String.format("%d%%", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })*/
    }

    companion object {

        fun newInstance(): Remote2Fragment {
            val fragment = Remote2Fragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
