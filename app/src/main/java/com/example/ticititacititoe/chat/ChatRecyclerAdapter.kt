package com.example.ticititacititoe.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.util.Util
import com.example.ticititacititoe.chat.model.Message
import com.example.ticititacititoe.databinding.MessageRecievedItemBinding
import com.example.ticititacititoe.databinding.MessageSentItemBinding

class ChatRecyclerAdapter(private val currentUserId: String,
                          private val opponentName: String
): ListAdapter<Message,RecyclerView.ViewHolder>(Diff()) {
   private companion object {
        private const val SENT = 1
        private const val RECEIVED = 2
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == SENT) {
            val binding = MessageSentItemBinding.inflate(inflater, parent, false)
            SentViewHolder(binding)
        } else {
            val binding = MessageRecievedItemBinding.inflate(inflater, parent, false)
            RecievedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = getItem(position)
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is RecievedViewHolder -> holder.bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)

        return if (message.senderId == currentUserId) {
            SENT
        } else {
            RECEIVED
        }
    }


    inner class SentViewHolder(private val binding: MessageSentItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.rightChatTextView.text = message.message
            binding.timestampRightChat.text =  Util.formatTime(message.createdAt)
        }
    }

    inner class RecievedViewHolder(private val binding: MessageRecievedItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.leftChatTextView.text = message.message
            binding.timestampLeftChat.text = Util.formatTime(message.createdAt)
            binding.usernameLeftChat.text = opponentName
        }
    }

    class Diff: DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(
            a: Message,
            b: Message
        ) = a.id == b.id

        override fun areContentsTheSame(
            a: Message,
            b: Message
        ) = a == b

    }
}

