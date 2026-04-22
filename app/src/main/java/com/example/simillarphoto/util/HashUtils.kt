package com.example.simillarphoto.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

object HashUtils {
    private const val TAG = "HashUtils"
    private const val BLUR_THRESHOLD = 100.0

    /**
     * Generates a 64-bit dHash (Difference Hash) for a given bitmap.
     */
    fun generateHash(bitmap: Bitmap): Long {
        val resized = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
        var hash: Long = 0
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val pixelLeft = resized.getPixel(x, y)
                val pixelRight = resized.getPixel(x + 1, y)
                
                val grayLeft = toGrayscale(pixelLeft)
                val grayRight = toGrayscale(pixelRight)
                
                if (grayLeft > grayRight) {
                    hash = hash or (1L shl (y * 8 + x))
                }
            }
        }
        resized.recycle()
        return hash
    }

    private fun toGrayscale(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    fun calculateHammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    /**
     * Laplacian variance method for blur detection.
     */
    fun isBlurry(bitmap: Bitmap): Boolean {
        val score = calculateBlurScore(bitmap)
        Log.d(TAG, "Blur score: $score")
        return score < BLUR_THRESHOLD
    }

    private fun calculateBlurScore(bitmap: Bitmap): Double {
        val resized = if (bitmap.width > 100 || bitmap.height > 100) {
            Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        } else {
            bitmap
        }
        
        val width = resized.width
        val height = resized.height
        val pixels = IntArray(width * height)
        resized.getPixels(pixels, 0, width, 0, 0, width, height)

        val grayscale = DoubleArray(width * height)
        for (i in pixels.indices) {
            grayscale[i] = toGrayscale(pixels[i]).toDouble()
        }

        // Simple 3x3 Laplacian kernel
        val kernel = doubleArrayOf(
            0.0, 1.0, 0.0,
            1.0, -4.0, 1.0,
            0.0, 1.0, 0.0
        )

        var sum = 0.0
        var sumSq = 0.0
        val count = (width - 2) * (height - 2)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var lapValue = 0.0
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        lapValue += grayscale[(y + ky) * width + (x + kx)] * kernel[(ky + 1) * 3 + (kx + 1)]
                    }
                }
                sum += lapValue
                sumSq += lapValue * lapValue
            }
        }

        if (resized != bitmap) resized.recycle()

        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)
        return variance
    }
}
