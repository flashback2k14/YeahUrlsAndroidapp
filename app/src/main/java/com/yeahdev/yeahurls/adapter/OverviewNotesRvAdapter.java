package com.yeahdev.yeahurls.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.interfaces.ICommunicationAdapter;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.Utilities;

import java.util.ArrayList;

public class OverviewNotesRvAdapter extends RecyclerView.Adapter<OverviewNotesRvAdapter.OverviewNotesViewHolder> {

    public static class OverviewNotesViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvNote;
        private TextView tvKeywordsNote;
        private FloatingActionButton fabRemoveNote;

        public OverviewNotesViewHolder(View itemView) {
            super(itemView);
            this.tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            this.tvNote = (TextView) itemView.findViewById(R.id.tvNote);
            this.tvKeywordsNote = (TextView) itemView.findViewById(R.id.tvKeywordsNote);
            this.fabRemoveNote = (FloatingActionButton) itemView.findViewById(R.id.fabRemoveNote);
        }
    }

    private Activity activity;
    private UserCreds userCreds;
    private ArrayList<NoteItem> noteItemCollection;
    private ICommunicationAdapter iCommAdapter;

    public OverviewNotesRvAdapter(Activity activity, UserCreds userCreds, ICommunicationAdapter iCommAdapter) {
        this.activity = activity;
        this.userCreds = userCreds;
        this.noteItemCollection = new ArrayList<>();
        this.iCommAdapter = iCommAdapter;
    }

    @Override
    public OverviewNotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.overview_note_item, parent, false);
        return new OverviewNotesViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final OverviewNotesViewHolder holder, final int position) {
        final NoteItem noteItem = noteItemCollection.get(position);
        holder.tvTitle.setText(noteItem.getTitle());
        holder.tvNote.setText(noteItem.getValue());
        holder.tvKeywordsNote.setText(noteItem.getKeywords());

        holder.fabRemoveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("Remove Item");
                alertDialogBuilder
                        .setMessage("Are you sure to remove this Item?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int idid) {
                                int id = noteItem.getId() - 1;

                                try {
                                    Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId()
                                            + "/notescollector/" + noteItem.getObjId() + "/" + id);

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
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    public void addItem(int position, NoteItem noteItem) {
        try {
            noteItemCollection.add(position, noteItem);
            notifyItemInserted(position);
            notifyDataSetChanged();
        } catch (Exception e) {
            Utilities.buildSnackbar(activity, "addItem failed! Error: " + e.getMessage());
        }
    }

    public void removeItem(int position) {
        try {
            noteItemCollection.remove(position);
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

    public void animateTo(ArrayList<NoteItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
    }

    private void applyAndAnimateRemovals(ArrayList<NoteItem> newModels) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final NoteItem model = noteItemCollection.get(i);
            if (!newModels.contains(model)) {
                removeFilterItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<NoteItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final NoteItem model = newModels.get(i);
            if (!noteItemCollection.contains(model)) {
                addFilterItem(i, model);
            }
        }
    }

    public NoteItem removeFilterItem(int position) {
        final NoteItem model = noteItemCollection.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addFilterItem(int position, NoteItem model) {
        noteItemCollection.add(position, model);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return noteItemCollection.size();
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
