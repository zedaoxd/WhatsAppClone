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

public class SelectedGroupAdapter extends RecyclerView.Adapter<SelectedGroupAdapter.MyViewHolder> {

    private List<Usuario> usuarioList;
    private Context context;

    public SelectedGroupAdapter(List<Usuario> usuarioList, Context context) {
        this.usuarioList = usuarioList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_group_selected, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario user = usuarioList.get(position);
        holder.name.setText(user.getNome());

        if (user.getFoto() != null){
            Uri uri = Uri.parse(user.getFoto());
            Glide.with(context).load(uri).into(holder.photo);
        } else {
            holder.photo.setImageResource(R.drawable.padrao);
        }
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView photo;
        private TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.idPhotoMemberSelected);
            name = itemView.findViewById(R.id.idNameMemberSelected);
        }
    }
}
