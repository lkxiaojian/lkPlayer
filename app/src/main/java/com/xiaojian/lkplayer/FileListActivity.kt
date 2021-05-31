package com.xiaojian.lkplayer

import android.Manifest
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.permissions.OnPermission
import com.hjq.permissions.XXPermissions
import com.xiaojian.lkplayer.databinding.ActivityFileListBinding


class FileListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileListBinding
    private val mList: ArrayList<VideoInfo> = arrayListOf()


    private val sLocalVideoColumns = arrayOf(
        MediaStore.Video.Media._ID,  // 视频id
        MediaStore.Video.Media.DATA,  // 视频路径
        MediaStore.Video.Media.SIZE,  // 视频字节大小
        MediaStore.Video.Media.DISPLAY_NAME,  // 视频名称 xxx.mp4
        MediaStore.Video.Media.TITLE,  // 视频标题
        MediaStore.Video.Media.DATE_ADDED,  // 视频添加到MediaProvider的时间
        MediaStore.Video.Media.DATE_MODIFIED,  // 上次修改时间，该列用于内部MediaScanner扫描，外部不要修改
        MediaStore.Video.Media.MIME_TYPE,  // 视频类型 video/mp4
        MediaStore.Video.Media.DURATION,  // 视频时长
        MediaStore.Video.Media.ARTIST,  // 艺人名称
        MediaStore.Video.Media.ALBUM,  // 艺人专辑名称
        MediaStore.Video.Media.RESOLUTION,  // 视频分辨率 X x Y格式
        MediaStore.Video.Media.DESCRIPTION,  // 视频描述
        MediaStore.Video.Media.IS_PRIVATE,
        MediaStore.Video.Media.TAGS,
        MediaStore.Video.Media.CATEGORY,  // YouTube类别
        MediaStore.Video.Media.LANGUAGE,  // 视频使用语言
        MediaStore.Video.Media.LATITUDE,  // 拍下该视频时的纬度
        MediaStore.Video.Media.LONGITUDE,  // 拍下该视频时的经度
        MediaStore.Video.Media.DATE_TAKEN,
        MediaStore.Video.Media.MINI_THUMB_MAGIC,
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
    )
    private val sLocalVideoThumbnailColumns = arrayOf(
        MediaStore.Video.Thumbnails.DATA,  // 视频缩略图路径
        MediaStore.Video.Thumbnails.VIDEO_ID,  // 视频id
        MediaStore.Video.Thumbnails.KIND,
        MediaStore.Video.Thumbnails.WIDTH,  // 视频缩略图宽度
        MediaStore.Video.Thumbnails.HEIGHT // 视频缩略图高度
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_file_list)

        binding = ActivityFileListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        XXPermissions.with(this).permission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).request(object :
            OnPermission {
            override fun hasPermission(granted: MutableList<String>?, all: Boolean) {
                if (all) {
                    getFiles()
                    setAdapter()
                }

            }

            override fun noPermission(denied: MutableList<String>?, never: Boolean) {
                XXPermissions.startPermissionActivity(this@FileListActivity, denied)
            }

        })

    }

    private fun setAdapter() {
        val videoAdapter = VideoAdapter(this, mList)
        binding.rv.layoutManager=LinearLayoutManager(this)
        binding.rv.adapter=videoAdapter
    }

    private fun getFiles() {
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sLocalVideoColumns,
            null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val videoInfo = VideoInfo()
                val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                val data: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                val size: Long =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                val displayName: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                val title: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                val dateAdded: Long =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
                val dateModified: Long =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))
                val mimeType: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
                val duration: Long =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                val artist: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST))
                val album: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM))
                val resolution: String ?=
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION))
                val description: String? =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION))
                val isPrivate: Int? =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.IS_PRIVATE))
                val tags: String ?=
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TAGS))
                val category: String ?=
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.CATEGORY))

                val bucketId: String? =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                val bucketDisplayName: String ?=
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                val bookmark: Int? =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK))
                val thumbnailCursor: Cursor? = contentResolver.query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, sLocalVideoThumbnailColumns,
                    MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null
                )
                if (thumbnailCursor != null && thumbnailCursor.moveToFirst()) {
                    do {
                        val thumbnailData: String ?=
                            thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA))
                        val kind: Int =
                            thumbnailCursor.getInt(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.KIND))
                        val width: Long =
                            thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.WIDTH))
                        val height: Long =
                            thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.HEIGHT))
                        videoInfo.thumbnailData = thumbnailData
                        videoInfo.kind = kind
                        videoInfo.width = width
                        videoInfo.height = height
                    } while (thumbnailCursor.moveToNext())
                    thumbnailCursor.close()
                }
                videoInfo.id = id
                videoInfo.data = data
                videoInfo.size = size
                videoInfo.displayName = displayName
                videoInfo.title = title
                videoInfo.dateAdded = dateAdded
                videoInfo.dateModified = dateModified
                videoInfo.mimeType = mimeType
                videoInfo.duration = duration
                videoInfo.artist = artist
                videoInfo.album = album

                videoInfo.isPrivate = isPrivate
                videoInfo.tags = tags
                videoInfo.category = category

                videoInfo.bucketId = bucketId
                videoInfo.bucketDisplayName = bucketDisplayName

                mList.add(videoInfo)
            } while (cursor.moveToNext())
            cursor.close()
        }


    }
}