package com.example.ticititacititoe.leaderboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.RankingListItemBinding
import com.example.ticititacititoe.profile.User

class LeaderboardAdapter(
    private var users: List<User>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(
        private val binding: RankingListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {

            binding.initialsTextView.text = user.username?.take(2)?.uppercase()
            binding.usernameTextView.text = "${user.username} ${user.rating}"
            binding.winLossTextView.text = "${user.wonGames}/${user.totalGames}"
            binding.averageTimeTextView.text = "Avg: ${user.winRate}"
            binding.averageMovesTextView.text = "Moves: ${user.totalMovesMade}"
            binding.rankingTextView.text = "#${bindingAdapterPosition+1}"

            // Set colors for top 3 ranks
            when (bindingAdapterPosition) {
                0 -> binding.rankingTextView.setTextColor(Color.parseColor("#FFD700")) // Gold
                1 -> binding.rankingTextView.setTextColor(Color.parseColor("#C0C0C0")) // Silver
                2 -> binding.rankingTextView.setTextColor(Color.parseColor("#CD7F32")) // Bronze
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
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateEntries(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}