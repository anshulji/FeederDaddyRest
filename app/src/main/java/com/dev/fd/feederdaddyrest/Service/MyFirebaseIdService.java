package com.dev.fd.feederdaddyrest.Service;

import android.content.Context;
import android.content.SharedPreferences;

import com.dev.fd.feederdaddyrest.model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
         updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone","N/A");
        String city = sharedPreferences.getString("city","N/A");

        Token token = new Token(tokenRefreshed,city+"0");

        tokens.child(phone).setValue(token);
    }
}
