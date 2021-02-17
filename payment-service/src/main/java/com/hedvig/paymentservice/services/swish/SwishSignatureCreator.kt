package com.hedvig.paymentservice.services.swish

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.File
import java.io.FileReader
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.util.Base64

object SwishSignatureCreator {

    fun createSignature(payload: String, signingPrivatePemPath: String): String {
        val msgBytes = payload.toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        val hashValue = md.digest(msgBytes);

        val sig = Signature.getInstance("NONEwithRSA")
        val privateKey = loadPrivateKey(signingPrivatePemPath)
        sig.initSign(privateKey)
        sig.update(hashValue)
        val signatureBytes = sig.sign()
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    private fun loadPrivateKey(signingPrivatePemPath: String): PrivateKey {
        val pemParser = PEMParser(FileReader(File(signingPrivatePemPath)))
        val privateKeyInfoAny = pemParser.readObject()
        val converter = JcaPEMKeyConverter()

        return converter.getPrivateKey(privateKeyInfoAny as PrivateKeyInfo)
    }

}
