package com.example.docmate.presentation.main.doctordetail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.docmate.presentation.main.doctordetail.tabs.AboutTabFragment
import com.example.docmate.presentation.main.doctordetail.tabs.ReviewsTabFragment
import com.example.docmate.presentation.main.doctordetail.tabs.ScheduleTabFragment


/**
 * Adapter بيربط ViewPager2 بالتابات الثلاثة (About/Schedule/Reviews)
 * في شاشة Doctor Detail. الترتيب هنا (0,1,2) لازم يتطابق مع نفس
 * ترتيب TabItem في fragment_doctor_detail.xml.
 */
class DoctorDetailPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AboutTabFragment()
            1 -> ScheduleTabFragment()
            2 -> ReviewsTabFragment()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    companion object {
        private const val TAB_COUNT = 3
    }
}
