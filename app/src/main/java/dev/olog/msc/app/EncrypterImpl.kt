package dev.olog.msc.app

import com.tozny.crypto.android.AesCbcWithIntegrity
import dev.olog.domain.IEncrypter
import dev.olog.msc.BuildConfig
import javax.inject.Inject

private val SALT = byteArrayOf(
    -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
    -45, 77, -117, -36, -113, -11, 32, -64, 89
)

class EncrypterImpl @Inject constructor(

) : IEncrypter {

    private val key by lazy {
        AesCbcWithIntegrity.generateKeyFromPassword(
            BuildConfig.AES_PASSWORD,
            SALT
        )
    }

    override fun encrypt(string: String): String {
        if (string.isNotBlank()) {
            val cipher = AesCbcWithIntegrity.encrypt(string, key)
            return cipher.toString()
        } else {
            return string
        }

    }

    override fun decrypt(string: String): String {
        if (string.isNotBlank()) {
            val cipher = AesCbcWithIntegrity.CipherTextIvMac(string)
            return AesCbcWithIntegrity.decryptString(cipher, key)
        } else {
            return string
        }

    }

}