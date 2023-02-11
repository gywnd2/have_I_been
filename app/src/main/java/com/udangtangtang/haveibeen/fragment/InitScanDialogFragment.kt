package com.udangtangtang.haveibeen.fragment

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PixelFormat.OPAQUE
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.udangtangtang.haveibeen.R

class InitScanDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder=AlertDialog.Builder(it)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val inflater= requireActivity().layoutInflater
            builder.setView(inflater.inflate(R.layout.fragment_initscan, null))
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    @SuppressLint("ResourceAsColor")
    override fun onResume() {
        dialog?.window?.setDimAmount(0.0f)
        val windowManager=context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val deviceSize= Point()
        display.getSize(deviceSize)

        val params=dialog?.window?.attributes
        params?.width=deviceSize.x
        params?.horizontalMargin=0.0f
        params?.y= -Integer.parseInt(androidx.appcompat.R.attr.actionBarSize.toString())+ dialog?.window?.decorView!!.height/2
        params?.windowAnimations=R.style.DialogAnimation
        dialog?.window?.attributes=params
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        super.onResume()
    }
}