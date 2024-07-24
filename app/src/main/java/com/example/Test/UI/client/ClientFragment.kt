package com.example.Test.UI.client

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.gesture.Gesture
import android.health.connect.datatypes.units.Pressure
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.Test.UI.client.config.ConfigFragment
import com.example.Test.UI.server.ServerViewModel
import com.example.Test.UI.start.StartFragmentDirections
import com.example.Test.UI.start.StartViewModel
import com.example.Test.data.model.Point
import com.example.test.databinding.FragmentClientBinding
import com.example.test.databinding.FragmentStartBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

class ClientFragment : Fragment(){

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!

    companion object {
        val OWN_IP_CONF = "ENTER_CONF"

        val RECIVED_IP_KEY = "RECIVED_IP"
        val RECIVED_PORT_KEY = "RECIVED_PORT"
        val RECIVED_PERIODICITY_KEY = "RECIVED_PERIODICITY"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var viewModel: ClientViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()


        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]

        setFragmentResultListener(OWN_IP_CONF) { _, bundle ->
            val receivedIP = bundle.getString(RECIVED_IP_KEY)
            val receivedPort = bundle.getInt(RECIVED_PORT_KEY, -1)
            val receivedPeriodically = bundle.getInt(RECIVED_PERIODICITY_KEY, -1)

            viewModel.setConf(receivedIP!!, receivedPort, receivedPeriodically)
        }

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.connectWebSocket()
                draw()
            } else {
                viewModel.disconnectWebSocket()
            }
        }

        binding.configButton.setOnClickListener {
            navController.navigate(
                ClientFragmentDirections.actionNavigationClientToNavigationConfig()
            )
        }
    }

    private val points = mutableListOf<Point>()
    private var isDragging = false
    private fun draw(){
        with(binding.swiper){
            onDrag  = { x, y, size, pressure ->
                if (isDragging){
                    points.add(Point(x, y, size, pressure))
                    viewModel.sendPoint(Point(x, y, size, pressure))
                    binding.swiper.drawLine(
                        points[points.size-2].x, points[points.size-2].y,
                        points[points.size-1].x, points[points.size-1].y,
                        size,
                        pressure
                    )
                }
            }
            onPointerDown = { x, y, size, pressure ->
                if(!isDragging){
                    points.add(Point(x, y, size, pressure))
                    viewModel.sendPoint(Point(x, y, size, pressure))
                }
                isDragging = true
            }
            onPointerUp = { x, y, size, pressure ->
                if(isDragging) {
                    viewModel.sendPoint(Point(x,y, size, pressure, true))
                    binding.swiper.clearLines()
                }
                isDragging = false
            }
        }
    }

    private fun pointsToJson(gesture: Gesture): String {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(points)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}