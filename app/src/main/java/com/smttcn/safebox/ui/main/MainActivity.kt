package com.smttcn.safebox.ui.main

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.widget.Toast
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.INTERVAL_BACK_BUTTON_QUIT_IN_MS
import android.view.Menu;
import android.view.MenuItem;
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.Manager.ImageManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.getDrawableCompat
import com.smttcn.commons.extensions.getFilenameFromPath
import com.smttcn.commons.extensions.loadImage
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.database.DbItem
import com.smttcn.safebox.ui.debug.DebugconsoleActivity
import com.smttcn.safebox.ui.settings.SettingsActivity
import com.smttcn.safebox.viewmodel.DbItemViewModel
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.itemListRecyclerView
import kotlinx.android.synthetic.main.activity_main.progressBarContainer
import kotlinx.android.synthetic.main.recyclerview_item.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseActivity() {

    private lateinit var dbItemViewModel: DbItemViewModel

    var BackButtonPressedOnce = false
    var LastPressedBackTime = System.currentTimeMillis()

    companion object {
        fun isAuthenticated(): Boolean {
            return MyApplication.globalAppAuthenticated.equals("yes")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        initialize()
        initializeUI()

        showProgressBar(false)
    }

    private fun initialize() {
        MyApplication.setMainContext(this)
    }

    private fun initializeUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.itemListRecyclerView)
        val adapter = DbItemAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter.onItemClick = { view, item ->
            onRecyclerItemClicked(view, item)
        }

        dbItemViewModel = ViewModelProviders.of(this).get(DbItemViewModel::class.java)

        dbItemViewModel.allDbItems.observe(this, Observer { item ->
            // Update the cached copy of the dbItems in the adapter.
            item?.let { adapter.setDbItems(it) }
        })
    }

    override fun onBackPressed() {
        val millisecondsSinceLastPressedBack = System.currentTimeMillis() - LastPressedBackTime
        if (!BackButtonPressedOnce || millisecondsSinceLastPressedBack > INTERVAL_BACK_BUTTON_QUIT_IN_MS) {
            toast(R.string.press_back_again_to_quit, Toast.LENGTH_LONG)
            BackButtonPressedOnce = true
            LastPressedBackTime = System.currentTimeMillis()
            return
        } else {
            finishAndRemoveTask()
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id) {
            R.id.menu_share -> {
                shareItem()
                return true
            }
            R.id.menu_refresh -> {
                refreshDbItemList()
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.menu_debug_console -> {
                startActivity(Intent(this@MainActivity, DebugconsoleActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onRecyclerItemClicked(view: View, item : DbItem) {
        // Todo: to open various type of files

        // Todo: try not to cache any physical file, use inputstream instead
        val targetfile = item.fullPathWithFilename
        if (targetfile.length > 0) {
            val imagePaths = listOf(targetfile)
            StfalconImageViewer.Builder<String>(this, imagePaths, ::loadImage)
                .withTransitionFrom(view.item_background)
                .show()
        }
    }

    private fun loadImage(imageView: ImageView, imagePath: String) {
        val aniFade = AnimationUtils.loadAnimation(applicationContext, R.anim.fadein)
        imageView.startAnimation(aniFade)
        imageView.setImageDrawable(getDrawableCompat(R.drawable.ic_image_gray_24dp))
        val decryptedFileByteArray = FileManager.decryptFileContentToByteArray(File(imagePath), MyApplication.getUS())
        if (decryptedFileByteArray != null) {
            imageView.setImageBitmap(ImageManager.toBitmap(decryptedFileByteArray!!))
        }

    }

    // todo: to further define the share routine to share all type of files
    private fun shareItem() {
        val files = FileManager.getFilesInDocumentRoot()

        if (files.isEmpty()) return

        var decryptedFilepath: String = ""
        val filename = files[0].name.getFilenameFromPath()
        val filepath = FileManager.toFullPathInDocumentRoot(filename)
        val targetfile = File(filepath)

        val targetpath = FileManager.getFolderInCacheFolder("temp_file_share", true)
        if (targetfile.length() > 0 && targetpath != null) {
            decryptedFilepath = FileManager.decryptFile(targetfile, MyApplication.getUS(), targetpath.canonicalPath, false)
        }

        if (decryptedFilepath.isNotEmpty()) {
            val AUTHORITY = "com.simttcn.safebox.fileprovider"

            val file = File(decryptedFilepath)
            val contentUri = FileProvider.getUriForFile(applicationContext, AUTHORITY, file)
            // create new Intent
            val sharingIntent = Intent(Intent.ACTION_SEND)
            // set flag to give temporary permission to external app to use your FileProvider
            sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // I am opening a PDF file so I give it a valid MIME type
            sharingIntent.setDataAndType(contentUri, "image/jpeg");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            // start sharing activity sheet
            startActivity(Intent.createChooser(sharingIntent, "Share Image"));
        }

    }

    fun refreshDbItemList() {
        // Todo: how to do manual refresh of database?
        //AppDatabase.refresh()
        val dt: String = SimpleDateFormat("hhmmss_SSS").format(Date())
        val dbItem = DbItem(
            fileName = "filename_" + dt,
            hashedFileName = "hashed_" + dt,
            isFolder = true,
            fullPathWithFilename = "",
            salt = "",
            size = 0,
            thumbnail = null
        )

        dbItemViewModel.insert(dbItem)
    }

    fun showProgressBar(show: Boolean) {
        if (show) {
            progressBarContainer.visibility = View.VISIBLE
            itemListRecyclerView.visibility = View.GONE
        } else {
            progressBarContainer.visibility = View.GONE
            itemListRecyclerView.visibility = View.VISIBLE
        }
    }


}
