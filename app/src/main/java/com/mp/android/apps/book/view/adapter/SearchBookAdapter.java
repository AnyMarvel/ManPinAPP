
package com.mp.android.apps.book.view.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mp.android.apps.R;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.utils.SearchSortUtils;
import com.mp.android.apps.book.widget.refreshview.RefreshRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public SearchBookAdapter() {
        super(true);
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_searchbook_item, parent, false));
    }

    @Override
    public void onBindViewholder(final RecyclerView.ViewHolder holder, final int position) {
        Glide.with(((Viewholder) holder).ivCover.getContext())
                .load(searchBooks.get(position).getCoverUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.img_cover_default)
                .into(((Viewholder) holder).ivCover);
        ((Viewholder) holder).tvName.setText(searchBooks.get(position).getName());
        ((Viewholder) holder).tvAuthor.setText(searchBooks.get(position).getAuthor());
        String state = searchBooks.get(position).getState();
        if (state == null || state.length() == 0) {
            ((Viewholder) holder).tvState.setVisibility(View.GONE);
        } else {
            ((Viewholder) holder).tvState.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvState.setText(state);
        }
        long words = searchBooks.get(position).getWords();
        if (words <= 0) {
            ((Viewholder) holder).tvWords.setVisibility(View.GONE);
        } else {
            String wordsS = Long.toString(words) + "字";
            if (words > 10000) {
                DecimalFormat df = new DecimalFormat("#.#");
                wordsS = df.format(words * 1.0f / 10000f) + "万字";
            }
            ((Viewholder) holder).tvWords.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvWords.setText(wordsS);
        }
        String kind = searchBooks.get(position).getKind();
        if (kind == null || kind.length() <= 0) {
            ((Viewholder) holder).tvKind.setVisibility(View.GONE);
        } else {
            ((Viewholder) holder).tvKind.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvKind.setText(kind);
        }
        if (searchBooks.get(position).getLastChapter() != null && searchBooks.get(position).getLastChapter().length() > 0)
            ((Viewholder) holder).tvLastest.setText(searchBooks.get(position).getLastChapter());
        else if (searchBooks.get(position).getDesc() != null && searchBooks.get(position).getDesc().length() > 0) {
            ((Viewholder) holder).tvLastest.setText(searchBooks.get(position).getDesc());
        } else
            ((Viewholder) holder).tvLastest.setText("");
        if (searchBooks.get(position).getOrigin() != null && searchBooks.get(position).getOrigin().length() > 0) {
            ((Viewholder) holder).tvOrigin.setVisibility(View.VISIBLE);
            ((Viewholder) holder).tvOrigin.setText("来源:" + searchBooks.get(position).getOrigin());
        } else {
            ((Viewholder) holder).tvOrigin.setVisibility(View.GONE);
        }
        if (searchBooks.get(position).getAdd()) {
            ((Viewholder) holder).tvAddShelf.setText("已添加");
            ((Viewholder) holder).tvAddShelf.setEnabled(false);
        } else {
            ((Viewholder) holder).tvAddShelf.setText("+添加");
            ((Viewholder) holder).tvAddShelf.setEnabled(true);
        }

        ((Viewholder) holder).flContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null)
                    itemClickListener.clickItem(((Viewholder) holder).ivCover, position, searchBooks.get(position));
            }
        });
        ((Viewholder) holder).tvAddShelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null)
                    itemClickListener.clickAddShelf(((Viewholder) holder).tvAddShelf, position, searchBooks.get(position));
            }
        });
    }

    @Override
    public int getItemViewtype(int position) {
        return 0;
    }

    @Override
    public int getItemcount() {
        return searchBooks.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        FrameLayout flContent;
        ImageView ivCover;
        TextView tvName;
        TextView tvAuthor;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLastest;
        TextView tvAddShelf;
        TextView tvOrigin;

        public Viewholder(View itemView) {
            super(itemView);
            flContent = (FrameLayout) itemView.findViewById(R.id.fl_content);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            tvState = (TextView) itemView.findViewById(R.id.tv_state);
            tvWords = (TextView) itemView.findViewById(R.id.tv_words);
            tvLastest = (TextView) itemView.findViewById(R.id.tv_lastest);
            tvAddShelf = (TextView) itemView.findViewById(R.id.tv_addshelf);
            tvKind = (TextView) itemView.findViewById(R.id.tv_kind);
            tvOrigin = (TextView) itemView.findViewById(R.id.tv_origin);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<SearchBookBean> newDatas,String content) {
        if(newDatas!=null && newDatas.size()>0){

            searchBooks.addAll(newDatas);

            removeDuplicationByHashSet(searchBooks);

            searchBooks = SearchSortUtils.filerSearchTools(searchBooks,content);

            notifyDataSetChanged();
        }
    }



    /**使用HashSet实现List去重(无序)
     *
     * @param list
     * */
    public  List<SearchBookBean> removeDuplicationByHashSet(List<SearchBookBean> list) {
        HashSet<SearchBookBean> set = new HashSet<SearchBookBean>(list);
        //把List集合所有元素清空
        list.clear();
        //把HashSet对象添加至List集合
        list.addAll(set);
        return list;
    }

    public void replaceAll(List<SearchBookBean> newData) {
        searchBooks.clear();
        if (newData != null && newData.size() > 0) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }

    public void setSearchBooks(List<SearchBookBean> searchBooks) {
        this.searchBooks = searchBooks;
    }
}