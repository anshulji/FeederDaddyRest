package com.dev.fd.feederdaddyrest.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dev.fd.feederdaddyrest.Interface.ItemClickListener;
import com.dev.fd.feederdaddyrest.R;

public class OrderViewHolder extends RecyclerView.ViewHolder
        //implements View.OnClickListener
         {

    public ImageView imgorderstatus,imgcalldeliveryboy;
    public TextView txtorderid,txtordertime,txttotalamount,txtviewbill,txtdeliveryboyname,txtotp,txtordereceivetime;
    public Button btnaccept,btncancel;
    public RelativeLayout rlort;


    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);


        imgorderstatus = itemView.findViewById(R.id.imgorderstatus);
        imgcalldeliveryboy = itemView.findViewById(R.id.imgcalldeliveryboy);

        txtorderid = itemView.findViewById(R.id.txtorderid);
        txtordertime = itemView.findViewById(R.id.txtordertime);
        txtdeliveryboyname = itemView.findViewById(R.id.txtdeliveryboyname);
        txtotp = itemView.findViewById(R.id.txtdeliveryboyotp);

        txttotalamount = itemView.findViewById(R.id.txttotalamount);
        txtviewbill = itemView.findViewById(R.id.txtviewbill);

        btnaccept= itemView.findViewById(R.id.btnaccept);
        btncancel = itemView.findViewById(R.id.btncancel);

        rlort = itemView.findViewById(R.id.rlort);
        txtordereceivetime = itemView.findViewById(R.id.txtorderreceivetime);
        




        //itemView.setOnClickListener(this);


    }

    /*public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }*/
}
