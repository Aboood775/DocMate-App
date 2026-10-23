package com.example.docmate.presentation.main.notification.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docmate.R
import com.example.docmate.databinding.FragmentNotificationBinding
import com.example.docmate.presentation.main.notification.adapter.NotificationAdapter
import com.example.docmate.presentation.main.notification.viewmodel.NotificationViewModel
import com.example.docmate.util.Resource
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter
    private var badge: BadgeDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        setupBadge()
        setupSwipeRefresh()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(
            onNotificationClick = { notification ->
                if (!notification.isRead) {
                    viewModel.markNotificationAsRead(notification.notificationId)
                }
            }
        )
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.tvMarkAllRead.setOnClickListener {
            viewModel.markAllAsRead()
        }
    }

    @OptIn(ExperimentalBadgeUtils::class)
    private fun setupBadge() {
        badge = BadgeDrawable.create(requireContext()).apply {
            isVisible = false
            maxCharacterCount = 2
            badgeGravity = BadgeDrawable.TOP_END
        }
        BadgeUtils.attachBadgeDrawable(badge!!, binding.ivBellIcon)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.docmate_primary),
            ContextCompat.getColor(requireContext(), R.color.docmate_accent),
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadNotifications()
        }
    }

    private fun observeViewModel() {
        viewModel.notificationsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.rvNotifications.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    adapter.submitList(resource.data)
                }
                is Resource.Empty -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.rvNotifications.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyTitle.text = "No Notifications Yet"
                    binding.tvEmptySubtitle.text = "We\u2019ll notify you when there\u2019s an update."
                }
                is Resource.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.rvNotifications.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyTitle.text = "Something went wrong"
                    binding.tvEmptySubtitle.text = resource.exception.localizedMessage ?: "Please try again."
                }
            }
        }

        viewModel.markAllReadState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success && resource.data) {
                Toast.makeText(
                    requireContext(),
                    "All notifications marked as read",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            updateBadge(count)
        }
    }

    private fun updateBadge(count: Int) {
        val b = badge ?: return
        if (count > 0) {
            b.isVisible = true
            b.number = if (count > 99) 99 else count
        } else {
            b.isVisible = false
        }
    }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun onDestroyView() {
        super.onDestroyView()
        badge?.let { BadgeUtils.detachBadgeDrawable(it, binding.ivBellIcon) }
        badge = null
        _binding = null
    }
}
