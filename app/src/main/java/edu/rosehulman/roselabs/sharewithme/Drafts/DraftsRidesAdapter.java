package edu.rosehulman.roselabs.sharewithme.Drafts;

import android.os.Bundle;
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
import edu.rosehulman.roselabs.sharewithme.Rides.RidesPost;
import edu.rosehulman.roselabs.sharewithme.Utils;

public class DraftsRidesAdapter extends RecyclerView.Adapter<DraftsRidesAdapter.ViewHolder> {

    private List<RidesPost> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Firebase mRefFirebaseDrafts;
    private ChildEventListener mChildEventListener;
    private boolean mEmpty;

    public DraftsRidesAdapter(OnListFragmentInteractionListener listener) {
        mValues = new ArrayList<>();
        mEmpty = true;
        mListener = listener;
        mRefFirebaseDrafts = new Firebase(Constants.FIREBASE_RIDES_DRAFT_URL);
        mChildEventListener = new RidesChildEventListener();
        mRefFirebaseDrafts.addChildEventListener(mChildEventListener);
        RidesPost emptyPost = new RidesPost("No drafts in this category", "*data may be loading", false);
        mValues.add(emptyPost);
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

        if (!mEmpty) {
            holder.mDescriptionTextView.setText(String.format("@%s at %s", post.getUserId(), Utils.getStringDate(post.getPostDate())));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateRidesDraftDialog crpd = new CreateRidesDraftDialog();
                    Bundle b = new Bundle();
                    b.putParcelable("post", mValues.get(position));
                    crpd.setArguments(b);
                    mListener.sendDialogFragmentToInflate(crpd, "Edit Post");
                }
            });

            //Este codigo abaixo funciona mas nao achei funcional pro futuro, so para apagar mais facil nos testes
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String key = post.getKey();
                    //TODO verify user permission
                    mRefFirebaseDrafts.child(key).removeValue();
                    return false;
                }
            });
        } else {
            holder.mDescriptionTextView.setText(post.getDescription());
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
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
            if (mEmpty) {
                mValues.remove(0);
                mEmpty = false;
            }
            RidesPost rp = dataSnapshot.getValue(RidesPost.class);
            rp.setKey(dataSnapshot.getKey());
            mValues.add(0, rp);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            //No change
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
            if (mValues.size() < 1) {
                RidesPost emptyPost = new RidesPost("No drafts in this category", "*data may be loading", false);
                mEmpty = true;
                mValues.add(emptyPost);
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
