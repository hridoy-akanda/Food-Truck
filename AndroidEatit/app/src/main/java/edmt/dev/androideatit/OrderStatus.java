package edmt.dev.androideatit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edmt.dev.androideatit.Common.Common;
import edmt.dev.androideatit.Database.Database;
import edmt.dev.androideatit.Model.Request;
import edmt.dev.androideatit.ViewHolder.OrderViewHolder;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,OrderViewHolder>adapter;

    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //if we start OrderStatus activity from home activity
        //we will not put any extra, so just loadOrder by phone from cammon
        loadOrders(Common.currentUser.getPhone());
//        if(getIntent() == null)
//
//        else
//            loadOrders(getIntent().getStringExtra("userPhone"));
    }

    private void loadOrders(String phone) {
        Log.e("Bal",phone+" "+Common.currentUser.toString());
        adapter= new FirebaseRecyclerAdapter<Request , OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("phone")
                .equalTo(phone)



        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {
               viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
               viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
               viewHolder.txtOrderAddress.setText(model.getAddress());
               viewHolder.txtOrderPhone.setText(model.getPhone());

            }
        };
        recyclerView.setAdapter(adapter);
    }


}
