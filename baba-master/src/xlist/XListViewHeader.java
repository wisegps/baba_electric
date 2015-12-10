package xlist;

import com.wise.baba.R;
import com.wise.baba.ui.widget.WaitLinearLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XListViewHeader extends LinearLayout {
	
	public WaitLinearLayout ll_top_wait;
    private LinearLayout mContainer;
    private TextView mHintTextView;
    private int mState = STATE_NORMAL;

    //dasdfsdf
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    
    private final int ROTATE_ANIM_DURATION = 180;
    
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    Context mContext;
    public XListViewHeader(Context context) {
        super(context);
        mContext = context;
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public XListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        //初始情况，设置下拉刷新view高度为0   
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.xlistview_header, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

        if (isInEditMode()) { return; }
        mHintTextView = (TextView)findViewById(R.id.xlistview_header_hint_textview);
        ll_top_wait = (WaitLinearLayout)findViewById(R.id.ll_wait);
        
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    public void setState(int state) {
        if (state == mState) return ;
        
        if (state == STATE_REFRESHING) {    // 显示进度
        	ll_top_wait.startWheel();
//            mArrowImageView.clearAnimation();
//            mArrowImageView.setVisibility(View.VISIBLE);
//            Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tip_fast);  
//            LinearInterpolator lin = new LinearInterpolator();  
//            operatingAnim.setInterpolator(lin); 
//            if (operatingAnim != null) {  
//                mArrowImageView.startAnimation(operatingAnim);  
//            }
        } else {    // 显示箭头图片
//            mArrowImageView.setVisibility(View.INVISIBLE);
//            mArrowImageView.clearAnimation();
        }
        
        switch(state){
        case STATE_NORMAL:
            if (mState == STATE_READY) {
                //mArrowImageView.startAnimation(mRotateDownAnim);
            }
            if (mState == STATE_REFRESHING) {
                //mArrowImageView.clearAnimation();
            }
            mHintTextView.setText(R.string.xlistview_header_hint_normal);
            break;
        case STATE_READY:
            if (mState != STATE_READY) {
                //mArrowImageView.clearAnimation();
                //mArrowImageView.startAnimation(mRotateUpAnim);
                mHintTextView.setText(R.string.xlistview_header_hint_ready);
            }
            break;
        case STATE_REFRESHING:
            mHintTextView.setText(R.string.xlistview_header_hint_loading);
            break;
            default:
        }
        
        mState = state;
    }
    
    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer
                .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return mContainer.getHeight();
    }

}