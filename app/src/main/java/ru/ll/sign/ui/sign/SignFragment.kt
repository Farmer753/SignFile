package ru.ll.sign.ui.sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.ll.sign.databinding.FragmentSignBinding
import timber.log.Timber
import java.io.IOException
import java.io.InputStream


class SignFragment : Fragment() {

    private val viewModel: SignViewModel by viewModel()

    private var _binding: FragmentSignBinding? = null
    val binding: FragmentSignBinding
        get() = _binding!!

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        println("uri: $uri")
        val inputStream: InputStream =
            requireContext().getContentResolver().openInputStream(uri!!)!!

//        val content = inputStream.bufferedReader().use(BufferedReader::readText)
        val fileToSign = inputStream.readBytes()
        Timber.d("content ${String(fileToSign)}")
        viewModel.onFileToSignChoosen(fileToSign)
    }

    private val fileCreator = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        println("CreatorUri: $uri")
//        val outputStream = requireContext().getContentResolver().openOutputStream(uri!!)!!
//        outputStream.bufferedWriter().use(BufferedWriter::flush)
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri!!)
                ?: return@registerForActivityResult
            outputStream.write(viewModel.signature)
            outputStream.close()
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

        binding.signButton.setOnClickListener { viewModel.onSignClick() }
        binding.choiceButton.setOnClickListener { filePicker.launch("*/*") }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signatureBytes
                .filterNotNull()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    fileCreator.launch("signature")
                }
        }
    }
}