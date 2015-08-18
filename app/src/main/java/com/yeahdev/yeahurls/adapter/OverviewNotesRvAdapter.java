package com.yeahdev.yeahurls.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.yeahdev.yeahurls.activities.AddNoteActivity;
import com.yeahdev.yeahurls.activities.MainActivity;
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
        private FloatingActionButton fabEditNote;
        private FloatingActionButton fabRemoveNote;

        public OverviewNotesViewHolder(View itemView) {
            super(itemView);
            this.tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            this.tvNote = (TextView) itemView.findViewById(R.id.tvNote);
            this.tvKeywordsNote = (TextView) itemView.findViewById(R.id.tvKeywordsNote);
            this.fabEditNote = (FloatingActionButton) itemView.findViewById(R.id.fabEditNote);
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
                                                OverviewNotesRvAdapter.this.removeItem(position);
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

        holder.fabEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AddNoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("Id", noteItem.getId() - 1);
                bundle.putString("ObjId", noteItem.getObjId());
                bundle.putString("Title", holder.tvTitle.getText().toString());
                bundle.putString("Keywords", holder.tvKeywordsNote.getText().toString());
                bundle.putString("Note", holder.tvNote.getText().toString());
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        });
    }

    /**
     * Method to add one Adapter Item on Parameter Position
     * @param position Position in Adapter
     * @param noteItem NoteItem
     */
    public void addItem(int position, NoteItem noteItem) {
        try {
            this.noteItemCollection.add(position, noteItem);
            this.iCommAdapter.getAddedItemPosition(position, noteItem);
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
            this.noteItemCollection.remove(position);
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
            this.noteItemCollection.clear();
            notifyDataSetChanged();
        }
    }
    /**
     * Method to find and replace update Adapter Element
     * @param noteItem NoteItem
     */
    public void searchAndReplaceItem(NoteItem noteItem) {
        for (NoteItem itemInAdapter : this.noteItemCollection) {
            if (noteItem.getObjId().matches(itemInAdapter.getObjId())) {
                int position = this.noteItemCollection.indexOf(itemInAdapter);
                if (position != -1) {
                    this.removeItem(position);
                    this.addItem(position, noteItem);
                    break;
                }
            }
        }
    }
    /**
     * BEGIN METHODS FOR FILTER ADAPTER ELEMENTS
     */
    /**
     * Method to call to filter RecyclerView Items
     * @param models new filtered ArrayList
     */
    public void animateTo(ArrayList<NoteItem> models) {
        this.applyAndAnimateRemovals(models);
        this.applyAndAnimateAdditions(models);
    }
    /**
     * Method to Remove not used ArrayList Elements from Adapter
     * @param newModels new filtered ArrayList
     */
    private void applyAndAnimateRemovals(ArrayList<NoteItem> newModels) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final NoteItem model = this.noteItemCollection.get(i);
            if (!newModels.contains(model)) {
                this.removeFilterItem(i);
            }
        }
    }
    /**
     * Method Remove Items on Parameter Position
     * @param position Position in Adapter
     */
    public void removeFilterItem(int position) {
        this.noteItemCollection.remove(position);
        notifyItemRemoved(position);
    }
    /**
     * Method to Apply only filtered ArrayList Elements to Adapter
     * @param newModels new filtered ArrayList
     */
    private void applyAndAnimateAdditions(ArrayList<NoteItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final NoteItem model = newModels.get(i);
            if (!this.noteItemCollection.contains(model)) {
                this.addFilterItem(i, model);
            }
        }
    }
    /**
     * Method Add Items on Parameter Position
     * @param position Position in Adapter
     * @param model NoteItem
     */
    public void addFilterItem(int position, NoteItem model) {
        this.noteItemCollection.add(position, model);
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
        return this.noteItemCollection.size();
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
