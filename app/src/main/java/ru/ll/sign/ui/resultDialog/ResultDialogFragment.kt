package ru.ll.sign.ui.resultDialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.ll.sign.databinding.FragmentDialogBinding
import ru.ll.sign.ui.sign.SignFragment.Companion.ARG_ERROR_MESSAGE


class ResultDialogFragment : DialogFragment() {

    private var _binding: FragmentDialogBinding? = null
   private val binding: FragmentDialogBinding
        get() = _binding!!

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.BOTTOM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val errorMessage = requireArguments().getString(ARG_ERROR_MESSAGE)
        binding.resultTextView.text = errorMessage ?: "Файл успешно подписан"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}