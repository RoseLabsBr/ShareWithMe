package edu.rosehulman.roselabs.sharewithme.Rides;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.List;

import edu.rosehulman.roselabs.sharewithme.Constants;
import edu.rosehulman.roselabs.sharewithme.Interfaces.OnListFragmentInteractionListener;
import edu.rosehulman.roselabs.sharewithme.R;
import edu.rosehulman.roselabs.sharewithme.Utils;

public class RidesAdapter extends RecyclerView.Adapter<RidesAdapter.ViewHolder> {

    private List<RidesPost> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Firebase mRefFirebasePosts;
    private ChildEventListener mChildEventListener;
    private int mToggleValue;

    public RidesAdapter(OnListFragmentInteractionListener listener) {
        mValues = new ArrayList<>();
        mListener = listener;
        mToggleValue = 2;
        mRefFirebasePosts = new Firebase(Constants.FIREBASE_URL + "/categories/Rides/posts");
        mChildEventListener = new RidesChildEventListener();
        mRefFirebasePosts.orderByChild("expirationDate").startAt(System.currentTimeMillis()).addChildEventListener(mChildEventListener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final RidesPost post = mValues.get(position);
        holder.mTitleTextView.setText(post.getTitle());
        holder.mDescriptionTextView.setText(String.format("@%s at %s", post.getUserId(),
                Utils.getStringDate(post.getPostDate())));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RidesDetailFragment(mValues.get(position));
                mListener.sendFragmentToInflate(fragment);
            }
        });

        //Este codigo abaixo funciona mas nao achei funcional pro futuro, so para apagar mais facil nos testes
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String key = post.getKey();
                //TODO verify user permission
                mRefFirebasePosts.child(key).removeValue();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setFilter(int value) {
        Query query;
        mRefFirebasePosts.removeEventListener(mChildEventListener);

        mToggleValue = value;
        query = mRefFirebasePosts.orderByChild("expirationDate").startAt(System.currentTimeMillis());

        mValues.clear();
        query.addChildEventListener(mChildEventListener);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView mTitleTextView;
        public TextView mDescriptionTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleTextView = (TextView) view.findViewById(R.id.post_title);
            mDescriptionTextView = (TextView) view.findViewById(R.id.post_description);
        }

        @Override
        public String toString() {
            return super.toString() + "TODO";
        }
    }

    private class RidesChildEventListener implements ChildEventListener {

        public void addPost(RidesPost rp){
            mValues.add(0, rp);
            notifyDataSetChanged();
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            RidesPost rp = dataSnapshot.getValue(RidesPost.class);
            rp.setKey(dataSnapshot.getKey());
            if(mToggleValue == 2){
                addPost(rp);
            }else{
                if(mToggleValue == 0){
                    if(rp.isOffer()){
                        addPost(rp);
                    }
                }else{
                    if(!rp.isOffer()){
                        addPost(rp);
                    }
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            for (int i = 0; i < mValues.size(); i++) {
                if (mValues.get(i).getKey().equals(key)) {
                    mValues.remove(i);
                    break;
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            //empty
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e("MQ", firebaseError.getMessage());
        }
    }
}
