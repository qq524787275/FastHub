package com.fastaccess.github.ui.modules.profile.repos

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.fastaccess.github.R
import com.fastaccess.github.base.BasePagerFragment
import com.fastaccess.github.base.BaseViewModel
import com.fastaccess.github.ui.adapter.ProfileReposAdapter
import com.fastaccess.github.ui.adapter.base.CurrentState
import com.fastaccess.github.ui.modules.profile.repos.viewmodel.ProfileReposViewModel
import com.fastaccess.github.utils.EXTRA
import com.fastaccess.github.utils.extensions.addDivider
import com.fastaccess.github.utils.extensions.observeNotNull
import kotlinx.android.synthetic.main.empty_state_layout.*
import kotlinx.android.synthetic.main.simple_refresh_list_layout.*
import javax.inject.Inject

/**
 * Created by Kosh on 06.10.18.
 */
class ProfileReposFragment : BasePagerFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(ProfileReposViewModel::class.java) }
    private val loginBundle: String by lazy { arguments?.getString(EXTRA) ?: "" }
    private val adapter by lazy { ProfileReposAdapter() }

    override fun viewModel(): BaseViewModel? = viewModel
    override fun layoutRes(): Int = R.layout.simple_refresh_list_layout

    override fun onFragmentCreatedWithUser(view: View, savedInstanceState: Bundle?) {
        recyclerView.adapter = adapter
        recyclerView.addDivider()
        recyclerView.setEmptyView(emptyLayout)
        fastScroller.attachRecyclerView(recyclerView)
        if (savedInstanceState == null) viewModel.loadRepos(loginBundle, true)
        listenToChanges()
        swipeRefresh.setOnRefreshListener { viewModel.loadRepos(loginBundle, true) }
    }

    private fun listenToChanges() {
        viewModel.loadMoreLiveData.observeNotNull(this) {
            viewModel.loadRepos(loginBundle)
        }

        viewModel.progress.observeNotNull(this) {
            adapter.currentState = if (it) CurrentState.LOADING else CurrentState.DONE
        }

        viewModel.repos.observeNotNull(this) {
            adapter.currentState = CurrentState.DONE
            adapter.submitList(it)
        }
    }

    override fun onPageSelected(page: Int) = recyclerView.scrollToPosition(0)

    companion object {
        fun newInstance(login: String) = ProfileReposFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA, login)
            }
        }
    }
}