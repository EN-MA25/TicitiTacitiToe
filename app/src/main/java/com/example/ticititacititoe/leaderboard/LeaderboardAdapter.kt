package com.example.ticititacititoe.leaderboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.RankingListItemBinding

class LeaderboardAdapter(
    private var entries: List<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(
        private val binding: RankingListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            val user = entry.user


            binding.initialsTextView.text = user.username.take(2).uppercase()

            binding.usernameTextView.text = "${user.username} #${entry.rank}"

            binding.winLossTextView.text = "${user.wonGames}/${user.lostGames}"

            binding.averageTimeTextView.text = "Avg: --s"

            binding.averageMovesTextView.text = "Moves: --"

            binding.rankingTextView.text = "#${entry.rank}"

            // Set colors for top 3 ranks
            when (entry.rank) {
                1 -> binding.rankingTextView.setTextColor(Color.parseColor("#FFD700")) // Gold
                2 -> binding.rankingTextView.setTextColor(Color.parseColor("#C0C0C0")) // Silver
                3 -> binding.rankingTextView.setTextColor(Color.parseColor("#CD7F32")) // Bronze
                else -> binding.rankingTextView.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = RankingListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    fun updateEntries(newEntries: List<LeaderboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}