/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shannan.converter.rateconverter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.shannan.converter.R
import com.shannan.converter.databinding.RateConvertFragBinding
import com.shannan.converter.util.getViewModelFactory
import com.shannan.converter.util.setupSnackbar
import java.util.*


/**
 * Main UI for the task detail screen.
 */
class RateConverterFragment : Fragment() {
    private var amountEt: EditText? = null
    private lateinit var viewDataBinding: RateConvertFragBinding

    private val args: RateConverterFragmentArgs by navArgs()

    private val viewModel by viewModels<RateConverterViewModel> { getViewModelFactory() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rate_convert_frag, container, false)
        viewDataBinding = RateConvertFragBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        initKeyBoard()
        viewModel.start(args.currency)

        return view
    }

    private fun initKeyBoard() {
        amountEt = view?.findViewById(R.id.base_et)
        amountEt?.requestFocus()
        val imgr: InputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imgr.showSoftInput(amountEt, InputMethodManager.SHOW_FORCED)
    }
}
