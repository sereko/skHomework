/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package net.sereko.skhomework.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.gmariotti.cardslib.library.R;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardGridCursorAdapter;

/**
 * Card Grid View.
 * It uses an {@link it.gmariotti.cardslib.library.internal.CardGridArrayAdapter} to populate items.
 * </p>
 * Usage:
 * <pre><code>
 *    <it.gmariotti.cardslib.library.view.CardGridView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:columnWidth="190dp"
 *     android:numColumns="auto_fit"
 *     android:verticalSpacing="3dp"
 *     android:horizontalSpacing="2dp"
 *     android:stretchMode="columnWidth"
 *     android:gravity="center"
 *     android:id="@+id/carddemo_grid_base1"/>
 * </code></pre>
 * It provides a default layout id for each row @layout/list_card_layout
 * Use can easily customize it using card:list_card_layout_resourceID attr in your xml layout.
 * </p>
 * Use this code to populate the grid view
 * <pre><code>
 * CardGridView gridView = (CardGridView) getActivity().findViewById(R.id.gridId);
 * gridView.setAdapter(mCardGridArrayAdapter);
 * </code></pre>
 * This type of view, doesn't support swipe and collapse/expand actions.
 * </p>
 * Currently you have to use the same inner layout for each card in gridView.
 * </p>
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class CardGridView extends GridView implements CardView.OnExpandListAnimatorListener {

    protected static String TAG = "CardGridView";

    /**
     *  Card Grid Array Adapter
     */
    protected CardGridArrayAdapter mAdapter;

    /**
     * Card Cursor Adapter
     */
    protected CardGridCursorAdapter mCursorAdapter;

    //--------------------------------------------------------------------------
    // Fields for expand/collapse animation
    //--------------------------------------------------------------------------

    private boolean mShouldRemoveObserver = false;

    private List<View> mViewsToDraw = new ArrayList<View>();

    private int[] mTranslate;

    //--------------------------------------------------------------------------
    // Custom Attrs
    //--------------------------------------------------------------------------

    /**
     * Default layout to apply to card
     */
    protected int list_card_layout_resourceID = R.layout.list_card_layout;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------


    public CardGridView(Context context) {
        super(context);
        init(null, 0);
    }

    public CardGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CardGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    //--------------------------------------------------------------------------
    // Init
    //--------------------------------------------------------------------------

    /**
     * Initialize
     *
     * @param attrs
     * @param defStyle
     */
    protected void init(AttributeSet attrs, int defStyle){

        //Init attrs
        initAttrs(attrs,defStyle);

    }


    /**
     * Init custom attrs.
     *
     * @param attrs
     * @param defStyle
     */
    protected void initAttrs(AttributeSet attrs, int defStyle) {

        list_card_layout_resourceID = R.layout.list_card_layout;

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.card_options, defStyle, defStyle);

        try {
            list_card_layout_resourceID = a.getResourceId(R.styleable.card_options_list_card_layout_resourceID, this.list_card_layout_resourceID);
        } finally {
            a.recycle();
        }
    }

    //--------------------------------------------------------------------------
    // Adapter
    //--------------------------------------------------------------------------

    /**
     * Forces to use a {@link CardGridArrayAdapter}
     *
     * @param adapter
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof CardGridArrayAdapter){
            setAdapter((CardGridArrayAdapter)adapter);
        }else if (adapter instanceof CardGridCursorAdapter){
            setAdapter((CardGridCursorAdapter)adapter);
        }else{
            Log.w(TAG,"You are using a generic adapter. Pay attention: your adapter has to call cardGridArrayAdapter#getView method." );
            super.setAdapter(adapter);
        }
    }

    /**
     * Set {@link CardGridArrayAdapter} and layout used by items in ListView
     *
     * @param adapter {@link CardGridArrayAdapter}
     */
    public void setAdapter(CardGridArrayAdapter adapter) {
        super.setAdapter(adapter);

        //Set Layout used by items
        adapter.setRowLayoutId(list_card_layout_resourceID);

        adapter.setCardGridView(this);
        mAdapter=adapter;
    }

    /**
     * Set {@link it.gmariotti.cardslib.library.internal.CardGridCursorAdapter} and layout used by items in ListView
     *
     * @param adapter {@link it.gmariotti.cardslib.library.internal.CardGridCursorAdapter}
     */
    public void setAdapter(CardGridCursorAdapter adapter) {
        super.setAdapter(adapter);

        //Set Layout used by items
        adapter.setRowLayoutId(list_card_layout_resourceID);

        adapter.setCardGridView(this);
        mCursorAdapter=adapter;
    }

    /**
     * You can use this method, if you are using external adapters.
     * Pay attention. The generic adapter#getView() method has to call the cardArrayAdapter#getView() method to work.
     *
     * @param adapter {@link android.widget.ListAdapter} generic adapter
     * @param cardGridArrayAdapter    {@link it.gmariotti.cardslib.library.internal.CardGridArrayAdapter} cardGridArrayAdapter
     */
    public void setExternalAdapter(ListAdapter adapter, CardGridArrayAdapter cardGridArrayAdapter) {

        setAdapter(adapter);

        mAdapter=cardGridArrayAdapter;
        mAdapter.setCardGridView(this);
        mAdapter.setRowLayoutId(list_card_layout_resourceID);
    }

    /**
     * You can use this method, if you are using external adapters.
     * Pay attention. The generic adapter#getView() method has to call the cardCursorAdapter#getView() method to work.
     *
     * @param adapter {@link android.widget.ListAdapter} generic adapter
     * @param cardCursorAdapter    {@link it.gmariotti.cardslib.library.internal.CardCursorAdapter} cardArrayAdapter
     */
    public void setExternalAdapter(ListAdapter adapter, CardGridCursorAdapter cardCursorAdapter) {

        setAdapter(adapter);

        mCursorAdapter=cardCursorAdapter;
        mCursorAdapter.setCardGridView(this);
        mCursorAdapter.setRowLayoutId(list_card_layout_resourceID);
    }

    //--------------------------------------------------------------------------
    // Expand and Collapse animator
    // Don't use this animator in a grid.
    // All cells in the same row should expand/collapse a hidden area of same dimensions.
    //--------------------------------------------------------------------------

    @Override
    public void onExpandStart(CardView viewCard,View expandingLayout) {
        //do nothing. Don't use this kind of animation in a grid
        //prepareExpandView(viewCard,expandingLayout);
    }

    @Override
    public void onCollapseStart(CardView viewCard,View expandingLayout) {
        //do nothing. Don't use this kind of animation in a grid
        //prepareCollapseView(viewCard,expandingLayout);
    }

    private void prepareExpandView(final CardView view,final View expandingLayout) {
        final Card card = (Card)getItemAtPosition(getPositionForView
                (view));

        /* Store the original top and bottom bounds of all the cells.*/
        final int oldTop = view.getTop();
        final int oldBottom = view.getBottom();

        final HashMap<View, int[]> oldCoordinates = new HashMap<View, int[]>();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (Build.VERSION.SDK_INT >= 16){
                v.setHasTransientState(true);
            }
            oldCoordinates.put(v, new int[] {v.getTop(), v.getBottom()});
        }

         /* Update the layout so the extra content becomes visible.*/
        if (expandingLayout!=null)
            expandingLayout.setVisibility(View.VISIBLE);

        /* Add an onPreDraw Listener to the listview. onPreDraw will get invoked after onLayout
        * and onMeasure have run but before anything has been drawn. This
        * means that the final post layout properties for all the items have already been
        * determined, but still have not been rendered onto the screen.*/
        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                /* Determine if this is the first or second pass.*/
                if (!mShouldRemoveObserver) {
                    mShouldRemoveObserver = true;

                    /* Calculate what the parameters should be for setSelectionFromTop.
                    * The ListView must be offset in a way, such that after the animation
                    * takes place, all the cells that remain visible are rendered completely
                    * by the ListView.*/
                    int newTop = view.getTop();
                    int newBottom = view.getBottom();

                    int newHeight = newBottom - newTop;
                    int oldHeight = oldBottom - oldTop;
                    int delta = newHeight - oldHeight;

                    mTranslate = getTopAndBottomTranslations(oldTop, oldBottom, delta, true);

                    int currentTop = view.getTop();
                    int futureTop = oldTop - mTranslate[0];

                    int firstChildStartTop = getChildAt(0).getTop();
                    int firstVisiblePosition = getFirstVisiblePosition();
                    int deltaTop = currentTop - futureTop;

                    int i;
                    int childCount = getChildCount();
                    for (i = 0; i < childCount; i++) {
                        View v = getChildAt(i);
                        int height = v.getBottom() - Math.max(0, v.getTop());
                        if (deltaTop - height > 0) {
                            firstVisiblePosition++;
                            deltaTop -= height;
                        } else {
                            break;
                        }
                    }

                    if (i > 0) {
                        firstChildStartTop = 0;
                    }

                    setSelection(firstVisiblePosition);
                    //setSelectionFromTop(firstVisiblePosition, firstChildStartTop - deltaTop);

                    /* Request another layout to update the layout parameters of the cells.*/
                    requestLayout();

                    /* Return false such that the ListView does not redraw its contents on
                     * this layout but only updates all the parameters associated with its
                     * children.*/
                    return false;
                }

                /* Remove the predraw listener so this method does not keep getting called. */
                mShouldRemoveObserver = false;
                observer.removeOnPreDrawListener(this);

                int yTranslateTop = mTranslate[0];
                int yTranslateBottom = mTranslate[1];

                ArrayList<Animator> animations = new ArrayList<Animator>();

                int index = indexOfChild(view);
                int numOfColumns = getNumColumns();
                int rowOfSelectedItem = (int) index/numOfColumns;

                /* Loop through all the views that were on the screen before the cell was
                *  expanded. Some cells will still be children of the ListView while
                *  others will not. The cells that remain children of the ListView
                *  simply have their bounds animated appropriately. The cells that are no
                *  longer children of the ListView also have their bounds animated, but
                *  must also be added to a list of views which will be drawn in dispatchDraw.*/
                for (View v: oldCoordinates.keySet()) {
                    int[] old = oldCoordinates.get(v);
                    v.setTop(old[0]);
                    v.setBottom(old[1]);
                    if (v.getParent() == null) {
                        mViewsToDraw.add(v);
                        int delta = old[0] < oldTop ? -yTranslateTop : yTranslateBottom;
                        animations.add(getAnimation(v, delta, delta));
                    } else {
                        int i = indexOfChild(v);
                        if (v != view) {
                            int rowOfv= (int) i/numOfColumns;
                            int delta = ( i > index && rowOfv > rowOfSelectedItem) ? yTranslateBottom : -yTranslateTop;
                            animations.add(getAnimation(v, delta, delta));
                        }
                        if (Build.VERSION.SDK_INT >= 16){
                            v.setHasTransientState(false);
                        }
                    }
                }


                /* Adds animation for expanding the cell that was clicked. */
                animations.add(getAnimation(view, -yTranslateTop, yTranslateBottom));

                /* Adds an animation for fading in the extra content. */
                animations.add(ObjectAnimator.ofFloat(expandingLayout,
                        View.ALPHA, 0, 1));

                /* Disabled the ListView for the duration of the animation.*/
                setEnabled(false);
                setClickable(false);

                /* Play all the animations created above together at the same time. */
                AnimatorSet s = new AnimatorSet();
                s.playTogether(animations);
                s.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setExpanded(true);//card.setExpanded(true);
                        setEnabled(true);
                        setClickable(true);
                        if (mViewsToDraw.size() > 0) {
                            for (View v : mViewsToDraw) {
                                if (Build.VERSION.SDK_INT >= 16){
                                    v.setHasTransientState(false);
                                }
                            }
                        }
                        mViewsToDraw.clear();

                        if (card.getOnExpandAnimatorEndListener()!=null)
                            card.getOnExpandAnimatorEndListener().onExpandEnd(card);
                    }
                });
                s.start();
                return true;
            }
        });
    }

    /**
     * This method collapses the view that was clicked and animates all the views
     * around it to close around the collapsing view. There are several steps required
     * to do this which are outlined below.
     *
     * 1. Update the layout parameters of the view clicked so as to minimize its height
     *    to the original collapsed (default) state.
     * 2. After invoking a layout, the listview will shift all the cells so as to display
     *    them most efficiently. Therefore, during the first predraw pass, the listview
     *    must be offset by some amount such that given the custom bound change upon
     *    collapse, all the cells that need to be on the screen after the layout
     *    are rendered by the listview.
     * 3. On the second predraw pass, all the items are first returned to their original
     *    location (before the first layout).
     * 4. The collapsing view's bounds are animated to what the final values should be.
     * 5. The bounds above the collapsing view are animated downwards while the bounds
     *    below the collapsing view are animated upwards.
     * 6. The extra text is faded out as its contents become visible throughout the
     *    animation process.
     */

    private void prepareCollapseView(final CardView view,final View expandingLayout) {
        final Card card = (Card)getItemAtPosition(getPositionForView
                (view));

        /* Store the original top and bottom bounds of all the cells.*/
        final int oldTop = view.getTop();
        final int oldBottom = view.getBottom();

        final HashMap<View, int[]> oldCoordinates = new HashMap<View, int[]>();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (Build.VERSION.SDK_INT >= 16){
                v.setHasTransientState(true);
            }
            oldCoordinates.put(v, new int [] {v.getTop(), v.getBottom()});
        }

        /* Update the layout so the extra content becomes invisible.*/
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                view.getCollapsedHeight()));

         /* Add an onPreDraw listener. */
        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {

                if (!mShouldRemoveObserver) {
                    /*Same as for expandingView, the parameters for setSelectionFromTop must
                    * be determined such that the necessary cells of the ListView are rendered
                    * and added to it.*/
                    mShouldRemoveObserver = true;

                    int newTop = view.getTop();
                    int newBottom = view.getBottom();

                    int newHeight = newBottom - newTop;
                    int oldHeight = oldBottom - oldTop;
                    int deltaHeight = oldHeight - newHeight;

                    mTranslate = getTopAndBottomTranslations(oldTop, oldBottom, deltaHeight, false);

                    int currentTop = view.getTop();
                    int futureTop = oldTop + mTranslate[0];

                    int firstChildStartTop = getChildAt(0).getTop();
                    int firstVisiblePosition = getFirstVisiblePosition();
                    int deltaTop = currentTop - futureTop;

                    int i;
                    int childCount = getChildCount();
                    for (i = 0; i < childCount; i++) {
                        View v = getChildAt(i);
                        int height = v.getBottom() - Math.max(0, v.getTop());
                        if (deltaTop - height > 0) {
                            firstVisiblePosition++;
                            deltaTop -= height;
                        } else {
                            break;
                        }
                    }

                    if (i > 0) {
                        firstChildStartTop = 0;
                    }

                    setSelection(firstVisiblePosition);
                    //setSelectionFromTop(firstVisiblePosition, firstChildStartTop - deltaTop);

                    requestLayout();

                    return false;
                }

                mShouldRemoveObserver = false;
                observer.removeOnPreDrawListener(this);

                int yTranslateTop = mTranslate[0];
                int yTranslateBottom = mTranslate[1];

                int index = indexOfChild(view);
                int numOfColumns = getNumColumns();
                int rowOfSelectedItem = (int) index/numOfColumns;

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View v = getChildAt(i);
                    int [] old = oldCoordinates.get(v);
                    if (old != null) {
                        /* If the cell was present in the ListView before the collapse and
                        * after the collapse then the bounds are reset to their old values.*/
                        v.setTop(old[0]);
                        v.setBottom(old[1]);
                        if (Build.VERSION.SDK_INT >= 16){
                            v.setHasTransientState(false);
                        }
                    } else {
                        /* If the cell is present in the ListView after the collapse but
                         * not before the collapse then the bounds are calculated using
                         * the bottom and top translation of the collapsing cell.*/
                        int rowOfv= (int) i/numOfColumns;
                        int delta = ( i > index && rowOfv>rowOfSelectedItem) ? yTranslateBottom : -yTranslateTop;
                        v.setTop(v.getTop() + delta);
                        v.setBottom(v.getBottom() + delta);
                    }
                }



                /* Animates all the cells present on the screen after the collapse. */
                ArrayList <Animator> animations = new ArrayList<Animator>();
                for (int i = 0; i < childCount; i++) {
                    View v = getChildAt(i);
                    if (v != view) {
                        float diff = i > index ? -yTranslateBottom : yTranslateTop;
                        animations.add(getAnimation(v, diff, diff));
                    }
                }

                /*
                ValueAnimator animator = ValueAnimator.ofInt( yTranslateTop,-yTranslateBottom);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int value = (Integer) valueAnimator.getAnimatedValue();

                        ViewGroup.LayoutParams layoutParams = expandingLayout.getLayoutParams();
                        layoutParams.height = value;
                        expandingLayout.setLayoutParams(layoutParams);
                    }
                });
                animations.add(animator);*/

                /* Adds animation for collapsing the cell that was clicked. */
                animations.add(getAnimation(view, yTranslateTop, -yTranslateBottom));

                /* Adds an animation for fading out the extra content. */
                animations.add(ObjectAnimator.ofFloat(expandingLayout, View.ALPHA, 1, 0));

                /* Disabled the ListView for the duration of the animation.*/
                setEnabled(false);
                setClickable(false);

                /* Play all the animations created above together at the same time. */
                AnimatorSet s = new AnimatorSet();
                s.playTogether(animations);
                s.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        expandingLayout.setVisibility(View.GONE);
                        view.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        view.setExpanded(false);
                        setEnabled(true);
                        setClickable(true);
                        /* Note that alpha must be set back to 1 in case this view is reused
                        * by a cell that was expanded, but not yet collapsed, so its state
                        * should persist in an expanded state with the extra content visible.*/
                        expandingLayout.setAlpha(1);

                        if (card.getOnCollapseAnimatorEndListener()!=null)
                            card.getOnCollapseAnimatorEndListener().onCollapseEnd(card);
                    }
                });
                s.start();

                return true;
            }
        });
    }



    /**
     * Calculates the top and bottom bound changes of the selected item. These values are
     * also used to move the bounds of the items around the one that is actually being
     * expanded or collapsed.
     *
     * This method can be modified to achieve different user experiences depending
     * on how you want the cells to expand or collapse. In this specific demo, the cells
     * always try to expand downwards (leaving top bound untouched), and similarly,
     * collapse upwards (leaving top bound untouched). If the change in bounds
     * results in the complete disappearance of a cell, its lower bound is moved is
     * moved to the top of the screen so as not to hide any additional content that
     * the user has not interacted with yet. Furthermore, if the collapsed cell is
     * partially off screen when it is first clicked, it is translated such that its
     * full contents are visible. Lastly, this behaviour varies slightly near the bottom
     * of the listview in order to account for the fact that the bottom bounds of the actual
     * listview cannot be modified.
     */
    private int[] getTopAndBottomTranslations(int top, int bottom, int yDelta,
                                              boolean isExpanding) {
        int yTranslateTop = 0;
        int yTranslateBottom = yDelta;

        int height = bottom - top;

        if (isExpanding) {
            boolean isOverTop = top < 0;
            boolean isBelowBottom = (top + height + yDelta) > getHeight();
            if (isOverTop) {
                yTranslateTop = top;
                yTranslateBottom = yDelta - yTranslateTop;
            } else if (isBelowBottom){
                int deltaBelow = top + height + yDelta - getHeight();
                yTranslateTop = top - deltaBelow < 0 ? top : deltaBelow;
                yTranslateBottom = yDelta - yTranslateTop;
            }
        } else {
            int offset = computeVerticalScrollOffset();
            int range = computeVerticalScrollRange();
            int extent = computeVerticalScrollExtent();
            int leftoverExtent = range-offset - extent;

            boolean isCollapsingBelowBottom = (yTranslateBottom > leftoverExtent);
            boolean isCellCompletelyDisappearing = bottom - yTranslateBottom < 0;

            if (isCollapsingBelowBottom) {
                yTranslateTop = yTranslateBottom - leftoverExtent;
                yTranslateBottom = yDelta - yTranslateTop;
            } else if (isCellCompletelyDisappearing) {
                yTranslateBottom = bottom;
                yTranslateTop = yDelta - yTranslateBottom;
            }
        }

        return new int[] {yTranslateTop, yTranslateBottom};
    }


    /**
     * This method takes some view and the values by which its top and bottom bounds
     * should be changed by. Given these params, an animation which will animate
     * these bound changes is created and returned.
     */
    private Animator getAnimation(final View view, float translateTop, float translateBottom) {

        int top = view.getTop();
        int bottom = view.getBottom();

        int endTop = (int)(top + translateTop);
        int endBottom = (int)(bottom + translateBottom);

        PropertyValuesHolder translationTop = PropertyValuesHolder.ofInt("top", top, endTop);
        PropertyValuesHolder translationBottom = PropertyValuesHolder.ofInt("bottom", bottom,
                endBottom);

        return ObjectAnimator.ofPropertyValuesHolder(view, translationTop, translationBottom);
    }

    /**
     * By overriding dispatchDraw, we can draw the cells that disappear during the
     * expansion process. When the cell expands, some items below or above the expanding
     * cell may be moved off screen and are thus no longer children of the ListView's
     * layout. By storing a reference to these views prior to the layout, and
     * guaranteeing that these cells do not get recycled, the cells can be drawn
     * directly onto the canvas during the animation process. After the animation
     * completes, the references to the extra views can then be discarded.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mViewsToDraw.size() == 0) {
            return;
        }

        for (View v: mViewsToDraw) {
            canvas.translate(0, v.getTop());
            v.draw(canvas);
            canvas.translate(0, -v.getTop());
        }
    }

}
