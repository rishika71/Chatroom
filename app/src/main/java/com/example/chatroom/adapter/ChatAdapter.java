package com.example.chatroom.adapter;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatroom.GlideApp;
import com.example.chatroom.R;
import com.example.chatroom.databinding.ChatLayoutBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    User user;

    public ChatAdapter(Chatroom chatroom, ArrayList<Chat> chats) {
        this.chatroom = chatroom;
        this.chats = chats;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ChatLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        am = (IChatAdapter) parent.getContext();
        user = am.getUser();
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        Chat chat = chats.get(position);

        binding = holder.binding;

        if (chat.getOwnerId().equals(user.getId())) {
            binding.chatlayout.setBackgroundResource(R.drawable.outgoing_bubble);
        }

        binding.textView7.setText(chat.getOwnerName());
        binding.textView17.setText(Utils.getDateString(chat.getCreated_at()));

        if (chat.getOwnerRef() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(chat.getOwnerId()).child(chat.getOwnerRef());
            GlideApp.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imageView2);
        } else {
            GlideApp.with(binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(binding.imageView2);
        }

        binding.imageView3.setVisibility(View.GONE);

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        if (chat.getChatType() == Chat.CHAT_MESSAGE) {

            binding.textView8.setText(chat.getContent());

            binding.textView9.setText(chat.getLikedBy().size() + " ♥");

            holder.liked = chat.getLikedBy().contains(user.getId());
            if (holder.liked) binding.imageView.setImageResource(R.drawable.like_favorite);

            if (chat.getOwnerId().equals(user.getId())) {
                binding.imageView.setVisibility(View.GONE);
                binding.imageView3.setVisibility(View.VISIBLE);
            }

            binding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.liked) {
                        chat.unLike(user.getId());
                        HashMap<String, Object> upd = new HashMap<>();
                        upd.put("likedBy", chat.getLikedBy());
                        am.toggleDialog(true);
                        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                binding.imageView.setImageResource(R.drawable.like_not_favorite);
                                holder.liked = false;
                            }
                        });
                    } else {
                        chat.addLike(user.getId());
                        HashMap<String, Object> upd = new HashMap<>();
                        upd.put("likedBy", chat.getLikedBy());
                        am.toggleDialog(true);
                        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                binding.imageView.setImageResource(R.drawable.like_favorite);
                                holder.liked = true;
                            }
                        });
                    }
                }
            });

        } else if (chat.getChatType() == Chat.CHAT_LOCATION) {
            binding.imageView.setVisibility(View.GONE);
            if (chat.getOwnerId().equals(user.getId())) {
                binding.imageView3.setVisibility(View.VISIBLE);
            }
            binding.textView8.setText("Sent their Location!\nTap for more info");
            binding.textView8.setTypeface(null, Typeface.ITALIC);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] loc = chat.getContent().split("\n");
                    String url = "http://maps.google.com/maps?z=12&t=m&q=loc:" + loc[0] + "+" + loc[1];
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    binding.getRoot().getContext().startActivity(i);
                }
            });
        } else if (chat.getChatType() == Chat.CHAT_RIDE_REQUEST) {
            binding.imageView.setVisibility(View.GONE);
            String[] loc = chat.getContent().split("\n");
            if (!chat.getOwnerId().equals(user.getId())) {
                binding.textView8.setText("Requested a Ride!\nTap for more info");
                binding.textView8.setTypeface(null, Typeface.ITALIC);
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Utils.DB_CHAT, chat);
                        Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomFragment_to_rideDetailsFragment, bundle);
                    }
                });
            } else {
                binding.imageView3.setVisibility(View.VISIBLE);
                binding.textView8.setText("You requested a ride in this chatroom!");
                binding.textView8.setTypeface(null, Typeface.ITALIC);
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "http://maps.google.com?z=12&saddr=" + loc[0] + "," + loc[1] + "&daddr=" + loc[2] + "," + loc[3];
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        binding.getRoot().getContext().startActivity(i);
                    }
                });
            }
        } else if (chat.getChatType() == Chat.CHAT_RIDE_OFFER) {
            binding.imageView.setVisibility(View.GONE);
            String[] names = chat.getContent().split("\n");
            if (names[0].equals(user.getId())) {
                binding.textView8.setText("You received a ride offer from " + names[1] + "!\nTap for more info");
                binding.textView8.setTypeface(null, Typeface.ITALIC);
                if (user.getRideReq() != null)
                    user.getRideReq().addOffer(chat.getId());
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Utils.DB_CHAT, chat);
                        Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomFragment_to_rideOfferDetailFragment, bundle);
                    }
                });
            } else if (chat.getOwnerId().equals(user.getId())) {
                binding.imageView3.setVisibility(View.VISIBLE);
                binding.textView8.setText("You sent a ride offer to " + names[2] + "!");
                binding.textView8.setTypeface(null, Typeface.ITALIC);
            } else {
                binding.getRoot().setVisibility(View.GONE);
            }
        } else if (chat.getChatType() == Chat.CHAT_RIDE_STARTED) {
            binding.imageView.setVisibility(View.GONE);
            String[] names = chat.getContent().split("\n");
            if (names[1].equals(user.getId())) {
                user.addRide(names[0]);
                binding.textView8.setText("Your ride with " + names[3] + " has started!\nTap for more info");
                binding.textView8.setTypeface(null, Typeface.ITALIC);

            } else if (chat.getOwnerId().equals(user.getId())) {
                binding.imageView3.setVisibility(View.VISIBLE);
                binding.textView8.setText("Your drive with " + names[2] + " has started!\nTap for more info");
                binding.textView8.setTypeface(null, Typeface.ITALIC);
            } else {
                binding.getRoot().setVisibility(View.GONE);
            }
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomFragment_to_tripListFragment);
                }
            });
        }

        binding.imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                am.toggleDialog(true);
                db.collection(Utils.DB_RIDE_REQ).document(chat.getId()).delete();
                db.collection(Utils.DB_RIDE_OFFER).document(chat.getId()).delete();
                DocumentReference dbc = db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId());
                dbc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        am.toggleDialog(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(view.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            task.getException().printStackTrace();
                        }
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

        User getUser();

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
