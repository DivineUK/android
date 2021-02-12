package org.divineuk.divineapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.data.Resource
import org.divineuk.divineapp.network.model.Carousel
import org.divineuk.divineapp.network.model.HomeContent
import org.divineuk.divineapp.network.model.Image
import org.divineuk.divineapp.network.model.Title
import org.divineuk.divineapp.ui.adapter.HomeAdapter
import org.divineuk.divineapp.ui.components.UiComponents

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {


    private val viewModel: HomeViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }


    private fun setupObservers() {
        viewModel.homePageContent.observe(viewLifecycleOwner, Observer {
            Log.e("Sabin", "setupObservers: ${it}")
            when (it.status) {

                Resource.Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    recycler_view.adapter = HomeAdapter(it.data!!)
                }
                Resource.Status.ERROR -> {
                    progressBar.visibility = View.GONE
                    Log.e("Sabin", "error: $it ")
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }


                Resource.Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    Log.e("Sabin", "loading: ")
                }
            }
        })
    }



}