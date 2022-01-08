package com.example.whatszapclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatszapclone.R;
import com.example.whatszapclone.model.Conversa;
import com.example.whatszapclone.model.Group;
import com.example.whatszapclone.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterConversations extends RecyclerView.Adapter<AdapterConversations.MyViewHolder> {

    private List<Conversa> conversaList;
    private Context context;

    public AdapterConversations(List<Conversa> conversaList, Context context) {
        this.conversaList = conversaList;
        this.context = context;
    }

    public List<Conversa> getConversas(){
        return this.conversaList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_conversations, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversa conversa = conversaList.get(position);

        if (conversa.getIsGroup().equals("true")){
            Group group = conversa.getGroup();
            holder.name.setText(group.getName());
            holder.lastMessage.setText(conversa.getLastMessage() != null ? conversa.getLastMessage() : "");

            if (group.getPhoto() != null) {
                Uri url = Uri.parse(group.getPhoto());
                Glide.with(context)
                        .load(url)
                        .into(holder.photo);
            } else {
                holder.photo.setImageResource(R.drawable.padrao);
            }
        } else {
            Usuario user = conversa.getUserExibition();
            if (user != null) {
                holder.name.setText(user.getNome());
                holder.lastMessage.setText(conversa.getLastMessage() != null ? conversa.getLastMessage() : "");

                if (user.getFoto() != null) {
                    Uri url = Uri.parse(user.getFoto());
                    Glide.with(context)
                            .load(url)
                            .into(holder.photo);
                } else {
                    holder.photo.setImageResource(R.drawable.padrao);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversaList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView photo;
        private TextView name, lastMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.imageViewPhotoConversation);
            name = itemView.findViewById(R.id.textViewNameConversation);
            lastMessage = itemView.findViewById(R.id.textViewLastMessageConversation);
        }
    }
}
