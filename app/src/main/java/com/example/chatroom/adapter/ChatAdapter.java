package com.example.chatroom.adapter;

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
import com.google.firebase.firestore.DocumentSnapshot;
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

    public ChatAdapter(Chatroom chatroom, ArrayList<Chat> chats) {
        this.chatroom = chatroom;
        this.chats = chats;
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

        holder.binding.textView8.setText("> " + chat.getContent());
        holder.binding.textView10.setText(Utils.getDateString(chat.getCreated_at()));
        holder.binding.textView9.setText(chat.getLikedBy().size() + " ♥");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser cur_user = mAuth.getCurrentUser();

        holder.liked = chat.getLikedBy().contains(cur_user.getUid());
        if (holder.liked) {
            holder.binding.imageView.setImageResource(R.drawable.like_favorite);
        }

        holder.binding.imageView3.setVisibility(View.GONE);
        if (chat.owner.equals(cur_user.getUid())) {
            holder.binding.imageView.setVisibility(View.GONE);
            holder.binding.imageView3.setVisibility(View.VISIBLE);
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

        db.collection(Utils.DB_PROFILE).document(chat.getOwner()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.get("photoref") != null) {
                        String photoRef = (String) documentSnapshot.get("photoref");
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(chat.getOwner()).child(photoRef);
                        GlideApp.with(holder.binding.getRoot())
                                .load(storageReference)
                                .into(holder.binding.imageView2);
                    } else {
                        GlideApp.with(holder.binding.getRoot())
                                .load(R.drawable.profile_image)
                                .into(holder.binding.imageView2);
                    }
                    holder.binding.textView7.setText(documentSnapshot.get("firstname") + " " + documentSnapshot.get("lastname"));
                } else {
                    task.getException().printStackTrace();
                }
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
