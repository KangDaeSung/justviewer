package com.kds3393.just.justviewer2.utils.glide

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import common.lib.debug.CLog
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import java.nio.charset.Charset

data class ZipData(val path:String, val entry:FileHeader, val width:Int = -1, val height:Int = -1)

class ZipImageLoader : ModelLoader<ZipData, Bitmap> {
    override fun buildLoadData(model: ZipData, width: Int, height: Int, options: Options): LoadData<Bitmap> {
        val key = "code:${model.path};entry:${model.entry.fileName}"
        return LoadData(ObjectKey(key), ZipDataFetcher(model))
    }

    override fun handles(model: ZipData): Boolean {
        return true
    }

    class Factory : ModelLoaderFactory<ZipData, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ZipData, Bitmap> {
            return ZipImageLoader()
        }
        override fun teardown() {}
    }
}

class ZipDataFetcher(private val model: ZipData) : DataFetcher<Bitmap> {
    override fun getDataClass(): Class<Bitmap> = Bitmap::class.java
    @Volatile private var isCancelled: Boolean = false

    override fun cleanup() {} //cleanup data fetcher | run on background thread

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    override fun cancel() {
        isCancelled = true
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val buffer = unzipTargetBuffer(model)
        val option = if (model.width > 0 && model.height > 0) {
            getBitmapOption(buffer, model.width, model.height)
        } else {
            null
        }
        if (buffer != null) {
            val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size, option)
            callback.onDataReady(bitmap)
        } else {
            callback.onLoadFailed(IllegalArgumentException("ZipImage Load failed ${model.entry.fileName}"))
        }
    }

    private fun unzipTargetBuffer(zipData: ZipData): ByteArray? {
        try {
            val buffer = ByteArray(1024)
            val imageBuffer = ByteArray(zipData.entry.uncompressedSize.toInt())
            var total = 0
            val zFile = ZipFile(zipData.path)
            zFile.setCharset(Charset.forName("EUC-KR"))
            val zio = zFile.getInputStream(zipData.entry)

            var len : Int
            while (zio.read(buffer).also { len = it } != -1) {
                if (isCancelled) {
                    zio.close()
                    return null
                }
                System.arraycopy(buffer, 0, imageBuffer, total, len)
                total += len
            }
            zio.close()
            return imageBuffer
        } catch (e: Exception) {
            CLog.e("unzipTargetBuffer", e)
        }
        return null
    }

    private fun getBitmapOption(buffer: ByteArray?, maxWidth: Int, maxHeight: Int): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(buffer, 0, buffer!!.size, options)
        var sampleSize = 1
        if (options.outWidth > 0 && options.outHeight > 0) {
            while (options.outWidth / sampleSize > maxWidth || options.outHeight / sampleSize > maxHeight) {
                sampleSize *= 2
            }
        } else {
            CLog.e("getBitmapOption", "Exception:divide by zero")
        }
        options.inSampleSize = sampleSize
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inJustDecodeBounds = false
        return options
    }
}