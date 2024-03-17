package ru.ll.sign.ui.sign

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.Enumeration

class SignViewModel : ViewModel() {

    init {
        Timber.d("SignViewModel")
    }

    companion object {
        const val ALIAS = "test"
        const val ANDROID_KEY_STORE="AndroidKeyStore"
    }

    private var fileToSign: ByteArray? = null
    var signature: ByteArray? = null
        private set

    sealed class Event {
        data object SignSuccess : Event()
        data class SignError(val errorMessage: String) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private val _fileToSignName: MutableStateFlow<String?> = MutableStateFlow(null)
    val fileToSignName: StateFlow<String?> = _fileToSignName

    private val _password: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _signatureBytes = Channel<ByteArray?>(Channel.BUFFERED)
    val signatureBytes = _signatureBytes.receiveAsFlow()
    fun onSignClick() {
        Timber.d("onSignClick")
        viewModelScope.launch {
            if (_password.value.isNullOrEmpty()) {
                eventChannel.send(Event.SignError("Введите пароль"))
                return@launch
            }

            val entry = getKeys()
            if (entry == null) {
                eventChannel.send(Event.SignError("Не найден ключ подписи"))
                return@launch
            }
            val signature: ByteArray = Signature.getInstance("SHA256withECDSA").run {
                initSign(entry.privateKey)
                update(fileToSign)
                sign()
            }

            _signatureBytes.send(signature)
            this@SignViewModel.signature = signature
            Timber.d("fileToSign $fileToSign")
            Timber.d("signature $signature")

            val valid: Boolean = Signature.getInstance("SHA256withECDSA").run {
                initVerify(entry.certificate)
                update(fileToSign)
                verify(signature)
            }

            Timber.d("valid $valid")

            val data1: ByteArray = "ByteArray1".toByteArray()

            val valid1: Boolean = Signature.getInstance("SHA256withECDSA").run {
                initVerify(entry.certificate)
                update(data1)
                verify(signature)
            }

            Timber.d("valid1 $valid1")
        }
    }

    fun onFileToSignChoosen(fileToSign: ByteArray) {
        this.fileToSign = fileToSign
    }

    fun onFileToSignNameReceived(fileName: String) {
        viewModelScope.launch { _fileToSignName.emit(fileName) }
    }

    fun onPasswordEntered(toString: String) {
        viewModelScope.launch { _password.emit(toString) }
    }

    fun onSignSuccess() {
        viewModelScope.launch {
            eventChannel.send(Event.SignSuccess)
            fileToSign = null
            signature = null
            _fileToSignName.emit(null)
        }
    }

    fun generateKeys() {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEY_STORE
        )
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            build()
        }

        kpg.initialize(parameterSpec)

        val kp = kpg.generateKeyPair()

        Timber.d("kp ${kp.private.algorithm}")
        Timber.d("kp ${kp.public.format}")
    }

    private fun getKeys(): KeyStore.PrivateKeyEntry? {
        val ks: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()

        Timber.d("aliases ${aliases.toList()}")
        val entry: KeyStore.Entry? = ks.getEntry(ALIAS, null)
        Timber.d("entry ${(entry as? KeyStore.PrivateKeyEntry)?.privateKey?.algorithm}")
        return entry as? KeyStore.PrivateKeyEntry
    }

    fun deleteKeys() {
        val ks: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }
        ks.deleteEntry(ALIAS)
    }
}