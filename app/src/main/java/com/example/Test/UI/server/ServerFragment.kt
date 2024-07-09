package com.example.Test.UI.server

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.Test.data.AppDataBase
import com.example.Test.data.db.PointDao
import com.example.Test.data.model.Point
import com.example.test.databinding.FragmentServerBinding
import kotlinx.coroutines.launch

class ServerFragment : Fragment() {


    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!

    private var lastDrawnPoint: Point? = null
    private var pointsFT = mutableListOf<Point>()

    private lateinit var db :AppDataBase
    private lateinit var pointDao:PointDao


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var viewModel: ServerViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDataBase.getDatabase(binding.root.context)
        pointDao = db.pointDao()

        viewModel = ViewModelProvider(this)[ServerViewModel::class.java]

        viewModel.serverRepository.onGetNewPoints = { points ->
            drawLines(points, false)
        }

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPort(binding.editTextPort.text.toString())
            if (isChecked) {
                viewModel.serverOn()
            } else {
                viewModel.serverOff()

                lifecycleScope.launch {
                    pointDao?.insertPoints(viewModel.serverRepository.getPoints())
                }

            }
        }

        binding.buttonFullTrack.setOnClickListener {
            lifecycleScope.launch {
                dbToPoint()
                drawLines(pointsFT, true)
            }
        }

        viewModel.editTextPort.observe(viewLifecycleOwner, Observer { port ->
            binding.editTextPort.setText(port.toString())
        })
    }

    private fun drawLines(points: MutableList<Point>, isFullTrack: Boolean) {
        if (lastDrawnPoint != null && !lastDrawnPoint!!.isEnd) {
            binding.swiper.drawLine(
                lastDrawnPoint!!.x,
                lastDrawnPoint!!.y,
                points[0].x,
                points[0].y,
                points[0].size,
                points[0].pressure
            )
            lastDrawnPoint = points[0]
        }
        if (points.size == 1) {
            if (points[0].isEnd) {
                if(isFullTrack)
                    Thread.sleep(1000)
                binding.swiper.clearLines()
                lastDrawnPoint = points[0]
            }
        }
        for (i in 0 until points.size - 1) {
            if (points[i].isEnd) {
                if(isFullTrack)
                    Thread.sleep(1000)
                binding.swiper.clearLines()
                continue
            }
            binding.swiper.drawLine(
                points[i].x,
                points[i].y,
                points[i + 1].x,
                points[i + 1].y,
                points[i + 1].size,
                points[i + 1].pressure
            )
            lastDrawnPoint = points[i + 1]
            if (points[i + 1].isEnd) {
                if(isFullTrack)
                    Thread.sleep(1000)
                binding.swiper.clearLines()
            }
        }
    }

    private suspend fun dbToPoint() {
        if (pointsFT.size != 0) {
            pointsFT= mutableListOf<Point>()
        }
        for (i in 0..pointDao.getAllPoints().size) {
            var pointDb = Point(
                pointDao.getAllPoints()[i].x,
                pointDao.getAllPoints()[i].y,
                pointDao.getAllPoints()[i].size,
                pointDao.getAllPoints()[i].pressure,
                pointDao.getAllPoints()[i].isEnd
            )
            pointsFT.add(pointDb)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}