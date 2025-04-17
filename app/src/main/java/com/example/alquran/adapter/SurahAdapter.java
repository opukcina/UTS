package com.example.alquran.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.alquran.R;
import com.example.alquran.model.Surah;

import java.lang.ref.Reference;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.ViewHolder> {

    private final List<Surah> surahList;
    private final OnItemClickListener listener;

    public void updateData(List<Surah> data) {
        
    }

    public interface OnItemClickListener {
        void onItemClick(Surah surah);
    }

    public SurahAdapter(List<Surah> surahList, OnItemClickListener listener) {
        this.surahList = surahList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Surah surah = surahList.get(position);
        holder.tvSurahNumber.setText(String.valueOf(surah.getNumber()));
        holder.tvSurahName.setText(surah.getName());
        holder.tvSurahLatin.setText(surah.getEnglishName());
        holder.tvAyahCount.setText(surah.getNumberOfAyahs() + " ayat");

        holder.bind(surah, listener);
    }

    @Override
    public int getItemCount() {
        return surahList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSurahNumber, tvSurahName, tvSurahLatin, tvAyahCount;
        private Reference<Object> data;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSurahNumber = itemView.findViewById(R.id.tv_surah_number);
            tvSurahName = itemView.findViewById(R.id.tv_surah_name);
            tvSurahLatin = itemView.findViewById(R.id.tv_surah_latin);
            tvAyahCount = itemView.findViewById(R.id.tv_ayah_count);
        }

        public void bind(Surah surah, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                Log.d("SurahAdapter", "Item clicked: " + surah.getName());
                // Trigger the click listener
                listener.onItemClick(surah);
                // Optionally, you can also show a Toast or perform any other action
                // Toast.makeText(itemView.getContext(), "Clicked: " + surah.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}