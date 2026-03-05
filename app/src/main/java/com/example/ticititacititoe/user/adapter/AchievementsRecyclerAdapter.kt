package com.example.ticititacititoe.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.achievements.Achievement
import com.example.ticititacititoe.achievements.UserAchievement
import com.example.ticititacititoe.databinding.AchievementHeaderBinding
import com.example.ticititacititoe.databinding.AchievementListItemBinding
import com.example.ticititacititoe.util.Util


class AchievementsRecyclerAdapter(
    unlocked: List<UserAchievement>,
    locked: List<Achievement>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_UNLOCKED = 1
        private const val TYPE_LOCKED = 2
    }

    private val items: List<Any>

    init {
        val list = mutableListOf<Any>()

        if (unlocked.isNotEmpty()) {
            list.add("Unlocked Achievements")
            list.addAll(unlocked)
        }

        if (locked.isNotEmpty()) {
            list.add("Locked Achievements")
            list.addAll(locked)
        }

        items = list
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> TYPE_HEADER
            is UserAchievement -> TYPE_UNLOCKED
            is Achievement -> TYPE_LOCKED
            else -> throw IllegalStateException("Unknown item type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_HEADER -> {
                val binding = AchievementHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            TYPE_UNLOCKED -> {
                val binding = AchievementListItemBinding.inflate(inflater, parent, false)
                UnlockedAchievementViewHolder(binding)
            }

            TYPE_LOCKED -> {
                val binding = AchievementListItemBinding.inflate(inflater, parent, false)
                LockedAchievementViewHolder(binding)
            }

            else -> throw IllegalStateException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is HeaderViewHolder -> holder.bind(items[position] as String)

            is UnlockedAchievementViewHolder ->
                holder.bind(items[position] as UserAchievement)

            is LockedAchievementViewHolder ->
                holder.bind(items[position] as Achievement)
        }
    }

    class HeaderViewHolder(
        private val binding: AchievementHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String) {
            binding.headerTitleTextView.text = title
        }
    }

    inner class UnlockedAchievementViewHolder(
        private val binding: AchievementListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserAchievement) {

            binding.achievementTitleTextView.text =
                item.achievement.title

            binding.achievementDescriptionTextView.text =
                item.achievement.description

            binding.achievementUnlockedTextView.text =
                Util.formatDate(item.unlockedAt, "d MMMM yyyy")

        }
    }

    inner class LockedAchievementViewHolder(
        private val binding: AchievementListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Achievement) {

            binding.achievementTitleTextView.text =
                item.title

            binding.achievementDescriptionTextView.text =
                item.description

            binding.achievementUnlockedTextView.text =
                "Locked"
        }
    }
}