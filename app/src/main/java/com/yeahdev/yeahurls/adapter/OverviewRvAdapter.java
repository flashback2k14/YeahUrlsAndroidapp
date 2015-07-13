package com.yeahdev.yeahurls.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.interfaces.ICommunicationAdapter;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.Utilities;

import java.util.ArrayList;


public class OverviewRvAdapter extends RecyclerView.Adapter<OverviewRvAdapter.OverviewViewHolder> {

    public static class OverviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateTime;
        private TextView tvUrl;
        private TextView tvKeywords;
        private FloatingActionButton fabRemove;

        public OverviewViewHolder(View itemView) {
            super(itemView);
            this.tvDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);
            this.tvUrl = (TextView) itemView.findViewById(R.id.tvUrl);
            this.tvUrl.setMovementMethod(LinkMovementMethod.getInstance());
            this.tvKeywords = (TextView) itemView.findViewById(R.id.tvKeywords);
            this.fabRemove = (FloatingActionButton) itemView.findViewById(R.id.fabRemove);
        }
    }

    private Activity activity;
    private UserCreds userCreds;
    private ArrayList<UrlItem> urlItemCollection;
    private ICommunicationAdapter iCommAdapter;

    public OverviewRvAdapter(Activity activity, UserCreds userCreds, ICommunicationAdapter iCommAdapter) {
        this.activity = activity;
        this.userCreds = userCreds;
        this.urlItemCollection = new ArrayList<>();
        this.iCommAdapter = iCommAdapter;
    }

    @Override
    public OverviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.overview_item, parent, false);
        return new OverviewViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final OverviewViewHolder holder, final int position) {
        final UrlItem urlItem = urlItemCollection.get(position);
        holder.tvDateTime.setText("Date: " + urlItem.getDate() + "\nTime: " + urlItem.getTime());
        holder.tvUrl.setText(urlItem.getValue());
        holder.tvKeywords.setText(urlItem.getKeywords());

        holder.fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = urlItem.getId() - 1;

                try {
                    Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId()
                            + "/urlcollector/" + urlItem.getObjId() + "/" + id);

                    ref.removeValue(new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Utilities.buildSnackbar(activity, "Error Code:" + firebaseError.getCode()
                                        + ", Msg: " + firebaseError.getMessage());
                            } else {
                                removeItem(position);
                            }
                        }
                    });
                } catch (Exception e) {
                    Utilities.buildSnackbar(activity, "authWithPassword failed! Error: " + e.getMessage());
                }
            }
        });

        holder.tvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(holder.tvUrl.getText().toString())));
            }
        });
    }

    public void addItem(int position, UrlItem urlItem) {
        try {
            urlItemCollection.add(position, urlItem);
            notifyItemInserted(position);
            notifyDataSetChanged();
        } catch (Exception e) {
            Utilities.buildSnackbar(activity, "addItem failed! Error: " + e.getMessage());
        }
    }

    public void removeItem(int position) {
        try {
            urlItemCollection.remove(position);
            notifyItemRemoved(position);
            iCommAdapter.getRemovedItemPosition(position);
            notifyDataSetChanged();
        } catch (Exception e) {
            Utilities.buildSnackbar(activity, "removeItem failed! Error: " + e.getMessage());
        }
    }

    public void removeAllItems() {
        if (getItemCount() > 0) {
            for (int i = 0; i < getItemCount(); i++) {
                removeItem(i);
            }
        }
    }

    public void animateTo(ArrayList<UrlItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
    }

    private void applyAndAnimateRemovals(ArrayList<UrlItem> newModels) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final UrlItem model = urlItemCollection.get(i);
            if (!newModels.contains(model)) {
                removeFilterItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<UrlItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final UrlItem model = newModels.get(i);
            if (!urlItemCollection.contains(model)) {
                addFilterItem(i, model);
            }
        }
    }

    public UrlItem removeFilterItem(int position) {
        final UrlItem model = urlItemCollection.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addFilterItem(int position, UrlItem model) {
        urlItemCollection.add(position, model);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return urlItemCollection.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

}
