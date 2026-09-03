package com.terangashield.app.data.bloom

import java.nio.ByteBuffer
import kotlin.math.abs

/**
 * Bloom filter minimal, sans dépendance externe : structure compacte pour la base de numéros
 * signalés, embarquée en asset et chargée en mémoire au démarrage (voir [ReportedNumbersIndex]).
 * Faux négatifs impossibles, faux positifs rares et acceptables (un numéro "peut-être signalé"
 * déclenche simplement la vérification normale, pas un blocage direct).
 */
class BloomFilter private constructor(
    private val bitSetSizeBits: Int,
    private val numHashFunctions: Int,
    private val bits: ByteArray,
) {
    fun add(value: String) {
        for (hash in hashes(value)) {
            val index = abs(hash % bitSetSizeBits)
            bits[index / 8] = (bits[index / 8].toInt() or (1 shl (index % 8))).toByte()
        }
    }

    fun mightContain(value: String): Boolean {
        for (hash in hashes(value)) {
            val index = abs(hash % bitSetSizeBits)
            if ((bits[index / 8].toInt() shr (index % 8)) and 1 == 0) return false
        }
        return true
    }

    /** Double hashing (Kirsch-Mitzenmacher) : k hashes dérivés de deux hashes de base seulement. */
    private fun hashes(value: String): IntArray {
        val h1 = value.hashCode()
        val h2 = value.reversed().hashCode()
        return IntArray(numHashFunctions) { i -> h1 + i * h2 }
    }

    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(8 + bits.size)
        buffer.putInt(bitSetSizeBits)
        buffer.putInt(numHashFunctions)
        buffer.put(bits)
        return buffer.array()
    }

    companion object {
        fun empty(bitSetSizeBits: Int = DEFAULT_BITSET_SIZE_BITS, numHashFunctions: Int = DEFAULT_HASH_FUNCTIONS): BloomFilter =
            BloomFilter(bitSetSizeBits, numHashFunctions, ByteArray(bitSetSizeBits / 8 + 1))

        fun fromByteArray(bytes: ByteArray): BloomFilter {
            val buffer = ByteBuffer.wrap(bytes)
            val bitSetSizeBits = buffer.int
            val numHashFunctions = buffer.int
            val bits = ByteArray(bytes.size - 8)
            buffer.get(bits)
            return BloomFilter(bitSetSizeBits, numHashFunctions, bits)
        }

        // ~1M bits (128 Ko) et 4 fonctions de hash : jusqu'à ~50k numéros signalés pour un taux
        // de faux positifs d'environ 1%, largement dans le budget de taille de l'app.
        const val DEFAULT_BITSET_SIZE_BITS = 1_048_576
        const val DEFAULT_HASH_FUNCTIONS = 4
    }
}
