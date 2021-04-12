package com.sillylife.knocknock.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.sillylife.knocknock.R
import okhttp3.ResponseBody
import java.io.*
import java.text.DecimalFormat
import java.util.*


object FileUtils {
    val DOCUMENTS_DIR = "documents"

    // configured android:authorities in AndroidManifest (https://developer.android.com/reference/android/support/v4/content/FileProvider)
    val AUTHORITY = "YOUR_AUTHORITY.provider"
    val HIDDEN_PREFIX = "."

    /**
     * TAG for log messages.
     */
    internal val TAG = "FileUtils"
    private val DEBUG = false // Set to true to enable logging

    /**
     * File and folder comparator. TODO Expose sorting option method
     */
    var sComparator: Comparator<File> = Comparator { f1, f2 ->
        // Sort alphabetically by lower case, which is much cleaner
        f1.name.toLowerCase().compareTo(
                f2.name.toLowerCase()
        )
    }

    /**
     * File (not directories) filter.
     */
    var sFileFilter: FileFilter = FileFilter { file ->
        val fileName = file.name
        // Return files only (not directories) and skip hidden files
        file.isFile && !fileName.startsWith(HIDDEN_PREFIX)
    }

    /**
     * Folder (directories) filter.
     */
    var sDirFilter: FileFilter = FileFilter { file ->
        val fileName = file.name
        // Return directories only and skip hidden directories
        file.isDirectory && !fileName.startsWith(HIDDEN_PREFIX)
    }

    val downloadsDir: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val availableSpaceInMB: Long
        get() {
            if (isExternalStorageAvailable && !isExternalStorageReadOnly && Environment.getExternalStorageDirectory().exists()) {
                val SIZE_KB = 1024L
                val SIZE_MB = SIZE_KB * SIZE_KB
                var availableSpace = -1L
                val stat = StatFs(Environment.getExternalStorageDirectory().path)
                availableSpace = stat.availableBlocksLong * stat.blockSizeLong
                return availableSpace / SIZE_MB
            } else {
                return -1L
            }
        }

    val isExternalStorageAvailable: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == extStorageState
        }

    val isExternalStorageReadOnly: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
        }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }
        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    fun isMediaUri(uri: Uri): Boolean {
        return "media".equals(uri.authority!!, ignoreCase = true)
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    fun getUri(file: File?): Uri? {
        return if (file != null) {
            Uri.fromFile(file)
        } else null
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    fun getPathWithoutFilename(file: File?): File? {
        if (file != null) {
            if (file.isDirectory) {
                // no file to be split off. Return everything
                return file
            } else {
                val filename = file.name
                val filepath = file.absolutePath

                // Construct path without file name.
                var pathwithoutname = filepath.substring(
                        0,
                        filepath.length - filename.length
                )
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length - 1)
                }
                return File(pathwithoutname)
            }
        }
        return null
    }

    /**
     * @return The MIME type for the given file.
     */
    fun getMimeType(file: File): String? {

        val extension = getExtension(file.name)

        return if (extension!!.length > 0) MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1)) else "application/octet-stream"

    }

    /**
     * @return The MIME type for the give Uri.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getMimeType(context: Context, uri: Uri): String? {
        val file = File(getPath(context, uri))
        return getMimeType(file)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is local.
     */
    fun isLocalStorageDocument(uri: Uri): Boolean {
        return AUTHORITY == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isGoogleDrive(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = MediaStore.Files.FileColumns.DATA
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor)

                val columnIndex = cursor.getColumnIndex(column)
                if (columnIndex > -1) {
                    return cursor.getString(columnIndex)
                } else {
                    return null
                }
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @see .isLocal
     * @see .getFile
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

        if (DEBUG)
            Log.d(
                    "$TAG File -",
                    "Authority: " + uri.authority +
                            ", Fragment: " + uri.fragment +
                            ", Port: " + uri.port +
                            ", Query: " + uri.query +
                            ", Scheme: " + uri.scheme +
                            ", Host: " + uri.host +
                            ", Segments: " + uri.pathSegments.toString()
            )

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//        content://com.google.android.apps.docs.storage/document/acc=1;doc=encoded=X8mOUUhHoGMFQtK0OI66FjLvwxGHdhIiUEEu7BdeysJkeDWPFum1GU8n
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri)
            } else if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)

                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4)
                }

                val contentUriPrefixesToTry =
                        arrayOf("content://downloads/public_downloads", "content://downloads/my_downloads")

                for (contentUriPrefix in contentUriPrefixesToTry) {
                    val contentUri =
                            ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id!!))
                    try {
                        val path = getDataColumn(context, contentUri, null, null)
                        if (!CommonUtil.textIsEmpty(path)) {
                            Log.d(TAG, path!! + " - okay i got a path")
                            return path
                        }
                    } catch (e: Exception) {
                    }

                }

                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                val fileName = getFileName(context, uri)
                val cacheDir = getDocumentCacheDir(context)
                val file = generateFileName(fileName, cacheDir)
                var destinationPath: String? = null
                if (file != null) {
                    destinationPath = file.absolutePath
                    saveFileFromUri(context, uri, destinationPath)
                }

                return destinationPath
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
            // MediaProvider
            // DownloadsProvider
            // ExternalStorageProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @author paulburke
     * @see .getPath
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getFile(context: Context, uri: Uri?): File? {
        if (uri != null) {
            val path = getPath(context, uri)
            if (path != null && isLocal(path)) {
                return File(path)
            }
        }
        return null
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     * @author paulburke
     */
    fun getReadableFileSize(size: Int): String {
        val BYTES_IN_KILOBYTES = 1024
        val dec = DecimalFormat("###.#")
        val KILOBYTES = " KB"
        val MEGABYTES = " MB"
        val GIGABYTES = " GB"
        var fileSize = 0f
        var suffix = KILOBYTES

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES
                    suffix = GIGABYTES
                } else {
                    suffix = MEGABYTES
                }
            }
        }
        return dec.format(fileSize.toDouble()) + suffix
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     */
    fun createGetContentIntent(): Intent {
        // Implicitly allow the user to select a particular kind of data
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // The MIME data type filter
        intent.type = "*/*"
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }


    fun getDocumentCacheDir(@NonNull context: Context): File {
        val dir = File(context.cacheDir, DOCUMENTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        logDir(context.cacheDir)
        logDir(dir)

        return dir
    }

    private fun logDir(dir: File) {
        if (!DEBUG) return
        Log.d(TAG, "Dir=$dir")
        val files = dir.listFiles()
        for (file in files) {
            Log.d(TAG, "File=" + file.path)
        }
    }

    @Nullable
    fun generateFileName(@Nullable name: String?, directory: File): File? {
        var name: String? = name ?: return null

        var file = File(directory, name)

        if (file.exists()) {
            var fileName: String = name!!
            var extension = ""
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex)
                extension = name.substring(dotIndex)
            }

            var index = 0

            while (file.exists()) {
                index++
                name = "$fileName($index)$extension"
                file = File(directory, name)
            }
        }

        try {
            if (!file.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            Log.w(TAG, e)
            return null
        }

        logDir(directory)

        return file
    }

    /**
     * Writes response body to disk
     *
     * @param body ResponseBody
     * @param path file path
     * @return File
     */
    fun writeResponseBodyToDisk(body: ResponseBody, path: String): File? {
        try {
            val target = File(path)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                inputStream = body.byteStream()
                outputStream = FileOutputStream(target)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read)
                }

                outputStream.flush()

                return target
            } catch (e: IOException) {
                return null
            } finally {
                inputStream?.close()

                outputStream?.close()
            }
        } catch (e: IOException) {
            return null
        }

    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String?) {
        var `is`: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
            val buf = ByteArray(1024)
            `is`!!.read(buf)
            do {
                bos.write(buf)
            } while (`is`.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun readBytesFromFile(filePath: String): ByteArray {

        var fileInputStream: FileInputStream? = null
        var bytesArray: ByteArray? = null

        try {

            val file = File(filePath)
            bytesArray = ByteArray(file.length().toInt())

            //read file into bytes[]
            fileInputStream = FileInputStream(file)
            fileInputStream.read(bytesArray)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        }

        return bytesArray!!

    }

    @Throws(IOException::class)
    fun createTempImageFile(context: Context, fileName: String): File {
        // Create an image file name
        val storageDir = File(context.cacheDir, DOCUMENTS_DIR)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getFileName(@NonNull context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        var filename: String? = null

        if (mimeType == null && context != null) {
            val path = getPath(context, uri)
            if (path == null) {
                filename = getName(uri.toString())
            } else {
                val file = File(path)
                filename = file.name
            }
        } else {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }

        return filename
    }

    fun getName(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val index = filename.lastIndexOf('/')
        return filename.substring(index + 1)
    }

    fun getOfflineEpisodeFilePathIfExists(episodeId: Int, context: Context): String? {
        var filePath: String? = null
        val contents = getEpisodeDownloadDirectory(context).listFiles()
        if (contents.size > 0) {
            for (f in contents) {
                if (f.name.toLowerCase().contains(episodeId.toString())) {
                    filePath = f.path
                    break
                }
            }
        }
        return filePath
    }

    fun getEpisodeDownloadDirectory(context: Context): File {
        val videoDirectory = File(context.applicationContext.filesDir, getAppName(context))
        if (!videoDirectory.exists()) {
            videoDirectory.mkdirs()
        }
        return videoDirectory
    }

    fun getAppsFileDirectory(context: Context): File {
        val directory = File(context.filesDir, getAppName(context))
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    private fun getAppName(context: Context): String {
        return "KnockNock"
    }

    fun isEnoughSpaceAvailable(currentFileLength: Long): Boolean {
        val spaceAvailable = availableSpaceInMB
        val kb = calculateFileSizeInKb(currentFileLength)
        val fileSize = calculateFileSizeInMb(kb)
        return spaceAvailable >= fileSize
    }

    fun checkAndGetFile(audioFile: Uri?, context: Context): Uri? {
        var file = File(audioFile.toString())
        if (audioFile != null && File(audioFile?.path).path.split("/")[1].contains("root_path")) {
            file = File(File(audioFile?.path).path.removePrefix("/root_path"))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && audioFile != null) {
                file = File(getPath(context, audioFile))
            }
        }
        return if (audioFile != null && File(file.path).exists() && File(file.path).length() > 0) {
            Uri.parse(file.path)
        } else {
            null
        }
    }

    fun getPublicAlbumStorageDir(context: Context): File? {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), context.getString(
                R.string.app_name))
        if (file.mkdirs()) {

        }
        return file
    }


    fun getAvailableSpaceInMB(): Long? {
        val SIZE_KB = 1024L
        val SIZE_MB = SIZE_KB * SIZE_KB
        var availableSpace = -1L
        val stat = StatFs(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)
        availableSpace = stat.availableBlocks.toLong() * stat.blockSize.toLong()
        return availableSpace / SIZE_MB
    }

    fun calculateFileSizeInKb(fileSizeInBytes: Long): Long {
        return fileSizeInBytes / 1024
    }

    fun calculateFileSizeInMb(fileSizeInMb: Long): Long {
        return fileSizeInMb / 1024
    }

    fun isEnoughSpaceAvailable(currentFileLength: Long?): Boolean? {
        val spaceAvailable = getAvailableSpaceInMB()
        val kb = calculateFileSizeInKb(currentFileLength ?: 0)
        val fileSize = calculateFileSizeInMb(kb)
        return spaceAvailable!! >= fileSize
    }


}