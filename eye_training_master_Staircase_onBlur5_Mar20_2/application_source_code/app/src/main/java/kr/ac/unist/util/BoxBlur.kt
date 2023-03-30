package kr.ac.unist.util

import android.graphics.Bitmap


class BoxBlur {

companion object {
    fun fastblur(sentBitmap: Bitmap, scale: kotlin.Float, radius: Int): Bitmap? {
        var sentBitmap = sentBitmap
        val width = java.lang.Math.round(sentBitmap.width * scale)
        val height = java.lang.Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
        val bitmap = sentBitmap.copy(sentBitmap.config, true)
        if (radius < 1) {
            return (null)
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(java.lang.Math.max(w, h))
        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = (i / divsum)
            i++
        }
        yi = 0
        yw = yi
        val stack = kotlin.Array<IntArray>(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + java.lang.Math.min(wm, java.lang.Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = r1 - java.lang.Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack.get(stackstart % div)
                routsum -= sir.get(0)
                goutsum -= sir.get(1)
                boutsum -= sir.get(2)
                if (y == 0) {
                    vmin[x] = java.lang.Math.min(x + radius + 1, wm)
                }
                p = pix.get(yw + vmin.get(x))
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rinsum += sir.get(0)
                ginsum += sir.get(1)
                binsum += sir.get(2)
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack.get((stackpointer) % div)
                routsum += sir.get(0)
                goutsum += sir.get(1)
                boutsum += sir.get(2)
                rinsum -= sir.get(0)
                ginsum -= sir.get(1)
                binsum -= sir.get(2)
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = java.lang.Math.max(0, yp) + x
                sir = stack.get(i + radius)
                sir[0] = r.get(yi)
                sir[1] = g.get(yi)
                sir[2] = b.get(yi)
                rbs = r1 - java.lang.Math.abs(i)
                rsum += r.get(yi) * rbs
                gsum += g.get(yi) * rbs
                bsum += b.get(yi) * rbs
                if (i > 0) {
                    rinsum += sir.get(0)
                    ginsum += sir.get(1)
                    binsum += sir.get(2)
                } else {
                    routsum += sir.get(0)
                    goutsum += sir.get(1)
                    boutsum += sir.get(2)
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] =
                    (-0x1000000 and pix.get(yi)) or (dv.get(rsum) shl 16) or (dv.get(gsum) shl 8) or dv.get(
                        bsum
                    )
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack.get(stackstart % div)
                routsum -= sir.get(0)
                goutsum -= sir.get(1)
                boutsum -= sir.get(2)
                if (x == 0) {
                    vmin[y] = java.lang.Math.min(y + r1, hm) * w
                }
                p = x + vmin.get(y)
                sir[0] = r.get(p)
                sir[1] = g.get(p)
                sir[2] = b.get(p)
                rinsum += sir.get(0)
                ginsum += sir.get(1)
                binsum += sir.get(2)
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack.get(stackpointer)
                routsum += sir.get(0)
                goutsum += sir.get(1)
                boutsum += sir.get(2)
                rinsum -= sir.get(0)
                ginsum -= sir.get(1)
                binsum -= sir.get(2)
                yi += w
                y++
            }
            x++
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return (bitmap)
    }
}
}