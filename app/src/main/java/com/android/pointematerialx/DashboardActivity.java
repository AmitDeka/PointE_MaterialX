package com.android.pointematerialx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.pointematerialx.utils.AdapterModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class DashboardActivity extends AppCompatActivity {
    ImageView noNotesImg;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    RecyclerView noteRecyclerView;

    private CoordinatorLayout mDashboard;
    private FirestoreRecyclerAdapter<AdapterModel, DashNoteViewHolder> NoteViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        final String[] darkModeValues = getResources().getStringArray(R.array.dark_mode_values);
        String pref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.dark_mode), getString(R.string.dark_mode_def_value));
        if (pref.equals(darkModeValues[0]))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (pref.equals(darkModeValues[1]))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (pref.equals(darkModeValues[2]))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        MaterialToolbar materialToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(materialToolbar);

        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        FloatingActionButton btnAdd = findViewById(R.id.floating_addbtn);

        mDashboard = findViewById(R.id.dash_board);
        noNotesImg = findViewById(R.id.dash_no_notes);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();


        Query query = fStore.collection("AllNotes").document(fUser.getUid()).collection("UserNotes").orderBy("date", Query.Direction.DESCENDING);

        noteRecyclerView = findViewById(R.id.dash_note_recycle_view);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));


        FirestoreRecyclerOptions<AdapterModel> mAdapterModel = new FirestoreRecyclerOptions.Builder<AdapterModel>().setQuery(query, snapshot -> {
            AdapterModel AModel = snapshot.toObject(AdapterModel.class);
            AModel.setDocumentId(snapshot.getId());
            return AModel;
        }).setLifecycleOwner(this).build();

//        FirestoreRecyclerOptions<AdapterModel> mAdapterModel = new FirestoreRecyclerOptions.Builder<AdapterModel>().setQuery(query, AdapterModel.class).build();


        NoteViewAdapter = new FirestoreRecyclerAdapter<AdapterModel, DashNoteViewHolder>(mAdapterModel) {
            @NonNull
            @Override
            public DashNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout, parent, false);
                return new DashNoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DashboardActivity.DashNoteViewHolder holder, int position, @NonNull AdapterModel model) {
                holder.noteTitle.setText(model.getTitle());
                holder.noteContent.setText(model.getContent());
                holder.itemView.setTag(model.documentId);
                final int colorCode = getRandomColor();
                holder.noteBar.setBackgroundColor(holder.view.getResources().getColor(colorCode, null));
                final String docID = NoteViewAdapter.getSnapshots().getSnapshot(position).getId();

                holder.view.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), NoteDetailActivity.class);
                    intent.putExtra("title", model.getTitle());
                    intent.putExtra("content", model.getContent());
                    intent.putExtra("code", colorCode);
                    intent.putExtra("noteId", docID);
                    v.getContext().startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }

            @Override
            public void onDataChanged() {
                if (NoteViewAdapter.getItemCount() == 0) {
                    noNotesImg.setVisibility(View.VISIBLE);
                    noteRecyclerView.setVisibility(View.INVISIBLE);
                } else {
                    noNotesImg.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.VISIBLE);
                }
            }


        };
        noteRecyclerView.setAdapter(NoteViewAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String documentId = (String) viewHolder.itemView.getTag();
                fStore.collection("AllNotes")
                        .document(fUser.getUid())
                        .collection("UserNotes")
                        .document(documentId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Snackbar.make(mDashboard, "Note deleted successfully.", Snackbar.LENGTH_LONG)
                                        .setAction("Undo", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        })
                                        .show();
                            }
                        })
                        .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getLocalizedMessage()));
            }
        }).attachToRecyclerView(noteRecyclerView);

        btnAdd.setOnClickListener(v -> {
            Intent addNote = new Intent(DashboardActivity.this, AddNoteActivity.class);
            startActivity(addNote);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.dash_home:
                    Toast.makeText(DashboardActivity.this, "Hone Click", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.dash_settings:
                    Intent settingIntent = new Intent(DashboardActivity.this, SettingPrefActivity.class);
                    startActivity(settingIntent);
                    return true;
                case R.id.dash_about:
                    Intent aboutIntent = new Intent(DashboardActivity.this, AboutActivity.class);
                    startActivity(aboutIntent);
                    return true;
            }
            return false;
        });
    }

    public static class DashNoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;
        View view, noteBar;

        public DashNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.note_title);
            noteContent = itemView.findViewById(R.id.note_content);
            noteBar = itemView.findViewById(R.id.note_bar);
            view = itemView;
        }
    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.purple_randf);
        colorCode.add(R.color.amber_randf);
        colorCode.add(R.color.red_randf);
        colorCode.add(R.color.blue_randf);
        colorCode.add(R.color.cyan_randf);
        colorCode.add(R.color.green_randf);
        colorCode.add(R.color.lime_randf);
        colorCode.add(R.color.orange_randf);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    @Override
    public void onStart() {
        super.onStart();
        NoteViewAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (NoteViewAdapter != null) {
            NoteViewAdapter.stopListening();
        }
    }

}