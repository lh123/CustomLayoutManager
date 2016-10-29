package com.example.liuhui.customlayoutmanager.layoutmanager;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liuhui on 2016/10/28.
 * 蜂巢布局管理器
 */

public class HiveLayoutManager extends RecyclerView.LayoutManager {

    private LayoutState mState;

    private PointF mAnchorPoint;

    private List<SparseArray<RectF>> mFloors;
    private HivePositionInfo mTempHiveInfo;

    private SparseBooleanArray mViewState;

    private float mHiveLenght;

    public HiveLayoutManager() {
        mState = new LayoutState();
        mFloors = new ArrayList<>();
        mViewState = new SparseBooleanArray();
        mTempHiveInfo = new HivePositionInfo();
        setAutoMeasureEnabled(true);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemCount = state.getItemCount();
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        mViewState.clear();
        initAnchorInfo(recycler);
        initFloors();
        initViewFrame();
        detachAndScrapAttachedViews(recycler);

        fillAndRecycler(recycler, state);
    }

    private void fillAndRecycler(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemCount = state.getItemCount();
        if (itemCount <= 0) {
            return;
        }

        checkAllFloor(itemCount);
        updateContentRect(itemCount);
        updateViewRect();

        checkViewOffset();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int position = getPosition(view);
            RectF rectF = getHiveBoundOfPosition(position);
            if (!RectF.intersects(rectF, mState.mViewRect)) {
                removeAndRecycleView(view, recycler);
                mViewState.put(position, false);
            }
        }

        for (int i = 0; i < state.getItemCount(); i++) {
            RectF rectF = getHiveBoundOfPosition(i);
            if (rectF != null && !mViewState.get(i, false) && RectF.intersects(mState.mViewRect, rectF)) {
                View view = recycler.getViewForPosition(i);
                measureChild(view, 0, 0);
                addView(view);
                mViewState.put(i, true);
                RectF rectF1 = new RectF(rectF);
                rectF1.offset(-mState.mOffsetX, -mState.mOffsetY);
                layoutDecorated(view, (int) (rectF.left - mState.mOffsetX),
                        (int) (rectF.top - mState.mOffsetY),
                        (int) (rectF.right - mState.mOffsetX),
                        (int) (rectF.bottom - mState.mOffsetY));
            }
        }
    }

    //检查View的偏移量
    private void checkViewOffset() {
        int contentWidth = (int) mState.mContentRect.width();
        int contentHeight = (int) mState.mContentRect.height();
        int windowsWidth = (int) mState.mViewRect.width();
        int windowsHeight = (int) mState.mViewRect.height();
        if (contentWidth < windowsWidth) {
            mState.mViewRect.offset(-mState.mOffsetX, 0);
            mState.mOffsetX = 0;
            mState.mLastOffsetX = 0;
        } else {
            if (mState.mContentRect.right < mState.mViewRect.right) {
                float diff = mState.mViewRect.right - mState.mContentRect.right;
                mState.mViewRect.offset(-diff, 0);
                mState.mOffsetX -= diff;
                mState.mLastOffsetX = 0;
            } else if (mState.mContentRect.left > mState.mViewRect.left) {
                float diff = mState.mContentRect.left - mState.mViewRect.left;
                mState.mViewRect.offset(diff, 0);
                mState.mOffsetX += diff;
                mState.mLastOffsetX = 0;
            }
        }
        if (contentHeight < windowsHeight) {
            if (mState.mContentRect.bottom > mState.mViewRect.bottom) {
                float diff = mState.mViewRect.bottom - mState.mContentRect.bottom;
                mState.mViewRect.offset(0, -diff);
                mState.mOffsetY -= diff;
                mState.mLastOffsetY = 0;
            }else {
                mState.mViewRect.offset(0, -mState.mOffsetY);
                mState.mOffsetY = 0;
                mState.mLastOffsetY = 0;
            }
        } else {
            if (mState.mContentRect.bottom < mState.mViewRect.bottom) {
                float diff = mState.mViewRect.bottom - mState.mContentRect.bottom;
                mState.mViewRect.offset(0, -diff);
                mState.mOffsetY -= diff;
                mState.mLastOffsetY = 0;
            } else if (mState.mContentRect.top > mState.mViewRect.top) {
                float diff = mState.mContentRect.top - mState.mViewRect.top;
                mState.mViewRect.offset(0, diff);
                mState.mOffsetY += diff;
                mState.mLastOffsetY = 0;
            }
        }
    }

    private void updateViewRect() {
        boolean widthInfinite = getWidthMode() == View.MeasureSpec.UNSPECIFIED;
        boolean heightInfinite = getHeightMode() == View.MeasureSpec.UNSPECIFIED;
        if (widthInfinite) {
            mState.mViewRect.left = mState.mContentRect.left;
            mState.mViewRect.right = mState.mContentRect.right;
        } else {
            mState.mViewRect.offset(mState.mLastOffsetX, 0);
        }
        if (heightInfinite) {
            mState.mViewRect.top = mState.mContentRect.top;
            mState.mViewRect.bottom = mState.mContentRect.bottom;
        } else {
            mState.mViewRect.offset(0, mState.mLastOffsetY);
        }
        mState.mLastOffsetX = 0;
        mState.mLastOffsetY = 0;
    }

    private void updateContentRect(int itemCount) {
        int floorCount = mFloors.size();
        calculateHiveInfoOfPosition(mTempHiveInfo, itemCount - 1);
        int floorIndex = mTempHiveInfo.mFloorIndex;
        int floorPosition = mTempHiveInfo.mPosition;
        float commonHorizontalSpace = floorCount * mHiveLenght * 2 - mHiveLenght;
        float commonVerticalSpace = (float) (mHiveLenght + floorIndex * mHiveLenght * Math.sqrt(3));
        float edgeLeftSpace = commonHorizontalSpace;
        float edgeBottomSpace = commonVerticalSpace;
        float edgeTopSpace = commonVerticalSpace;
        if (floorPosition < floorIndex) {
            edgeBottomSpace -= Math.sqrt(3) * mHiveLenght;
        }
        if (floorPosition < 3 * floorIndex) {
            edgeLeftSpace -= Math.min(3 * floorIndex - floorPosition, 2) * mHiveLenght;
        }
        if (floorPosition < 4 * floorIndex) {
            edgeTopSpace -= Math.sqrt(3) * mHiveLenght;
        }
        float x = mAnchorPoint.x;
        float y = mAnchorPoint.y;
        mState.mContentRect.set(x - edgeLeftSpace, y - edgeTopSpace, x + commonHorizontalSpace, y + edgeBottomSpace);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int willScroll = dx;
        int edgeRight = (int) mState.mContentRect.right;
        int edgeLeft = (int) mState.mContentRect.left;
        int viewRight = (int) mState.mViewRect.right;
        int viewLeft = (int) mState.mViewRect.left;
        if (viewRight - viewLeft >= edgeRight - edgeLeft) {
            return 0;
        }
        if (viewRight + willScroll > edgeRight) {
            willScroll = edgeRight - viewRight;
        } else if (viewLeft + willScroll < edgeLeft) {
            willScroll = edgeLeft - viewLeft;
        }
        mState.mOffsetX += willScroll;
        mState.mLastOffsetX = willScroll;
        offsetChildrenHorizontal(-willScroll);
        fillAndRecycler(recycler, state);
        return willScroll;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int willScroll = dy;
        int edgeTop = (int) mState.mContentRect.top;
        int edgeBottom = (int) mState.mContentRect.bottom;
        int viewTop = (int) mState.mViewRect.top;
        int viewBottom = (int) mState.mViewRect.bottom;
        if (viewBottom - viewTop >= edgeBottom - edgeTop) {
            return 0;
        }

        if (viewTop + willScroll < edgeTop) {
            willScroll = edgeTop - viewTop;
        } else if (viewBottom + willScroll > edgeBottom) {
            willScroll = edgeBottom - viewBottom;
        }
        mState.mOffsetY += willScroll;
        mState.mLastOffsetY = willScroll;
        offsetChildrenVertical(-willScroll);
        fillAndRecycler(recycler, state);
        return willScroll;
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getHorizontallSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    //检查每一层
    private void checkAllFloor(int itemCount) {
        if (mFloors.size() > 1) {
            calculateHiveInfoOfPosition(mTempHiveInfo, itemCount - 1);
            int remaindIndex = mTempHiveInfo.mFloorIndex;
            for (int i = remaindIndex + 1; i < mFloors.size(); i++) {//移除多余的层
                mFloors.remove(i);
            }
        }
        for (int i = 0; i < itemCount; i++) {
            calculateHiveInfoOfPosition(mTempHiveInfo, i);
            checkFloor(mTempHiveInfo.mFloorIndex);
        }
    }

    private void checkFloor(int index) {
        if (mFloors.size() <= index) {
            for (int i = mFloors.size(); i <= index; i++) {
                SparseArray<RectF> preFloor = mFloors.get(i - 1);
                mFloors.add(calculateNextFloor(preFloor, i));
            }
        }
    }

    private SparseArray<RectF> calculateNextFloor(SparseArray<RectF> preFloor, int floorIndex) {
        SparseArray<RectF> hives = new SparseArray<>();
        RectF preRect = calculateNextRect(preFloor.get(0), 5);
        int index = 1;
        hives.put(0, preRect);
        for (int i = 1; i <= 6; i++) {
            int count = floorIndex;
            if (i == 6) {
                count--;
            }
            for (int j = 0; j < count; j++) {
                RectF temp = calculateNextRect(preRect, i);
                hives.put(index, temp);
                index++;
                preRect = temp;
            }
        }
        return hives;
    }

    private RectF calculateNextRect(RectF rectF, int mode) {
        RectF nextRect = new RectF(rectF);
        float offsetX = rectF.width() / 2;
        float offsetY = (float) (offsetX * Math.sqrt(3));
        switch (mode) {
            case 1://左下
                nextRect.offset(-offsetX, offsetY);
                break;
            case 2://左
                nextRect.offset(-rectF.width(), 0);
                break;
            case 3://左上
                nextRect.offset(-offsetX, -offsetY);
                break;
            case 4://右上
                nextRect.offset(offsetX, -offsetY);
                break;
            case 5://右
                nextRect.offset(rectF.width(), 0);
                break;
            case 6://右下
                nextRect.offset(offsetX, offsetY);
                break;
        }
        return nextRect;
    }


    //根据当前位置计算所在层数和在这层中的位置
    private void calculateHiveInfoOfPosition(HivePositionInfo info, int position) {
        if (position <= 0) {
            info.mFloorIndex = 0;
            info.mPosition = 0;
        } else {
            int i = 1;
            int number = 6;
            while (position - number > 0) {
                position -= number;
                i++;
                number = getCountOfFloor(i);
            }
            info.mPosition = position - 1;
            info.mFloorIndex = i;
        }
    }

    //得到每层的Hive数量
    private int getCountOfFloor(int floor) {
        if (floor < 0) {
            return 0;
        } else if (floor == 0) {
            return 1;
        } else {
            return floor * 6;
        }
    }

    private RectF getHiveBoundOfPosition(int position) {
        calculateHiveInfoOfPosition(mTempHiveInfo, position);
        if (mTempHiveInfo.mFloorIndex < mFloors.size()) {
            return mFloors.get(mTempHiveInfo.mFloorIndex).get(mTempHiveInfo.mPosition);
        }
        return null;
    }

    //初始化锚点位置
    private void initAnchorInfo(RecyclerView.Recycler recycler) {
        if (mAnchorPoint == null) {
            mAnchorPoint = new PointF();
            mAnchorPoint.set(getHorizontallSpace() / 2f, getVerticalSpace() / 2f);
            View view = recycler.getViewForPosition(0);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            mHiveLenght = getDecoratedMeasuredHeight(view) / 2f;
        }
    }

    //初始化Floor
    private void initFloors() {
        if (mFloors.size() == 0) {
            SparseArray<RectF> hives = new SparseArray<>();
            float left = mAnchorPoint.x - mHiveLenght;
            float top = mAnchorPoint.y - mHiveLenght;
            float right = mAnchorPoint.x + mHiveLenght;
            float bottom = mAnchorPoint.y + mHiveLenght;
            RectF rectF = new RectF(left, top, right, bottom);
            hives.put(0, rectF);
            mFloors.add(hives);
        }
    }

    private void initViewFrame() {
        mState.mViewRect.set(mState.mOffsetX,
                mState.mOffsetY,
                mState.mOffsetX + getHorizontallSpace(),
                mState.mOffsetY + getVerticalSpace());
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return (int) mState.mViewRect.width();
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return (int) mState.mContentRect.width();
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return (int) (mState.mViewRect.left - mState.mContentRect.left);
    }


    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return (int) mState.mViewRect.height();
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return (int) mState.mContentRect.height();
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return (int) (mState.mViewRect.top - mState.mContentRect.top);
    }

    //LayoutState
    private class LayoutState {

        int mOffsetX;
        int mOffsetY;

        int mLastOffsetX;
        int mLastOffsetY;

        RectF mContentRect = new RectF();//内容的总区域
        RectF mViewRect = new RectF();//显示的区域
    }


    private class HivePositionInfo {
        int mPosition;
        int mFloorIndex;
    }
}
