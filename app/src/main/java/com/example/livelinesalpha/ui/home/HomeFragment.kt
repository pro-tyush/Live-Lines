package com.example.livelinesalpha.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.livelinesalpha.databinding.FragmentHomeBinding
import com.example.livelinesalpha.ui.home.adapter.CoinAdapter
import com.example.livelinesalpha.ui.home.model.Coin
import com.example.livelinesalpha.ui.home.model.CoinDetails
import com.example.livelinesalpha.ui.home.viewModel.HomeViewModel

class HomeFragment : Fragment(){

    private var _binding: FragmentHomeBinding? = null
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: CoinAdapter
    private val coinDetails = ArrayList<CoinDetails>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var errorText: TextView

    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        errorText = binding.errorText
        recyclerView = binding.coins
        adapter = CoinAdapter(this.requireContext().applicationContext, coinDetails)
        val layoutManager = GridLayoutManager(this.requireContext().applicationContext, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.getCryptoAsset()
        val dataObserver = Observer<Coin> { assets ->
            coinDetails.addAll(assets.coinData)
            adapter.notifyDataSetChanged()
            errorText.visibility = View.GONE
        }
        val errorObserver = Observer<String> {  error ->
            errorText.text = error
            errorText.visibility = View.VISIBLE
        }

        viewModel.coinsData.observe(this.viewLifecycleOwner, dataObserver)
        viewModel.coinsDataError.observe(this.viewLifecycleOwner, errorObserver)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}