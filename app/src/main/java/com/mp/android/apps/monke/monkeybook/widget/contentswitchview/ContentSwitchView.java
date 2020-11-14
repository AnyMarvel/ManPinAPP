package com.mp.android.apps.monke.monkeybook.widget.contentswitchview;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.mp.android.apps.monke.monkeybook.ReadBookControl;
import com.mp.android.apps.monke.monkeybook.utils.DensityUtil;
import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.contentAnimtion.ContentPageStatus;
import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.contentAnimtion.ConverPageAnim;
import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.contentAnimtion.MyPageAnimation;

import java.util.ArrayList;
import java.util.List;

public class ContentSwitchView extends FrameLayout implements BookContentView.SetDataListener, MyPageAnimation.onLayoutStatus {
    private final int screenWidth = DensityUtil.getWindowWidth(getContext());
    private final int screenHeight = DensityUtil.getWindowHeight(getContext());

    private int state = ContentPageStatus.NONE.getStatus();    //0是有上一页   也有下一页 ;  2是只有下一页  ；1是只有上一页;-1是没有上一页 也没有下一页；

    private BookContentView durPageView;

    private List<BookContentView> viewContents;

    public interface OnBookReadInitListener {
        public void success();
    }

    private OnBookReadInitListener bookReadInitListener;

    public ContentSwitchView(Context context) {
        super(context);
        init();
    }

    public ContentSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContentSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ContentSwitchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private ReadBookControl readBookControl;
    ConverPageAnim myConverPageAnim;

    private void init() {
        readBookControl = ReadBookControl.getInstance();


        durPageView = new BookContentView(getContext());
        durPageView.setReadBookControl(readBookControl);

        viewContents = new ArrayList<>();
        viewContents.add(durPageView);

        addView(durPageView);
        myConverPageAnim = new ConverPageAnim(getContext());

    }

    /**
     * 初始化读书内容页
     *
     * @param bookReadInitListener
     */
    public void bookReadInit(OnBookReadInitListener bookReadInitListener) {
        this.bookReadInitListener = bookReadInitListener;
        durPageView.getTvContent().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (bookReadInitListener != null) {
                    bookReadInitListener.success();


                }
                durPageView.getTvContent().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * 开始加载
     */
    public void startLoading() {
        if (durPageView != null) {
            int height = durPageView.getTvContent().getHeight();
            if (height > 0) {
                if (loadDataListener != null && durHeight != height) {
                    durHeight = height;
                    loadDataListener.initData(durPageView.getLineCount(height));
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float startX = -1;
    private float startY = -1;
    // 唤醒菜单的区域
    private RectF mCenterRect = null;
    // 手势是否在移动
    private boolean isMove = false;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                isMove = false;

                myConverPageAnim.onTouchEvent(event, this, viewContents, ContentSwitchView.this, loadDataListener);
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(startX - event.getX()) > slop || Math.abs(startY - event.getY()) > slop;
                }
                // 如果滑动了，则进行翻页。
                if (isMove) {

                    myConverPageAnim.onTouchEvent(event, this, viewContents, ContentSwitchView.this, loadDataListener);
                }
                break;
            case MotionEvent.ACTION_CANCEL:  //小米8长按传送门会引导手势进入action_cancel
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(screenWidth / 5, screenHeight / 3,
                                screenWidth * 4 / 5, screenHeight * 2 / 3);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (loadDataListener != null)
                            loadDataListener.showMenu();
                        return true;
                    }
                }

                myConverPageAnim.onTouchEvent(event, this, viewContents, ContentSwitchView.this, loadDataListener);
                break;
            default:
                break;

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (viewContents.size() > 0) {
            myConverPageAnim.onLayout(changed, left, top, right, bottom, this, viewContents);
        } else {
            super.onLayout(changed, left, top, right, bottom);
        }
    }

    public void setInitData(int durChapterIndex, int chapterAll, int durPageIndex) {
//        updateOtherPage(durChapterIndex, chapterAll, durPageIndex, -1);
        durPageView.setLoadDataListener(loadDataListener, this);
        durPageView.loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex) : "", durChapterIndex, chapterAll, durPageIndex);

        if (loadDataListener != null)
            loadDataListener.updateProgress(durPageView.getDurChapterIndex(), durPageView.getDurPageIndex());
    }

    public void setState(int state) {
        this.state = state;
    }

    /**
     * 加载完当前页面数据后,更新后期加载数据
     *
     * @param durChapterIndex 当前章节数
     * @param chapterAll      总章节数
     * @param durPageIndex    当前章节的当前页面数
     * @param pageAll         当前章节划分的总页数
     */
    private void updateOtherPage(int durChapterIndex, int chapterAll, int durPageIndex, int pageAll) {
        if (chapterAll > 1 || pageAll > 1) {
            if ((durChapterIndex == 0 && pageAll == -1) || (durChapterIndex == 0 && durPageIndex == 0 && pageAll != -1)) {
                //ONLYNEXT
                addNextPage(durChapterIndex, chapterAll, durPageIndex, pageAll);
                if (onlyPre() || preAndNext()) {
                    this.removeView(viewContents.get(0));
                    viewContents.remove(0);
                }
                state = ContentPageStatus.ONLYNEXT.getStatus();
            } else if ((durChapterIndex == chapterAll - 1 && pageAll == -1) || (durChapterIndex == chapterAll - 1 && durPageIndex == pageAll - 1 && pageAll != -1)) {
                //ONLYPRE
                addPrePage(durChapterIndex, chapterAll, durPageIndex, pageAll);
                if (onlyNext() || preAndNext()) {
                    this.removeView(viewContents.get(2));
                    viewContents.remove(2);
                }
                state = ContentPageStatus.ONLYPRE.getStatus();
            } else {
                //PREANDNEXT
                addNextPage(durChapterIndex, chapterAll, durPageIndex, pageAll);
                addPrePage(durChapterIndex, chapterAll, durPageIndex, pageAll);
                state = ContentPageStatus.PREANDNEXT.getStatus();
            }
        } else {
            //NONE
            if (onlyPre()) {
                this.removeView(viewContents.get(0));
                viewContents.remove(0);
            } else if (onlyNext()) {
                this.removeView(viewContents.get(1));
                viewContents.remove(1);
            } else if (preAndNext()) {
                this.removeView(viewContents.get(0));
                this.removeView(viewContents.get(2));
                viewContents.remove(2);
                viewContents.remove(0);
            }
            state = ContentPageStatus.NONE.getStatus();
        }
    }

    /**
     * 加载下一页内容
     *
     * @param durChapterIndex 当前章节数
     * @param chapterAll      总章节数
     * @param durPageIndex    当前章节的当前页面数
     * @param pageAll         当前章节划分的总页数
     */
    public void addNextPage(int durChapterIndex, int chapterAll, int durPageIndex, int pageAll) {
        if (onlyNext() || preAndNext()) {
            int temp = (onlyNext() ? 1 : 2);
            if (pageAll > 0 && durPageIndex >= 0 && durPageIndex < pageAll - 1)
                viewContents.get(temp).loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex) : "", durChapterIndex, chapterAll, durPageIndex + 1);
            else
                viewContents.get(temp).loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex + 1) : "", durChapterIndex + 1, chapterAll, BookContentView.DURPAGEINDEXBEGIN);
        } else if (onlyPre() || onlyOne()) {
            BookContentView next = new BookContentView(getContext());
            next.setReadBookControl(readBookControl);
            next.setLoadDataListener(loadDataListener, this);
            if (pageAll > 0 && durPageIndex >= 0 && durPageIndex < pageAll - 1)
                next.loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex) : "", durChapterIndex, chapterAll, durPageIndex + 1);
            else
                next.loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex + 1) : "", durChapterIndex + 1, chapterAll, BookContentView.DURPAGEINDEXBEGIN);
            viewContents.add(next);
            this.addView(next, 0);
        }
    }

    /**
     * 加载上一页内容
     *
     * @param durChapterIndex 当前章节数
     * @param chapterAll      总章节数
     * @param durPageIndex    当前章节的当前页面数
     * @param pageAll         当前章节划分的总页数
     */
    public void addPrePage(int durChapterIndex, int chapterAll, int durPageIndex, int pageAll) {
        if (onlyNext() || onlyOne()) {
            BookContentView pre = new BookContentView(getContext());
            pre.setReadBookControl(readBookControl);
            pre.setLoadDataListener(loadDataListener, this);
            if (pageAll > 0 && durPageIndex >= 0 && durPageIndex > 0)
                pre.loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex) : "", durChapterIndex, chapterAll, durPageIndex - 1);
            else
                pre.loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex - 1) : "", durChapterIndex - 1, chapterAll, BookContentView.DURPAGEINDEXEND);
            viewContents.add(0, pre);
            this.addView(pre);
        } else if (onlyPre() || preAndNext()) {
            if (pageAll > 0 && durPageIndex >= 0 && durPageIndex > 0)
                viewContents.get(0).loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex) : "", durChapterIndex, chapterAll, durPageIndex - 1);
            else
                viewContents.get(0).loadData(null != loadDataListener ? loadDataListener.getChapterTitle(durChapterIndex - 1) : "", durChapterIndex - 1, chapterAll, BookContentView.DURPAGEINDEXEND);
        }
    }

    /**
     * 当前页面加载完成后,向后预加载一页数据
     * 停止方式为判断当前 BookContentView是否是当前界面展示View. 只往后加载一页数据
     *
     * @param bookContentView 当前展示View
     * @param durChapterIndex 当前章节数
     * @param chapterAll      总章节数
     * @param durPageIndex    当前第几页
     * @param pageAll         总页数
     * @param fromPageIndex   从第几页开始
     */
    @Override
    public void setDataFinish(BookContentView bookContentView, int durChapterIndex, int chapterAll, int durPageIndex, int pageAll, int fromPageIndex) {
        if (null != getDurContentView() && bookContentView == getDurContentView() && chapterAll > 0 && pageAll > 0) {
            updateOtherPage(durChapterIndex, chapterAll, durPageIndex, pageAll);
        }
    }

    public interface LoadDataListener {
        public void loaddata(BookContentView bookContentView, long tag, int chapterIndex, int pageIndex);

        public void updateProgress(int chapterIndex, int pageIndex);

        public String getChapterTitle(int chapterIndex);

        public void initData(int lineCount);

        public void showMenu();
    }

    private LoadDataListener loadDataListener;

    public LoadDataListener getLoadDataListener() {
        return loadDataListener;
    }

    public void setLoadDataListener(LoadDataListener loadDataListener) {
        this.loadDataListener = loadDataListener;
    }

    public BookContentView getDurContentView() {
        return durPageView;
    }


    private int durHeight = 0;

    public Paint getTextPaint() {
        return durPageView.getTvContent().getPaint();
    }

    public int getContentWidth() {
        return durPageView.getTvContent().getWidth();
    }

    public void changeBg() {
        for (BookContentView item : viewContents) {
            item.setBg(readBookControl);
        }
    }

    public void changeTextSize() {
        for (BookContentView item : viewContents) {
            item.setTextKind(readBookControl);
        }
        loadDataListener.initData(durPageView.getLineCount(durHeight));
    }


    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (readBookControl.getCanKeyTurn() && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        } else if (readBookControl.getCanKeyTurn() && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return false;
    }

    public OnBookReadInitListener getBookReadInitListener() {
        return bookReadInitListener;
    }

    public void setBookReadInitListener(OnBookReadInitListener bookReadInitListener) {
        this.bookReadInitListener = bookReadInitListener;
    }

    public void loadError() {
        if (durPageView != null) {
            durPageView.loadError();
        }
    }

    @Override
    public boolean preAndNext() {
        return state == ContentPageStatus.PREANDNEXT.getStatus() && viewContents.size() >= 3;
    }

    @Override
    public boolean onlyPre() {
        return state == ContentPageStatus.ONLYPRE.getStatus() && viewContents.size() >= 2;
    }

    @Override
    public boolean onlyNext() {
        return state == ContentPageStatus.ONLYNEXT.getStatus() && viewContents.size() >= 2;
    }

    @Override
    public boolean onlyOne() {
        return state == ContentPageStatus.NONE.getStatus() && viewContents.size() >= 1;
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public void onAnimationEnd(Animator animation, int orderXY) {
        BookContentView durPageView;
        if (orderXY == 0) {
            //翻向前一页
            durPageView = viewContents.get(0);
            if (preAndNext()) {
                removeView(viewContents.get(viewContents.size() - 1));
                viewContents.remove(viewContents.size() - 1);
            }
            setState(ContentPageStatus.ONLYNEXT.getStatus());

            if (durPageView.getDurChapterIndex() - 1 >= 0 || durPageView.getDurPageIndex() - 1 >= 0) {
                addPrePage(durPageView.getDurChapterIndex(), durPageView.getChapterAll(), durPageView.getDurPageIndex(), durPageView.getPageAll());
                if (onlyOne())
                    setState(ContentPageStatus.ONLYPRE.getStatus());
                else
                    setState(ContentPageStatus.PREANDNEXT.getStatus());

            }
        } else {
            //翻向后一页
            if (onlyNext()) {
                durPageView = viewContents.get(1);
            } else {
                durPageView = viewContents.get(2);
                removeView(viewContents.get(0));
                viewContents.remove(0);
            }
            setState(ContentPageStatus.ONLYPRE.getStatus());

            if (durPageView.getDurChapterIndex() + 1 <= durPageView.getChapterAll() - 1 || durPageView.getDurPageIndex() + 1 <= durPageView.getPageAll() - 1) {
                addNextPage(durPageView.getDurChapterIndex(), durPageView.getChapterAll(), durPageView.getDurPageIndex(), durPageView.getPageAll());
                if (onlyOne())
                    setState(ContentPageStatus.ONLYNEXT.getStatus());
                else
                    setState(ContentPageStatus.PREANDNEXT.getStatus());
            }
        }
        if (loadDataListener != null)
            loadDataListener.updateProgress(durPageView.getDurChapterIndex(), durPageView.getDurPageIndex());
    }

}