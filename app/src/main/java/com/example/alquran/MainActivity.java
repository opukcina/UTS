package com.example.alquran;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alquran.adapter.SurahAdapter;
import com.example.alquran.api.ApiClient;
import com.example.alquran.api.ApiService;
import com.example.alquran.model.SurahResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private Button btnLogin, btnLogout;
    private TextView tvProfile;
    private RecyclerView recyclerView;
    private SurahAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tambahkan Toolbar sebagai action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);
        tvProfile = findViewById(R.id.tvProfile);
        recyclerView = findViewById(R.id.rv_surah);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseAuth = FirebaseAuth.getInstance();
        configureGoogleSignIn();

        btnLogin.setOnClickListener(v -> signIn());
        btnLogout.setOnClickListener(v -> signOut());

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id)) // Wajib ada untuk Firebase
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
            });

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful: " + account.getEmail());
                String idToken = account.getIdToken();
                if (idToken == null) {
                    Log.e(TAG, "ID Token is null");
                    Toast.makeText(this, "ID Token tidak ditemukan", Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, task1 -> {
                            if (task1.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                Log.d(TAG, "Firebase Sign-In successful: " + Objects.requireNonNull(user).getEmail());
                                updateUI(user);
                            } else {
                                Log.e(TAG, "Firebase Sign-In failed: " + task1.getException());
                                Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        });
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed", e);
            Toast.makeText(this, "Google Sign-In gagal", Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void signOut() {
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
    }

    @SuppressLint("StringFormatInvalid")
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            tvProfile.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            String displayName = user.getDisplayName() != null ? user.getDisplayName() : "User";
            String email = user.getEmail() != null ? user.getEmail() : "";

            tvProfile.setText("Login sebagai: " + displayName + " (" + email + ")");
            fetchSurahList();
        } else {
            tvProfile.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void fetchSurahList() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<SurahResponse> call = apiService.getSurahList();
        call.enqueue(new Callback<SurahResponse>() {
            @Override
            public void onResponse(@NonNull Call<SurahResponse> call, @NonNull Response<SurahResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getData().isEmpty()) {
                    adapter = new SurahAdapter(response.body().getData(), surah -> {
                        Intent intent = new Intent(MainActivity.this, SurahDetailActivity.class);
                        intent.putExtra("SURAH_NUMBER", surah.getNumber());
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SurahResponse> call, @NonNull Throwable t) {
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
