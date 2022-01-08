package com.example.whatszapclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatszapclone.R;
import com.example.whatszapclone.model.Message;
import com.example.whatszapclone.utils.Base64Custom;
import com.example.whatszapclone.utils.UserFirebase;

import org.w3c.dom.Text;

import java.util.List;

public class AdapterMessages extends RecyclerView.Adapter<AdapterMessages.MyViewHolder> {

    private List<Message> messageList;
    private Context context;

    private static final int TYPE_SENDER = 0;
    private static final int TYPE_RECIPIENT = 1;

    public AdapterMessages(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_SENDER){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_message_sender, parent, false);
        } else if (viewType == TYPE_RECIPIENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_message_recipient, parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        String msg = message.getMessage();
        String image = message.getImage();
        if (image != null){

            Uri url = Uri.parse(image);
            Glide.with(context).load(url).into(holder.image);
            String name = message.getName();
            if (!name.isEmpty()){
                holder.name.setText(name);
            } else {
                holder.name.setVisibility(View.GONE);
            }
            holder.message.setVisibility(View.GONE);

        } else {

            String name = message.getName();
            if (!name.isEmpty()){
                holder.name.setText(name);
            } else {
                holder.name.setVisibility(View.GONE);
            }
            holder.message.setText(msg);
            holder.image.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        String idUser = UserFirebase.getUserIdentifier();
        return idUser.equals(message.getUserId()) ? TYPE_SENDER : TYPE_RECIPIENT;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView message, name;
        ImageView image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.textViewMessage);
            image = itemView.findViewById(R.id.imageViewMessage);
            name = itemView.findViewById(R.id.textNameExibition);
        }
    }
}
