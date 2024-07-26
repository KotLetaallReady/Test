package com.example.Test.UI.server

import android.content.Context
import android.hardware.input.InputManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log

import android.view.InputDevice
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

class ServerFragment : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ServerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestRootAccess()

        viewModel = ViewModelProvider(requireActivity()).get(ServerViewModel::class.java)

        setupViewModelObservers()

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPort(binding.editTextPort.text.toString())
            if (isChecked) {
                viewModel.serverOn()
                draw()
            } else {
                viewModel.serverOff()
                viewModel.savePoint(requireContext())
            }
        }

        binding.buttonFullTrack.setOnClickListener {
            viewModel.getPoint(requireContext())
        }
    }

    private val points = mutableListOf<Point>()
    private var isDragging = false
    private fun draw(){
        with(binding.swiper){
            onDrag  = { x, y, size, pressure ->
                if (isDragging){
                    points.add(Point(x, y, size, pressure, false))
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
                    points.add(Point(x, y, size, pressure, false))
                }
                isDragging = true
            }
            onPointerUp = { x, y, size, pressure ->
                if(isDragging) {
                    points.add(Point(x, y, size, pressure, true))
                    binding.swiper.clearLines()
                }
                isDragging = false
            }
        }
    }

    private fun setupViewModelObservers() {
        viewModel.apply {
            points.observe(viewLifecycleOwner, Observer { points ->
                replaySwipe(points)
            })
        }
    }

    private fun replaySwipe(points: List<Point>) {
        if (points.isEmpty()) return

        val inputManager = requireContext().getSystemService(Context.INPUT_SERVICE) as InputManager

        viewLifecycleOwner.lifecycleScope.launch {
            var previousPoint: Point? = null
            for (point in points) {
                if (previousPoint != null) {
                    binding.swiper.drawLine(
                        previousPoint.x, previousPoint.y,
                        point.x, point.y,
                        point.size,
                        point.pressure
                    )
                }

                val eventTime = SystemClock.uptimeMillis()
                val action = if (point.isEnd) MotionEvent.ACTION_UP else MotionEvent.ACTION_MOVE

                val event = MotionEvent.obtain(
                    eventTime,
                    eventTime,
                    action,
                    point.x,
                    point.y,
                    point.pressure,
                    point.size,
                    0,
                    1.0f,
                    1.0f,
                    0,
                    0
                ).apply {
                    source = InputDevice.SOURCE_TOUCHSCREEN
                }

                try {
                    val method = InputManager::class.java.getMethod(
                        "injectInputEvent",
                        MotionEvent::class.java,
                        Int::class.javaPrimitiveType
                    )
                    method.invoke(inputManager, event, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                previousPoint = point

                if (point.isEnd) {
                    delay(500)
                    binding.swiper.clearLines()
                    previousPoint = null
                }
            }

            binding.swiper.clearLines()
        }
    }

    private fun requestRootAccess() {
        GlobalScope.launch(Dispatchers.Main) {
            val shell = withContext(Dispatchers.IO) { Shell.getShell() }
            if (shell.isRoot) {
                Toast.makeText(requireContext(), "Root access granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Root access denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.serverOff()
        _binding = null
    }
}