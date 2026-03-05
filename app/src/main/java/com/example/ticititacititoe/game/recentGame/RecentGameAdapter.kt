package com.example.ticititacititoe.game.recentGame

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.RecentGameListItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentGameAdapter(
    private var recentGames: List<RecentGame>,
    private val onPlayAgainClick: (RecentGame) -> Unit,
    private val onUserClick: (RecentGame) -> Unit,
    private val onDeleteClick: (RecentGame) -> Unit)

    : RecyclerView.Adapter<RecentGameAdapter.RecentGameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentGameViewHolder {
        val binding = RecentGameListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecentGameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentGameViewHolder, position: Int) {
        holder.bind(recentGames[position], onPlayAgainClick, onUserClick, onDeleteClick)
    }

    override fun getItemCount(): Int = recentGames.size

    class RecentGameViewHolder(private val binding: RecentGameListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            game: RecentGame,
            onPlayAgainClick: (RecentGame) -> Unit,
            onUserClick: (RecentGame) -> Unit,
            onDeleteClick: (RecentGame) -> Unit
        ){
            binding.initialsTextView.text = game.opponentUsername.take(2).uppercase()
            binding.deleteButton.setOnClickListener {
                onDeleteClick(game)
            }
            binding.usernameTextView.text = game.opponentUsername
            binding.resultTextView.text = "You ${game.result} against: "
            binding.gameTimeTextView.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(game.timestamp))
            binding.movesMadeTextView.text = "in ${game.movesMade} moves"
            binding.playAgainButton.setOnClickListener {
                onPlayAgainClick(game)
            }
            binding.initialsTextView.setOnClickListener {
                onUserClick(game)
            }
        }
    }
}