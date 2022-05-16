package com.example.newbluetoothbus;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.craftman.cardform.Card;
import com.craftman.cardform.CardForm;
import com.craftman.cardform.OnPayBtnClickListner;

public class CardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // on below line we are creating a variable
        // for our pay view and initializing it.
        CardForm cardForm = findViewById(R.id.cardform);
        TextView txtDes = findViewById(com.craftman.cardform.R.id.payment_amount);
        Button btnPay = findViewById(com.craftman.cardform.R.id.btn_pay);
        //TextView cardName = findViewById(com.craftman.cardform.R.id.card_name);
        TextView txt = findViewById(com.craftman.cardform.R.id.payment_amount_holder);
        txt.setText("Добавление карты");
        //cardName.setText("Имя, фамилия");
        txtDes.setText("");
        btnPay.setText("Подтвердить");

        cardForm.setPayBtnClickListner(new OnPayBtnClickListner() {
            @Override
            public void onClick(Card card) {
                Toast.makeText(CardActivity.this, "Name : " + card.getName()+" | Last 4 digits: " + card.getLast4(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
