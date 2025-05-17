package com.example.financetracker.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentCount = 3

    override fun getItemCount(): Int = fragmentCount

    override fun createFragment(position: Int): Fragment {
        return OnboardingFragment.newInstance(position)
    }
} 