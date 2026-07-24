package com.mp.android.apps.book.model.impl;

import android.text.TextUtils;

import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.I99wxApi;
import com.mp.android.apps.book.model.IReaderBookModel;
import com.mp.android.apps.read.bean.BookChapterBean;
import com.mp.android.apps.read.bean.ChapterInfoBean;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.utils.MD5Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;

/**
 * 鸟书网(99wx) 书源解析实现。
 * 搜索：GET /read/search/?searchkey=；详情用 og meta；目录在详情页 div.listmain（跳过顶部"最新章节"重复块，正序）；
 * 正文 div#content.showtxt。全链路 GET，无验证码/无转码接口。
 */
public class Content99wxModelImpl extends MBaseModelImpl implements IReaderBookModel {

    public static final String TAG = "http://www.99wx.info";
    private static final String ORIGIN = "鸟书网";

    public static Content99wxModelImpl getInstance() {
        return new Content99wxModelImpl();
    }

    ////////////////////////////////////////// 搜索 //////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        // 该站搜索单页返回(最多100条)，无分页；翻页(page>1)直接返回空，避免重复
        if (page > 1) {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                    e.onNext(new ArrayList<SearchBookBean>());
                    e.onComplete();
                }
            });
        }
        return getRetrofitObject(TAG).create(I99wxApi.class).searchBook(content)
                .flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
                    @Override
                    public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                        return analySearchBook(s);
                    }
                });
    }

    private Observable<List<SearchBookBean>> analySearchBook(final String s) {
        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                List<SearchBookBean> books = new ArrayList<>();
                try {
                    Document doc = Jsoup.parse(s);
                    Elements boxes = doc.select("div.type_show div.bookbox");
                    for (Element box : boxes) {
                        Element nameA = box.select("h4.bookname a").first();
                        if (nameA == null) continue;
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(TAG);
                        item.setOrigin(ORIGIN);
                        item.setName(nameA.text());
                        item.setNoteUrl(absUrl(nameA.attr("href")));
                        Element img = box.select("div.bookimg img").first();
                        item.setCoverUrl(img == null ? "" : absUrl(img.attr("src")));
                        Element author = box.select("div.author").first();
                        if (author != null) item.setAuthor(author.text().replace("作者：", "").trim());
                        // 不设置分类(kind)：搜索结果项高度固定，显示分类标签会挤压下方"最新章节"，与 SoDu 源保持一致
                        Element update = box.select("div.update a").first();
                        if (update != null) item.setLastChapter(update.text());
                        books.add(item);
                    }
                } catch (Exception ex) {
                    android.util.Log.e("Content99wxModel", "search parse failed", ex);
                }
                e.onNext(books);
                e.onComplete();
            }
        });
    }

    ////////////////////////////////////////// 详情 //////////////////////////////////////////
    @Override
    public Observable<CollBookBean> getBookInfo(final CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(I99wxApi.class).getBookInfo(collBookBean.get_id())
                .flatMap(new Function<String, ObservableSource<CollBookBean>>() {
                    @Override
                    public ObservableSource<CollBookBean> apply(String s) throws Exception {
                        return analyBookInfo(s, collBookBean);
                    }
                });
    }

    private Observable<CollBookBean> analyBookInfo(final String s, final CollBookBean collBookBean) {
        return Observable.create(new ObservableOnSubscribe<CollBookBean>() {
            @Override
            public void subscribe(ObservableEmitter<CollBookBean> e) throws Exception {
                try {
                    collBookBean.setBookTag(TAG);
                    Document doc = Jsoup.parse(s);
                    String title = metaContent(doc, "og:novel:book_name");
                    if (TextUtils.isEmpty(title)) title = metaContent(doc, "og:title");
                    if (!TextUtils.isEmpty(title)) collBookBean.setTitle(title);
                    String author = metaContent(doc, "og:novel:author");
                    if (!TextUtils.isEmpty(author)) collBookBean.setAuthor(author);
                    String cover = metaContent(doc, "og:image");
                    if (!TextUtils.isEmpty(cover)) collBookBean.setCover(cover);
                    String intro = metaContent(doc, "og:description");
                    if (!TextUtils.isEmpty(intro)) collBookBean.setShortIntro(intro.trim());
                    String updated = metaContent(doc, "og:novel:update_time");
                    if (!TextUtils.isEmpty(updated)) collBookBean.setUpdated(updated);
                    String last = metaContent(doc, "og:novel:latest_chapter_name");
                    if (!TextUtils.isEmpty(last)) collBookBean.setLastChapter(last);
                    // 目录就在详情页，章节列表 URL 复用详情 URL
                    collBookBean.setBookChapterUrl(collBookBean.get_id());
                } catch (Exception ex) {
                    android.util.Log.e("Content99wxModel", "book info parse failed", ex);
                }
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }

    ////////////////////////////////////////// 目录 //////////////////////////////////////////
    @Override
    public Single<List<BookChapterBean>> getBookChapters(final CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(I99wxApi.class).getChapterLists(collBookBean.getBookChapterUrl())
                .flatMap(new Function<String, Single<List<BookChapterBean>>>() {
                    @Override
                    public Single<List<BookChapterBean>> apply(String s) throws Exception {
                        return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
                            @Override
                            public void subscribe(SingleEmitter<List<BookChapterBean>> emitter) throws Exception {
                                emitter.onSuccess(analyChapterList(s, collBookBean));
                            }
                        });
                    }
                });
    }

    private List<BookChapterBean> analyChapterList(String s, CollBookBean collBookBean) {
        List<BookChapterBean> chapters = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(s);
            Element dl = doc.select("div.listmain dl").first();
            if (dl == null) return chapters;
            boolean started = false;
            int index = 0;
            for (Element child : dl.children()) {
                String tag = child.tagName();
                if ("dt".equals(tag)) {
                    // 第一个 dt 是"最新章节"(与末尾重复的最新章节块)，跳过；之后的分卷标题起开始收录，保证从第一章正序
                    started = !child.text().contains("最新章节");
                } else if ("dd".equals(tag) && started) {
                    Element a = child.select("a").first();
                    if (a == null) continue;
                    String link = absUrl(a.attr("href"));
                    if (TextUtils.isEmpty(link)) continue;
                    BookChapterBean bean = new BookChapterBean();
                    bean.setId(MD5Utils.strToMd5By16(link));
                    bean.setTitle(a.text());
                    bean.setLink(link);
                    bean.setBookId(collBookBean.get_id());
                    bean.setUnreadble(false);
                    bean.setPosition(index++);
                    chapters.add(bean);
                }
            }
        } catch (Exception ex) {
            android.util.Log.e("Content99wxModel", "chapter list parse failed", ex);
        }
        return chapters; // 页面本身正序，无需 reverse
    }

    ////////////////////////////////////////// 正文 //////////////////////////////////////////
    @Override
    public Single<ChapterInfoBean> getChapterInfo(final String url) {
        return getRetrofitObject(TAG).create(I99wxApi.class).getChapterInfo(url)
                .flatMap(new Function<String, Single<ChapterInfoBean>>() {
                    @Override
                    public Single<ChapterInfoBean> apply(String s) throws Exception {
                        return Single.create(new SingleOnSubscribe<ChapterInfoBean>() {
                            @Override
                            public void subscribe(SingleEmitter<ChapterInfoBean> emitter) throws Exception {
                                emitter.onSuccess(analyChapterInfo(s));
                            }
                        });
                    }
                });
    }

    private ChapterInfoBean analyChapterInfo(String s) {
        ChapterInfoBean info = new ChapterInfoBean();
        try {
            Document doc = Jsoup.parse(s);
            Element el = doc.select("div#content.showtxt").first();
            if (el == null) el = doc.select("#content").first();
            if (el == null) el = doc.select("div.showtxt").first();
            if (el == null) {
                info.setBody("章节解析失败，请翻页尝试，或到我的界面联系管理员");
                return info;
            }
            el.select("script").remove();
            // 依次读取文本节点与 <br>，还原换行
            StringBuilder raw = new StringBuilder();
            for (Node node : el.childNodes()) {
                if (node instanceof TextNode) {
                    raw.append(((TextNode) node).getWholeText());
                } else if (node instanceof Element) {
                    Element e = (Element) node;
                    if ("br".equalsIgnoreCase(e.tagName())) {
                        raw.append("\n");
                    } else {
                        raw.append(e.text());
                    }
                }
            }
            StringBuilder body = new StringBuilder();
            for (String line : raw.toString().split("\n")) {
                String t = line.replace(' ', ' ').replace("&nbsp;", " ").trim();
                if (t.length() > 0) {
                    body.append("　　").append(t).append("\r\n");
                }
            }
            info.setBody(body.length() > 0 ? body.toString() : "本章内容为空");
        } catch (Exception ex) {
            android.util.Log.e("Content99wxModel", "chapter parse failed", ex);
            info.setBody("章节解析失败，请翻页尝试，或到我的界面联系管理员");
        }
        return info;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    ////////////////////////////////////////// 工具 //////////////////////////////////////////
    private String metaContent(Document doc, String property) {
        Element m = doc.select("meta[property=" + property + "]").first();
        return m == null ? "" : m.attr("content");
    }

    private String absUrl(String href) {
        if (TextUtils.isEmpty(href)) return "";
        href = href.trim();
        if (href.startsWith("http")) return href;
        if (!href.startsWith("/")) href = "/" + href;
        return TAG + href;
    }
}
