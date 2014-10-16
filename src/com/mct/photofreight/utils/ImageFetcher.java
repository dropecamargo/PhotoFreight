package com.mct.photofreight.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;

import com.mct.photofreight.models.Image;

public class ImageFetcher {
	
	private static final int FADE_IN_TIME = 200;
	
	private Bitmap mLoadingBitmap;
	protected Resources mResources;
	private BitmapManager bitmapmanager;
	
	private boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();
	
	protected int mImageWidth;
    protected int mImageHeight;
    
    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;
    
    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context) {
    	mResources = context.getResources();
    	bitmapmanager = new BitmapManager(context);	
    }     
    
    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }
    
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }
    
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }
        
    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }
    
    public void loadImage(Image data, ImageView imageView) {
        if (data == null) {
            return;
        }
        
		final String dataString = String.valueOf(data.getPath());		
        BitmapDrawable value = null;     
        
        if (mImageCache != null) {
        	value = mImageCache.getBitmapFromMemCache(dataString);
        }
        
        if (value != null) {
            imageView.setImageDrawable(value);
        } else if (cancelPotentialWork(data, imageView)) {
        	 final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
             final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
             imageView.setImageDrawable(asyncDrawable);             
             task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
        }
    }
    
    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
    
    /**
     * Called when the processing is complete and the final drawable should be 
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        // Transition drawable with a transparent drawable and the final drawable
        final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
            new ColorDrawable(android.R.color.transparent), drawable
        });
        
        // Set background to loading bitmap
        imageView.setBackground(new BitmapDrawable(mResources, mLoadingBitmap));

        imageView.setImageDrawable(td);
        td.startTransition(FADE_IN_TIME);        
    }
    
    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {
    	 
    	private Image data;    	
        private final WeakReference<ImageView> imageViewReference;

    	public BitmapWorkerTask(ImageView imageView) {
    		imageViewReference = new WeakReference<ImageView>(imageView);
    	}

    	/**
    	 * Background processing.
    	 */
    	@Override
    	protected BitmapDrawable doInBackground(Object... params) {    		
    		data = (Image) params[0];
    		final String dataString = String.valueOf(data.getPath());
    		
    		Bitmap bitmap = null;
    		BitmapDrawable drawable = null;
    		
    		// Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }
            
            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(dataString);
            }
            
            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
                bitmap = processBitmap(data);            	
            }
                        
            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {            	
        		drawable = new BitmapDrawable(mResources, bitmap);
        		if (mImageCache != null) {
        			mImageCache.addBitmapToCache(data.getPath(), drawable);
        		}
            }    		
    		return drawable;
    	}

    	@Override
    	protected void onPostExecute(BitmapDrawable value) {    		
    		// if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }
            
    		final ImageView imageView = getAttachedImageView();    		
    		if (value != null && imageView != null) {
    			setImageDrawable(imageView, value);
    		}    		
		}      
    	
    	@Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }
    	
    	/**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }
            return null;
        }
    }
        
    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
    	private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    
    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }    
    
    public Bitmap processBitmap(Image data){
    	Bitmap bitmap = null;
    	if(data.getPath() != null){
    		try {
    			bitmap = bitmapmanager.decodeSampledBitmapFromUri(data.getPath(), mImageWidth, mImageHeight);
	    	} catch (FileNotFoundException e) { } catch (IOException e) { }
    	}
    	return bitmap;
    }
    
    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param fragmentManager
     * @param cacheParams The cache parameters to use for the image cache.
     */
    public void addImageCache(FragmentManager fragmentManager, ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = ImageCache.getInstance(fragmentManager, mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    
    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
    	if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
    	if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
    	if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
    	if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }
}
