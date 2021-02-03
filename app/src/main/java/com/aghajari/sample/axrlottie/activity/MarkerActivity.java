package com.aghajari.sample.axrlottie.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.rlottie.AXrLottieMarker;
import com.aghajari.sample.axrlottie.R;

import java.util.List;

public class MarkerActivity extends AppCompatActivity {
    AXrLottieImageView lottieView;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        lottieView = findViewById(R.id.lottie_view);
        rv = findViewById(R.id.rv);

        lottieView.setLottieDrawable(AXrLottieDrawable.fromAssets(this, "marker.json")
                .setCacheName("marker")
                .setSize(512, 512)
                .setCacheEnabled(false)
                .setAutoRepeatMode(AXrLottieDrawable.REPEAT_MODE_REVERSE)
                .setAutoRepeat(true)
                .setSelectedMarker(null)
                .build());
        lottieView.playAnimation();

        rv.setLayoutManager(new LinearLayoutManager(this));
        MarkerAdapter adapter = new MarkerAdapter();
        adapter.setAnimation(lottieView.getLottieDrawable());
        rv.setAdapter(adapter);
    }


    private class MarkerAdapter extends RecyclerView.Adapter<MarkerAdapter.ViewHolder> {

        List<AXrLottieMarker> list;
        AXrLottieDrawable drawable;
        int selected;

        public void setAnimation(AXrLottieDrawable drawable) {
            this.drawable = drawable;
            drawable.selectMarker(null);

            list = drawable.getMarkers();
            // markers
            Log.i("AXrLottie", "Markers : ");
            for (AXrLottieMarker marker : list) {
                Log.i("AXrLottie", marker.toString());
            }

            list.add(0, new AXrLottieMarker("default", -1, -1));
            selected = 0;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_marker_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.onBind(drawable, this, position, list.get(position));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            TextView markerName;
            AppCompatButton button;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                markerName = ((ViewGroup) itemView).findViewById(R.id.marker_name);
                button = ((ViewGroup) itemView).findViewById(R.id.btn);
            }

            public void onBind(final AXrLottieDrawable drawable, final MarkerAdapter adapter,
                               final int position, final AXrLottieMarker marker) {

                markerName.setText(marker.getMarker());
                button.setEnabled(adapter.selected != position);
                button.setText(button.isEnabled() ? "SELECT" : "SELECTED");

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.selected = position;
                        drawable.selectMarker(marker);
                        drawable.restart();
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }
}