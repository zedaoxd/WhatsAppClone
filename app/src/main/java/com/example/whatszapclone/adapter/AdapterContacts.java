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
import com.example.whatszapclone.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterContacts extends RecyclerView.Adapter<AdapterContacts.MyViewHolder> {

    private List<Usuario> listContacts;
    private Context context;

    public AdapterContacts(List<Usuario> listContacts, Context context){
        this.listContacts = listContacts;
        this.context = context;
    }

    public List<Usuario> getListContacts(){
        return this.listContacts;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contacts, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario user = listContacts.get(position);
        holder.name.setText(user.getNome());
        holder.email.setText(user.getEmail());
        if (user.getFoto() != null){
            Uri uri = Uri.parse(user.getFoto());
            Glide.with(context)
                    .load(uri)
                    .into(holder.photo);
        } else {
            if (user.getEmail().isEmpty()){
                holder.photo.setImageResource(R.drawable.icone_grupo);
                holder.email.setVisibility(View.GONE);
            } else {
                holder.photo.setImageResource(R.drawable.padrao);
            }
        }
    }

    @Override
    public int getItemCount() {
        return listContacts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView photo;
        private TextView name, email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.imageViewPhotoConversation);
            name = itemView.findViewById(R.id.textViewNameConversation);
            email = itemView.findViewById(R.id.textViewLastMessageConversation);
        }
    }
}
