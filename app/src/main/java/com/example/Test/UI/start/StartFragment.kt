package com.example.Test.UI.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.test.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var viewModel: StartViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.buttonClient.setOnClickListener {
            navController.navigate(
                StartFragmentDirections.actionNavigationStartToNavigationClient()
            )
        }

        binding.buttonServer.setOnClickListener {
            navController.navigate(
                StartFragmentDirections.actionNavigationStartToNavigationServer()
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}