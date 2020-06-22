package at.allaboutapps.gdpr;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import at.allaboutapps.gdpr.widget.ServiceAdapter;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "DividerItem";
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private final Drawable mDivider;

    private final Rect mBounds = new Rect();

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     * {@link androidx.recyclerview.widget.LinearLayoutManager}.
     *
     * @param context Current context, it will be used to access resources.
     */
    public DividerItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        if (mDivider == null) {
            Log.w(TAG, "@android:attr/listDivider was not set in the theme used for this "
                    + "DividerItemDecoration. Please set that attribute all call setDrawable()");
        }
        a.recycle();
    }

    @Override
    public void onDraw(@NotNull Canvas c, RecyclerView parent, @NotNull RecyclerView.State state) {
        if (parent.getLayoutManager() == null || mDivider == null) {
            return;
        }
        drawVertical(c, parent);
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);

            // Find the next view. We may have to skip some items due to animations adding a second
            // ViewHolder for the same layout position
            View next = parent.getChildAt(i + 1);
            RecyclerView.ViewHolder nextHolder = parent.getChildViewHolder(next);
            while (nextHolder.getLayoutPosition() <= i) {
                i++;

                if (i + 1 >= childCount) {
                    nextHolder = null;
                    break;
                }

                next = parent.getChildAt(i + 1);
                nextHolder = parent.getChildViewHolder(next);
            }

            if (nextHolder == null) break;
            if (nextHolder.getItemViewType() != ServiceAdapter.SERVICE) {
                // draw divider between services only
                continue;
            }
            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
            final int top = bottom - mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent,
                               @NotNull RecyclerView.State state) {
        if (mDivider == null) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}
