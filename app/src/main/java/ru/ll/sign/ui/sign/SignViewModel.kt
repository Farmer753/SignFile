package ru.ll.sign.ui.sign

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.ViewModel
import timber.log.Timber
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.Enumeration

class SignViewModel : ViewModel() {

    init {
        Timber.d("SignViewModel")
    }

    var fileToSign: ByteArray? = null

    fun onSignClick() {
        Timber.d("onSignClick")
        //        val keygen = KeyGenerator.getInstance("AES")
//        keygen.init(256)
//        val key: SecretKey = keygen.generateKey()
//        key.
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            "test",
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            build()
        }

        kpg.initialize(parameterSpec)

        val kp = kpg.generateKeyPair()

        println("kp ${kp.private.algorithm}")
        println("kp ${kp.public.format}")
//
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()

        println("aliases ${aliases.toList()}")
        val entry: KeyStore.Entry = ks.getEntry("test", null)
        println("entry ${(entry as KeyStore.PrivateKeyEntry).privateKey.algorithm}")

        val signature: ByteArray = Signature.getInstance("SHA256withECDSA").run {
            initSign(entry.privateKey)
            update(fileToSign)
            sign()
        }

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

    fun onFileToSignChoosen(fileToSign: ByteArray) {
        this.fileToSign = fileToSign
    }
}