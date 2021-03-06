package edu.rosehulman.roselabs.sharewithme.Rides;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rosehulman.roselabs.sharewithme.Comments.Comment;
import edu.rosehulman.roselabs.sharewithme.Comments.CommentsAdapter;
import edu.rosehulman.roselabs.sharewithme.Constants;
import edu.rosehulman.roselabs.sharewithme.Interfaces.OnListFragmentInteractionListener;
import edu.rosehulman.roselabs.sharewithme.R;
import edu.rosehulman.roselabs.sharewithme.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RidesDetailFragment extends Fragment {

    private RidesPost mPost;
    private CommentsAdapter mAdapter;
    private OnListFragmentInteractionListener mListener;

    public RidesDetailFragment() {
        // Required empty public constructor
    }

    public RidesDetailFragment(RidesPost post){
        this.mPost = post;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListener = (OnListFragmentInteractionListener) getActivity();

        View view = inflater.inflate(R.layout.fragment_rides_detail, container, false);

        TextView option = (TextView) view.findViewById(R.id.result_option_text_view);
        TextView price = (TextView) view.findViewById(R.id.result_price_text_view);
        TextView title = (TextView) view.findViewById(R.id.result_title_text_view);
        TextView departure = (TextView) view.findViewById(R.id.result_departure_text_view);
        TextView date = (TextView) view.findViewById(R.id.result_date_text_view);
        TextView destination = (TextView) view.findViewById(R.id.result_destination_text_view);
        TextView description = (TextView) view.findViewById(R.id.result_description_text_view);
        TextView expiration = (TextView) view.findViewById(R.id.result_expiration_date_text_view);
        TextView keyword = (TextView) view.findViewById(R.id.result_keyword_text_view);
        TextView authorTextView = (TextView) view.findViewById(R.id.author_textView);
        TextView editButton = (TextView) view.findViewById(R.id.edit_ride_post_text_view);
        TextView inactivateButton = (TextView) view.findViewById(R.id.inactivate_post_text_view);

        if (!mPost.getUserId().equalsIgnoreCase(new Firebase(Constants.FIREBASE_URL).getAuth().getUid()))
            hideView(editButton);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRidesPostDialog crpd = new CreateRidesPostDialog();
                Bundle b = new Bundle();
                b.putParcelable("post", mPost);
                crpd.setArguments(b);
                crpd.show(getFragmentManager(), "Edit Post");
            }
        });

        if (!mPost.getUserId().equalsIgnoreCase(new Firebase(Constants.FIREBASE_URL).getAuth().getUid()))
            hideView(inactivateButton);

        inactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setMessage(R.string.inactivate_alert_message);
                ad.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.inactivatePost(mPost);
                    }
                }).setNegativeButton(android.R.string.cancel, null);
                ad.show();
            }
        });

        authorTextView.setText(String.format("@%s at %s", mPost.getUserId(), Utils.getStringDate(mPost.getPostDate())));

        authorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.sendProfileFragmentToInflate(mPost.getUserId());
            }
        });

        mAdapter = new CommentsAdapter("rides", mPost.getKey(), (OnListFragmentInteractionListener) getContext());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.comments_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mAdapter);

        Button sendButton = (Button) view.findViewById(R.id.send_comment_button);
        final EditText commentEditText = (EditText) view.findViewById(R.id.comment_message_edit_text);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentEditText.getText().toString();
                if (!comment.isEmpty()) {
                    Date date = new Date();
                    Comment c = new Comment();
                    c.setContent(comment);
                    c.setPostKey(mPost.getKey());
                    c.setDate(date);
                    c.setUserId(new Firebase(Constants.FIREBASE_URL).getAuth().getUid());
                    mAdapter.add(c);
                    commentEditText.setText("");
                    sendNotification(c.getUserId());
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });

        String optionValue;
        if (mPost.isOffer())
            optionValue = "Offer";
        else
            optionValue = "Request";

        //Displays the values and hides the view if empty
        option.setText(optionValue);

        if (mPost.getPrice().isEmpty()) {
            price.setText("$ not informed");
        } else {
            price.setText("$ " + mPost.getPrice());
        }

        title.setText(mPost.getTitle());

        if (mPost.getDepartureLocal().isEmpty()) {
            hideView(departure);
            hideView(view.findViewById(R.id.departure_text_view));
        } else {
            departure.setText(mPost.getDepartureLocal());
        }
        if (mPost.getRideDate() != null)
            date.setText(Utils.getStringDate(mPost.getRideDate()));

        if (mPost.getDestinationLocal().isEmpty()) {
            hideView(destination);
            hideView(view.findViewById(R.id.destination_text_view));
        } else {
            destination.setText(mPost.getDestinationLocal());
        }

        if (mPost.getDescription().isEmpty()) {
            hideView(description);
            hideView(view.findViewById(R.id.description_text_view));
        } else {
            description.setText(mPost.getDescription());
        }

        if (mPost.getExpirationDate() == null){
            expiration.setText("Expiration Date");
        } else {
            expiration.setText("Expires at " + Utils.getStringDate(mPost.getExpirationDate()));
        }

        if (mPost.getKeywords().isEmpty()) {
            hideView(keyword);
            hideView(view.findViewById(R.id.keyword_text_view));
        } else {
            keyword.setText(mPost.getKeywords());
        }

        return view;
    }

    public void sendNotification(final String cUser){
        Firebase firebase = new Firebase(Constants.FIREBASE_URL + "/comments/rides");
        final Query query = firebase.orderByChild("postKey").equalTo(mPost.getKey());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> notificationUsers = new ArrayList<>();
                notificationUsers.add(mPost.getUserId());
                Map<String, HashMap<String, String>> map = (Map<String, HashMap<String, String>>) dataSnapshot.getValue();;
                for (HashMap<String, String> s : map.values()) {
                    String userId = s.get("userId");
                    if (!notificationUsers.contains(userId))
                        notificationUsers.add(userId);
                }
                Utils.sendNotification(cUser, notificationUsers, mPost.getKey(), "rides");
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    public void setPost(RidesPost post){
        mPost = post;
    }

}
