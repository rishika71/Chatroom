package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.ChatAdapter;
import com.example.chatroom.databinding.FragmentChatroomBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;


public class ChatroomFragment extends Fragment {

    FragmentChatroomBinding binding;

    IChat am;

    FirebaseAuth mAuth;

    FirebaseUser cur_user;

    FirebaseFirestore db;

    Chatroom chatroom;

    @Override
    public void onStop() {
        chatroom.removeViewer(cur_user.getUid());
        HashMap<String, Object> upd = new HashMap<>();
        upd.put("viewers", chatroom.getViewers());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        super.onStop();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IChat) {
            am = (IChat) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatroom = (Chatroom) getArguments().getSerializable(Utils.DB_CHATROOM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Chatroom " + chatroom.getNumber());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cur_user = mAuth.getCurrentUser();

        chatroom.addViewer(cur_user.getUid());
        HashMap<String, Object> upd = new HashMap<>();
        upd.put("viewers", chatroom.getViewers());
        am.toggleDialog(true);
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

        setViewerNames(chatroom.getViewers());

        binding = FragmentChatroomBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.chatsView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.chatsView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.chatsView.getContext(),
                llm.getOrientation());
        binding.chatsView.addItemDecoration(dividerItemDecoration);

        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).orderBy("created_at", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                ArrayList<Chat> chats = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Chat chat = doc.toObject(Chat.class);
                    chat.setId(doc.getId());
                    chats.add(chat);
                }
                binding.chatsView.setAdapter(new ChatAdapter(chatroom, chats));
            }
        });

        binding.floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.editTextTextPersonName.getText().toString();
                if (msg.isEmpty()) {
                    Toast.makeText(getContext(), "Send a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<String, Object> chat = new HashMap<>();
                chat.put("created_at", FieldValue.serverTimestamp());
                chat.put("content", msg);
                chat.put("owner", cur_user.getUid());
                chat.put("likedBy", new ArrayList<>());
                db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                            binding.editTextTextPersonName.setText("");
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });

            }
        });

        return view;
    }

    public void setViewerNames(ArrayList users) {
        users.remove(cur_user.getUid());
        db.collection(Utils.DB_PROFILE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String user_str = "Viewers - ";
                    for (DocumentSnapshot snapshot : task.getResult()) {
                        if (users.contains(snapshot.getId())) {
                            user_str += snapshot.get("firstname") + " " + snapshot.get("lastname") + ", ";
                        }
                    }
                    binding.textView6.setText(user_str.substring(0, user_str.length() - 2));
                    am.toggleDialog(false);
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    interface IChat {

        void toggleDialog(boolean show);

    }
}