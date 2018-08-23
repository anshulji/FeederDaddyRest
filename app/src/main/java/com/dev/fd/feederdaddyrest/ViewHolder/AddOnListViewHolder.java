package com.dev.fd.feederdaddyrest.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.fd.feederdaddyrest.Interface.ItemClickListener;
import com.dev.fd.feederdaddyrest.R;

public class AddOnListViewHolder extends RecyclerView.ViewHolder
        //implements View.OnClickListener
        {

    public ImageView imgdelete;
    public TextView txtaddonitemname,txtaddonitemprice;

    private ItemClickListener itemClickListener;


    public AddOnListViewHolder(@NonNull View itemView) {
        super(itemView);

        imgdelete = itemView.findViewById(R.id.imgdeleteaddon);
        txtaddonitemname = itemView.findViewById(R.id.txtaddonitemname);
        txtaddonitemprice = itemView.findViewById(R.id.txtaddonitemprice);

        //itemView.setOnClickListener(this);


    }

//    public void setItemClickListener(ItemClickListener itemClickListener) {
//        this.itemClickListener = itemClickListener;
//    }
//
//    @Override
//    public void onClick(View view) {
//        itemClickListener.onClick(view,getAdapterPosition(),false);
//
//    }

}
