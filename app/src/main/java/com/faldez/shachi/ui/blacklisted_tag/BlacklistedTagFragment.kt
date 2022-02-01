package com.faldez.shachi.ui.blacklisted_tag

import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.shachi.MainActivity
import com.faldez.shachi.data.BlacklistTagRepository
import com.faldez.shachi.data.ServerRepository
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.BlacklistedTagEditDialogBinding
import com.faldez.shachi.databinding.BlacklistedTagFragmentBinding
import com.faldez.shachi.model.BlacklistedTag
import com.faldez.shachi.model.BlacklistedTagWithServer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlacklistedTagFragment : Fragment() {

    private lateinit var binding: BlacklistedTagFragmentBinding
    private lateinit var viewModel: BlacklistedTagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.build(requireContext())
        viewModel = ViewModelProvider(this,
            BlacklistedTagViewModelFactory(ServerRepository(db), BlacklistTagRepository(db))).get(
            BlacklistedTagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = BlacklistedTagFragmentBinding.inflate(inflater, container, false)
        prepareAppBar()
        binding.bind()
        return binding.root
    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.blacklistedTagToolbar)
        binding.blacklistedTagAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun BlacklistedTagFragmentBinding.bind() {
        val adapter = BlacklistedTagAdapter(
            onEdit = { position ->
                val servers =
                    viewModel.serverList.value ?: listOf()
                val item = viewModel.blacklistedTagFlow.value?.get(position)
                val selectedServer = servers.map { server ->
                    item?.servers?.indexOfFirst { it.serverId == server.serverId } != -1
                }.toMutableList()

                Log.d("Dialog", "$servers $selectedServer")
                val dialogBinding =
                    BlacklistedTagEditDialogBinding.inflate(LayoutInflater.from(requireContext()))
                dialogBinding.blacklistedTagsInput.text =
                    SpannableStringBuilder(item?.blacklistedTag?.tags)
                val serverAdapter =
                    BlacklistedTagServerAdapter(servers, selectedServer)
                dialogBinding.serverListView.adapter = serverAdapter
                dialogBinding.serverListView.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Blacklist tags")
                    .setView(dialogBinding.root)
                    .setPositiveButton("Save", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            val tags = dialogBinding.blacklistedTagsInput.text.toString()
                            val selectedServers = serverAdapter.getSelected()
                            val data =
                                item?.copy(blacklistedTag = item.blacklistedTag.copy(tags = tags),
                                    servers = selectedServers)
                            Log.d("Dialog", "$data")
                            data?.let {
                                viewModel.insertBlacklistTag(it)
                            }
                        }

                    })
                    .show()
            },
            onDelete = { position ->
                val item = viewModel.blacklistedTagFlow.value?.get(position)
                item?.blacklistedTag?.let {
                    viewModel.deleteBlacklistTag(it)
                }
            }
        )
        blacklistedTagListRecyclerview.adapter = adapter

        blacklistedTagListRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        blacklistedTagFloatingButton.setOnClickListener {
            val servers =
                viewModel.serverList.value ?: listOf()
            Log.d("Dialog", "$servers")
            val dialogBinding =
                BlacklistedTagEditDialogBinding.inflate(LayoutInflater.from(requireContext()))
            val serverAdapter =
                BlacklistedTagServerAdapter(servers, servers.map { false }.toMutableList())
            dialogBinding.serverListView.adapter = serverAdapter
            dialogBinding.serverListView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Blacklist tags")
                .setView(dialogBinding.root)
                .setPositiveButton("Save", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val tags = dialogBinding.blacklistedTagsInput.text.toString()
                        val selectedServers = serverAdapter.getSelected()
                        val data = BlacklistedTagWithServer(
                            blacklistedTag = BlacklistedTag(tags = tags),
                            servers = selectedServers
                        )
                        Log.d("Dialog", "$data")
                        viewModel.insertBlacklistTag(data)
                    }

                })
                .show()
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.blacklistedTagFlow.collectLatest { list ->
                    list?.let {
                        Log.d("Dialog", "$it")
                        adapter.setData(it)
                    }
                }
            }
        }
    }
}