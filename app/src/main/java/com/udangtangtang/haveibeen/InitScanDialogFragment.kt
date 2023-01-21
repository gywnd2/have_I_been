package com.udangtangtang.haveibeen

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.naver.maps.map.a.g

class InitScanDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder=AlertDialog.Builder(it)
            val inflater= requireActivity().layoutInflater;
            builder.setView(inflater.inflate(R.layout.fragment_initscan, null))
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }
}