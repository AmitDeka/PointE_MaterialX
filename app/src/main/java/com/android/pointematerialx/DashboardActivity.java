package com.android.pointematerialx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pointematerialx.utils.NoteAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity {

    RecyclerView noteList;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    FirestoreRecyclerAdapter<NoteAdapter, NoteViewHolder> NoteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        final String[] darkModeValues = getResources().getStringArray(R.array.dark_mode_values);
        // The apps theme is decided depending upon the saved preferences on app startup
        String pref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.dark_mode), getString(R.string.dark_mode_def_value));
        // Comparing to see which preference is selected and applying those theme settings
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

        noteList = findViewById(R.id.dash_recycle_view);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        Query query = fStore.collection("AllNotes").document(fUser.getUid()).collection("UserNotes").orderBy("title", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<NoteAdapter> allNotes = new FirestoreRecyclerOptions.Builder<NoteAdapter>().setQuery(query, NoteAdapter.class).build();

        NoteAdapter = new FirestoreRecyclerAdapter<NoteAdapter, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull NoteAdapter noteAdapter) {
                noteViewHolder.noteTitle.setText(noteAdapter.getTitle());
                noteViewHolder.noteContent.setText(noteAdapter.getContent());
                final int colorCode = getRandomColor();
                noteViewHolder.noteBar.setBackgroundColor(noteViewHolder.view.getResources().getColor(colorCode, null));
                final String docId =NoteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.view.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), NoteDetailActivity.class);
                    intent.putExtra("title", noteAdapter.getTitle());
                    intent.putExtra("content", noteAdapter.getContent());
                    intent.putExtra("code", colorCode);
                    intent.putExtra("noteId", docId);
                    v.getContext().startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });


            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        noteList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList.setAdapter(NoteAdapter);

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

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;
        View view, noteBar;

        public NoteViewHolder(@NonNull View itemView) {
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
        NoteAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (NoteAdapter != null) {
            NoteAdapter.stopListening();
        }
    }
}