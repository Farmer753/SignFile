package ru.ll.sign.ui.sign

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.ll.sign.R
import ru.ll.sign.databinding.FragmentSignBinding
import ru.ll.sign.util.getOriginalFileName
import timber.log.Timber
import java.io.IOException
import java.io.InputStream


class SignFragment : Fragment() {

    companion object {
        const val ARG_ERROR_MESSAGE = "ARG_ERROR_MESSAGE"
    }

    private val viewModel: SignViewModel by viewModel()

    private var _binding: FragmentSignBinding? = null
    private val binding: FragmentSignBinding
        get() = _binding!!

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        Timber.d("uri: $uri")
        if (uri == null) {
            return@registerForActivityResult
        }
        val fileNameFromUri = uri.getOriginalFileName(requireContext())
        Timber.d("fileNameFromUri $fileNameFromUri")
        viewModel.onFileToSignNameReceived(fileNameFromUri!!)
        val inputStream: InputStream =
            requireContext().contentResolver.openInputStream(uri)!!
        val fileToSign = inputStream.readBytes()
        Timber.d("content ${String(fileToSign)}")
        viewModel.onFileToSignChoosen(fileToSign)
    }

    private val fileCreator = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        Timber.d("CreatorUri: $uri")
        if (uri == null) {
            return@registerForActivityResult
        }
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri)
                ?: return@registerForActivityResult
            outputStream.write(viewModel.signature)
            outputStream.close()
            viewModel.onSignSuccess()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

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
        binding.signButton.isEnabled = false
        binding.signButton.setOnClickListener { viewModel.onSignClick() }

        binding.keyGenerationButton.setOnClickListener { viewModel.generateKeys() }
        binding.keyDeleteButton.setOnClickListener { viewModel.deleteKeys() }

        binding.passwordEditText.addTextChangedListener { viewModel.onPasswordEntered(it.toString()) }
        binding.passwordEditText.setOnEditorActionListener { v, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_NEXT || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //hide the keyboard
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                //Take action
                v.clearFocus()
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }
        binding.choiceButton.setOnClickListener { filePicker.launch("*/*") }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signatureBytes
                .filterNotNull()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    fileCreator.launch("${viewModel.fileToSignName.value}.signature")
                }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fileToSignName
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.fileNameTextView.text = getString(
                        R.string.chosen_file_label,
                        it ?: getString(R.string.not_chosen)
                    )
                    binding.signButton.isEnabled = it != null
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventsFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is SignViewModel.Event.SignError -> {
                            findNavController().navigate(
                                R.id.action_open_dialog,
                                bundleOf(ARG_ERROR_MESSAGE to event.errorMessage)
                            )
                        }

                        SignViewModel.Event.SignSuccess -> {
                            findNavController().navigate(
                                R.id.action_open_dialog
                            )
                        }
                    }
                }
        }
    }
}