package com.aghajari.sample.axrlottie.activity;

import android.graphics.Color;
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

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.rlottie.AXrLottieLayerInfo;
import com.aghajari.rlottie.AXrLottieProperty;
import com.aghajari.sample.axrlottie.R;

import java.util.List;
import java.util.Random;

public class LottieEditorActivity extends AppCompatActivity {
    AXrLottieImageView lottieView;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        lottieView = findViewById(R.id.lottie_view);
        rv = findViewById(R.id.rv);

        lottieView.setLottieDrawable(AXrLottie.Loader.createFromAssets(this, "mountain.json",
                "editor", 512, 512, false, false));
        lottieView.playAnimation();

        rv.setLayoutManager(new LinearLayoutManager(this));
        LayerAdapter adapter = new LayerAdapter();
        adapter.setAnimation(lottieView.getLottieDrawable());
        rv.setAdapter(adapter);
    }

    private class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder> {

        List<AXrLottieLayerInfo> list;
        AXrLottieDrawable drawable;

        public void setAnimation(AXrLottieDrawable drawable) {
            this.drawable = drawable;
            list = drawable.getLayers();
            notifyDataSetChanged();

            Log.i("AXrLottie", "Layers : ");
            for (AXrLottieLayerInfo layerInfo : list) {
                Log.i("AXrLottie", layerInfo.toString());
            }

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layer_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.onBind(drawable, list.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            TextView layerName;
            AppCompatButton button;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                layerName = ((ViewGroup) itemView).findViewById(R.id.layer_name);
                button = ((ViewGroup) itemView).findViewById(R.id.btn);
            }

            public void onBind(final AXrLottieDrawable drawable, final String layer) {
                layerName.setText(layer);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        drawable.setLayerProperty(layer + ".**", AXrLottieProperty.fillColor(findColor()));
                    }
                });
            }
        }

        Random rnd = new Random();

        private int findColor() {
            return Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        }
    }
}