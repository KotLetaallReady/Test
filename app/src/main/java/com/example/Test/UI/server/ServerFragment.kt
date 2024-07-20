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
import java.io.DataOutputStream

class ServerFragment : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ServerViewModel

    private var points = mutableListOf<Point>()
    private var isDragging = false
    private var lastProcessedPoints: List<Point> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ServerViewModel::class.java)

        setupViewModelObservers()
        setupDrawing()

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPort(binding.editTextPort.text.toString())
            if (isChecked) {
                viewModel.serverOn()
            } else {
                viewModel.serverOff()
                viewModel.savePoint(requireContext())
            }
        }

        binding.buttonFullTrack.setOnClickListener {
            viewModel.getPoint(requireContext())
            viewModel.points.observe(viewLifecycleOwner, Observer { points ->
                if (!points.isNullOrEmpty()) {
                    replaySwipesWithDelay(points, 100)
                }
            })
        }
    }

    private fun setupViewModelObservers() {
        viewModel.apply {
            serverRepository.onGetNewPoints = { points ->
                simulateSwipeWithAdb(points)
            }

            points.observe(viewLifecycleOwner, Observer { points ->
                binding.swiper.clearLines()
                simulateSwipeWithAdb(points)
            })

            editTextPort.observe(viewLifecycleOwner, Observer { port ->
                binding.editTextPort.setText(port.toString())
            })
        }
    }

    private fun setupDrawing() {
        binding.swiper.apply {
            onDrag = { x, y, size, pressure ->
                if (isDragging) {
                    points.add(Point(x, y, size, pressure))
                    binding.swiper.drawLine(
                        points[points.size - 2].x, points[points.size - 2].y,
                        points[points.size - 1].x, points[points.size - 1].y,
                        size,
                        pressure
                    )
                }
            }

            onPointerDown = { x, y, size, pressure ->
                if (!isDragging) {
                    points.add(Point(x, y, size, pressure))
                }
                isDragging = true
            }

            onPointerUp = { x, y, size, pressure ->
                if (isDragging) {
                    points.add(Point(x, y, size, pressure, true))
                    binding.swiper.clearLines()
                }
                isDragging = false
            }
        }
    }

    private fun replaySwipesWithDelay(points: List<Point>, delay: Long) {
        if (points.isEmpty()) return

        val handler = Handler(Looper.getMainLooper())
        val commands = mutableListOf<String>()

        points.forEachIndexed { index, point ->
            val action = if (point.isEnd) "up" else "move"
            commands.add("input touchscreen $action ${point.x} ${point.y}")

            if (index == 0) {
                commands.add(0, "input touchscreen down ${point.x} ${point.y}")
            }

            if (index == points.size - 1 || point.isEnd) {
                commands.add("input touchscreen up ${point.x} ${point.y}")
            }
        }

        commands.forEachIndexed { index, command ->
            handler.postDelayed({
                runShellCommand(command)
            }, (index * delay).toLong())
        }
    }

    private fun simulateSwipeWithAdb(points: List<Point>) {
        if (points.isEmpty()) return

        val handler = Handler(Looper.getMainLooper())
        val commands = mutableListOf<String>()

        points.forEachIndexed { index, point ->
            val action = if (point.isEnd) "up" else "move"
            commands.add("input touchscreen $action ${point.x} ${point.y}")

            if (index == 0) {
                commands.add(0, "input touchscreen down ${point.x} ${point.y}")
            }

            if (index == points.size - 1 || point.isEnd) {
                commands.add("input touchscreen up ${point.x} ${point.y}")
            }
        }

        commands.forEachIndexed { index, command ->
            handler.postDelayed({
                runShellCommand(command)
            }, (index * 10).toLong())
        }
    }

    private fun runShellCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            DataOutputStream(process.outputStream).use { outputStream ->
                outputStream.writeBytes("$command\n")
                outputStream.flush()
                outputStream.writeBytes("exit\n")
                outputStream.flush()
            }
            val exitValue = process.waitFor()
            if (exitValue != 0) {
                Log.e("runShellCommand", "Root access denied or command failed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.serverOff()
        viewModel.deletePoints()
        _binding = null
    }
}