package com.example.ticititacititoe.game.recentGame

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.RecentGameListItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentGameAdapter(private var recentGames: List<RecentGame>)
    : RecyclerView.Adapter<RecentGameAdapter.RecentGameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentGameViewHolder {
        val binding = RecentGameListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecentGameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentGameViewHolder, position: Int) {
        holder.bind(recentGames[position])
    }

    override fun getItemCount(): Int = recentGames.size

    class RecentGameViewHolder(private val binding: RecentGameListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: RecentGame) {
            binding.initialsTextView.text = game.opponentUsername.take(1).uppercase()
            binding.usernameTextView.text = game.opponentUsername
            binding.resultTextView.text = game.result
            binding.gameTimeTextView.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(game.timestamp))
            binding.movesMadeTextView.text = "${game.movesMade} moves"
        }
    }
}