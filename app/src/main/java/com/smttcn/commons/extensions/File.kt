package com.smttcn.commons.extensions

import android.webkit.MimeTypeMap
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.helpers.*
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import java.io.File

fun File.isMediaFile() = absolutePath.isImageFast() || absolutePath.isVideoFast() || absolutePath.isGif() || absolutePath.isRawFast() || absolutePath.isSvg()
fun File.isGif() = absolutePath.endsWith(".gif", true)
fun File.isVideoFast() = videoExtensions.any { absolutePath.endsWith(it, true) }
fun File.isImageFast() = photoExtensions.any { absolutePath.endsWith(it, true) }
fun File.isAudioFast() = audioExtensions.any { absolutePath.endsWith(it, true) }
fun File.isRawFast() = rawExtensions.any { absolutePath.endsWith(it, true) }
fun File.isSvg() = absolutePath.isSvg()

fun File.isImageSlow() = absolutePath.isImageFast() || getMimeType().startsWith("image")
fun File.isVideoSlow() = absolutePath.isVideoFast() || getMimeType().startsWith("video")
fun File.isAudioSlow() = absolutePath.isAudioFast() || getMimeType().startsWith("audio")

fun File.getMimeType() = absolutePath.getMimeType()
fun File.getMimeTypeOfEncryptedFile() = absolutePath.getMimeTypeOfEncryptedFile()

fun File.getProperSize(countHiddenItems: Boolean): Long {
    return if (isDirectory) {
        getDirectorySize(this, countHiddenItems)
    } else {
        length()
    }
}

private fun getDirectorySize(dir: File, countHiddenItems: Boolean): Long {
    var size = 0L
    if (dir.exists()) {
        val files = dir.listFiles()
        if (files != null) {
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    size += getDirectorySize(files[i], countHiddenItems)
                } else if (!files[i].isHidden && !dir.isHidden || countHiddenItems) {
                    size += files[i].length()
                }
            }
        }
    }
    return size
}

fun File.getFileCount(countHiddenItems: Boolean, includeDirectory: Boolean = true): Int {
    return if (isDirectory) {
        getDirectoryFileCount(this, countHiddenItems, includeDirectory)
    } else {
        1
    }
}

private fun getDirectoryFileCount(dir: File, countHiddenItems: Boolean, includeDirectory: Boolean = true): Int {
    var count = -1
    if (dir.exists()) {
        val files = dir.listFiles()
        if (files != null) {
            count++
            for (i in files.indices) {
                val file = files[i]
                if (file.isDirectory) {
                    if (!file.isHidden || countHiddenItems) {
                        if (includeDirectory) count++
                        count += getDirectoryFileCount(file, countHiddenItems, includeDirectory)
                    }
                } else if (!file.isHidden || countHiddenItems) {
                    count++
                }
            }
        }
    }
    return count
}

fun File.getDirectChildrenCount(countHiddenItems: Boolean) = listFiles()?.filter { if (countHiddenItems) true else !it.isHidden }?.size ?: 0

fun File.toFileDirItem() = FileDirItem(this)

fun File.copyToFolder(targetFolder: File): File {
    val targetFile = File(targetFolder!!.canonicalPath.appendPath(this.name))
    return this.copyTo(targetFile)
}

fun File.containsNoMedia() = isDirectory && File(this, NOMEDIA).exists()

fun File.doesThisOrParentHaveNoMedia(): Boolean {
    var curFile = this
    while (true) {
        if (curFile.containsNoMedia()) {
            return true
        }
        curFile = curFile.parentFile ?: break
        if (curFile.absolutePath == "/") {
            break
        }
    }
    return false
}

fun File.getFileTypeDrawableId(): Int {
    val ext = "." + name.getFilenameExtensionOfEncryptedFile()

    if (photoExtensions.contains(ext)) return R.drawable.ic_image_file_50
    if (videoExtensions.contains(ext)) return R.drawable.ic_video_file_50
    if (audioExtensions.contains(ext)) return R.drawable.ic_audio_file_50
    if (rawExtensions.contains(ext)) return R.drawable.ic_raw_50
    if (officeWorldExtensions.contains(ext)) return R.drawable.ic_document_50
    if (officeExcelExtensions.contains(ext)) return R.drawable.ic_document_50
    if (officePowerPointExtensions.contains(ext)) return R.drawable.ic_document_50
    if (officePdfExtensions.contains(ext)) return R.drawable.ic_pdf_50
    if (officeTextExtensions.contains(ext)) return R.drawable.ic_txt_50
    if (archiveExtensions.contains(ext)) return R.drawable.ic_archive_50

    return R.drawable.ic_document_50
}
