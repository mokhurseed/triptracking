package com.innov.geotracking.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.innov.geotracking.Constant
import com.innov.geotracking.R
import com.innov.geotracking.utils.AppUtils
import com.innov.geotracking.utils.ImageUtils
import com.innov.geotracking.utils.enum.MsgTypes

open class BaseActivity : AppCompatActivity(){

    var mCurrentPhotoPath: String? = ""

    //   preferenceUtils
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onResume() {
        super.onResume()
        AppUtils.INSTANCE?.setLang(this)
    }

    fun showToast(msg: String, msgType: MsgTypes, isDialog: Boolean = false, dialog: Dialog? = null) {
        AppUtils.INSTANCE?.showLongToast(this,msg)

    /*    val alerter : Alerter = if (isDialog && dialog!= null) {
            Alerter.create(dialog)
        }
        else{
            Alerter.create(this@BaseActivity)
        }
        alerter.enableSwipeToDismiss()
        when (msgType) {
            MsgTypes.SUCCESS -> {
                alerter.setTitle(getString(R.string.Success))
                    .setIcon(R.drawable.ic_check)
                    .setBackgroundColorRes(
                        R.color.jungle_green
                    )
            }
            MsgTypes.ERROR -> {
                alerter.setTitle(getString(R.string.error))
                    .setIcon(R.drawable.ic_warning)
                    .setBackgroundColorRes(
                        R.color.rejected_color
                    )
            }
            else -> {
                alerter.setTitle(getString(R.string.warning))
                    .setIcon(R.drawable.ic_warning)
                    .setBackgroundColorRes(
                        R.color.pizazz
                    )
            }
        }
        alerter.setDuration(2000)
            .setText(msg)
            .setOnClickListener {

            }
            .show()*/
    }



    fun toggleFadeView(
        parent: View,
        loader: View,
        imageView: ImageView,
        showLoader: Boolean
    ) {

        if (showLoader) {
            AppUtils.INSTANCE?.hideFadeView(parent, Constant.VIEW_ANIMATE_DURATION)
            AppUtils.INSTANCE?.showFadeView(loader, Constant.VIEW_ANIMATE_DURATION)
            ImageUtils.INSTANCE?.loadLocalGIFImage(imageView, R.drawable.loader)
            loader.visibility = VISIBLE
        } else {
            AppUtils.INSTANCE?.hideView(loader, Constant.VIEW_ANIMATE_DURATION)
            AppUtils.INSTANCE?.showView(parent, Constant.VIEW_ANIMATE_DURATION)
            loader.visibility = GONE
        }
    }

    fun noDataLayout(
        noDataLayout: View,
        recyclerView: RecyclerView,
        isShow:Boolean
    ){
        if (isShow){
            recyclerView.visibility= GONE
            noDataLayout.visibility= VISIBLE
        }else
        {
            recyclerView.visibility= VISIBLE
            noDataLayout.visibility= GONE
        }
    }

}