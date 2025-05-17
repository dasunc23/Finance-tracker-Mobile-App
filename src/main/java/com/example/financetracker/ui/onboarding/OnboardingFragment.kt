package com.example.financetracker.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.financetracker.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val position = arguments?.getInt(ARG_POSITION) ?: 0
        
        when (position) {
            0 -> {
                binding.ivOnboardingImage.setImageResource(com.example.financetracker.R.drawable.ic_onboarding_track)
                binding.tvTitle.text = getString(com.example.financetracker.R.string.onboarding_title_1)
                binding.tvDescription.text = getString(com.example.financetracker.R.string.onboarding_desc_1)
            }
            1 -> {
                binding.ivOnboardingImage.setImageResource(com.example.financetracker.R.drawable.ic_onboarding_budget)
                binding.tvTitle.text = getString(com.example.financetracker.R.string.onboarding_title_2)
                binding.tvDescription.text = getString(com.example.financetracker.R.string.onboarding_desc_2)
            }
            2 -> {
                binding.ivOnboardingImage.setImageResource(com.example.financetracker.R.drawable.ic_onboarding_analyze)
                binding.tvTitle.text = getString(com.example.financetracker.R.string.onboarding_title_3)
                binding.tvDescription.text = getString(com.example.financetracker.R.string.onboarding_desc_3)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 