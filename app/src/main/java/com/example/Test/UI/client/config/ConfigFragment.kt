package com.example.Test.UI.client.config

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.Test.UI.client.ClientViewModel
import com.example.Test.UI.start.StartFragmentDirections
import com.example.Test.UI.start.StartViewModel
import com.example.test.R
import com.example.test.databinding.FragmentClientBinding
import com.example.test.databinding.FragmentConfigBinding

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!

    companion object {
        val OWN_IP_KEY = "RECIVED_IP"
        val OWN_PORT_KEY = "RECIVED_PORT"
        val OWN_PERIODICITY_KEY = "RECIVED_PERIODICITY"

        val SENDED_CONF = "ENTER_CONF"
    }

    private lateinit var viewModel: ConfigViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ConfigViewModel::class.java)

        val navController = findNavController()

        viewModel.editTextIp.observe(viewLifecycleOwner, Observer { newIp ->
            binding.ipEditText.setText(newIp ?: "0.0.0.0")
        })

        viewModel.editTextPort.observe(viewLifecycleOwner, Observer { newPort ->
            binding.portEditText.setText(newPort?.toString() ?: "8080")
        })

        viewModel.editTextPeriodicity.observe(viewLifecycleOwner, Observer { newPeriodicity ->
            binding.periodicityEditText.setText(newPeriodicity?.toString() ?: "150")
        })

        binding.backButton.setOnClickListener {
            val ipText = binding.ipEditText.text?.toString().takeIf { it!!.isNotBlank() } ?: "0.0.0.0"
            val portText = binding.portEditText.text?.toString()?.toIntOrNull() ?: 8080
            val periodicityText = binding.periodicityEditText.text?.toString()?.toIntOrNull() ?: 150

            setFragmentResult(SENDED_CONF, bundleOf(
                OWN_IP_KEY to ipText,
                OWN_PORT_KEY to portText,
                OWN_PERIODICITY_KEY to periodicityText,
            ))

            viewModel.setConf(ipText, portText, periodicityText)

            navController.navigate(
                ConfigFragmentDirections.actionNavigationConfigToNavigationClient()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
