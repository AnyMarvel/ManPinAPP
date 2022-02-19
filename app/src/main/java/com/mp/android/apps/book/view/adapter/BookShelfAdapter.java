
package com.mp.android.apps.book.view.adapter;

import android.os.Handler;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mp.android.apps.R;
import com.mp.android.apps.book.dao.BookChapterBeanDao;
import com.mp.android.apps.book.widget.refreshview.RefreshRecyclerViewAdapter;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.BookRecordBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.local.BookRepository;
import com.mp.android.apps.readActivity.local.DaoDbHelper;
import com.mp.android.apps.readActivity.utils.Constant;
import com.mp.android.apps.readActivity.utils.StringUtils;
import com.mp.android.apps.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.grantland.widget.AutofitTextView;

public class BookShelfAdapter extends RefreshRecyclerViewAdapter {
    private final int TYPE_LASTEST = 1;
    private final int TYPE_OTHER = 2;

    private final long DURANIMITEM = 130;   //item动画启动间隔

    private List<CollBookBean> books;

    private Boolean needAnim = true;

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void toSearch();

        void onClick(CollBookBean collBookBean, int index);

        void onLongClick(View view, CollBookBean bookShelfBean, int index);
    }

    public BookShelfAdapter() {
        super(false);
        books = new ArrayList<>();
    }

    @Override
    public int getItemcount() {
        if (books.size() == 0) {
            return 1;
        } else {
            if (books.size() % 3 == 0) {
                return 1 + books.size() / 3;
            } else {
                return 1 + (books.size() / 3 + 1);
            }
        }
    }

    public int getRealItemCount() {
        return books.size();
    }

    @Override
    public int getItemViewtype(int position) {
        if (position == 0) {
            return TYPE_LASTEST;
        } else {
            return TYPE_OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewholder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LASTEST) {
            return new LastestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_lastest, parent, false));
        } else {
            return new OtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bookshelf_other, parent, false));
        }
    }

    @Override
    public void onBindViewholder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_LASTEST) {
            bindLastestViewHolder((LastestViewHolder) holder, position);
        } else {
            bindOtherViewHolder((OtherViewHolder) holder, position - 1);
        }
    }

    private void bindOtherViewHolder(final OtherViewHolder holder, int index) {
        final int index_1 = index * 3;
        if (needAnim) {
            final Animation animation = AnimationUtils.loadAnimation(holder.flContent_1.getContext(), R.anim.anim_bookshelf_item);
            animation.setAnimationListener(new AnimatontStartListener() {
                @Override
                void onAnimStart(Animation animation) {
                    needAnim = false;
                    holder.flContent_1.setVisibility(View.VISIBLE);
                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != holder)
                        holder.flContent_1.startAnimation(animation);
                }
            }, index_1 * DURANIMITEM);
        } else {
            holder.flContent_1.setVisibility(View.VISIBLE);
        }
        Glide.with(holder.ivCover_1.getContext()).load(books.get(index_1).getCover()).
                dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover_1);
        holder.tvName_1.setText(books.get(index_1).getTitle());
        if (books.get(index_1).getIsUpdate()){
            holder.tvUpdateNotify_1.setVisibility(View.VISIBLE);
        }else {
            holder.tvUpdateNotify_1.setVisibility(View.GONE);
        }

        holder.ibContent_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null)
                    itemClickListener.onClick(books.get(index_1), index_1);
            }
        });
        holder.ibContent_1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(holder.ivCover_1, books.get(index_1), index_1);
                    return true;
                } else
                    return false;
            }
        });

        final int index_2 = index_1 + 1;
        if (index_2 < books.size()) {
            if (needAnim) {
                final Animation animation = AnimationUtils.loadAnimation(holder.flContent_2.getContext(), R.anim.anim_bookshelf_item);
                animation.setAnimationListener(new AnimatontStartListener() {
                    @Override
                    void onAnimStart(Animation animation) {
                        needAnim = false;
                        holder.flContent_2.setVisibility(View.VISIBLE);
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (null != holder)
                            holder.flContent_2.startAnimation(animation);
                    }
                }, index_2 * DURANIMITEM);
            } else {
                holder.flContent_2.setVisibility(View.VISIBLE);
            }
            Glide.with(holder.ivCover_2.getContext()).load(books.get(index_2).getCover())
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                    .placeholder(R.drawable.img_cover_default).into(holder.ivCover_2);
            holder.tvName_2.setText(books.get(index_2).getTitle());
            if (books.get(index_2).getIsUpdate()){
                holder.tvUpdateNotify_2.setVisibility(View.VISIBLE);
            }else {
                holder.tvUpdateNotify_2.setVisibility(View.GONE);
            }
            holder.ibContent_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onClick(books.get(index_2), index_2);
                }
            });
            holder.ibContent_2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        if (itemClickListener != null)
                            itemClickListener.onLongClick(holder.ivCover_2, books.get(index_2), index_2);
                        return true;
                    } else
                        return false;
                }
            });

            final int index_3 = index_2 + 1;
            if (index_3 < books.size()) {
                if (needAnim) {
                    final Animation animation = AnimationUtils.loadAnimation(holder.flContent_3.getContext(), R.anim.anim_bookshelf_item);
                    animation.setAnimationListener(new AnimatontStartListener() {
                        @Override
                        void onAnimStart(Animation animation) {
                            needAnim = false;
                            holder.flContent_3.setVisibility(View.VISIBLE);
                        }
                    });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null != holder)
                                holder.flContent_3.startAnimation(animation);
                        }
                    }, index_3 * DURANIMITEM);
                } else {
                    holder.flContent_3.setVisibility(View.VISIBLE);
                }
                Glide.with(holder.ivCover_3.getContext()).load(books.get(index_3).getCover()).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover_3);
                holder.tvName_3.setText(books.get(index_3).getTitle());
                if (books.get(index_3).getIsUpdate()){
                    holder.tvUpdateNotify_3.setVisibility(View.VISIBLE);
                }else {
                    holder.tvUpdateNotify_3.setVisibility(View.GONE);
                }
                holder.ibContent_3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null)
                            itemClickListener.onClick(books.get(index_3), index_3);
                    }
                });
                holder.ibContent_3.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (itemClickListener != null) {
                            if (itemClickListener != null)
                                itemClickListener.onLongClick(holder.ivCover_3, books.get(index_3), index_3);
                            return true;
                        } else
                            return false;
                    }
                });
            } else {
                holder.flContent_3.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.flContent_2.setVisibility(View.INVISIBLE);
            holder.flContent_3.setVisibility(View.INVISIBLE);
        }
    }

    private void bindLastestViewHolder(final LastestViewHolder holder, final int index) {
        if (books.size() == 0) {
            holder.tvWatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != itemClickListener) {
                        itemClickListener.toSearch();
                    }
                }
            });
            holder.ivCover.setImageResource(R.drawable.img_cover_default);
            holder.flLastestTip.setVisibility(View.INVISIBLE);
            holder.tvName.setText("最近阅读的书在这里");
            holder.tvDurprogress.setText("");
            holder.llDurcursor.setVisibility(View.INVISIBLE);
            holder.mpbDurprogress.setVisibility(View.INVISIBLE);
            holder.mpbDurprogress.setProgressListener(null);
            holder.tvWatch.setText("去选书");
        } else {
            Glide.with(holder.ivCover.getContext()).load(books.get(index).getCover()).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop().placeholder(R.drawable.img_cover_default).into(holder.ivCover);

            holder.flLastestTip.setVisibility(View.VISIBLE);

            holder.tvName.setText(String.format(holder.tvName.getContext().getString(R.string.tv_book_name), books.get(index).getTitle()));
            holder.tvDurprogress.setText(String.format(holder.tvDurprogress.getContext().getString(R.string.tv_read_durprogress), BookRepository.getInstance().getRecordBookChapterTitle(books.get(index))));

            holder.llDurcursor.setVisibility(View.VISIBLE);
            holder.mpbDurprogress.setVisibility(View.VISIBLE);
            List<BookChapterBean> bookChapterBeans = DaoDbHelper.getInstance().getSession().getBookChapterBeanDao().queryBuilder()
                    .where(BookChapterBeanDao.Properties.BookId.eq(books.get(index).get_id())).list();
            if (bookChapterBeans != null) {
                holder.mpbDurprogress.setMaxProgress(bookChapterBeans.size());
            }
            BookRecordBean bookRecordBean = BookRepository.getInstance().getBookRecord(books.get(index).get_id());
            int currentCharterCount = 0;
            if (bookRecordBean != null) {
                currentCharterCount = bookRecordBean.getChapter();
            }
            float speed = currentCharterCount * 1.0f / 100;

            holder.mpbDurprogress.setSpeed(speed <= 0 ? 1 : speed);
            holder.mpbDurprogress.setProgressListener(new OnProgressListener() {
                @Override
                public void moveStartProgress(float dur) {

                }

                @Override
                public void durProgressChange(float dur) {
                    float rate = dur / holder.mpbDurprogress.getMaxProgress();
                    holder.llDurcursor.setPadding((int) (holder.mpbDurprogress.getMeasuredWidth() * rate), 0, 0, 0);
                }

                @Override
                public void moveStopProgress(float dur) {

                }

                @Override
                public void setDurProgress(float dur) {

                }
            });
            if (needAnim) {
                holder.mpbDurprogress.setDurProgressWithAnim(currentCharterCount);
            } else {
                holder.mpbDurprogress.setDurProgress(currentCharterCount);
            }

            if (books.get(index).getIsUpdate()){
                holder.tvUpdateNotifyLast.setVisibility(View.VISIBLE);
            }else {
                holder.tvUpdateNotifyLast.setVisibility(View.GONE);
            }
            holder.tvWatch.setText("继续阅读");
            holder.tvWatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != itemClickListener) {
                        itemClickListener.onClick(books.get(index), index);
                    }
                }
            });
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public Boolean getNeedAnim() {
        return needAnim;
    }

    public void setNeedAnim(Boolean needAnim) {
        this.needAnim = needAnim;
    }

    class LastestViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        FrameLayout flLastestTip;
        AutofitTextView tvName;
        AutofitTextView tvDurprogress;
        LinearLayout llDurcursor;
        MHorProgressBar mpbDurprogress;
        /**
         * 去选书
         */
        TextView tvWatch;
        //最新一本书更新提示
        TextView tvUpdateNotifyLast;

        public LastestViewHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            flLastestTip = (FrameLayout) itemView.findViewById(R.id.fl_lastest_tip);
            tvName = (AutofitTextView) itemView.findViewById(R.id.tv_name);
            tvDurprogress = (AutofitTextView) itemView.findViewById(R.id.tv_durprogress);
            llDurcursor = (LinearLayout) itemView.findViewById(R.id.ll_durcursor);
            mpbDurprogress = (MHorProgressBar) itemView.findViewById(R.id.mpb_durprogress);
            tvWatch = (TextView) itemView.findViewById(R.id.tv_watch);
            tvUpdateNotifyLast=(TextView)itemView.findViewById(R.id.tv_update_notify_last);
        }
    }

    class OtherViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flContent_1;
        ImageView ivCover_1;
        AutofitTextView tvName_1;
        ImageButton ibContent_1;
        TextView tvUpdateNotify_1;

        FrameLayout flContent_2;
        ImageView ivCover_2;
        AutofitTextView tvName_2;
        ImageButton ibContent_2;
        TextView tvUpdateNotify_2;

        FrameLayout flContent_3;
        ImageView ivCover_3;
        AutofitTextView tvName_3;
        ImageButton ibContent_3;
        TextView tvUpdateNotify_3;

        public OtherViewHolder(View itemView) {
            super(itemView);
            flContent_1 = (FrameLayout) itemView.findViewById(R.id.fl_content_1);
            ivCover_1 = (ImageView) itemView.findViewById(R.id.iv_cover_1);
            tvName_1 = (AutofitTextView) itemView.findViewById(R.id.tv_name_1);
            ibContent_1 = (ImageButton) itemView.findViewById(R.id.ib_content_1);
            tvUpdateNotify_1=(TextView) itemView.findViewById(R.id.tv_update_notify_1);

            flContent_2 = (FrameLayout) itemView.findViewById(R.id.fl_content_2);
            ivCover_2 = (ImageView) itemView.findViewById(R.id.iv_cover_2);
            tvName_2 = (AutofitTextView) itemView.findViewById(R.id.tv_name_2);
            ibContent_2 = (ImageButton) itemView.findViewById(R.id.ib_content_2);
            tvUpdateNotify_2=(TextView) itemView.findViewById(R.id.tv_update_notify_2);

            flContent_3 = (FrameLayout) itemView.findViewById(R.id.fl_content_3);
            ivCover_3 = (ImageView) itemView.findViewById(R.id.iv_cover_3);
            tvName_3 = (AutofitTextView) itemView.findViewById(R.id.tv_name_3);
            ibContent_3 = (ImageButton) itemView.findViewById(R.id.ib_content_3);
            tvUpdateNotify_3=(TextView) itemView.findViewById(R.id.tv_update_notify_3);
        }
    }

    abstract class AnimatontStartListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            onAnimStart(animation);
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        abstract void onAnimStart(Animation animation);
    }

    /**
     * 修改数据源
     * @param newDatas
     */
    public synchronized void replaceAll(List<CollBookBean> newDatas) {
        books.clear();
        if (null != newDatas && newDatas.size() > 0) {
            books.addAll(newDatas);
        }
        order();

        notifyDataSetChanged();
    }

    /**
     * 基于最后读书时间对图书进行重新排序
     */
    private void order() {
        if (books != null && books.size() > 0) {
            for (int i = 0; i < books.size(); i++) {
                int temp = i;
                for (int j = i + 1; j < books.size(); j++) {
                    //基于最后读书时间进行对图书进行重新排序
                    long tempTime = Objects.requireNonNull(StringUtils.convertData(books.get(temp).getLastRead(), Constant.FORMAT_BOOK_DATE)).getTime();
                    Logger.d("tempTime" + books.get(temp).getTitle() + ":" + books.get(temp).getLastRead() + ":long:" + tempTime);
                    long jTime = Objects.requireNonNull(StringUtils.convertData(books.get(j).getLastRead(), Constant.FORMAT_BOOK_DATE)).getTime();
                    Logger.d("jTime" + books.get(j).getTitle() + ":" + books.get(j).getLastRead() + ":long:" + jTime);
                    if (tempTime < jTime) {
                        temp = j;
                    }
                }
                CollBookBean tempBookShelfBean = books.get(i);
                books.set(i, books.get(temp));
                books.set(temp, tempBookShelfBean);
            }
        }
    }

    public List<CollBookBean> getBooks() {
        return books;
    }
}