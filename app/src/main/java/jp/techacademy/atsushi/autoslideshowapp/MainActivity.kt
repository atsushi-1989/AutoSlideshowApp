package jp.techacademy.atsushi.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.AbstractCursor
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.logging.Logger.global

class MainActivity : AppCompatActivity(){

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer : Timer? = null
    private var mHandler = Handler()

    lateinit var cursor :Cursor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo()

            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onResume() {
        super.onResume()


        //次ボタンをおしたら
        nextButton.setOnClickListener {
            if (cursor.moveToNext()) {
                showUri(cursor)

            }else{
                cursor.moveToFirst()
                showUri(cursor)

            }
        }

        //戻るボタンをおしたら
        backButton.setOnClickListener {
            if (cursor.move(-1)) {
                showUri(cursor)

            }else{
                cursor.moveToLast()
                showUri(cursor)
            }

        }

        //再生ボタンをおしたら
        playButton.setOnClickListener {
            nextButton.isEnabled = false
            backButton.isEnabled = false

            if (mTimer == null){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask(){
                    override fun run() {
                        mHandler.post {

                            if (cursor.moveToNext()){
                                showUri(cursor)
                            }else{
                                cursor.moveToFirst()
                                showUri(cursor)
                            }
                        }
                    }

                },500,500)
                playButton.text = "停止"
            }else{
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                    nextButton.isEnabled = true
                    backButton.isEnabled = true
                    playButton.text = "再生"
                }
            }

        }

    }

    override fun onPause() {
        super.onPause()
        cursor.close()
    }

    override fun onRestart() {
        super.onRestart()
        getContentsInfo()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する


        var resolver = contentResolver
         cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )!!

        if (cursor.moveToFirst()) {
            showUri(cursor)
        }

    }

    private fun showUri(cursor : Cursor){

        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
            )

        imageView.setImageURI(imageUri)
    }

}
