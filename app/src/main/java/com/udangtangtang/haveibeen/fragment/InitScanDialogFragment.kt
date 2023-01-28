package com.udangtangtang.haveibeen.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.udangtangtang.haveibeen.R

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