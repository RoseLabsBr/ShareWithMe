package edu.rosehulman.roselabs.sharewithme.Drafts;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import edu.rosehulman.roselabs.sharewithme.Constants;
import edu.rosehulman.roselabs.sharewithme.Interfaces.OnListFragmentInteractionListener;
import edu.rosehulman.roselabs.sharewithme.R;
import edu.rosehulman.roselabs.sharewithme.Rides.CreateRidesPostDialog;
import edu.rosehulman.roselabs.sharewithme.Rides.RidesDetailFragment;
import edu.rosehulman.roselabs.sharewithme.Rides.RidesPost;
import edu.rosehulman.roselabs.sharewithme.Utils;

/**
 * Created by Thais Faria on 1/29/2016.
 */
public class DraftsRidesAdapter extends RecyclerView.Adapter<DraftsRidesAdapter.ViewHolder>{

    private List<RidesPost> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Firebase mRefFirebasePosts;
    private Firebase mRefFirebaseDrafts;
    private ChildEventListener mChildEventListener;

    public DraftsRidesAdapter(OnListFragmentInteractionListener listener){
        Log.d("THAIS", "Construtor do DraftsRidesAdapter");
        mValues = new ArrayList<>();
        mListener = listener;
        mRefFirebasePosts = new Firebase(Constants.FIREBASE_URL + "/categories/Rides/posts");
        mRefFirebaseDrafts = new Firebase(Constants.FIREBASE_DRAFTS_URL + "/categories/Rides/posts");
        mChildEventListener = new RidesChildEventListener();
        mRefFirebaseDrafts.addChildEventListener(mChildEventListener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Log.d("THAIS", "Deu log no BindViewHolder do DraftsRides");

        final RidesPost post = mValues.get(position);
        holder.mTitleTextView.setText(post.getTitle());
        holder.mDescriptionTextView.setText(String.format("@%s at %s", post.getUserId(), Utils.getStringDate(post.getPostDate())));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRidesPostDialog crpd = new CreateRidesPostDialog();
                Bundle b = new Bundle();
                b.putParcelable("post", mValues.get(position));
                crpd.setArguments(b);
//                crpd.show(getFragmentManager(), "Edit Post");

                mListener.sendDialogFragmentToInflate(crpd, "Edit Post");
//                Fragment fragment = new CreateRidesPostDialog();
//                mListener.sendFragmentToInflate(crpd);
            }
        });

        //Este codigo abaixo funciona mas nao achei funcional pro futuro, so para apagar mais facil nos testes
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("THAIS", "Chamou a funcao");
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

    public void addPost(RidesPost post){
        mRefFirebasePosts.push().setValue(post);
    }

    public void addDraft(RidesPost post){
        mRefFirebaseDrafts.push().setValue(post);
    }

    public void update(RidesPost post) {
        mRefFirebasePosts.child(post.getKey()).setValue(post);
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
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("THAIS", "Deu log no onChildAdded do DraftsRides");
            RidesPost rp = dataSnapshot.getValue(RidesPost.class);
            rp.setKey(dataSnapshot.getKey());
            mValues.add(0, rp);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            for (int i = 0; i < mValues.size(); i++){
                if(mValues.get(i).getKey().equals(key)){
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
