package com.yan.haha.com.yan.haha.adapter;

import android.animation.Animator;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.yan.haha.BrainRiddle;
import com.yan.haha.R;
import com.yan.haha.com.yan.haha.utils.Utils;

import java.util.ArrayList;

public class BrainRiddleAdapter extends RecyclerView.Adapter<BrainRiddleAdapter.ViewHolder> {
    private ArrayList<BrainRiddle> mBrainData = new ArrayList<BrainRiddle>();
    private ArrayList<ViewHolder> mHolderList = new ArrayList<ViewHolder>();

    private static final int CIRCULAR_REVEAL_DURATION = 500;
    private static final int RUN_UP_ANI_DURATION = 700;
    private static final int RUN_UP_ANI_TIME_GAP = 200;
    private int mGoUpDuration = RUN_UP_ANI_DURATION;

    private final static int MSG_ID_RESET_GO_UP_ANI_STATE = 0;
    private int mLastBindPosition = -1;

    private int mMenuItemHeight = -1;

    private int mDelPosMark = -1;

    private View mPreExpandedView = null;
    private int mCurrExpandedPosition = -1;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup mLayoutView = null;

        public ViewHolder(ViewGroup v) {
            super(v);
            mLayoutView = v;
        }
    }

    public BrainRiddleAdapter() {
        super();
    }

    public BrainRiddleAdapter(ArrayList<BrainRiddle> brainList) {
        this();
        mBrainData = brainList;
    }

    public void setBrainDataList(ArrayList<BrainRiddle> brainList) {
        mBrainData = brainList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.brain_riddle_list_item, parent, false);

        ViewHolder vh = new ViewHolder((ViewGroup) v);
        return vh;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ID_RESET_GO_UP_ANI_STATE:
                    mGoUpDuration = RUN_UP_ANI_DURATION;
                    mDelPosMark = -1;
                    break;
            }
        }
    };

    private void runUpAnimation(final View view, int position) {
        if (position > mLastBindPosition) {
            mLastBindPosition = position;
            if (mDelPosMark >= 0) {
                view.setTranslationY(mMenuItemHeight);
            } else {
                view.setTranslationY(Utils.getScreenHeight());
            }
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.0f))
                    .setDuration(mGoUpDuration)
                    .start();
            mGoUpDuration += 100;
            if (mGoUpDuration >= RUN_UP_ANI_DURATION + RUN_UP_ANI_TIME_GAP * 10) {
                mGoUpDuration = RUN_UP_ANI_DURATION + RUN_UP_ANI_TIME_GAP * 10;
            }
            mHandler.removeMessages(MSG_ID_RESET_GO_UP_ANI_STATE);
            mHandler.sendEmptyMessageDelayed(MSG_ID_RESET_GO_UP_ANI_STATE, mGoUpDuration + 50);
        }
    }

    private void sweepAnimation(View view, final Runnable finishCallback) {
        view.setTranslationX(0);
        view.animate()
                .translationX(Utils.getScreenWidth())
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(RUN_UP_ANI_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        notifyDataSetChanged();
                        if (finishCallback != null) {
                            finishCallback.run();
                        }
                    }

                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }

    private void expandView(final View view, int width, int height) {
        if (Build.VERSION.SDK_INT >= 21 && view.isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    view, width / 2, height / 2, 0, width);
            anim.setDuration(CIRCULAR_REVEAL_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                public void onAnimationEnd(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void collapseView(final View view, int width, int height) {
        if (Build.VERSION.SDK_INT >= 21 && view.isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    view, width / 2, height / 2, width, 0);
            anim.setDuration(CIRCULAR_REVEAL_DURATION);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }

                public void onAnimationStart(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.start();
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void onMenuItemClick(View item, int position) {
        View contentView = item.findViewById(R.id.brain_riddle_unexpanded);
        final View answerView = item.findViewById(R.id.brain_riddle_expanded);
        int height = contentView.getHeight();
        int width = contentView.getWidth();
        if (mPreExpandedView != null) {
            collapseView(mPreExpandedView, width, height);
        }
        if (answerView.getVisibility() == View.GONE) {
            // 显示答案
            expandView(answerView, width, height);
            mPreExpandedView = answerView;
            mCurrExpandedPosition = position;
        } else {
            // 收起答案
            collapseView(answerView, width, height);
            mPreExpandedView = null;
            mCurrExpandedPosition = -1;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ViewGroup container = holder.mLayoutView;

        // 设置 menu item 内容
        TextView question = (TextView) container.findViewById(R.id.brain_riddle_question);
        TextView answer = (TextView) container.findViewById(R.id.brain_riddle_answer);
        TextView answer2 = (TextView) container.findViewById(R.id.brain_riddle_answer_2);
        View expandView = container.findViewById(R.id.brain_riddle_expanded);

        if (position == mCurrExpandedPosition) {
            expandView.setVisibility(View.VISIBLE);
            mPreExpandedView = expandView;
        } else {
            expandView.setVisibility(View.GONE);
        }
        question.setText(mBrainData.get(position).getQuestion());
        answer.setText(mBrainData.get(position).getAnswer());
        answer2.setText(mBrainData.get(position).getAnswer());

        // 恢复删除 item 时用到的 tran x
        container.setTranslationX(0);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuItemClick(view, position);
            }
        });

        // 保存 holder
        if (position >=0 && position < mHolderList.size()) {
            mHolderList.set(position, holder);
        } else {
            mHolderList.add(position, holder);
        }

        // 获取菜单项高度
        if (mMenuItemHeight <= 0) {
            mMenuItemHeight = container.getHeight();
        }

        // 动画显示
        runUpAnimation(container, position);
    }

    @Override
    public int getItemCount() {
        if (mBrainData != null) {
            return mBrainData.size();
        } else {
            return 0;
        }
    }

    public void deleteItem(int position, Runnable finishCallback) {
        mLastBindPosition = position - 1;
        mDelPosMark = position;
        ViewHolder holder = mHolderList.get(position);
        sweepAnimation(holder.mLayoutView, finishCallback);
    }
}
