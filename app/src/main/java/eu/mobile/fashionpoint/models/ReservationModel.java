package eu.mobile.fashionpoint.models;

import java.util.Date;

public class ReservationModel {

    private long    mId;
    private Date    mStartDate;
    private Date    mEndDate;
    private String  mClientName;
    private String  mService;
    private String  mSpecialist;
    private String  mRoom;
    private String  mUrl;
    private String  mColor;
    private Boolean mIsRead;

    public ReservationModel(){}

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public Date getmStartDate() {
        return mStartDate;
    }

    public void setmStartDate(Date mStartDate) {
        this.mStartDate = mStartDate;
    }

    public Date getmEndDate() {
        return mEndDate;
    }

    public void setmEndDate(Date mEndDate) {
        this.mEndDate = mEndDate;
    }

    public String getmClientName() {
        return mClientName;
    }

    public void setmClientName(String mClientName) {
        this.mClientName = mClientName;
    }

    public String getmService() {
        return mService;
    }

    public void setmService(String mService) {
        this.mService = mService;
    }

    public String getmSpecialist() {
        return mSpecialist;
    }

    public void setmSpecialist(String mSpecialist) {
        this.mSpecialist = mSpecialist;
    }

    public String getmRoom() {
        return mRoom;
    }

    public void setmRoom(String mRoom) {
        this.mRoom = mRoom;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public Boolean getmIsRead() {
        return mIsRead;
    }

    public void setmIsRead(Boolean mIsRead) {
        this.mIsRead = mIsRead;
    }

    public String getmColor() {
        return mColor;
    }

    public void setmColor(String mColor) {
        this.mColor = mColor;
    }
}
