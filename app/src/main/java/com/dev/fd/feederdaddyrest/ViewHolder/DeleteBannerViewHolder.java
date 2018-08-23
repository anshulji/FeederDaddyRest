package com.dev.fd.feederdaddyrest.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.fd.feederdaddyrest.Interface.ItemClickListener;
import com.dev.fd.feederdaddyrest.R;

public class DeleteBannerViewHolder extends RecyclerView.ViewHolder
        //implements View.OnClickListener
        {

    public ImageView imgdeletebanner,imgbanner;
    public TextView txtbannername;

    private ItemClickListener itemClickListener;


    public DeleteBannerViewHolder(@NonNull View itemView) {
        super(itemView);

        imgdeletebanner = itemView.findViewById(R.id.imgdeletebanner);
        imgbanner = itemView.findViewById(R.id.imgbanner);
        txtbannername = itemView.findViewById(R.id.txtbanner);

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
