package com.yeahdev.yeahurls.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.activities.AddNoteActivity;
import com.yeahdev.yeahurls.activities.AddUrlActivity;
import com.yeahdev.yeahurls.fragments.OverviewFragment;
import com.yeahdev.yeahurls.interfaces.ICommunicationAdapter;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.SharedPreferencesHelper;
import com.yeahdev.yeahurls.util.Utilities;

import java.util.ArrayList;


public class OverviewRvAdapter extends RecyclerView.Adapter<OverviewRvAdapter.OverviewViewHolder> {

    public static class OverviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateTime;
        private TextView tvUrl;
        private TextView tvKeywords;
        private FloatingActionButton fabEdit;
        private FloatingActionButton fabRemove;

        public OverviewViewHolder(View itemView) {
            super(itemView);
            this.tvDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);
            this.tvUrl = (TextView) itemView.findViewById(R.id.tvUrl);
            this.tvUrl.setMovementMethod(LinkMovementMethod.getInstance());
            this.tvKeywords = (TextView) itemView.findViewById(R.id.tvKeywords);
            this.fabEdit = (FloatingActionButton) itemView.findViewById(R.id.fabEdit);
            this.fabRemove = (FloatingActionButton) itemView.findViewById(R.id.fabRemove);
        }
    }

    private Activity activity;
    private UserCreds userCreds;
    private ArrayList<UrlItem> urlItemCollection;
    private ICommunicationAdapter iCommAdapter;
    private boolean isScrolledToBottom;

    public OverviewRvAdapter(Activity activity, UserCreds userCreds, ICommunicationAdapter iCommAdapter) {
        this.activity = activity;
        this.userCreds = userCreds;
        this.urlItemCollection = new ArrayList<>();
        this.iCommAdapter = iCommAdapter;
        this.isScrolledToBottom = false;
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
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("Remove Item");
                alertDialogBuilder
                        .setMessage("Are you sure to remove this Item?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int idid) {
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
                                                OverviewRvAdapter.this.removeItem(position);
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    Utilities.buildSnackbar(activity, "authWithPassword failed! Error: " + e.getMessage());
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        holder.fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AddUrlActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("Id", urlItem.getId() - 1);
                bundle.putString("ObjId", urlItem.getObjId());
                bundle.putString("DateTime", holder.tvDateTime.getText().toString());
                bundle.putString("Keywords", holder.tvKeywords.getText().toString());
                bundle.putString("Url", holder.tvUrl.getText().toString());
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        });

        holder.tvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(holder.tvUrl.getText().toString())));
            }
        });
    }

    /**
     * Method to add one Adapter Item on Parameter Position
     * @param position Position in Adapter
     * @param urlItem UrlItem
     */
    public void addItem(int position, UrlItem urlItem) {
        try {
            this.urlItemCollection.add(position, urlItem);
            this.iCommAdapter.getAddedItemPosition(position, urlItem);
            notifyItemInserted(position);
        } catch (Exception e) {
            Utilities.buildSnackbar(activity, "addItem failed! Error: " + e.getMessage());
        }
    }
    /**
     * Method to remove one Adapter Item on Parameter Position
     * @param position Position in Adapter
     */
    public void removeItem(int position) {
        try {
            this.urlItemCollection.remove(position);
            this.iCommAdapter.getRemovedItemPosition(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            Utilities.buildSnackbar(activity, "removeItem failed! Error: " + e.getMessage());
        }
    }
    /**
     * Method to remove all Items from RecyclerView Adapter
     */
    public void removeAllItems() {
        if (this.getItemCount() > 0) {
            this.urlItemCollection.clear();
            notifyDataSetChanged();
        }
    }
    /**
     * Method to find and replace update Adapter Element
     * @param urlItem UrlItem
     */
    public void searchAndReplaceItem(UrlItem urlItem) {
        for (UrlItem itemInAdapter : this.urlItemCollection) {
            if (urlItem.getObjId().matches(itemInAdapter.getObjId())) {
                int position = this.urlItemCollection.indexOf(itemInAdapter);
                if (position != -1) {
                    this.removeItem(position);
                    this.addItem(position, urlItem);
                    break;
                }
            }
        }
    }


    public void setScrolledToBottom(boolean value) {
        this.isScrolledToBottom = value;
    }

    public boolean isScrolledToBottom() {
        return this.isScrolledToBottom ;
    }

    /**
     * BEGIN METHODS FOR FILTER ADAPTER ELEMENTS
     */
    /**
     * Method to call to filter RecyclerView Items
     * @param models new filtered ArrayList
     */
    public void animateTo(ArrayList<UrlItem> models) {
        this.applyAndAnimateRemovals();
        this.applyAndAnimateAdditions(models);
    }
    /**
     * Method to Remove not used ArrayList Elements from Adapter
     */
    private void applyAndAnimateRemovals() {
        this.urlItemCollection.clear();
        notifyDataSetChanged();
    }
    /**
     * Method to Apply only filtered ArrayList Elements to Adapter
     * @param newModels new filtered ArrayList
     */
    private void applyAndAnimateAdditions(ArrayList<UrlItem> newModels) {
        for (int i = 0; i < newModels.size(); i++) {
            this.addFilterItem(i, newModels.get(i));
        }
    }
    /**
     * Method Add Items on Parameter Position
     * @param position Position in Adapter
     * @param model UrlItem
     */
    public void addFilterItem(int position, UrlItem model) {
        this.urlItemCollection.add(position, model);
        notifyItemInserted(position);
    }
    /**
     * END METHODS FOR FILTER ADAPTER ELEMENTS
     */

    /**
     * Methode to get Adapter Size
     * @return int Adapter Size
     */
    @Override
    public int getItemCount() {
        return this.urlItemCollection.size();
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
