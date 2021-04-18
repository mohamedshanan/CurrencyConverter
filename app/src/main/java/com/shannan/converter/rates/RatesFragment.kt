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

package com.shannan.converter.rates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shannan.converter.EventObserver
import com.shannan.converter.data.RatesResponse
import com.shannan.converter.util.getViewModelFactory
import com.shannan.converter.util.setupRefreshLayout
import com.shannan.converter.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import com.shannan.converter.databinding.RatesFragBinding
import timber.log.Timber

/**
 * Display a grid of [RatesResponse]s. User can choose to view all, active or completed rates.
 */
class RatesFragment : Fragment() {

    private val viewModel by viewModels<RatesViewModel> { getViewModelFactory() }

    private lateinit var viewDataBinding: RatesFragBinding

    private lateinit var listAdapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = RatesFragBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupSnackbar()
        setupListAdapter()
        setupRefreshLayout(viewDataBinding.refreshLayout, viewDataBinding.ratesList)
        setupNavigation()
    }

    private fun setupNavigation() {
        viewModel.openConverterEvent.observe(viewLifecycleOwner, EventObserver {
            openConverter(it)
        })
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }


    private fun openConverter(currency: String) {
        val action = RatesFragmentDirections.actionRatesFragmentToRateConverterFragment(currency)
        findNavController().navigate(action)
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = TasksAdapter(viewModel)
            viewDataBinding.ratesList.adapter = listAdapter
        } else {
            Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }
}
