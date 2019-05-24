package com.btinternet.jackbaxter007.dmhelper;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.BeanHolder>{

    private List<Character> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private OnCharacterItemClick onCharacterItemClick;

    public CharactersAdapter(List<Character> list,Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        this.onCharacterItemClick = (OnCharacterItemClick) context;
    }
    @Override
    public BeanHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.character_list_item,parent,false);
        return new BeanHolder(view);
    }

    @Override
    public void onBindViewHolder(BeanHolder holder, int position) {
        Log.e("bind", "onBindViewHolder: "+ list.get(position));
        holder.textViewCharacter.setText(list.get(position).getChar_name());
        holder.textViewPlayer.setText(list.get(position).getPlayer_name());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class BeanHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewPlayer;
        TextView textViewCharacter;
        public BeanHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textViewPlayer = itemView.findViewById(R.id.item_playername);
            textViewCharacter = itemView.findViewById(R.id.item_charname);
        }

        @Override
        public void onClick(View view) {
            onCharacterItemClick.onCharacterClick(getAdapterPosition());
        }
    }

    public interface OnCharacterItemClick{
        void onCharacterClick(int pos);
    }
}
