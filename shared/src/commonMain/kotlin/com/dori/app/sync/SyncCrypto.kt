package com.dori.app.sync

import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Crypto building blocks for sync, using only what both the JVM and Android
 * ship with (no extra dependency): HMAC-SHA256/HKDF for key derivation,
 * AES-256-GCM for the wire, ECDH P-256 for pairing.
 */
internal object SyncCrypto {
    private val random = SecureRandom()

    fun randomBytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)

    fun b64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    fun unb64(text: String): ByteArray = Base64.getDecoder().decode(text)

    fun sha256(vararg parts: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        parts.forEach { digest.update(lengthPrefix(it)); digest.update(it) }
        return digest.digest()
    }

    fun hmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    /** RFC 5869 HKDF-SHA256. */
    fun hkdf(ikm: ByteArray, salt: ByteArray, info: String, length: Int = 32): ByteArray {
        val prk = hmac(if (salt.isEmpty()) ByteArray(32) else salt, ikm)
        val out = ByteArray(length)
        var previous = ByteArray(0)
        var offset = 0
        var counter = 1
        while (offset < length) {
            previous = hmac(prk, previous + info.toByteArray(Charsets.UTF_8) + byteArrayOf(counter.toByte()))
            val n = minOf(previous.size, length - offset)
            previous.copyInto(out, offset, 0, n)
            offset += n
            counter++
        }
        return out
    }

    /**
     * Proves "same group" in a discovery beacon without revealing the group:
     * only holders of the group key can compute or check it, and it differs
     * per device, so strangers can't even link beacons of one group together.
     */
    fun beaconTag(groupKey: ByteArray, deviceId: String): String {
        val beaconKey = hkdf(groupKey, ByteArray(0), "dori-beacon-v1")
        return b64(hmac(beaconKey, deviceId.toByteArray(Charsets.UTF_8)).copyOf(16))
    }

    fun beaconTagMatches(groupKey: ByteArray, deviceId: String, tag: String?): Boolean {
        if (tag == null) return false
        return MessageDigest.isEqual(
            beaconTag(groupKey, deviceId).toByteArray(Charsets.UTF_8),
            tag.toByteArray(Charsets.UTF_8)
        )
    }

    fun generateEcKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()

    fun ecdh(myKeys: KeyPair, peerPublicEncoded: ByteArray): ByteArray {
        val peerPublic = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(peerPublicEncoded))
        return KeyAgreement.getInstance("ECDH").run {
            init(myKeys.private)
            doPhase(peerPublic, true)
            generateSecret()
        }
    }

    /** The 6-digit code both sides show during pairing; equal codes mean nobody sits in between. */
    fun shortAuthString(sharedSecret: ByteArray, transcript: ByteArray): String {
        val bytes = hkdf(sharedSecret, transcript, "dori-pair-sas-v1", 4)
        val value = ByteBuffer.wrap(bytes).int.toLong() and 0xFFFFFFFFL
        return (value % 1_000_000).toString().padStart(6, '0')
    }

    private fun lengthPrefix(bytes: ByteArray) = ByteBuffer.allocate(4).putInt(bytes.size).array()
}

/**
 * One direction-pair of AES-256-GCM keys for a single connection. Nonces are
 * a per-direction message counter, which is safe because each connection
 * derives fresh keys from fresh random nonces on both sides.
 */
internal class SecureChannel(private val sendKey: ByteArray, private val receiveKey: ByteArray) {
    private var sendCounter = 0L
    private var receiveCounter = 0L

    fun seal(plaintext: ByteArray): ByteArray = crypt(Cipher.ENCRYPT_MODE, sendKey, sendCounter++, plaintext)

    /** Throws if the message was tampered with, replayed, reordered or made with another key. */
    fun open(ciphertext: ByteArray): ByteArray = crypt(Cipher.DECRYPT_MODE, receiveKey, receiveCounter++, ciphertext)

    private fun crypt(mode: Int, key: ByteArray, counter: Long, data: ByteArray): ByteArray {
        val nonce = ByteBuffer.allocate(12).putInt(0).putLong(counter).array()
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
            doFinal(data)
        }
    }

    companion object {
        /** Keys are bound to both parties' nonces and ids, so nothing from an old connection can be replayed. */
        fun derive(secret: ByteArray, transcript: ByteArray, label: String, isClient: Boolean): SecureChannel {
            val clientToServer = SyncCrypto.hkdf(secret, transcript, "$label|c2s")
            val serverToClient = SyncCrypto.hkdf(secret, transcript, "$label|s2c")
            return if (isClient) SecureChannel(clientToServer, serverToClient) else SecureChannel(serverToClient, clientToServer)
        }
    }
}
