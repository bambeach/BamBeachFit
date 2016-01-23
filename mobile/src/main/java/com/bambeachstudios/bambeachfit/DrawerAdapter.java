package com.bambeachstudios.bambeachfit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by holls_gurl on 1/22/16.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    DrawerAdapter(){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_header, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.profileImage.setBackgroundResource(R.drawable.shape_circle);
        viewHolder.profileName.setText("BJ Christiansen");
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView profileName;
        TextView profileAltInfo;

        public ViewHolder(View itemView) {
            super(itemView);

            profileImage = (ImageView) itemView.findViewById(R.id.header_profile_picture);
            profileName = (TextView) itemView.findViewById(R.id.header_name);
        }
    }
}
