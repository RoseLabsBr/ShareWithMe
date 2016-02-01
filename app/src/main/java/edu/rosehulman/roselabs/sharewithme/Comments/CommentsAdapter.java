package edu.rosehulman.roselabs.sharewithme.Comments;

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
import edu.rosehulman.roselabs.sharewithme.FormatData.FormatData;
import edu.rosehulman.roselabs.sharewithme.R;

/**
 * Created by rodrigr1 on 2/1/2016.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private final Firebase mRefFirebase;
    private List<Comment> mComments;
    private String mCategory, mPostKey;

    public CommentsAdapter(String category, String postKey) {
        mComments = new ArrayList<>();
        mCategory = category.toLowerCase();
        mPostKey = postKey;
        mRefFirebase = new Firebase(Constants.FIREBASE_URL + "/comments/" + mCategory);
        Query query = mRefFirebase.orderByChild("postKey").equalTo(mPostKey);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                comment.setKey(dataSnapshot.getKey());
                mComments.add(comment);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentsAdapter.ViewHolder holder, int position) {
        final Comment comment = mComments.get(position);
        holder.mContentTextView.setText(comment.getContent());
        holder.mAuthorTextView.setText(String.format("@%s at %s", comment.getUserId(), FormatData.getStringDate(comment.getDate())));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView mContentTextView;
        public TextView mAuthorTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentTextView = (TextView) view.findViewById(R.id.comment_message);
            mAuthorTextView = (TextView) view.findViewById(R.id.author_textView);
        }
    }

    public void add(Comment comment){
        comment.setUserId(mRefFirebase.getAuth().getUid());
        mRefFirebase.push().setValue(comment);
    }
}