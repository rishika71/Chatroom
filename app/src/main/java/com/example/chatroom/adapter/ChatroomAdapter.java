package com.example.chatroom.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatroom.R;
import com.example.chatroom.databinding.ChatroomLayoutBinding;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.Utils;

import java.util.ArrayList;

public class ChatroomAdapter extends RecyclerView.Adapter<ChatroomAdapter.UViewHolder> {

    ArrayList<Chatroom> chatrooms;

    ChatroomLayoutBinding binding;

    IChatRoomAdapter am;

    public ChatroomAdapter(ArrayList<Chatroom> chatrooms) {
        this.chatrooms = chatrooms;
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ChatroomLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        am = (IChatRoomAdapter) parent.getContext();
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        Chatroom chatroom = chatrooms.get(position);

        int num = position + 1;
        holder.binding.textView3.setText("ChatRoom " + num);
        holder.binding.textView4.setText("Created: " + Utils.getDateString(chatroom.created_at));
        holder.binding.textView5.setText("Creator: " + chatroom.created_by);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utils.DB_CHATROOM, chatroom);
                Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomsFragmentNav_to_chatroomFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.chatrooms.size();
    }

    public interface IChatRoomAdapter {

        void toggleDialog(boolean show);

    }

    public static class UViewHolder extends RecyclerView.ViewHolder {

        ChatroomLayoutBinding binding;

        public UViewHolder(@NonNull ChatroomLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}