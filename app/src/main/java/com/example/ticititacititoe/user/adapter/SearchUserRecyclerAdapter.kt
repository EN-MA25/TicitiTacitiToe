package com.example.ticititacititoe.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.UserListItemBinding
import com.example.ticititacititoe.user.model.User
import com.example.ticititacititoe.user.ui.UserSearchUIModel

class SearchUserRecyclerAdapter(
    val onUserClick: (User) -> Unit,
    val onAddFriendClick: (User) -> Unit,
    val onDeleteFriendClick: (User) -> Unit,
    val onInitialsClick: (User) -> Unit
    ): RecyclerView.Adapter<SearchUserRecyclerAdapter.UserViewHolder>() {
    private var users: List<UserSearchUIModel> = emptyList()
    fun submitList(userList: List<UserSearchUIModel>) {
        users = userList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding = UserListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(users[position])
    }


    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: UserListItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserSearchUIModel) {
            val user = item.user

            if (item.isFriend) {
                binding.friendsImageButton.setImageResource(R.drawable.delete_friend)
                binding.friendsImageButton.setOnClickListener {
                    onDeleteFriendClick(user)
                }
            } else {
                binding.friendsImageButton.setImageResource(R.drawable.add_friend)
                binding.friendsImageButton.setOnClickListener {
                    onAddFriendClick(user)
                }
            }
            binding.usernameTextView.text = user.username
            binding.initialsTextView.text = user.username?.take(2)?.uppercase()
            binding.winLossTextView.text = user.rating.toString()
            binding.averageMovesTextView.text = "Streak: ${user.currentStreak}"
            binding.averageTimeTextView.text = "Won ${user.winRate}%"
            binding.initialsTextView.setOnClickListener { onInitialsClick(user) }
            binding.friendsImageButton
            binding.root.setOnClickListener { onUserClick(user)}


        }

    }
}