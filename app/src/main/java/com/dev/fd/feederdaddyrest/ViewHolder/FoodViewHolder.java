package com.dev.fd.feederdaddyrest.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.dev.fd.feederdaddyrest.Common.Common;
import com.dev.fd.feederdaddyrest.Interface.ItemClickListener;
import com.dev.fd.feederdaddyrest.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener {


    public TextView food_name,food_price,txtrating,txttotalrates;
    public ImageView food_image,imgveg,imgnonveg;
    public RatingBar ratingBar;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        food_name = itemView.findViewById(R.id.food_name);
        food_price = itemView.findViewById(R.id.food_price);
        food_image = itemView.findViewById(R.id.food_image);
        txtrating = itemView.findViewById(R.id.txtrating);
        txttotalrates = itemView.findViewById(R.id.txttotalrates);
        imgveg = itemView.findViewById(R.id.imgveg);
        imgnonveg = itemView.findViewById(R.id.imgnonveg);
        ratingBar =itemView.findViewById(R.id.ratingbar);

        itemView.setOnCreateContextMenuListener(this);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action !");
        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);
        contextMenu.add(0,2,getAdapterPosition(), Common.SETASBANNER);
        contextMenu.add(0,3,getAdapterPosition(), Common.HIDEORSHOW);


    }
}
