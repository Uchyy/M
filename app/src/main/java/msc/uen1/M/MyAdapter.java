package msc.uen1.M;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    ArrayList <String> listitems;
    Context context;
    static ItemClickListener clickListener;

    public MyAdapter(ArrayList<String> listitems, Context context,  ItemClickListener clickListener) {
        this.listitems = listitems;
        this.context = context;
        this.clickListener = clickListener;

    }

    public MyAdapter(Folder folder, ArrayList<String> listitems) {
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;

        //ItemClickListener clickListener;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            imgView = view.findViewById(R.id.imageView3);
            //this.clickListener = clickListener;
            //view.setOnClickListener(this);
        }

        public ImageView getImgView() {
            return imgView;
        }
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.database, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        Picasso.with(context).load(listitems.get(position)).into(holder.getImgView());
        holder.getImgView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onClick(holder.getLayoutPosition());
            }
        });
        //Glide.with(this /* context */).load(storageReference).into(imageView);
    }

    @Override
    public int getItemCount() {
        return listitems.size();
    }
}
