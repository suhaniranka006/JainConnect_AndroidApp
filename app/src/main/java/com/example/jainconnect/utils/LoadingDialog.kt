package com.example.jainconnect.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.mycompany.jainconnect.R

class LoadingDialog(context: Context) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        setContentView(R.layout.dialog_loading)
        
        // Add rotation animation to the loading icon
        val loadingIcon = findViewById<ImageView>(R.id.loading_icon)
        val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
        loadingIcon.startAnimation(rotateAnimation)
    }
}
