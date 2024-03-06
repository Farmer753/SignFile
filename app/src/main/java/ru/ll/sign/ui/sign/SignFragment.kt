package ru.ll.sign.ui.sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.ll.sign.databinding.FragmentSignBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class SignFragment : Fragment() {

    val viewModel: SignViewModel by viewModel()

    var _binding: FragmentSignBinding? = null
    val binding: FragmentSignBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signButton.setOnClickListener {
            viewModel.onSignClick()
        }
    }
}