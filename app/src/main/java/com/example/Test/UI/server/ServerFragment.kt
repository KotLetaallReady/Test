package com.example.Test.UI.server

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.Test.UI.client.config.ConfigViewModel
import com.example.Test.data.AppDataBase
import com.example.Test.data.db.PointDao
import com.example.Test.data.model.Point
import com.example.Test.data.model.PointDB
import com.example.test.databinding.FragmentServerBinding
import kotlinx.coroutines.launch

class ServerFragment : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!

    private var lastDrawnPoint: Point? = null
    private var pointsFT = mutableListOf<Point>()

    private lateinit var db: AppDataBase
    private lateinit var pointDao: PointDao

    private lateinit var viewModel: ServerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDataBase.getDatabase(binding.root.context)
        pointDao = db.pointDao()

        viewModel = ViewModelProvider(requireActivity()).get(ServerViewModel::class.java)

        viewModel.serverRepository.onGetNewPoints = { points ->
            drawLines(points, false)
        }

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPort(binding.editTextPort.text.toString())
            if (isChecked) {
                viewModel.serverOn()
            } else {
                viewModel.serverOff()
                viewModel.savePoint(binding.root.context)
            }
        }

        binding.buttonFullTrack.setOnClickListener {
            context?.let { context ->
                viewModel.getPoint(context)
            }
        }

        viewModel.points.observe(viewLifecycleOwner, Observer { points ->
            pointsFT.clear()
            binding.swiper.clearLines()
            pointsFT.addAll(points)
            drawLines(pointsFT, true)
        })

        viewModel.editTextPort.observe(viewLifecycleOwner, Observer { port ->
            binding.editTextPort.setText(port.toString())
        })
    }

    private fun drawLines(points: MutableList<Point>, isFullTrack: Boolean) {
        lastDrawnPoint = null
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            var index = 0
            override fun run() {
                if (index < points.size) {
                    val point = points[index]
                    if (lastDrawnPoint != null && !lastDrawnPoint!!.isEnd) {
                        binding.swiper.drawLine(
                            lastDrawnPoint!!.x,
                            lastDrawnPoint!!.y,
                            point.x,
                            point.y,
                            point.size,
                            point.pressure
                        )
                    }
                    lastDrawnPoint = point
                    index++
                    if (point.isEnd) {
                        binding.swiper.clearLines()
                        lastDrawnPoint = null
                        if (isFullTrack) {
                            handler.postDelayed(this, 1000)
                        } else {
                            handler.post(this)
                        }
                    } else {
                        handler.post(this)
                    }
                }
            }
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.serverOff()
        viewModel.deletePoints()
        _binding = null
    }
}