package com.example.liuhui.customlayoutmanager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liuhui on 2016/10/29.
 */

public class ImgBean implements Parcelable {
    /*"id":"276",
    "link":"http://s.tu.ihuan.me/bgc/16-02-23.png",
            "source":"小幻bing抓取",
            "sourcelink":"http://www.bing.com",
            "time":"2016-02-23",
            "describe":"中国，陕西省，华山 (© Tao Ming/REX Shutterstock)",
            "title":"雪中华山",
            "d":"华山被认为是五岳当中最险峻的一座。怪石嶙峋，奇峰林立，每一条步道都让人屏住呼吸，每一座峰顶都有迷人美景。入冬后，雪中的华山更是让人迷醉，恍若置身仙境。",
            "date":"February 23",
            "attribute":"中国，陕西,华山"*/

    private String id;
    private String link;
    private String title;
    private String describe;

    private ImgBean(Parcel in) {
        id = in.readString();
        link = in.readString();
        title = in.readString();
        describe = in.readString();
    }

    public static final Creator<ImgBean> CREATOR = new Creator<ImgBean>() {
        @Override
        public ImgBean createFromParcel(Parcel in) {
            return new ImgBean(in);
        }

        @Override
        public ImgBean[] newArray(int size) {
            return new ImgBean[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(link);
        parcel.writeString(title);
        parcel.writeString(describe);
    }
}
