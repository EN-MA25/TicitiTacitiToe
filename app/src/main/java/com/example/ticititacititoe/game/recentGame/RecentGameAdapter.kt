package com.example.ticititacititoe.game.recentGame

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.RecentGameListItemBinding

class RecentGameAdapter(private var recentGame: List<RecentGame>)
    : RecyclerView.Adapter<RecentGameAdapter.RecentGameViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentGameViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(
        holder: RecentGameViewHolder,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        recentGame = 5
    }

    class RecentGameViewHolder( private val binding: RecentGameListItemBinding): RecyclerView.ViewHolder(binding.root){

    }

}