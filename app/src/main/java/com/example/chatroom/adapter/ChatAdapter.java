package com.example.chatroom.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatroom.GlideApp;
import com.example.chatroom.R;
import com.example.chatroom.databinding.ChatLayoutBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.UViewHolder> {

    ArrayList<Chat> chats;

    Chatroom chatroom;

    ChatLayoutBinding binding;

    IChatAdapter am;

    FirebaseFirestore db;

    FirebaseAuth mAuth;

    FirebaseUser cur_user;

    public ChatAdapter(Chatroom chatroom, ArrayList<Chat> chats) {
        this.chatroom = chatroom;
        this.chats = chats;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cur_user = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ChatLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        am = (IChatAdapter) parent.getContext();
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        Chat chat = chats.get(position);

        holder.binding.textView7.setText(chat.getOwner());
        holder.binding.textView8.setText("> " + chat.getContent());
        holder.binding.textView10.setText(Utils.getDateString(chat.getCreated_at()));

        if (chat.getOwnerRef() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(chat.getOwnerId()).child(chat.getOwnerRef());
            GlideApp.with(holder.binding.getRoot())
                    .load(storageReference)
                    .into(holder.binding.imageView2);
        } else {
            GlideApp.with(holder.binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(holder.binding.imageView2);
        }

        holder.binding.imageView3.setVisibility(View.GONE);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        if (chat.getChatType() == Chat.CHAT_MESSAGE) {

            holder.binding.textView9.setText(chat.getLikedBy().size() + " ♥");

            holder.liked = chat.getLikedBy().contains(cur_user.getUid());
            if (holder.liked) holder.binding.imageView.setImageResource(R.drawable.like_favorite);

            if (chat.getOwnerId().equals(cur_user.getUid())) {
                holder.binding.imageView.setVisibility(View.GONE);
                holder.binding.imageView3.setVisibility(View.VISIBLE);
            }

            holder.binding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.liked) {
                        chat.unLike(cur_user.getUid());
                        HashMap<String, Object> upd = new HashMap<>();
                        upd.put("likedBy", chat.getLikedBy());
                        am.toggleDialog(true);
                        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                holder.binding.imageView.setImageResource(R.drawable.like_not_favorite);
                                holder.liked = false;
                            }
                        });
                    } else {
                        chat.addLike(cur_user.getUid());
                        HashMap<String, Object> upd = new HashMap<>();
                        upd.put("likedBy", chat.getLikedBy());
                        am.toggleDialog(true);
                        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                holder.binding.imageView.setImageResource(R.drawable.like_favorite);
                                holder.liked = true;
                            }
                        });
                    }
                }
            });

        } else if (chat.getChatType() == Chat.CHAT_LOCATION) {
            holder.binding.imageView.setVisibility(View.GONE);
            if (chat.getOwnerId().equals(cur_user.getUid())) {
                holder.binding.imageView3.setVisibility(View.VISIBLE);
            }
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] loc = chat.getContent().split("\n");
                    String url = "http://maps.google.com/maps?z=12&t=m&q=loc:" + loc[2] + "+" + loc[1];
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    holder.binding.getRoot().getContext().startActivity(i);
                }
            });
        }

        holder.binding.imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                am.toggleDialog(true);
                DocumentReference dbc = db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId());
                dbc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dbc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                if (task.isSuccessful()) {
                                    Toast.makeText(view.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    task.getException().printStackTrace();
                                }
                            }
                        });

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.chats.size();
    }

    public interface IChatAdapter {

        void toggleDialog(boolean show);

    }

    public static class UViewHolder extends RecyclerView.ViewHolder {

        ChatLayoutBinding binding;
        boolean liked = false;

        public UViewHolder(@NonNull ChatLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}
