package ru.ll.sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import ru.ll.sign.databinding.FragmentDialogBinding


class ResultDialogFragment : DialogFragment() {

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> println("uri: $uri") }

    var _binding: FragmentDialogBinding? = null
    val binding: FragmentDialogBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.main.setOnClickListener {
            filePicker.launch("*/*");
        }
    }
}